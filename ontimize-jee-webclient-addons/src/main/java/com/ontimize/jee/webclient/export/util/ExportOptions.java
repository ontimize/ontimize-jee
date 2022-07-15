/**
 * ExportOptions.java 21-jun-2017
 * <p>
 * Copyright 2017 Imatia.com
 */
package com.ontimize.jee.webclient.export.util;

import java.util.List;
import java.util.function.Supplier;

/**
 * The Class ExportOptions.
 *
 * @author <a href="daniel.grana@imatia.com">Daniel Grana</a>
 */
public class ExportOptions {

    /**
     * Incluye la marca de tiempo en el nombre del fichero exportado
     **/
    private boolean includeTime = false;

    /**
     * Se congelan las cabeceras para que no hagan scroll
     */
    private boolean freezeHeaders = true;

    private Supplier<List<String>> excludeColumnsSupplier;


    /**
     * Instancia un nuevo export options.
     */
    public ExportOptions() {
        // no-op
    }


    /**
     * Chequea si include time.
     * @return true, si include time
     */
    public boolean isIncludeTime() {
        return this.includeTime;
    }

    /**
     * Establece include time.
     * @param includeTime nuevo include time
     */
    public void setIncludeTime(final boolean includeTime) {
        this.includeTime = includeTime;
    }

    /**
     * Obtiene exclude columns supplier.
     * @return exclude columns supplier
     */
    public Supplier<List<String>> getExcludeColumnsSupplier() {
        return this.excludeColumnsSupplier;
    }

    /**
     * Establece exclude columns supplier.
     * @param excludeColumnsSupplier nuevo exclude columns supplier
     */
    public void setExcludeColumnsSupplier(final Supplier<List<String>> excludeColumnsSupplier) {
        this.excludeColumnsSupplier = excludeColumnsSupplier;
    }

    public boolean isFreezeHeaders() {
        return this.freezeHeaders;
    }

    public void setFreezeHeaders(final boolean freezeHeaders) {
        this.freezeHeaders = freezeHeaders;
    }

}
