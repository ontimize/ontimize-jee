package com.ontimize.jee.common.dto;

public class PreferencesParamsDto {
    public enum PreferencesType {
        REPORT, CHART
    }
    private String preferenceName;
    private String preferenceDescription;
    private String preferenceEntity;
    private String preferenceService;
    private PreferencesType preferenceType;
    private Object preferencePreferences;

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

    public Object getPreferencePreferences() {
        return preferencePreferences;
    }

    public void setPreferencePreferences(Object preferencePreferences) {
        this.preferencePreferences = preferencePreferences;
    }	

    public PreferencesType getPreferenceType() {
        return preferenceType;
    }

    public void setPreferenceType(PreferencesType preferenceType) {
        this.preferenceType = preferenceType;
    }

}
