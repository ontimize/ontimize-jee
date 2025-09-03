package com.ontimize.jee.webclient.openai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontimize.jee.webclient.openai.model.OpenAiClientConfig;
import com.ontimize.jee.webclient.openai.model.ProcessRequest;
import com.ontimize.jee.webclient.openai.model.ProcessResult;
import com.ontimize.jee.webclient.openai.util.JsonSchemaValidator;
import com.ontimize.jee.webclient.openai.util.PromptBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public class OpenAiImageProcessorService<T> {
    private final OpenAiClientConfig config;
    private final PromptBuilder promptBuilder;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiImageProcessorService(OpenAiClientConfig config,
                                       PromptBuilder promptBuilder,
                                       JsonSchemaValidator jsonSchemaValidator) {
        this.config = config;
        this.promptBuilder = promptBuilder;
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    public ProcessResult<T> processImage(ProcessRequest<T> request) {
        int actualTry = 0;
        List<String> errors = new ArrayList<>();
        MultipartFile file = request.getFile();
        Class<T> outputClass = request.getOutputClass();
        while (actualTry < request.getRetries()) {
            try {
                T emptyDto = outputClass.getDeclaredConstructor().newInstance();
                String outputSchemaJson = objectMapper.writeValueAsString(emptyDto);
                String prompt = promptBuilder.buildPrompt(
                        request.getPrompt(),
                        outputSchemaJson,
                        actualTry > 0 ? errors.get(errors.size() - 1) : null
                );
                String responseJson = callVisionApi(prompt, file);
                T result = objectMapper.readValue(responseJson, outputClass);
                jsonSchemaValidator.validate(result, outputSchemaJson);
                return new ProcessResult<>(result, errors, actualTry);
            } catch (Exception e) {
                errors.add(e.getMessage());
                actualTry++;
            }
        }
        return new ProcessResult<>(null, errors, actualTry);
    }

    private String callVisionApi(String promptText, MultipartFile image) throws Exception {
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", config.getModel().getValue());
        payload.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", promptText),
                                Map.of("type", "image_url", "image_url", Map.of(
                                        "url", "data:image/jpeg;base64," + base64Image,
                                        "detail", "high"
                                ))
                        )
                )
        ));
        payload.put("max_tokens", config.getMaxTokens());
        payload.put("temperature", config.getTemperature());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                String.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("OpenAI API error: " + response.getStatusCode() + " - " + response.getBody());
        }
        return objectMapper.readTree(response.getBody())
                .path("choices").get(0)
                .path("message").path("content")
                .asText();
    }
}
