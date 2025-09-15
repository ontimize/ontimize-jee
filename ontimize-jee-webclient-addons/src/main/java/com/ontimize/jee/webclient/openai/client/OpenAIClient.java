package com.ontimize.jee.webclient.openai.client;


import com.ontimize.jee.webclient.openai.model.ProcessRequest;
import com.ontimize.jee.webclient.openai.model.ProcessResult;
import com.ontimize.jee.webclient.openai.service.OpenAiImageProcessorService;
import com.ontimize.jee.webclient.openai.util.JsonSchemaValidator;

public class OpenAiClient {

    private final String apiKey;

    public OpenAiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public <T> ProcessResult<T> processImage(ProcessRequest<T> request) {
        OpenAiImageProcessorService<T> service = new OpenAiImageProcessorService<>(this.apiKey, new JsonSchemaValidator());
        return service.processImage(request);
    }
}