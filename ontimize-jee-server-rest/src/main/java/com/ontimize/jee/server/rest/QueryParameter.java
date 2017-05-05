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
	protected List<? extends Serializable>								columns;

    @XmlElement
	protected Map<? extends Serializable, ? extends Serializable>		filter;

    @XmlElement
	protected HashMap<? extends Serializable, ? extends Serializable>	sqltypes;

	public Map<? extends Serializable, ? extends Serializable> getFilter() {
        return this.filter;
    }

	public void setKv(Map<? extends Serializable, ? extends Serializable> filter) {
        this.filter = filter;
    }

	public List<? extends Serializable> getColumns() {
        return this.columns;
    }

	public void setColumns(List<? extends Serializable> columns) {
        this.columns = columns;
    }

	public HashMap<? extends Serializable, ? extends Serializable> getSqltypes() {
        return this.sqltypes;
    }

	public void setSqltypes(HashMap<? extends Serializable, ? extends Serializable> sqltypes) {
        this.sqltypes = sqltypes;
    }
}