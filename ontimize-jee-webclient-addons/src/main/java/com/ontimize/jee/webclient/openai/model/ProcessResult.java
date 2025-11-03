package com.ontimize.jee.webclient.openai.model;

import java.util.List;

public class ProcessResult<T> {
    private T data;
    private List<String> errors;
    private int retries;

    public ProcessResult() {
    }

    public ProcessResult(T data, List<String> errors, int retries) {
        this.data = data;
        this.errors = errors;
        this.retries = retries;
    }

    public T getData() {
        return data;
    }

    public List<String> getErrors() {
        return errors;
    }

    public int getRetries() {
        return retries;
    }
}
