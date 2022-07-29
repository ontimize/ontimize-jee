package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.webclient.export.exception.ExportException;

import java.io.File;

/**
 */
public interface ExportService {

    enum ExportExtensionTypes {
        xlsx, pdf, csv
    }
    
    File export(ExportQueryParameters exportParam) throws ExportException;

}
