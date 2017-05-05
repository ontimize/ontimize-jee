package com.ontimize.jee.server.rest;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UpdateParameter implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement
	protected Map<? extends Serializable, ? extends Serializable>	data;

    @XmlElement
	protected Map<? extends Serializable, ? extends Serializable>	filter;

    @XmlElement
	protected Map<? extends Serializable, ? extends Serializable>	sqltypes;

	public Map<? extends Serializable, ? extends Serializable> getFilter() {
        return this.filter;
    }

	public void setFilter(Map<? extends Serializable, ? extends Serializable> filter) {
        this.filter = filter;
    }

	public Map<? extends Serializable, ? extends Serializable> getData() {
        return this.data;
    }

	public void setData(Map<? extends Serializable, ? extends Serializable> data) {
        this.data = data;
    }

	public Map<? extends Serializable, ? extends Serializable> getSqltypes() {
        return this.sqltypes;
    }

	public void setSqltypes(Map<? extends Serializable, ? extends Serializable> sqltypes) {
        this.sqltypes = sqltypes;
    }
}