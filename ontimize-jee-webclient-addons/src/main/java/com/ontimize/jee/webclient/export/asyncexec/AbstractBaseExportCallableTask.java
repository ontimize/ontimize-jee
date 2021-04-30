package com.ontimize.jee.webclient.export.asyncexec;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.webclient.export.ExportType;
import com.ontimize.jee.webclient.export.executor.AsyncResult;
import com.ontimize.jee.webclient.export.executor.TaskControl;
import com.ontimize.jee.webclient.export.util.ExportOptions;
import com.ontimize.jee.webclient.export.util.ExportResult;


@SuppressWarnings("rawtypes")
public abstract class AbstractBaseExportCallableTask<C>
        implements ExportCallableTask<ExportResult, C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBaseExportCallableTask.class);

    private File file;

    private ExportType exporType;

    private ExportOptions exportOptions;

    protected C table;

    public AbstractBaseExportCallableTask() {
        // no-op
    }

    @Override
    public void setExportFile(final File file) {
        this.file = file;
    }

    public File getExportFile() {
        return this.file;
    }

    @Override
    public void setExportType(final ExportType exportType) {
        this.exporType = exportType;
    }

    public ExportType getExportType() {
        return this.exporType;
    }

    public abstract C getTable();

    public ExportOptions getExportOptions() {
        return this.exportOptions;
    }

    @Override
    public void setExportOptions(final ExportOptions exportOptions) {
        this.exportOptions = exportOptions;
    }

    @Override
    public Future<ExportResult> call(final TaskControl taskControl) {

        if ((this.getExportFile() != null)) {
            final Object value = this.getTable();
            if (value == null) {
                new AsyncResult<>(new ExportResult(ExportResult.RESULT_CODE_ERROR, "No data to export"));
            }

            try (final FileOutputStream stream = new FileOutputStream(this.getExportFile())) {
                this.doExport(stream, this.getTable(), this.getExportOptions());
            } catch (final Exception e) {
                LOGGER.error("doExport", e);
                throw new RuntimeException(e);
            }
            return new AsyncResult<>(new ExportResult(ExportResult.RESULT_CODE_OK));
        }

        return new AsyncResult<>(new ExportResult(ExportResult.RESULT_CODE_ERROR,
                "No export file nor table were found to perfrom export operation"));
    }

    protected abstract void doExport(final FileOutputStream fileOutputStream,
            final C table, final ExportOptions exportOptions);

}
