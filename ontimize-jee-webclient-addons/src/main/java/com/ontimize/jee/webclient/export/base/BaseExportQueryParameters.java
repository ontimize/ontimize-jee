package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.server.rest.AdvancedQueryParameter;
import com.ontimize.jee.server.rest.QueryParameter;

public class BaseExportQueryParameters implements ExportQueryParameters {

    private AdvancedQueryParameter queryParam;

    private String dao;

    private String service;
    
    private String path;

    private boolean advQuery;


    public BaseExportQueryParameters() {
        super();
    }

    public BaseExportQueryParameters(AdvancedQueryParameter queryParam, String dao, String service, String path, boolean advQuery) {
        super();
        this.queryParam = queryParam;
        this.dao = dao;
        this.service = service;
        this.path = path;
        this.advQuery = advQuery;
    }

    public AdvancedQueryParameter getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(AdvancedQueryParameter queryParam) {
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
