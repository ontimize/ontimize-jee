package com.ontimize.jee.webclient.export.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ontimize.jee.server.rest.FilterParameter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseExportQueryParameters implements ExportQueryParameters {

    private FilterParameter queryParam;

    private String dao;

    private String service;

    private String path;

    private boolean advQuery;

    private Boolean landscape;


    public BaseExportQueryParameters() {
        super();
    }

    public BaseExportQueryParameters(FilterParameter queryParam, String dao, String service, String path, boolean advQuery) {
        super();
        this.queryParam = queryParam;
        this.dao = dao;
        this.service = service;
        this.path = path;
        this.advQuery = advQuery;
    }

    public FilterParameter getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(FilterParameter queryParam) {
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

    public Boolean getLandscape() {
      return landscape;
    }

    public void setLandscape(Boolean landscape) {
      this.landscape = landscape;
    }

}
