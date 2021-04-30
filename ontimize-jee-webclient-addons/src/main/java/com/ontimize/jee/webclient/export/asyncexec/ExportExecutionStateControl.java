package com.ontimize.jee.webclient.export.asyncexec;

import java.io.File;

import com.ontimize.jee.webclient.export.executor.ExecutionStateControl;
import com.ontimize.jee.webclient.export.util.ExportOptions;


public interface ExportExecutionStateControl<T> extends ExecutionStateControl<T> {

    void setExportFile(File file);

    void setExportOptions(ExportOptions options);

}
