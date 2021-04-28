package com.ontimize.jee.common.gui.field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class represents an attribute in a reference field.
 * <p>
 *
 * @author Imatia Innovation
 */
public class ReferenceFieldAttribute implements Serializable {

    /**
     * The cod reference. By default, null.
     */
    protected String cod = null;

    /**
     * The entity name. By default, null.
     */
    protected String entityName = null;

    /**
     * The attribute name. By default, null.
     */
    protected String attr = null;

    /**
     * The cols reference. By default, null.
     */
    protected List cols = null;

    /**
     * The class constructor. Fixes the parameters.
     * <p>
     * @param attr attribute name
     * @param entity entity name
     * @param cod cod reference
     * @param cols column reference
     */
    public ReferenceFieldAttribute(String attr, String entity, String cod, List cols) {
        this.cod = cod;
        this.entityName = entity;
        this.attr = attr;
        this.cols = cols;
    }

    /**
     * Gets the name of column in the 1-side of relation.
     * @return the cod
     */
    public String getCod() {
        return this.cod;
    }

    /**
     * Gets the entity name.
     * <p>
     * @return the entity name
     */
    public String getEntity() {
        return this.entityName;
    }

    /**
     * Gets the name of attribute in the N-side of relation.
     * @return the cod
     */
    public String getAttr() {
        return this.attr;
    }

    /**
     * Gets the columns to ask for the entity.
     * <p>
     * @return the cols
     */
    public List getCols() {
        return this.cols;
    }

    /**
     * Processes the reference field attribute and returns a <code>Map</code> where the
     * ReferenceFieldAttribute keys will be replaced by their cods.
     * @param keysValues the original <code>Map</code>
     * @return the replaced <code>Map</code>
     */
    public static Map processReferenceFieldAttribute(Map keysValues) {
        if (keysValues == null) {
            return null;
        }
        Map res = new HashMap();
        Enumeration c = Collections.enumeration(keysValues.keySet());
        while (c.hasMoreElements()) {
            Object oKey = c.nextElement();
            Object oValue = keysValues.get(oKey);
            if (oKey instanceof ReferenceFieldAttribute) {
                String attr = ((ReferenceFieldAttribute) oKey).getAttr();
                res.put(attr, oValue);
            } else {
                res.put(oKey, oValue);
            }
        }
        return res;
    }

    /**
     * Processes the reference field attribute and returns a <code>List</code> where the
     * {@link ReferenceFieldAttribute#getAttr()}will be added.
     * @param a the original <code>List</code>
     * @return the modified <code>List</code>
     */
    public static List processReferenceFieldAttribute(List a) {
        if (a == null) {
            return null;
        }
        List res = new ArrayList();
        for (int i = 0; i < a.size(); i++) {
            Object at = a.get(i);
            // Adds the attribute
            res.add(at);
            // If attribute is ReferenceFieldAttribute then adds
            // attribute.getattr too
            if (at instanceof ReferenceFieldAttribute) {
                res.add(((ReferenceFieldAttribute) at).getAttr());
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return this.attr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ReferenceFieldAttribute) {
            boolean sameAttribute = true;
            if (this.attr != null) {
                sameAttribute = this.attr.equals(((ReferenceFieldAttribute) o).getAttr());
            } else if (((ReferenceFieldAttribute) o).getAttr() != null) {
                sameAttribute = false;
            }

            boolean sameCode = true;
            if (this.cod != null) {
                sameCode = this.cod.equals(((ReferenceFieldAttribute) o).getCod());
            } else if (((ReferenceFieldAttribute) o).getCod() != null) {
                sameCode = false;
            }

            boolean sameCols = true;
            if (this.cols != null) {
                if (((ReferenceFieldAttribute) o).getCols() == null) {
                    sameCols = false;
                } else {
                    if (this.cols.size() != ((ReferenceFieldAttribute) o).getCols().size()) {
                        sameCols = false;
                    } else {
                        for (int i = 0; i < this.cols.size(); i++) {
                            if (!((ReferenceFieldAttribute) o).getCols().contains(this.cols.get(i))) {
                                sameCols = false;
                                break;
                            }
                        }
                    }
                }
            } else if (((ReferenceFieldAttribute) o).getCols() != null) {
                sameCols = false;
            }

            boolean sameEntity = true;
            if (this.entityName != null) {
                sameEntity = this.entityName.equals(((ReferenceFieldAttribute) o).getEntity());
            } else if (((ReferenceFieldAttribute) o).getEntity() != null) {
                sameEntity = false;
            }

            return sameAttribute && sameCode && sameCols && sameEntity;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int i = 0;
        if (this.attr != null) {
            i += this.attr.hashCode();
        }
        if (this.cod != null) {
            i += this.cod.hashCode();
        }
        if (this.cols != null) {
            for (int j = 0; j < this.cols.size(); j++) {
                if (this.cols.get(j) != null) {
                    i += this.cols.get(j).hashCode();
                }
            }
        }
        if (this.entityName != null) {
            i += this.entityName.hashCode();
        }
        return i;
    }

}
