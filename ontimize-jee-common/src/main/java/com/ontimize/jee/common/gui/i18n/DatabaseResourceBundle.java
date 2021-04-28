package com.ontimize.jee.common.gui.i18n;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;


public class DatabaseResourceBundle extends ResourceBundle implements Serializable {

    protected Map values;

    protected Locale locale;

    public DatabaseResourceBundle(Map data, Locale l) {
        this.values = data;
        if (this.values == null) {
            this.values = new HashMap();
        }
        this.locale = l;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public Enumeration getKeys() {
        return Collections.enumeration(this.values.keySet());
    }

    @Override
    protected Object handleGetObject(String key) {
        return this.values.get(key);
    }

    public Map getValues() {
        return this.values;
    }

    public void updateValues(Map values) {
        this.values = values;
    }

}
