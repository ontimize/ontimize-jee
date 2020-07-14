package com.ontimize.jee.server.rest;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.caucho.hessian.util.IExceptionTranslator;
import com.ontimize.db.AdvancedEntityResult;
import com.ontimize.db.EntityResult;
import com.ontimize.db.NullValue;
import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.jee.common.jackson.OntimizeMapper;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.ReflectionTools;


public abstract class ORestController<S> {

    /** The logger. */
    private final static Logger logger = LoggerFactory.getLogger(ORestController.class);

    public static final String QUERY = "Query";

    public static final String PAGINATION_QUERY = "PaginationQuery";

    public static final String INSERT = "Insert";

    public static final String DELETE = "Delete";

    public static final String UPDATE = "Update";

    public static final String BASIC_EXPRESSION = "@basic_expression";

    public static final String FILTER_EXPRESSION = "@filter_expression";

    public abstract S getService();

    @Autowired
    OntimizeMapper mapper;

    @Autowired(required = false)
    protected IExceptionTranslator exceptionTranslator;

    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> query(@PathVariable("name") String name,
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "columns", required = false) String columns) {
        CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
        StringBuffer buffer = new StringBuffer();
        buffer.append(name).append(ORestController.QUERY);
        try {
            Map<Object, Object> keysValues = this.createKeysValues(filter);
            List<Object> attributes = this.createAttributesValues(columns);
            EntityResult eR = (EntityResult) ReflectionTools.invoke(this.getService(), buffer.toString(), keysValues,
                    attributes);
            return new ResponseEntity<>(eR, HttpStatus.OK);
        } catch (Exception error) {
            ORestController.logger.error(null, error);
            EntityResult entityResult = new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.BEST_COMPRESSION);
            entityResult.setMessage(error.getMessage());
            return new ResponseEntity<>(entityResult, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{name}/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> query(@PathVariable("name") String name,
            @RequestBody QueryParameter queryParameter) throws Exception {
        ORestController.logger.debug("Invoked /{}/search", name);
        CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
        ORestController.logger.debug("Service name: {}", this.getService());
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append(name).append(ORestController.QUERY);

            Map<?, ?> kvQueryParameter = queryParameter.getFilter();
            List<?> avQueryParameter = queryParameter.getColumns();
            HashMap<?, ?> hSqlTypes = queryParameter.getSqltypes();

            Map<Object, Object> keysValues = this.createKeysValues(kvQueryParameter, hSqlTypes);
            List<Object> attributesValues = this.createAttributesValues(avQueryParameter, hSqlTypes);

            EntityResult eR = (EntityResult) ReflectionTools.invoke(this.getService(), buffer.toString(), keysValues,
                    attributesValues);
            return new ResponseEntity<>(eR, HttpStatus.OK);
        } catch (Exception e) {
            return this.processError(e);
        }
    }

    @RequestMapping(value = "/{name}/advancedsearch", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdvancedEntityResult> query(@PathVariable("name") String name,
            @RequestBody AdvancedQueryParameter aQueryParameter) throws Exception {
        ORestController.logger.debug("Invoked /{}/advancedsearch", name);
        CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
        ORestController.logger.debug("Service name: {}", this.getService());
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append(name).append(ORestController.PAGINATION_QUERY);

            Map<?, ?> kvQueryParameter = aQueryParameter.getFilter();
            List<?> avQueryParameter = aQueryParameter.getColumns();
            HashMap<?, ?> hSqlTypes = aQueryParameter.getSqltypes();
            Integer pagesize = aQueryParameter.getPageSize();
            Integer offset = aQueryParameter.getOffset();
            List<SQLOrder> orderby = aQueryParameter.getOrderBy();

            Map<Object, Object> keysValues = this.createKeysValues(kvQueryParameter, hSqlTypes);
            List<Object> attributesValues = this.createAttributesValues(avQueryParameter, hSqlTypes);

            AdvancedEntityResult eR = (AdvancedEntityResult) ReflectionTools.invoke(this.getService(),
                    buffer.toString(), keysValues, attributesValues, pagesize, offset, orderby);
            return new ResponseEntity<>(eR, HttpStatus.OK);
        } catch (Exception e) {
            return this.processAdvancedError(e);
        }
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> insert(@PathVariable("name") String name,
            @RequestBody InsertParameter insertParameter) {
        CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
        StringBuffer buffer = new StringBuffer();
        buffer.append(name).append(ORestController.INSERT);
        try {
            Map<?, ?> avInsertParameter = insertParameter.getData();
            Map<?, ?> hSqlTypes = insertParameter.getSqltypes();
            Map<?, ?> attributes = this.createKeysValues(avInsertParameter, hSqlTypes);
            EntityResult eR = (EntityResult) ReflectionTools.invoke(this.getService(), buffer.toString(), attributes);
            return new ResponseEntity<>(eR, HttpStatus.OK);
        } catch (Exception e) {
            return this.processError(e);
        }
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> delete(@PathVariable("name") String name,
            @RequestBody DeleteParameter deleteParameter) {
        Object service = this.getService();
        CheckingTools.failIf(service == null, NullPointerException.class, "Service is null");
        StringBuffer buffer = new StringBuffer();
        buffer.append(name).append(ORestController.DELETE);
        try {
            Map<?, ?> kvDeleteParameter = deleteParameter.getFilter();
            Map<?, ?> hSqlTypes = deleteParameter.getSqltypes();
            Map<?, ?> attributes = this.createKeysValues(kvDeleteParameter, hSqlTypes);
            EntityResult eR = (EntityResult) ReflectionTools.invoke(this.getService(), buffer.toString(), attributes);
            return new ResponseEntity<>(eR, HttpStatus.OK);
        } catch (Exception e) {
            return this.processError(e);
        }
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> update(@PathVariable("name") String name,
            @RequestBody UpdateParameter updateParameter) {
        CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
        StringBuffer buffer = new StringBuffer();
        buffer.append(name).append(ORestController.UPDATE);
        try {
            Map<?, ?> filter = updateParameter.getFilter();
            Map<?, ?> data = updateParameter.getData();
            Map<?, ?> hSqlTypes = updateParameter.getSqltypes();
            Map<?, ?> kv = this.createKeysValues(filter, hSqlTypes);
            Map<?, ?> av = this.createKeysValues(data, hSqlTypes);
            EntityResult eR = (EntityResult) ReflectionTools.invoke(this.getService(), buffer.toString(), av, kv);
            return new ResponseEntity<>(eR, HttpStatus.OK);
        } catch (Exception e) {
            return this.processError(e);
        }
    }

    protected ResponseEntity<EntityResult> processError(Exception error) {
        ORestController.logger.error("{}", error.getMessage(), error);
        EntityResult entityResult = new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.BEST_COMPRESSION);
        entityResult.setMessage(this.getErrorMessage(error));
        return new ResponseEntity<>(entityResult, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity<AdvancedEntityResult> processAdvancedError(Exception error) {
        ORestController.logger.error("{}", error.getMessage(), error);
        AdvancedEntityResult advancedEntityResult = new AdvancedEntityResult(EntityResult.OPERATION_WRONG,
                EntityResult.BEST_COMPRESSION);
        advancedEntityResult.setMessage(this.getErrorMessage(error));
        return new ResponseEntity<>(advancedEntityResult, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected String getErrorMessage(Exception error) {
        if (this.exceptionTranslator != null) {
            if (error.getCause() != null) {
                // deberian venir siempre en una ontimizejeeruntimeexception que lanza reflectiontools
                return this.exceptionTranslator.translateException(error.getCause()).getMessage();
            }
            return this.exceptionTranslator.translateException(error).getMessage();
        }
        if (error.getCause() != null) {
            return error.getCause().getMessage();
        }
        return error.getMessage();
    }

    protected Map<Object, Object> createKeysValues(String filter) {
        Map<Object, Object> keysValues = new HashMap<>();
        if ((filter == null) || (filter.length() == 0)) {
            return keysValues;
        }
        StringTokenizer tokens = new StringTokenizer(filter, "&");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            int index = token.indexOf("=");
            if (index >= 0) {
                String columnName = token.substring(0, index).trim();
                String columnValue = token.substring(index + 1).trim();
                keysValues.put(columnName, columnValue);
            }
        }
        return keysValues;
    }

    protected List<Object> createAttributesValues(String columns) {
        List<Object> attributes = new ArrayList<>();
        if ((columns == null) || (columns.length() == 0)) {
            attributes.add("*");
            return attributes;
        }

        StringTokenizer tokens = new StringTokenizer(columns, ",");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            attributes.add(token);
        }
        return attributes;
    }

    protected Map<Object, Object> createKeysValues(Map<?, ?> kvQueryParam, Map<?, ?> hSqlTypes) {
        Map<Object, Object> kv = new HashMap<>();
        if ((kvQueryParam == null) || kvQueryParam.isEmpty()) {
            return kv;
        }

        if (kvQueryParam.containsKey(ORestController.BASIC_EXPRESSION)) {
            Object basicExpressionValue = kvQueryParam.remove(ORestController.BASIC_EXPRESSION);
            this.processBasicExpression(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, kv,
                    basicExpressionValue, hSqlTypes);
        }

        if (kvQueryParam.containsKey(ORestController.FILTER_EXPRESSION)) {
            Object basicExpressionValue = kvQueryParam.remove(ORestController.FILTER_EXPRESSION);
            this.processBasicExpression(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.FILTER_KEY, kv,
                    basicExpressionValue, hSqlTypes);
        }

        for (Entry<?, ?> next : kvQueryParam.entrySet()) {
            Object key = next.getKey();
            Object value = next.getValue();
            if ((hSqlTypes != null) && hSqlTypes.containsKey(key)) {
                int sqlType = (Integer) hSqlTypes.get(key);
                value = ParseUtilsExt.getValueForSQLType(value, sqlType);
                if (value == null) {
                    if (ParseUtilsExt.BASE64 == sqlType) {
                        sqlType = Types.BINARY;
                    }
                    value = new NullValue(sqlType);
                }
            } else if (value == null) {
                value = new NullValue();
            }
            kv.put(key, value);
        }
        return kv;
    }

    protected List<Object> createAttributesValues(List<?> avQueryParam, Map<?, ?> hSqlTypes) {
        List<Object> av = new ArrayList<>();
        if ((avQueryParam == null) || avQueryParam.isEmpty()) {
            av.add("*");
            return av;
        }
        av.addAll(avQueryParam);
        return av;
    }

    protected void processBasicExpression(String key, Map<?, ?> keysValues, Object basicExpression,
            Map<?, ?> hSqlTypes) {
        if (basicExpression instanceof Map) {
            try {
                BasicExpression bE = BasicExpressionProcessor.getInstance()
                    .processBasicEspression(basicExpression, hSqlTypes);
                ((Map<Object, Object>) keysValues).put(key, bE);
            } catch (Exception error) {
                ORestController.logger.error(null, error);
            }
        }

    }

    protected void processBasicExpression(String key, Map<Object, Object> keysValues, Object basicExpression) {
        this.processBasicExpression(key, keysValues, basicExpression, new HashMap<>());
    }

}
