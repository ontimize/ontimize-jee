package com.ontimize.jee.webclient.excelexport.asyncexec;

import java.io.File;

import com.ontimize.jee.webclient.excelexport.ExportType;
import com.ontimize.jee.webclient.excelexport.executor.CallableTask;
import com.ontimize.jee.webclient.excelexport.util.ExportOptions;



@SuppressWarnings("rawtypes")
public interface ExportCallableTask<T, C> extends CallableTask<T> {

    void setTable(C table);

    void setExportFile(File file);

    void setExportOptions(ExportOptions options);

    void setExportType(ExportType exportType);

}
