package com.ontimize.jee.common.gui.field;

import com.ontimize.jee.common.gui.MultipleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MultipleReferenceDataFieldAttribute implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(MultipleReferenceDataFieldAttribute.class);

    private String attr = null;

    private List cods = null;

    private List typecods = null;

    private List parentCods = null;

    private List keys = null;

    private List parentkeys = null;

    private String entity = null;

    private List cols = null;

    private final long timeCache = 60000;

    private long lastCache = 0;

    public MultipleReferenceDataFieldAttribute(String attr, String entity, List cods, List typecods, List keys,
            List cols, List parentcods, List parentkeys) {
        this.attr = attr;
        this.cods = cods;
        this.typecods = typecods;
        this.keys = keys;

        this.cols = cols;

        this.parentkeys = parentkeys;
        this.parentCods = parentcods;

        this.entity = entity;
    }

    public String getAttr() {
        return this.attr;
    }

    public List getCods() {
        return this.cods;
    }

    public List getKeys() {
        return this.keys;
    }

    public List getCols() {
        return this.cols;
    }

    public List getTypeCods() {
        return this.typecods;
    }

    public List getParentCods() {
        return this.parentCods;
    }

    public List getParentKeys() {
        return this.parentkeys;
    }

    public String getEntity() {
        return this.entity;
    }

    @Override
    public String toString() {
        return this.attr.toString();
    }

    public Integer getTypeData(Object cod) {
        int index = -1;
        if (this.cods != null) {
            index = this.cods.indexOf(cod);
        }
        if (index == -1) {
            return null;
        } else {
            return (Integer) this.typecods.get(index);
        }
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (o == this) {
                return true;
            }
            if (!(o instanceof MultipleReferenceDataFieldAttribute)) {
                return false;
            }
            return ((MultipleReferenceDataFieldAttribute) o).attr.equals(this.attr)
                    && (this.hashCode() == o.hashCode());
        } catch (Exception ex) {
            MultipleReferenceDataFieldAttribute.logger.error(null, ex);
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return this.attr != null ? this.attr.hashCode() : 0;
        } catch (Exception ex) {
            MultipleReferenceDataFieldAttribute.logger.error(null, ex);
            return -1;
        }
    }

    public boolean needRefreshCache() {
        if (this.parentCods != null) {
            return true;
        }
        if ((System.currentTimeMillis() - this.lastCache) > this.timeCache) {
            return true;
        }
        return false;
    }

    public void setLastCacheTime(long t) {
        this.lastCache = t;
    }

    /**
     * Process the attributes with class MultipleReferenceDataFieldAttribute.
     * @param kv
     * @return The same Map that the input parameter but using
     *         MultipleReferenceDataFieldAttribute.getCods() to replace the
     *         MultipleReferenceDataFieldAttribute attributes
     */
    public static Map processMultipleReferenceFieldAttributes(Map kv) {
        if (kv == null) {
            return null;
        }
        Map res = new HashMap();
        Enumeration c = Collections.enumeration(kv.keySet());
        while (c.hasMoreElements()) {
            Object oKey = c.nextElement();
            Object oValue = kv.get(oKey);
            if (oKey instanceof MultipleReferenceDataFieldAttribute) {
                if (oValue instanceof MultipleValue) {
                    List cods = ((MultipleReferenceDataFieldAttribute) oKey).getCods();
                    for (Iterator i = cods.iterator(); i.hasNext();) {
                        Object k = i.next();
                        Object value = ((MultipleValue) oValue).get(k);
                        if (value != null) {
                            res.put(k, value);
                        }
                    }
                }
            } else {
                res.put(oKey, oValue);
            }
        }
        return res;
    }

    /**
     * Creates a List with the objects in the input parameters and adds all the objects inside the
     * attributes with class MultipleReferenceDataFieldAttribute.
     * @param a
     * @return
     */
    public static List processMultipleReferenceFieldAttributes(List a) {
        if (a == null) {
            return null;
        }
        List res = new ArrayList();
        for (int i = 0; i < a.size(); i++) {
            Object at = a.get(i);
            // Add the attribute
            res.add(at);
            if (at instanceof MultipleReferenceDataFieldAttribute) {
                List l = ((MultipleReferenceDataFieldAttribute) at).getCods();
                for (Iterator it = l.iterator(); it.hasNext();) {
                    res.add(it.next());
                }
            }
        }
        return res;
    }

}
