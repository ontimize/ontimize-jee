package com.ontimize.jee.common.db;

import com.ontimize.jee.common.dto.EntityResult;

public interface DirectSQLQueryEntity extends Entity {

    /**
     * Execute the specified sql statement against the database
     * @param sql SQL to execute
     * @param sessionId User session identifier
     * @return
     * @throws Exception
     */
    public EntityResult execute(String sql, int sessionId) throws Exception;

}
