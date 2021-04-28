/**
 * ExportOptions.java 21-jun-2017
 * <p>
 * Copyright 2017 Imatia.com
 */
package com.ontimize.jee.webclient.export.util;

import java.util.List;
import java.util.function.Supplier;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The Class ExportOptions.
 *
 * @author <a href="daniel.grana@imatia.com">Daniel Grana</a>
 */
public class ExportOptions {

    public enum ExcelActionTypes {

        EXPORT, EXPORT_MAIL

    }

    public enum PdfActionTypes {

        EXPORT, EXPORT_MAIL, EXPORT_PRINT

    }

    /**
     * Indica si se habilita la exportación a excel en la extensión. Por defecto true
     */
    private boolean enableExcelExport;

    /**
     * Indica si se habilita la exportación a excel y envio mail en la extensión. Por defecto true
     */
    private boolean enableExcelMailExport;

    /**
     * Habilita la exportacion a pdf.
     */
    private boolean enablePdfExport;

    /**
     * Habilita la exportacion a pdf con envio de correo.
     */
    private boolean enablePdfMailExport;

    /**
     * Habilita la impresion a pdf.
     */
    private boolean enablePdfPrintExport;

    /**
     * Establece si se debe abrir el fichero una vez realizada la exportación
     **/
    private boolean automaticFileOpen = false;

    /**
     * Incluye la marca de tiempo en el nombre del fichero exportado
     **/
    private boolean includeTime = false;

    /**
     * Se congelan las cabeceras para que no hagan scroll
     */
    private boolean freezeHeaders = true;

    private Supplier<List<String>> excludeColumnsSupplier;

    private ObjectProperty<ObservableList<ExcelActionTypes>> excelExportActions;

    private ObjectProperty<ObservableList<ExcelActionTypes>> excelExportActionsProperty() {
        if (this.excelExportActions == null) {
            this.excelExportActions = new SimpleObjectProperty<ObservableList<ExcelActionTypes>>(this,
                    "excel-export-actions") {
                @Override
                protected void invalidated() {
                    super.invalidated();
                    final ObservableList<ExcelActionTypes> list = this.get();
                    if (list != null) {
                        ExportOptions.this.setEnableExcelExport(false);
                        ExportOptions.this.setEnableExcelMailExport(false);
                        list.forEach(item -> {
                            switch (item) {
                                case EXPORT:
                                    ExportOptions.this.setEnableExcelExport(true);
                                    break;
                                case EXPORT_MAIL:
                                    ExportOptions.this.setEnableExcelMailExport(true);
                                    break;
                                default:
                                    break;
                            }
                        });
                    }
                }
            };
        }
        return this.excelExportActions;
    }

    private ObjectProperty<ObservableList<PdfActionTypes>> pdfExportActions;

    private ObjectProperty<ObservableList<PdfActionTypes>> pdfExportActionsProperty() {
        if (this.pdfExportActions == null) {
            this.pdfExportActions = new SimpleObjectProperty<ObservableList<PdfActionTypes>>(this,
                    "pdf-export-actions") {
                @Override
                protected void invalidated() {
                    super.invalidated();
                    final ObservableList<PdfActionTypes> list = this.get();
                    if (list != null) {
                        ExportOptions.this.setEnablePdfExport(false);
                        ExportOptions.this.setEnablePdfMailExport(false);
                        ExportOptions.this.setEnablePdfPrintExport(false);
                        list.forEach(item -> {
                            switch (item) {
                                case EXPORT:
                                    ExportOptions.this.setEnablePdfExport(true);
                                    break;
                                case EXPORT_MAIL:
                                    ExportOptions.this.setEnablePdfMailExport(true);
                                    break;
                                case EXPORT_PRINT:
                                    ExportOptions.this.setEnablePdfPrintExport(true);
                                    break;
                                default:
                                    break;
                            }
                        });
                    }
                }
            };
        }
        return this.pdfExportActions;
    }

    /**
     * Instancia un nuevo export options.
     */
    public ExportOptions() {
        // no-op
    }

    public boolean isEnableExcelExport() {
        return this.enableExcelExport;
    }

    public void setEnableExcelExport(final boolean enableExcelExport) {
        this.enableExcelExport = enableExcelExport;
    }

    public boolean isEnableExcelMailExport() {
        return this.enableExcelMailExport;
    }

    public void setEnableExcelMailExport(final boolean enableExcelMailExport) {
        this.enableExcelMailExport = enableExcelMailExport;
    }

    public boolean isEnablePdfExport() {
        return this.enablePdfExport;
    }

    public void setEnablePdfExport(final boolean enablePdfExport) {
        this.enablePdfExport = enablePdfExport;
    }

    public boolean isEnablePdfMailExport() {
        return this.enablePdfMailExport;
    }

    public void setEnablePdfMailExport(final boolean enablePdfMailExport) {
        this.enablePdfMailExport = enablePdfMailExport;
    }

    public boolean isEnablePdfPrintExport() {
        return this.enablePdfPrintExport;
    }

    public void setEnablePdfPrintExport(final boolean enablePdfPrintExport) {
        this.enablePdfPrintExport = enablePdfPrintExport;
    }

    /**
     * Chequea si automatic file open.
     * @return true, si automatic file open
     */
    public boolean isAutomaticFileOpen() {
        return this.automaticFileOpen;
    }

    /**
     * Establece automatic file open.
     * @param automaticFileOpen nuevo automatic file open
     */
    public void setAutomaticFileOpen(final boolean automaticFileOpen) {
        this.automaticFileOpen = automaticFileOpen;
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

    public List<ExcelActionTypes> getExcelExportActions() {
        return this.excelExportActionsProperty().get();
    }

    public void setExcelExportActions(final List<ExcelActionTypes> excelExportActions) {
        this.excelExportActionsProperty().set(FXCollections.observableArrayList(excelExportActions));
    }

    public void setExcelExportActions(final ExcelActionTypes... excelExportActions) {
        this.excelExportActionsProperty().set(FXCollections.observableArrayList(excelExportActions));
    }

    public List<PdfActionTypes> getPdfExportActions() {
        return this.pdfExportActionsProperty().get();
    }

    public void setPdfExportActions(final List<PdfActionTypes> pdfExportActions) {
        this.pdfExportActionsProperty().set(FXCollections.observableArrayList(pdfExportActions));
    }

    public void setPdfExportActions(final PdfActionTypes... pdfExportActions) {
        this.pdfExportActionsProperty().set(FXCollections.observableArrayList(pdfExportActions));
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
