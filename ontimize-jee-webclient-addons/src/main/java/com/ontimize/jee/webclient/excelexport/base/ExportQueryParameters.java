package com.ontimize.jee.webclient.excelexport.base;

import com.ontimize.jee.server.rest.QueryParameter;

public class ExportQueryParameters extends BaseExcelExportParameters{

	private QueryParameter queryParam;
	private String dao;
	private String service;
	
	

	public ExportQueryParameters() {
		super();
	}

	public ExportQueryParameters(QueryParameter queryParam, String dao, String service) {
		super();
		this.queryParam = queryParam;
		this.dao = dao;
		this.service = service;
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
	
	
}
