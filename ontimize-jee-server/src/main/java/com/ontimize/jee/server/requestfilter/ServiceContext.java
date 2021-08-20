/**
 * ServiceContext.java 20-oct-2014
 *
 * Copyright 2014 Imatia.
 */
package com.ontimize.jee.server.requestfilter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * The Class ServiceContext.
 *
 * @author <a href="luis.garcia@imatia.com">Luis Garcia</a>
 */
public class ServiceContext {

    /** The context. */
    private final Map<String, Object> context = Collections.synchronizedMap(new HashMap<String, Object>());


    /** Vars extracteted from com.caucho.services.server.ServiceContext */
    private static final ThreadLocal<ServiceContext> _localContext = new ThreadLocal<ServiceContext>();

    private ServletRequest _request;

    private ServletResponse _response;

    private String _serviceName;

    private String _objectId;

    private int _count;

    private HashMap _headers = new HashMap();


    /**
     * Gets the context property.
     * @param <T> the generic type
     * @param key the key
     * @return the context property
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextProperty(final String key) {
        return (T) this.context.get(key);
    }

    /**
     * Sets the context property.
     * @param <T> the generic type
     * @param key the key
     * @param value the value
     */
    public <T> void setContextProperty(final String key, final T value) {
        this.context.put(key, value);
    }

    /**
     * Reset.
     */
    public void reset() {
        this.context.clear();
    }

    /** Meths extracteted from com.caucho.services.server.ServiceContext */

    public ServiceContext() { // modified original from private cause ServiceContextHolder instantation
    }

    /**
     * Sets the request object prior to calling the service's method.
     * @param request the calling servlet request
     * @param serviceId the service identifier
     * @param objectId the object identifier
     */
    public static void begin(ServletRequest request,
            ServletResponse response,
            String serviceName,
            String objectId)
            throws ServletException {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context == null) {
            context = new ServiceContext();
            _localContext.set(context);
        }

        context._request = request;
        context._response = response;
        context._serviceName = serviceName;
        context._objectId = objectId;
        context._count++;
    }

    /**
     * Returns the service request.
     */
    public static ServiceContext getContext() {
        return (ServiceContext) _localContext.get();
    }

    /**
     * Adds a header.
     */
    public void addHeader(String header, Object value) {
        _headers.put(header, value);
    }

    /**
     * Gets a header.
     */
    public Object getHeader(String header) {
        return _headers.get(header);
    }

    /**
     * Gets a header from the context.
     */
    public static Object getContextHeader(String header) {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context.getHeader(header);
        else
            return null;
    }

    /**
     * Returns the service request.
     */
    public static ServletRequest getContextRequest() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context._request;
        else
            return null;
    }

    /**
     * Returns the service request.
     */
    public static ServletResponse getContextResponse() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context._response;
        else
            return null;
    }

    /**
     * Returns the service id, corresponding to the pathInfo of the URL.
     */
    public static String getContextServiceName() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context._serviceName;
        else
            return null;
    }

    /**
     * Returns the object id, corresponding to the ?id= of the URL.
     */
    public static String getContextObjectId() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context._objectId;
        else
            return null;
    }

    /**
     * Cleanup at the end of a request.
     */
    public static void end() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null && --context._count == 0) {
            context._request = null;
            context._response = null;

            context._headers.clear();

            _localContext.set(null);
        }
    }

    /**
     * Returns the service request.
     * @deprecated
     */
    public static ServletRequest getRequest() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context._request;
        else
            return null;
    }

    /**
     * Returns the service id, corresponding to the pathInfo of the URL.
     * @deprecated
     */
    public static String getServiceName() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context._serviceName;
        else
            return null;
    }

    /**
     * Returns the object id, corresponding to the ?id= of the URL.
     * @deprecated
     */
    public static String getObjectId() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context._objectId;
        else
            return null;
    }
    /** END: Meths extracteted from com.caucho.services.server.ServiceContext */

}
