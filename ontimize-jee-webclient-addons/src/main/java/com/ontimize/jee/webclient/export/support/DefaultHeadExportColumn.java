package com.ontimize.jee.webclient.export.support;

import java.util.ArrayList;
import java.util.List;

import com.ontimize.jee.webclient.export.HeadExportColumn;

public class DefaultHeadExportColumn<T> implements HeadExportColumn {

    String id;

    protected String title;

    T style;

    protected List<HeadExportColumn> list = new ArrayList<>();

    @Override
    public List<HeadExportColumn> getColumns() {
        return list;
    }

    public DefaultHeadExportColumn(String id, String title, T style) {
        this.id = id;
        this.title = title;
        this.style = style;
    }

    public DefaultHeadExportColumn(String id, final String title) {
        this(id, title, null);
    }

    public DefaultHeadExportColumn(String id) {
        this(id, id, null);
    }

    @Override
    public void addHeadExportColumn(final HeadExportColumn headExportColumn) {
        this.list.add(headExportColumn);
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public int getHeadExportColumnCount() {
        if (this.list != null) {
            return this.list.size();
        }
        return 0;
    }

    @Override
    public HeadExportColumn getHeadExportColumn(final int index) {
        if (this.list != null) {
            return this.list.get(index);
        }
        return null;
    }

    @Override
    public boolean contains(final HeadExportColumn headExportColumn) {
        return this.list.contains(headExportColumn);
    }

    public T getStyle() {
        return style;
    }

    public void setStyle(T style) {
        this.style = style;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
