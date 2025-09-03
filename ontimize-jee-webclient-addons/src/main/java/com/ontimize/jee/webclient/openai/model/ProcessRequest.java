package com.ontimize.jee.webclient.openai.model;

import org.springframework.web.multipart.MultipartFile;

public class ProcessRequest<T> {

    private MultipartFile file;
    private String prompt;
    private int retries;
    private Class<T> outputClass;
    private String model;
    private int maxTokens;
    private double temperature;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Class<T> getOutputClass() {
        return outputClass;
    }

    public void setOutputClass(Class<T> outputClass) {
        this.outputClass = outputClass;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}