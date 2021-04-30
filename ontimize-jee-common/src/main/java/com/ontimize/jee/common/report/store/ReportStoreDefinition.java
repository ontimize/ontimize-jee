package com.ontimize.jee.common.report.store;

import com.ontimize.jee.common.report.ReportResource;
import com.ontimize.jee.common.db.query.QueryExpression;

public interface ReportStoreDefinition extends ReportProperties {

    public String getXMLDefinition();

    public void setXMLDefinition(String def);

    public ReportResource[] getResources();

    public void setResources(ReportResource[] res);

    public QueryExpression getQueryExpression();

    public void setQueryExpression(QueryExpression queryExpression);

}
