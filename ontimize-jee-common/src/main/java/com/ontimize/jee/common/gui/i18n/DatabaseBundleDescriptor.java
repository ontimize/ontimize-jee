package com.ontimize.jee.common.gui.i18n;

public class DatabaseBundleDescriptor {

    protected Object bundleId;

    protected String bundleClassName;

    protected String bundleDescription;


    public DatabaseBundleDescriptor(Object id, String className, String description) {
        this.bundleId = id;
        this.bundleClassName = className;
        this.bundleDescription = description;
    }

    public Object getBundleId() {
        return this.bundleId;
    }

    public void setBundleId(Object bundleId) {
        this.bundleId = bundleId;
    }

    public String getBundleClassName() {
        return this.bundleClassName;
    }

    public void setBundleClassName(String bundleClassName) {
        this.bundleClassName = bundleClassName;
    }

    public String getBundleDescription() {
        return this.bundleDescription;
    }

    public void setBundleDescription(String bundleDescription) {
        this.bundleDescription = bundleDescription;
    }

}
