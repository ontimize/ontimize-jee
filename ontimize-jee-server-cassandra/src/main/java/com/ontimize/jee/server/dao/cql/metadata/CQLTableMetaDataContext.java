package com.ontimize.jee.server.dao.cql.metadata;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.common.INameConvention;

public class CQLTableMetaDataContext {

    protected final static Logger logger = LoggerFactory.getLogger(CQLTableMetaDataContext.class);

    private INameConvention nameConvention;

    private boolean processed;

    private String keyspace;

    private String tableName;

    /** List of columns objects to be used in this context */
    private final List<String> tableColumns = new ArrayList<String>();

    public INameConvention getNameConvention() {
        return this.nameConvention;
    }

    public void setNameConvention(INameConvention nameConvention) {
        this.nameConvention = nameConvention;
    }

    public void processMetaData(Session session) {
        CQLTableMetaDataContext.logger.debug("Attempting to get metadata for Cassandra table {}.{}", this.keyspace,
                this.tableName);

        final KeyspaceMetadata keyspaceMetadata = session.getCluster().getMetadata().getKeyspace(this.keyspace);
        if (keyspaceMetadata == null) {
            throw new OntimizeJEERuntimeException(this.keyspace + " keyspace doesn't exist");
        }

        final TableMetadata tableMetadata = keyspaceMetadata.getTable(this.tableName);
        if (tableMetadata == null) {
            throw new OntimizeJEERuntimeException(this.tableName + " table doesn't exist");
        }

        List<ColumnMetadata> columnDefinition = tableMetadata.getColumns();

        for (ColumnMetadata currentColumn : columnDefinition) {
            this.tableColumns.add(currentColumn.getName());
        }

        this.processed = true;
    }

    public boolean isProcessed() {
        return this.processed;
    }

    public String getKeyspace() {
        return this.keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getNameConventionTableColumns() {
        List<String> tableColumns = this.getTableColumns();
        return this.changeColumnNameToNameConvention(tableColumns);
    }

    private List<String> changeColumnNameToNameConvention(List<String> columnNames) {
        for (Object columnName : columnNames) {
            if (columnName instanceof String) {
                columnNames.set(columnNames.indexOf(columnName), this.nameConvention.convertName((String) columnName));
            }
        }
        return columnNames;
    }

    /**
     * Get a List of the table column names.
     */
    public List<String> getTableColumns() {
        return this.tableColumns;
    }

}
