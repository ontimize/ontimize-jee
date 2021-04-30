package com.ontimize.jee.common.report.store;

public interface ReportStore extends java.rmi.Remote {

    public void add(String reportId, ReportStoreDefinition rDef, int sessionId) throws Exception;

    public void remove(String reportId, int sessionId) throws Exception;

    public ReportProperties[] list(int sessionId) throws Exception;

    public ReportProperties[] list(String entity, String type, int sessionId) throws Exception;

    public ReportProperties[] list(String entity, int sessionId) throws Exception;

    public ReportStoreDefinition get(String reportId, int sessionId) throws Exception;

    public ReportProperties getReportProperties(String reportId, int sessionId) throws Exception;

    public java.net.URL getURL(String reportId, int sessionId) throws Exception;

    public boolean exists(String reportId, int sessionId) throws Exception;

    public String getDescription(int sessionId) throws Exception;

}
