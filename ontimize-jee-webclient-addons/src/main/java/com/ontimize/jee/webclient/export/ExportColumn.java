package com.ontimize.jee.webclient.export;

import java.util.List;

/**
 * ExportColumn, representación de una columna para exportar. Contiene el patrón de formato, el
 * título, y las posibles columnas anidadas
 */
public class ExportColumn<T> implements HeadExportColumn {

    String id;

    T style;

    int width;

    private String title;

    public ExportColumn(
            final String id,
            final String title,
            final int width,
            final T style) {
        this.id = id;
        this.title = title;
        this.width = width;
        this.style = style;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public T getStyle() {
        return this.style;
    }

    public void setStyle(final T style) {
        this.style = style;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeadExportColumnCount() {
        return 0;
    }

    @Override
    public HeadExportColumn getHeadExportColumn(final int index) {
        return null;
    }

    @Override
    public void addHeadExportColumn(final HeadExportColumn headExportColumn) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean contains(final HeadExportColumn headExportColumn) {
        return false;
    }

    @Override
    public List<HeadExportColumn> getColumns() {
        return null;
    }

}
