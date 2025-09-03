package com.ontimize.jee.webclient.openai.client;


import com.ontimize.jee.webclient.openai.model.OpenAiClientConfig;
import com.ontimize.jee.webclient.openai.model.ProcessRequest;
import com.ontimize.jee.webclient.openai.model.ProcessResult;
import com.ontimize.jee.webclient.openai.service.OpenAiImageProcessorService;
import com.ontimize.jee.webclient.openai.util.JsonSchemaValidator;
import com.ontimize.jee.webclient.openai.util.PromptBuilder;

public class OpenAiClient {

    private final OpenAiClientConfig config;

    public OpenAiClient(OpenAiClientConfig config) {
        this.config = config;
    }

    public <T> ProcessResult<T> processImage(ProcessRequest<T> request) {
        OpenAiImageProcessorService<T> service = new OpenAiImageProcessorService<>(
                config,
                new PromptBuilder(),
                new JsonSchemaValidator()
        );

        return service.processImage(request);
    }
}