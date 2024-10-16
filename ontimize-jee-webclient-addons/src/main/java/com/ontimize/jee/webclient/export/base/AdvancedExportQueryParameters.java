package com.ontimize.jee.webclient.export.base;

import javax.xml.bind.annotation.XmlElement;
import java.util.Map;

public class AdvancedExportQueryParameters extends BaseExportQueryParameters {

    @XmlElement
    private Map<String, Object> columns;

    @XmlElement
    private Map<String, String> columnTitles;

    @XmlElement
    private Map<String, String> columnTypes;

    @XmlElement
    private Map<String, Map<String, String>> styles;

    @XmlElement
    private Map<String, Object> columnStyles;

    @XmlElement
    private Map<String, Object> columnHeaderStyles;

    @XmlElement
    private Map<String, Object> rowStyles;

    @XmlElement
    private Map<String, Object> cellStyles;

    @XmlElement
    private Boolean landscape;

    public AdvancedExportQueryParameters() {
        super();
    }

    public Map<String, Object> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, Object> columns) {
        this.columns = columns;
    }

    public Map<String, String> getColumnTitles() {
        return columnTitles;
    }

    public void setColumnTitles(Map<String, String> columnTitles) {
        this.columnTitles = columnTitles;
    }

    public Map<String, String> getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(Map<String, String> columnTypes) {
        this.columnTypes = columnTypes;
    }

    public Map<String, Map<String, String>> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, Map<String, String>> styles) {
        this.styles = styles;
    }

    public Map<String, Object> getColumnStyles() {
        return columnStyles;
    }

    public void setColumnStyles(Map<String, Object> columnStyles) {
        this.columnStyles = columnStyles;
    }

    public Map<String, Object> getRowStyles() {
        return rowStyles;
    }

    public void setRowStyles(Map<String, Object> rowStyles) {
        this.rowStyles = rowStyles;
    }

    public Map<String, Object> getCellStyles() {
        return cellStyles;
    }

    public void setCellStyles(Map<String, Object> cellStyles) {
        this.cellStyles = cellStyles;
    }

    public Map<String, Object> getColumnHeaderStyles() {
        return columnHeaderStyles;
    }

    public void setColumnHeaderStyles(Map<String, Object> columnHeaderStyles) {
        this.columnHeaderStyles = columnHeaderStyles;
    }

    public Boolean getLandscape() {
      return landscape;
    }

    public void setLandscape(Boolean landscape) {
      this.landscape = landscape;
    }
}
