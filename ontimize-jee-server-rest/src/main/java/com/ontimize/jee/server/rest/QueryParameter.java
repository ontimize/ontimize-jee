package com.ontimize.jee.server.rest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QueryParameter implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement
    protected List<Object> columns;

    @XmlElement
    protected Map<Object, Object> filter;

    @XmlElement
    protected HashMap<Object, Object> sqltypes;

    public Map<Object, Object> getFilter() {
        return this.filter;
    }

    public void setKv(Map<Object, Object> filter) {
        this.filter = filter;
    }

    public List<Object> getColumns() {
        return this.columns;
    }

    public void setColumns(List<Object> columns) {
        this.columns = columns;
    }

    public HashMap<Object, Object> getSqltypes() {
        return this.sqltypes;
    }

    public void setSqltypes(HashMap<Object, Object> sqltypes) {
        this.sqltypes = sqltypes;
    }

}
