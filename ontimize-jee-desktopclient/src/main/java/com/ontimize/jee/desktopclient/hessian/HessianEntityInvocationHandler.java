package com.ontimize.jee.desktopclient.hessian;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.AdvancedEntity;
import com.ontimize.db.AdvancedEntityResult;
import com.ontimize.db.AdvancedQueryEntity;
import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.services.ServiceTools;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class HessianEntityInvocationHandler. Se encarga de realizar el mapeo nombre_entidad+método
 * -> método del servicio Por ejemplo, cuando un formulario quiera hacer un "query" a la entidad
 * "masterDataService.user" esta clase redireccionará la llamada al método "userQuery" del
 * servicio "masterDataService".
 */
public class HessianEntityInvocationHandler extends AbstractInvocationDelegate
        implements AdvancedEntity, AdvancedQueryEntity, InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(HessianEntityInvocationHandler.class);

    /** The service name. */
    protected String serviceName;

    /** The method prefix of the server. */
    protected String serviceMethodPrefix;

    /** The query id. */
    protected String queryId;

    /**
     * Instantiates a new hessian entity invocation handler.
     * @param entityName the entity name
     */
    public HessianEntityInvocationHandler(String entityName) {
        super();
        this.serviceName = ServiceTools.extractServiceFromEntityName(entityName);
        this.serviceMethodPrefix = ServiceTools.extractServiceMethodPrefixFromEntityName(entityName);
        this.queryId = ServiceTools.extractQueryIdFromEntityName(entityName);
    }

    /**
     * Devuelve el servicio asociado a la entidad. Por defecto se devuelve el servicio estándar de
     * ontimize que se encarga en la parte servidor de realizar la petición al repositorio de la
     * entidad.
     * @return the hessian service
     */
    private Object getHessianService() {
        return BeansFactory.getBean(this.serviceName);
    }

    /**
     * Check hessian method exists.
     * @param hessianMethod the hessian method
     * @param methodName the method name
     * @throws InvocationTargetException the invocation target exception
     */
    protected void checkHessianMethodExists(Method hessianMethod, String methodName) throws InvocationTargetException {
        if (hessianMethod == null) {
            throw new InvocationTargetException(null,
                    "Method " + methodName + " not found in service" + this.serviceName);
        }
    }

    private Object invoke(Method method, Object paramObject, Object... paramArrayOfObject) throws Exception {
        try {
            return method.invoke(paramObject, paramArrayOfObject);
        } catch (IllegalArgumentException e) {
            HessianEntityInvocationHandler.logger
                .error("The invoked method's declaration does not match given parameters", e);
            throw e;
        } catch (InvocationTargetException e) {
            HessianEntityInvocationHandler.logger.trace(null, e);
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            } else {
                throw new Exception(null, e.getCause());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.db.Entity#query(java.util.Hashtable, java.util.Vector, int)
     */
    @Override
    public EntityResult query(Hashtable keysValues, Vector attributes, int sessionId) throws Exception {
        Object hessianProxy = this.getHessianService();
        Method hessianMethod = null;
        String methodName = this.serviceMethodPrefix + "Query";
        hessianMethod = ReflectionTools.getMethodByName(hessianProxy.getClass(), methodName);
        this.checkHessianMethodExists(hessianMethod, methodName);
        if (this.queryId == null) {
            return (EntityResult) this.invoke(hessianMethod, hessianProxy, keysValues, attributes);
        } else {
            return (EntityResult) this.invoke(hessianMethod, hessianProxy, keysValues, attributes, this.queryId);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.db.Entity#insert(java.util.Hashtable, int)
     */
    @Override
    public EntityResult insert(Hashtable attributesValues, int sessionId) throws Exception {
        Object hessianProxy = this.getHessianService();
        Method hessianMethod = null;
        String methodName = this.serviceMethodPrefix + "Insert";
        hessianMethod = ReflectionTools.getMethodByName(hessianProxy.getClass(), methodName);
        this.checkHessianMethodExists(hessianMethod, methodName);
        return (EntityResult) this.invoke(hessianMethod, hessianProxy, attributesValues);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.db.Entity#update(java.util.Hashtable, java.util.Hashtable, int)
     */
    @Override
    public EntityResult update(Hashtable attributesValues, Hashtable keysValues, int sessionId) throws Exception {
        Object hessianProxy = this.getHessianService();
        Method hessianMethod = null;
        String methodName = this.serviceMethodPrefix + "Update";
        hessianMethod = ReflectionTools.getMethodByName(hessianProxy.getClass(), methodName);
        this.checkHessianMethodExists(hessianMethod, methodName);
        try {
            return (EntityResult) this.invoke(hessianMethod, hessianProxy, attributesValues, keysValues);
        } catch (Exception e) {
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.db.Entity#delete(java.util.Hashtable, int)
     */
    @Override
    public EntityResult delete(Hashtable keysValues, int sessionId) throws Exception {
        Object hessianProxy = this.getHessianService();
        Method hessianMethod = null;
        String methodName = this.serviceMethodPrefix + "Delete";
        hessianMethod = ReflectionTools.getMethodByName(hessianProxy.getClass(), methodName);
        this.checkHessianMethodExists(hessianMethod, methodName);
        return (EntityResult) this.invoke(hessianMethod, hessianProxy, keysValues);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.db.AdvancedEntity#query(java.util.Hashtable, java.util.Vector, int, int, int,
     * java.lang.String, boolean)
     */
    @Override
    public AdvancedEntityResult query(Hashtable kv, Vector attributes, int sessionId, int recordNumber, int startIndex,
            String orderBy, boolean desc) throws Exception {
        // TODO ojo que hessian no permite polimorfismo
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.db.AdvancedEntity#query(java.util.Hashtable, java.util.Vector, int, int, int,
     * java.util.Vector)
     */
    @Override
    public AdvancedEntityResult query(Hashtable keysValues, Vector attributes, int sessionId, int recordNumber,
            int startIndex, Vector orderBy) throws Exception {
        Object hessianProxy = this.getHessianService();
        Method hessianMethod = null;
        String methodName = this.serviceMethodPrefix + "PaginationQuery";
        hessianMethod = ReflectionTools.getMethodByNameAndParatemers(hessianProxy.getClass(), methodName,
                new Object[] { keysValues, attributes, recordNumber, startIndex, orderBy });
        this.checkHessianMethodExists(hessianMethod, methodName);
        if (this.queryId == null) {
            return (AdvancedEntityResult) this.invoke(hessianMethod, hessianProxy, keysValues, attributes, recordNumber,
                    startIndex, orderBy);
        } else {
            return (AdvancedEntityResult) this.invoke(hessianMethod, hessianProxy, keysValues, attributes, recordNumber,
                    startIndex, orderBy, this.queryId);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.db.AdvancedQueryEntity#getColumnListForAvancedQuery(int)
     */
    @Override
    public Map getColumnListForAvancedQuery(int sessionId) throws Exception {
        // TODO
        return null;
    }

}
