package com.ontimize.jee.webclient.export.asyncexec;

import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.webclient.export.util.ExportOptions;


public abstract class AbstractExportCallableTask<C> extends AbstractBaseExportCallableTask<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExportCallableTask.class);

    public AbstractExportCallableTask() {
        // no-op
    }

    @Override
    public void setTable(final C table) {
        this.table = table;
    }

    @Override
    public C getTable() {
        return this.table;
    }

    @Override
    protected abstract void doExport(final FileOutputStream fileOutputStream,
            final C table, final ExportOptions exportOptions);

}
