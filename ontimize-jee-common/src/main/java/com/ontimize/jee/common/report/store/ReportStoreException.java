package com.ontimize.jee.common.report.store;

public class ReportStoreException extends java.rmi.RemoteException {

    public ReportStoreException(String reason) {
        super(reason);
    }

    public ReportStoreException(String reason, Exception ex) {
        super(reason, ex);
    }

    public ReportStoreException() {
        super();
    }

}
