package com.ontimize.jee.server.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ontimize.db.SQLStatementBuilder.SQLOrder;

@XmlRootElement
public class AdvancedQueryParameter extends QueryParameter {

    @XmlElement(name = "pagesize")
    protected int pageSize = -1;

    @XmlElement
    protected int offset = -1;

    @XmlElement
    protected List<SQLOrder> orderBy;

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public List<SQLOrder> getOrderBy() {
        return this.orderBy;
    }

    public void setOrderBy(List<SQLOrder> orderBy) {
        this.orderBy = orderBy;
    }

}
