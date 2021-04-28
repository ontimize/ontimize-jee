package com.ontimize.jee.webclient.export.support;

import java.util.ArrayList;
import java.util.List;

import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public class BaseExportColumnProvider implements ExportColumnProvider {

    protected List<HeadExportColumn> headerColumns = new ArrayList<>();

    protected List<ExportColumn> bodyColumns = new ArrayList<>();

    @Override
    public List<HeadExportColumn> getHeaderColumns() {
        return this.headerColumns;
    }

    @Override
    public List<ExportColumn> getBodyColumns() {
        return this.bodyColumns;
    }

    public BaseExportColumnProvider(final List<HeadExportColumn> headerColumns, final List<ExportColumn> bodyColumns) {
        this.headerColumns = headerColumns;
        this.bodyColumns = bodyColumns;
    }

}
