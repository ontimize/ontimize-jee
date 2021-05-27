package com.ontimize.jee.common.db;

import java.util.Map;

public interface AdvancedQueryEntity extends Entity {

    /**
     * Usually this method return a Map {column, type} for the columns specified in the properties file.
     * The columns are specified using the parameter 'ReportAdvancedQueryColumns', and types with
     * 'ReportAdvancedQueryTypes'. If the parameter 'ReportAdvancedQueryColumns' is not indicated, the
     * value indicated in Columns is used, otherwise, if types are not specified, they are obtained from
     * the data base
     * @param sessionId User session identifier
     * @return
     * @throws Exception
     */
    public Map getColumnListForAvancedQuery(int sessionId) throws Exception;

}
