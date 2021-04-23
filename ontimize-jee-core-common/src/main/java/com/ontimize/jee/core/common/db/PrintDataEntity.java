package com.ontimize.jee.core.common.db;

import com.ontimize.jee.core.common.dto.EntityResult;

import java.util.Map;

public interface PrintDataEntity extends java.rmi.Remote {

    public EntityResult getPrintingData(Map keys, int sessionId) throws Exception;

}
