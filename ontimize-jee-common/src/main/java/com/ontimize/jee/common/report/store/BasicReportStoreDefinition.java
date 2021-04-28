package com.ontimize.jee.common.report.store;

import com.ontimize.jee.common.report.ReportResource;
import com.ontimize.jee.core.common.db.query.QueryExpression;

public class BasicReportStoreDefinition implements ReportStoreDefinition {

    private Object key = null;

    private String name = null;

    private String description = null;

    private String entity = null;

    private String xmlDefinition = null;

    private String sqlQuery = null;

    private String type = null;

    private ReportResource[] reportResourceList = null;

    private QueryExpression queryExpression = null;

    public BasicReportStoreDefinition(String name, String dscr, String entity, String xmlDef, String sqlQuery,
            String type, ReportResource[] res, QueryExpression queryExpression) {
        this(null, name, dscr, entity, xmlDef, sqlQuery, type, res, queryExpression);
    }

    public BasicReportStoreDefinition(Object key, String name, String dscr, String entity, String xmlDef,
            String sqlQuery, String type, ReportResource[] res,
            QueryExpression queryExpression) {
        this.key = key;
        this.name = name;
        this.description = dscr;
        this.entity = entity;
        this.xmlDefinition = xmlDef;
        this.sqlQuery = sqlQuery;
        this.type = type;
        this.reportResourceList = res;
        this.queryExpression = queryExpression;
    }

    @Override
    public Object getKey() {
        return this.key;
    }

    @Override
    public void setKey(Object key) {
        this.key = key;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String dscr) {
        this.description = dscr;
    }

    @Override
    public String getEntity() {
        return this.entity;
    }

    @Override
    public void setEntity(String entity) {
        this.entity = entity;
    }

    @Override
    public String getReportType() {
        return this.type;
    }

    @Override
    public void setReportType(String type) {
        this.type = type;
    }

    @Override
    public String getXMLDefinition() {
        return this.xmlDefinition;
    }

    @Override
    public void setXMLDefinition(String def) {
        this.xmlDefinition = def;
    }

    @Override
    public String getSQLQuery() {
        return this.sqlQuery;
    }

    @Override
    public void setSQLQuery(String query) {
        this.sqlQuery = query;
    }

    @Override
    public ReportResource[] getResources() {
        return this.reportResourceList;
    }

    @Override
    public void setResources(ReportResource[] res) {
        this.reportResourceList = res;
    }

    @Override
    public QueryExpression getQueryExpression() {
        return this.queryExpression;
    }

    @Override
    public void setQueryExpression(QueryExpression queryExpression) {
        this.queryExpression = queryExpression;
    }

    @Override
    public String toString() {
        return "name: " + this.name + " , description: " + this.description + " , entity: " + this.entity + " , sql: "
                + this.sqlQuery + " , \nxml: " + this.xmlDefinition;
    }

}
