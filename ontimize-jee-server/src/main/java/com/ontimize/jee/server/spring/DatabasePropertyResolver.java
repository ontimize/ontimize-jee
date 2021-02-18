package com.ontimize.jee.server.spring;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

import com.ontimize.dto.EntityResult;
import com.ontimize.jee.common.spring.parser.AbstractPropertyResolver;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;

/**
 * The Class DatabasePropertyResolver.
 */
public class DatabasePropertyResolver<T> extends AbstractPropertyResolver<T> implements InitializingBean {

    /** The dao. */
    private IOntimizeDaoSupport dao;

    /** The value column name. */
    private String valueColumnName;

    /** The filter column name. */
    private String filterColumnName;

    /** The filter column value. */
    private String filterColumnValue;

    /** The query id. */
    private String queryId;

    /**
     * The Constructor.
     */
    public DatabasePropertyResolver() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        CheckingTools.failIfNull(this.dao, "Dao not found");
        CheckingTools.failIfEmptyString(this.valueColumnName, "DataSource not found");
        CheckingTools.failIfEmptyString(this.filterColumnName, "DataSource not found");
        CheckingTools.failIfEmptyString(this.filterColumnValue, "DataSource not found");
    }

    /**
     * Sets the dao.
     * @param dao the dao
     */
    public void setDao(IOntimizeDaoSupport dao) {
        this.dao = dao;
    }

    /**
     * Gets the dao.
     * @return the dao
     */
    public IOntimizeDaoSupport getDao() {
        return this.dao;
    }

    /**
     * Sets the value column name.
     * @param valueColumnName the value column name
     */
    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    /**
     * Gets the value column name.
     * @return the value column name
     */
    public String getValueColumnName() {
        return this.valueColumnName;
    }

    /**
     * Sets the filter column name.
     * @param filterColumnName the filter column name
     */
    public void setFilterColumnName(String filterColumnName) {
        this.filterColumnName = filterColumnName;
    }

    /**
     * Gets the filter column name.
     * @return the filter column name
     */
    public String getFilterColumnName() {
        return this.filterColumnName;
    }

    /**
     * Sets the filter column value.
     * @param filterColumnValue the filter column value
     */
    public void setFilterColumnValue(String filterColumnValue) {
        this.filterColumnValue = filterColumnValue;
    }

    /**
     * Gets the filter column value.
     * @return the filter column value
     */
    public String getFilterColumnValue() {
        return this.filterColumnValue;
    }

    /**
     * @return the queryId
     */
    public String getQueryId() {
        return this.queryId;
    }

    /**
     * @param queryId the queryId to set
     */
    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    /**
     * Gets the value.
     * @return the value
     * @throws DataAccessException the data access exception
     */
    @Override
    public T getValue() throws DataAccessException {
        Map<String, Object> keysValues = new HashMap<>();
        keysValues.put(this.filterColumnName, this.filterColumnValue);
        List<String> attributes = Arrays.asList(new String[] { this.valueColumnName });
        EntityResult query = this.dao.query(keysValues, attributes, null, this.queryId);
        CheckingTools.failIf(query.calculateRecordNumber() != 1,
                "Invalid result number querying property %s with value %s", this.filterColumnName,
                this.filterColumnValue);
        return (T) ((List<Object>) query.get(this.valueColumnName)).get(0);
    }

}
