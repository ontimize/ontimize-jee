package com.ontimize.jee.common.db;

import com.ontimize.jee.common.dto.EntityResult;

import java.util.Map;

public interface PrintDataEntity extends java.rmi.Remote {

    public EntityResult getPrintingData(Map keys, int sessionId) throws Exception;

}
