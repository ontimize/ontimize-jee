package com.ontimize.jee.webclient.export;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

public class ExportParameter implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement
    private transient List<Map<Object, Object>> data;

    @XmlElement
    private transient Map<Object, Object> filter;

    private List<String> columns;

    @XmlElement
    private Map<String, String> columnNames;

    @XmlElement
    private Map<String, Integer> sqlTypes;

    public List<Map<Object, Object>> getData() {
        return data;
    }

    public void setData(List<Map<Object, Object>> data) {
        this.data = data;
    }

    public Map<Object, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<Object, Object> filter) {
        this.filter = filter;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public Map<String, String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(Map<String, String> columnNames) {
        this.columnNames = columnNames;
    }

    public Map<String, Integer> getSqlTypes() {
        return sqlTypes;
    }

    public void setSqlTypes(Map<String, Integer> sqlTypes) {
        this.sqlTypes = sqlTypes;
    }

}
