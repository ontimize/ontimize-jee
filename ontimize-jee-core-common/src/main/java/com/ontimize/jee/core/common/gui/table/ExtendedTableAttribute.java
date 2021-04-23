package com.ontimize.jee.core.common.gui.table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendedTableAttribute extends TableAttribute {

    private static final Logger logger = LoggerFactory.getLogger(ExtendedTableAttribute.class);

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        try {
            return (this.attributes != null ? this.attributes.hashCode() : 0) + this.entity
                .hashCode() + (this.keys != null ? this.keys.hashCode() : 0)
                    + (this.parentkeys != null ? this.parentkeys.hashCode() : 0);
        } catch (Exception ex) {
            ExtendedTableAttribute.logger.error(null, ex);
            return -1;
        }
    }

}
