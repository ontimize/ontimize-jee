package com.ontimize.jee.common.gui.field;

import com.ontimize.jee.common.gui.table.ExtendedTableAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Object to stores information about many table values <br>
 */
public class MultipleTableAttribute implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(MultipleTableAttribute.class);

    protected Map attributesTable = null;

    protected Object attr = null;

    public MultipleTableAttribute(Object attr) {
        this.attributesTable = new HashMap();
        this.attr = attr;
    }

    public Enumeration keys() {
        return Collections.enumeration(this.attributesTable.keySet());
    }

    /**
     * Get the number of ExtendedTableAttribute stored in this object
     * @return
     */
    public int getEntityCount() {
        return this.attributesTable.size();
    }

    /**
     * Get the object attribute
     * @return
     */
    public Object getAttribute() {
        return this.attr;
    }

    /**
     * Get the ExtendedTableAttribute for the specified key
     * @param key Typically is a entity name
     * @return
     */
    public ExtendedTableAttribute getExtendedTableAttribute(String key) {
        return (ExtendedTableAttribute) this.attributesTable.get(key);
    }

    /**
     * Insert an ExtendedTableAttribute with the specified key
     * @param key Table attribute key
     * @param extendedAttribute Value
     */
    public void setExtendedTableAttribute(String key, ExtendedTableAttribute extendedAttribute) {
        this.attributesTable.put(key, extendedAttribute);
    }

    public Map getExtendedTableAttributes() {
        return this.attributesTable;
    }

    @Override
    public String toString() {
        return this.attr.toString();
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (o == this) {
                return true;
            }
            if (!(o instanceof MultipleTableAttribute)) {
                return false;
            }
            return ((MultipleTableAttribute) o).attr.equals(this.attr) && (this.hashCode() == o.hashCode());
        } catch (Exception ex) {
            MultipleTableAttribute.logger.error(null, ex);
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return (this.attr != null ? this.attr.hashCode() : 0) + this.attr.hashCode()
                    + (this.attributesTable != null ? this.attributesTable.hashCode() : 0);
        } catch (Exception ex) {
            MultipleTableAttribute.logger.error(null, ex);
            return -1;
        }
    }

}
