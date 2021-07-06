package com.ontimize.jee.webclient.export.asyncexec.support;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.webclient.export.asyncexec.ExportExecutionStateControl;
import com.ontimize.jee.webclient.export.util.ExportOptions;
import com.ontimize.jee.webclient.export.util.ExportResult;

import javafx.concurrent.WorkerStateEvent;

public class DefaultExportExecutionStateControl implements ExportExecutionStateControl<ExportResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExportExecutionStateControl.class);

    private File file;

    private ExportOptions options;

    public DefaultExportExecutionStateControl() {
        // no-op
    }

    public DefaultExportExecutionStateControl(final ExportOptions exportOptions) {
        this.options = exportOptions;
    }

    public DefaultExportExecutionStateControl(final File file, final ExportOptions exportOptions) {
        this.file = file;
        this.options = exportOptions;
    }

    @Override
    public void setExportFile(final File file) {
        this.file = file;
    }

    public File getExportFile() {
        return this.file;
    }

    public ExportOptions getExportOptions() {
        return this.options;
    }

    @Override
    public void setExportOptions(final ExportOptions options) {
        this.options = options;
    }

    @Override
    public void onSucceeded(final WorkerStateEvent workerStateEvent, final ExportResult value) {

        if ((value.getResultCode() == ExportResult.RESULT_CODE_OK) && this.options.isAutomaticFileOpen()) {
            this.openExportedFile();
        }
    }

    protected void openExportedFile() {
        try {
            final String exportAbsFilePath = this.getExportFile().getAbsolutePath();
            Runtime.getRuntime()
                .exec(
                        new String[] { "cmd", "/c", "start", "\"DummyTitle\"", exportAbsFilePath });
        } catch (final IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

}
