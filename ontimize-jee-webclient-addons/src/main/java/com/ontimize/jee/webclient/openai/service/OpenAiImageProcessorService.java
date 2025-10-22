package com.ontimize.jee.webclient.openai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontimize.jee.webclient.openai.model.ProcessRequest;
import com.ontimize.jee.webclient.openai.model.ProcessResult;
import com.ontimize.jee.webclient.openai.util.JsonSchemaValidator;
import com.ontimize.jee.webclient.openai.util.Utils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static com.ontimize.jee.webclient.openai.naming.OpenAINaming.*;

public class OpenAiImageProcessorService<T> {
    private final JsonSchemaValidator jsonSchemaValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;

    public OpenAiImageProcessorService(String apiKey, JsonSchemaValidator jsonSchemaValidator) {
        this.jsonSchemaValidator = jsonSchemaValidator;
        this.apiKey = apiKey;
    }

    public ProcessResult<T> processImage(ProcessRequest<T> request) {
        int actualTry = 0;
        List<String> errors = new ArrayList<>();
        MultipartFile file = request.getFile();
        Class<T> outputClass = request.getOutputClass();
        ObjectMapper objectMapper = new ObjectMapper();

        String schemaStr;
        try {
            schemaStr = Utils.generateFullSchemaJson(outputClass);
        } catch (Exception e) {
            errors.add(OPENAI_API_SCHEMA_GENERATION_ERROR + e.getMessage());
            return new ProcessResult<>(null, errors, actualTry);
        }

        String parsedResult = null;
        ProcessResult<T> finalResult = null;

        while (actualTry < request.getRetries()) {
            try {
                String prompt = Utils.buildPrompt(request.getPrompt(), schemaStr, parsedResult, errors);

                String responseJsonRaw = callVisionApi(prompt, file, request.getModel(), request.getMaxTokens(),
                        request.getTemperature());

                String responseJson = JsonSchemaValidator.extractRawJson(responseJsonRaw);
                if (responseJson == null || responseJson.isBlank()) {
                    throw new IllegalStateException(OPENAI_API_NO_JSON_ERROR);
                }

                parsedResult = responseJsonRaw;
                jsonSchemaValidator.validate(responseJson, schemaStr);

                T result = objectMapper.readValue(responseJson, outputClass);
                finalResult = new ProcessResult<>(result, new ArrayList<>(errors), actualTry);
                break;

            } catch (Exception e) {
                errors.add(e.getMessage());
                actualTry++;
            }
        }
        return finalResult != null ? finalResult : new ProcessResult<>(null, errors, actualTry);
    }

    private String callVisionApi(String promptText, MultipartFile image, String model, int maxTokens,
            double temperature) throws Exception {
        HttpEntity<Map<String, Object>> request = prepareRequest(promptText, image, model, maxTokens, temperature);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(COMPLETIONS_URL, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception(OPENAI_API_ERROR + response.getStatusCode() + " - " + response.getBody());
        }
        return objectMapper.readTree(response.getBody()).path(CHOICES).get(0).path(MESSAGE).path(CONTENT).asText();
    }

    private HttpEntity<Map<String, Object>> prepareRequest(String promptText, MultipartFile image, String model,
            int maxTokens, double temperature) throws IOException {
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> imageUrlContent = Map.of(URL, IMAGE_TYPE + base64Image, DETAIL, HIGH);
        Map<String, Object> contentItem1 = Map.of(TYPE, TEXT, TEXT, promptText);
        Map<String, Object> contentItem2 = Map.of(TYPE, IMAGE_URL, IMAGE_URL, imageUrlContent);
        Map<String, Object> message = Map.of(ROLE, USER, CONTENT, List.of(contentItem1, contentItem2));

        Map<String, Object> payload = new HashMap<>();
        payload.put(MODEL, model);
        payload.put(MESSAGES, List.of(message));
        payload.put(MAX_TOKENS, maxTokens);
        payload.put(TEMPERATURE, temperature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(this.apiKey);

        return new HttpEntity<>(payload, headers);
    }
}
