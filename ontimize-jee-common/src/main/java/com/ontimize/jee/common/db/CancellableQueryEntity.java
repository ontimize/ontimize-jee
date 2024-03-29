package com.ontimize.jee.common.db;

import com.ontimize.jee.common.dto.EntityResult;

import java.util.List;
import java.util.Map;

public interface CancellableQueryEntity extends CancellableOperationEntity {

    public EntityResult query(Map keys, List attributes, int sessionId, String operationId) throws Exception;

}
