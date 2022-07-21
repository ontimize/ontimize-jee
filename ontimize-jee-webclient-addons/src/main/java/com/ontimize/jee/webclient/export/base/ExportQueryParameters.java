package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.server.rest.QueryParameter;

public class ExportQueryParameters extends BaseExcelExportParameters {

    private QueryParameter queryParam;

    private String dao;

    private String service;
    
    private String path;

    private boolean advQuery;


    public ExportQueryParameters() {
        super();
    }

    public ExportQueryParameters(QueryParameter queryParam, String dao, String service, String path, boolean advQuery) {
        super();
        this.queryParam = queryParam;
        this.dao = dao;
        this.service = service;
        this.path = path;
        this.advQuery = advQuery;
    }

    public QueryParameter getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(QueryParameter queryParam) {
        this.queryParam = queryParam;
    }

    public String getDao() {
        return dao;
    }

    public void setDao(String dao) {
        this.dao = dao;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isAdvQuery() {
        return advQuery;
    }

    public void setAdvQuery(boolean advQuery) {
        this.advQuery = advQuery;
    }

}
