package com.ontimize.jee.webclient.openai.exception;

public class OpenAIClientException extends RuntimeException {
    public OpenAIClientException(String message) {
        super(message);
    }

    public OpenAIClientException(String message, Throwable cause) {
        super(message, cause);
    }
}