package com.ontimize.jee.server.rest;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeleteParameter implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement
    protected Map<Object, Object> filter;

    @XmlElement
    protected Map<Object, Object> sqltypes;

    public Map<Object, Object> getFilter() {
        return this.filter;
    }

    public void setFilter(Map<Object, Object> filter) {
        this.filter = filter;
    }

    public Map<Object, Object> getSqltypes() {
        return this.sqltypes;
    }

    public void setSqltypes(Map<Object, Object> sqltypes) {
        this.sqltypes = sqltypes;
    }

}
