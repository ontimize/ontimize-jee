package com.ontimize.jee.common.db.query.store;

import com.ontimize.jee.common.db.query.QueryExpression;

public interface QueryStore {

    public void addQuery(String description, QueryExpression query);

    public void removeQuery(String description, String entity);

    public String[] list(String entity);

    public QueryExpression get(String description, String entity);

}
