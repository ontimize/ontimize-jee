package com.ontimize.jee.server.dao.cql.handler;

import java.util.List;
import java.util.Map;

import com.datastax.driver.core.ResultSet;
import com.ontimize.dto.EntityResult;
import com.ontimize.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.jee.server.dao.cql.CQLStatement;

public interface CQLStatementHandler {

    public void resultSetToEntityResult(ResultSet resultSet, EntityResult entityResult, List<String> columnNames)
            throws Exception;

    public CQLStatement createSelectQuery(String table, List<?> requestedColumns, Map<?, ?> conditions,
            List<String> wildcards, List<SQLOrder> columnSorting);

}
