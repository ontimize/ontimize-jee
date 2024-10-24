package com.ontimize.jee.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreferencesParamsDto {
    public enum PreferencesType {
        REPORT, CHART
    }
    
    @JsonProperty("preferencename")
    private String preferenceName;
    
    @JsonProperty("preferencedescription")
    private String preferenceDescription;
    
    @JsonProperty("preferenceentity")
    private String preferenceEntity;
    
    @JsonProperty("preferenceservice")
    private String preferenceService;
    
    @JsonProperty("preferencetype")
    private PreferencesType preferenceType;
    
    @JsonProperty("preferenceparameters")
    private Object preferenceParameters;

    public String getPreferenceName() {
        return preferenceName;
    }

    public void setPreferenceName(String preferenceName) {
        this.preferenceName = preferenceName;
    }

    public String getPreferenceDescription() {
        return preferenceDescription;
    }

    public void setDescription(String preferenceDescription) {
        this.preferenceDescription = preferenceDescription;
    }

    public String getPreferenceEntity() {
        return preferenceEntity;
    }

    public void setPreferenceEntity(String preferenceEntity) {
        this.preferenceEntity = preferenceEntity;
    }

    public String getPreferenceService() {
        return preferenceService;
    }

    public void setPreferenceService(String preferenceService) {
        this.preferenceService = preferenceService;
    }

    public Object getPreferenceParameters() {
        return preferenceParameters;
    }

    public void setPreferenceParameters(Object preferenceParameters) {
        this.preferenceParameters = preferenceParameters;
    }	

    public PreferencesType getPreferenceType() {
        return preferenceType;
    }

    public void setPreferenceType(PreferencesType preferenceType) {
        this.preferenceType = preferenceType;
    }

}
