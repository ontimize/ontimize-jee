package com.ontimize.jee.common.dto;

public class PreferencesParamsDto {
    public enum PreferencesType {
        REPORT, CHART
    }
    private String name;
    private String description;
    private String entity;
    private String service;
    private PreferencesType type;
    private Object params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public PreferencesType getType() {
        return type;
    }

    public void setType(PreferencesType type) {
        this.type = type;
    }

}
