package com.ontimize.jee.core.common.report.store;

import com.ontimize.jee.core.common.db.query.QueryExpression;
import com.ontimize.jee.core.common.report.ReportResource;

public interface ReportStoreDefinition extends ReportProperties {

    public String getXMLDefinition();

    public void setXMLDefinition(String def);

    public ReportResource[] getResources();

    public void setResources(ReportResource[] res);

    public QueryExpression getQueryExpression();

    public void setQueryExpression(QueryExpression queryExpression);

}
