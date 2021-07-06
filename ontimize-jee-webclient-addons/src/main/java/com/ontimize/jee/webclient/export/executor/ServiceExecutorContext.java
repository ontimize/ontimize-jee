/**
 * ServiceExecutorContext.java 23-jun-2017
 *
 * Copyright 2017 INDITEX. Departamento de Sistemas
 */

package com.ontimize.jee.webclient.export.executor;


import com.ontimize.jee.webclient.export.executor.support.SimpleServiceExecutor;

/**
 * The Class ServiceExecutorContext.
 *
 * @author <a href="daniel.grana@imatia.com">Daniel Grana</a>
 */
public final class ServiceExecutorContext {

    /** The service executor */
    private static ServiceExecutor serviceExecutor = new SimpleServiceExecutor();

    /**
     * Instancia un nuevo service executor context.
     */
    private ServiceExecutorContext() {
        // private
    }

    /**
     * Obtiene service executor.
     * @return service executor
     */
    public static ServiceExecutor getServiceExecutor() {
        return serviceExecutor;
    }

    /**
     * Establece service executor.
     * @param serviceExecutor nuevo service executor
     */
    public static void setServiceExecutor(final ServiceExecutor serviceExecutor) {
        ServiceExecutorContext.serviceExecutor = serviceExecutor;
    }

}
