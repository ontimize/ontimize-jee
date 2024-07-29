package com.ontimize.jee.common.dto;

public class PreferencesParamsDto {
    public enum PreferencesType {
        REPORT, CHART
    }
    private String preferencename;
    private String preferencedescription;
    private String preferenceentity;
    private String preferenceservice;
    private PreferencesType preferencetype;
    private Object preferencepreferences;

    public String getPreferencename() {
        return preferencename;
    }

    public void setPreferencename(String preferencename) {
        this.preferencename = preferencename;
    }

    public String getPreferencedescription() {
        return preferencedescription;
    }

    public void setDescription(String preferencedescription) {
        this.preferencedescription = preferencedescription;
    }

    public String getPreferenceentity() {
        return preferenceentity;
    }

    public void setPreferenceentity(String preferenceentity) {
        this.preferenceentity = preferenceentity;
    }

    public String getPreferenceservice() {
        return preferenceservice;
    }

    public void setPreferenceservice(String preferenceservice) {
        this.preferenceservice = preferenceservice;
    }

    public Object getPreferencepreferences() {
        return preferencepreferences;
    }

    public void setPreferencepreferences(Object preferencepreferences) {
        this.preferencepreferences = preferencepreferences;
    }	

    public PreferencesType getPreferencetype() {
        return preferencetype;
    }

    public void setPreferencetype(PreferencesType preferencetype) {
        this.preferencetype = preferencetype;
    }

}
