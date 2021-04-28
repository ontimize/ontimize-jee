package com.ontimize.jee.common.gui.table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Attribute used for the tables. Contains information about the column names, the entity, the keys,
 * the parent keys and the number of records to query
 */
public class TableAttribute extends HashMap {

    private static final Logger logger = LoggerFactory.getLogger(TableAttribute.class);

    protected int recordNumberToInitiallyDownload = -1;

    protected String entity = null;

    protected List attributes = new ArrayList();

    protected int totalRecordNumberInQuery = 0;

    protected List parentkeys = new ArrayList();

    protected Map hParentkeyEquivalences;

    protected List keys = null;

    protected Map queryFilter;

    protected List orderBy;

    /**
     * Creates a TableAttribute with recordNumberToInitiallyDownload= -1
     */
    public TableAttribute() {
        super();
    }

    /**
     * Creates a new TableAttribute.<br>
     * Parameter 'recordNumberToInitiallyDownload' indicates the number of records to download when a
     * query includes this attribute . If this parameter is less than 0 then no limit exists.
     */
    public TableAttribute(int recordNumberToInitiallyDownload) {
        super();
        this.recordNumberToInitiallyDownload = recordNumberToInitiallyDownload;
    }

    public int getQueryRecordNumber() {
        return this.totalRecordNumberInQuery;
    }

    public void setTotalRecordNumberInQuery(int recordNumberToInitiallyDownload) {
        this.totalRecordNumberInQuery = recordNumberToInitiallyDownload;
    }

    public int getRecordNumberToInitiallyDownload() {
        return this.recordNumberToInitiallyDownload;
    }

    public void setRecordNumberToInitiallyDownload(int recordNumberToInitiallyDownload) {
        this.recordNumberToInitiallyDownload = recordNumberToInitiallyDownload;
    }

    public void setEntityAndAttributes(String entity, List attributes) {
        this.entity = entity;
        this.attributes = attributes;
        this.put(entity, attributes);
    }

    public void setKeysParentkeysOtherkeys(List keys, List parentkeys) {
        this.keys = keys;
        this.parentkeys = parentkeys;
    }

    public Map getParentkeyEquivalences() {
        return this.hParentkeyEquivalences;
    }

    public String getParentkeyEquivalence(String parentkey) {
        if ((this.hParentkeyEquivalences != null) && this.hParentkeyEquivalences.containsKey(parentkey)) {
            return (String) this.hParentkeyEquivalences.get(parentkey);
        }
        return parentkey;
    }

    public void setParentkeyEquivalences(Map hParentkeyEquivalences) {
        this.hParentkeyEquivalences = hParentkeyEquivalences;
    }

    public String getEntity() {
        return this.entity;
    }

    public List getAttributes() {
        return (List) new ArrayList(this.attributes);
    }

    public List getKeys() {
        return (List) new ArrayList(this.keys);
    }

    public List getParentKeys() {
        return (List) new ArrayList(this.parentkeys);
    }

    public Map getQueryFilter() {
        return this.queryFilter;
    }

    public void setQueryFilter(Map queryFilter) {
        this.queryFilter = queryFilter;
    }

    public List getOrderBy() {
        return this.orderBy;
    }

    public void setOrderBy(List orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (o == this) {
                return true;
            }
            if (!(o instanceof TableAttribute)) {
                return false;
            }
            return ((TableAttribute) o).entity.equals(this.entity) && (this.hashCode() == o.hashCode());
        } catch (Exception ex) {
            TableAttribute.logger.error(null, ex);
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return this.entity.hashCode() + (this.keys != null ? this.keys.hashCode() : 0)
                    + (this.parentkeys != null ? this.parentkeys.hashCode() : 0);
        } catch (Exception ex) {
            TableAttribute.logger.error(null, ex);
            return -1;
        }
    }

    @Override
    public String toString() {
        return this.entity;
    }

}
