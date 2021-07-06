/**
 * MappingInfo.java 18/03/2014
 *
 * Copyright 2014 Imatia.
 */
package com.ontimize.jee.server.dao.jpa.common;

import java.util.ArrayList;
import java.util.List;

import com.ontimize.jee.server.dao.jpa.setup.ColumnMapping;

/**
 * The Class MappingInfo.
 *
 * @author <a href="">Sergio Padin</a>
 */
public class MappingInfo {

    private String returnTypeClassName;

    private Boolean returnTypeClassNameIsPrimitive;

    private String returnTypePrimitiveColumnMapping;

    private String returnTypePrimitiveColumnMappingType;

    private final List<ColumnMapping> columnMappings = new ArrayList<>();

    /**
     * Obtiene return type class name.
     * @return return type class name
     */
    public String getReturnTypeClassName() {
        return this.returnTypeClassName;
    }

    /**
     * Establece return type class name.
     * @param returnTypeClassName nuevo return type class name
     */
    public void setReturnTypeClassName(final String returnTypeClassName) {
        this.returnTypeClassName = returnTypeClassName;
    }

    /**
     * Obtiene return type class name is primitive.
     * @return return type class name is primitive
     */
    public Boolean getReturnTypeClassNameIsPrimitive() {
        return this.returnTypeClassNameIsPrimitive;
    }

    /**
     * Establece return type class name is primitive.
     * @param returnTypeClassNameIsPrimitive nuevo return type class name is primitive
     */
    public void setReturnTypeClassNameIsPrimitive(final Boolean returnTypeClassNameIsPrimitive) {
        this.returnTypeClassNameIsPrimitive = returnTypeClassNameIsPrimitive;
    }

    /**
     * Obtiene return type primitive column mapping.
     * @return return type primitive column mapping
     */
    public String getReturnTypePrimitiveColumnMapping() {
        return this.returnTypePrimitiveColumnMapping;
    }

    /**
     * Establece return type primitive column mapping.
     * @param returnTypePrimitiveColumnMapping nuevo return type primitive column mapping
     */
    public void setReturnTypePrimitiveColumnMapping(final String returnTypePrimitiveColumnMapping) {
        this.returnTypePrimitiveColumnMapping = returnTypePrimitiveColumnMapping;
    }

    /**
     * Obtiene column mappings.
     * @return column mappings
     */
    public List<ColumnMapping> getColumnMappings() {
        return this.columnMappings;
    }

    /**
     * Anade el column mapping.
     * @param columnMapping column mapping
     */
    public void addColumnMapping(final ColumnMapping columnMapping) {
        this.columnMappings.add(columnMapping);
    }

    /**
     * Establece return type primitive column mapping type.
     * @param returnTypePrimitiveColumnMappingType nuevo return type primitive column mapping type
     */
    public void setReturnTypePrimitiveColumnMappingType(final String returnTypePrimitiveColumnMappingType) {
        this.returnTypePrimitiveColumnMappingType = returnTypePrimitiveColumnMappingType;
    }

    /**
     * Obtiene return type primitive column mapping type.
     * @return return type primitive column mapping type
     */
    public String getReturnTypePrimitiveColumnMappingType() {
        return this.returnTypePrimitiveColumnMappingType;
    }

}
