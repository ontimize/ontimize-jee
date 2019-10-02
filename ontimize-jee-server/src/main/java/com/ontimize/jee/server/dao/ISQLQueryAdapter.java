package com.ontimize.jee.server.dao;

import java.util.List;
import java.util.Map;

import com.ontimize.db.SQLStatementBuilder.SQLStatement;

public interface ISQLQueryAdapter {

	SQLStatement adaptQuery(SQLStatement sqlStatement, IOntimizeDaoSupport dao, Map<?, ?> keysValues, Map<?, ?> validKeysValues, List<?> attributes, List<?> validAttributes,
			List<?> sort, String queryId);
}
