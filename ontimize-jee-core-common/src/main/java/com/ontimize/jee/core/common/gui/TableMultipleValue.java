package com.ontimize.jee.core.common.gui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TableMultipleValue implements Serializable {

    protected Object value = null;

    protected Map values = null;

    public TableMultipleValue(Object value) {
        this.value = value;
        this.values = new HashMap();
    }

    public void put(Object key, Object value) {
        this.values.put(key, value);
    }

    public Map getValues() {
        return this.values;
    }

    public Object getValue() {
        return this.value;
    }

}
