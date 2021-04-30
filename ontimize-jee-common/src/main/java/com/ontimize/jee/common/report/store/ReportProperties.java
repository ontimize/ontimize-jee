package com.ontimize.jee.common.report.store;

public interface ReportProperties extends java.io.Serializable {

    public static final String BASIC = "BASIC";

    public static final String ADVANCED = "ADVANCED";

    /**
     * <p>
     * Ontimize Jasper Report System Report Store type.
     */
    public static final String JASPERREPORT = "JASPERREPORT";

    public Object getKey();

    public String getName();

    public String getDescription();

    public String getEntity();

    public String getSQLQuery();

    public String getReportType();

    public void setKey(Object key);

    public void setName(String name);

    public void setDescription(String dscr);

    public void setEntity(String entity);

    public void setSQLQuery(String query);

    public void setReportType(String type);

}
