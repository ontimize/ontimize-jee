package com.ontimize.jee.common.gui;


import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MultipleValue implements java.io.Serializable {

    private final Map data = new HashMap();

    public MultipleValue(Map data) {
        if (data != null) {
            this.data.putAll(data);
        }
    }

    public Object get(Object attribute) {
        return this.data.get(attribute);
    }

    public void put(Object attribute, Object value) {
        this.data.put(attribute, value);
    }

    public void clear() {
        this.data.clear();
    }

    public Enumeration keys() {
        return Collections.enumeration(this.data.keySet());
    }

}
