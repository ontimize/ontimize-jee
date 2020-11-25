package com.ontimize.jee.webclient.excelexport.asyncexec;

import java.io.File;

import com.ontimize.jee.webclient.excelexport.executor.ExecutionStateControl;
import com.ontimize.jee.webclient.excelexport.util.ExportOptions;


public interface ExportExecutionStateControl<T> extends ExecutionStateControl<T> {

    void setExportFile(File file);

    void setExportOptions(ExportOptions options);

}
