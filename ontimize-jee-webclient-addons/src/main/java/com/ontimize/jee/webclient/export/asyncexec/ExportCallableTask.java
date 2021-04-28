package com.ontimize.jee.webclient.export.asyncexec;

import java.io.File;

import com.ontimize.jee.webclient.export.ExportType;
import com.ontimize.jee.webclient.export.executor.CallableTask;
import com.ontimize.jee.webclient.export.util.ExportOptions;


@SuppressWarnings("rawtypes")
public interface ExportCallableTask<T, C> extends CallableTask<T> {

    void setTable(C table);

    void setExportFile(File file);

    void setExportOptions(ExportOptions options);

    void setExportType(ExportType exportType);

}
