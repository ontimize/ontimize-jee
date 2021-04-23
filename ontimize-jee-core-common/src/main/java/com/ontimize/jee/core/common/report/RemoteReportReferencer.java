package com.ontimize.jee.core.common.report;

import java.rmi.Remote;
import java.util.List;

public interface RemoteReportReferencer extends Remote {

    public List getRemoteReportStore(int sessionId) throws Exception;

    public List getReportEntityNames(int sessionId) throws Exception;

}
