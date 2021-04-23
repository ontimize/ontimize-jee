package com.ontimize.jee.core.common.util.extend;

import java.util.Properties;

public class OrderProperties implements Comparable<OrderProperties> {

    protected int index;

    protected Properties property;

    protected String name;

    public OrderProperties(int index, Properties properties) {
        this(index, properties, null);
    }

    public OrderProperties(int index, Properties properties, String name) {
        this.index = index;
        this.property = properties;
        this.name = name;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setProperties(Properties properties) {
        this.property = properties;
    }

    public int getIndex() {
        return this.index;
    }

    public Properties getProperties() {
        return this.property;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int compareTo(OrderProperties compareProperties) {
        int compareIndex = compareProperties.getIndex();
        return this.index - compareIndex;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
