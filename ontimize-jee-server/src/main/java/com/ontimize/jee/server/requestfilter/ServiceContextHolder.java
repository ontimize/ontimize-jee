/**
 * ServiceContextHolder.java 20-oct-2014
 * 
 * Copyright 2014 Imatia.
 */
package com.ontimize.jee.server.requestfilter;

/**
 * The Class ServiceContextHolder.
 * 
 * @author sergio.padin
 */
public final class ServiceContextHolder {

    /** The Constant INSTANCE. */
    private static final ServiceContextHolder INSTANCE = new ServiceContextHolder();

    /** The service context. */
    private final ThreadLocal<ServiceContext> serviceContext = new ThreadLocal<ServiceContext>();


    /**
     * Gets the single instance of ServiceContextHolder.
     * 
     * @return single instance of ServiceContextHolder
     */
    public static ServiceContextHolder getInstance() {
        return ServiceContextHolder.INSTANCE;
    }

    /**
     * Gets the service context.
     * 
     * @return the service context
     */
    public ServiceContext getServiceContext() {
        ServiceContext serviceContext2 = this.serviceContext.get();
        if (serviceContext2 == null) {
            serviceContext2 = new ServiceContext();
            this.serviceContext.set(serviceContext2);
        }
        return serviceContext2;
    }
}
