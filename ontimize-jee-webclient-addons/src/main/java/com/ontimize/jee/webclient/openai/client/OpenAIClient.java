package com.ontimize.jee.webclient.openai.client;


import com.ontimize.jee.webclient.openai.model.ProcessRequest;
import com.ontimize.jee.webclient.openai.model.ProcessResult;
import com.ontimize.jee.webclient.openai.service.OpenAiImageProcessorService;
import com.ontimize.jee.webclient.openai.util.JsonSchemaValidator;

public class OpenAIClient {

    private final String apiKey;

    public OpenAIClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public <T> ProcessResult<T> processImage(ProcessRequest<T> request) {
        OpenAiImageProcessorService<T> service = new OpenAiImageProcessorService<>(this.apiKey, new JsonSchemaValidator());
        return service.processImage(request);
    }
}