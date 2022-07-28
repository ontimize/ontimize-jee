package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.webclient.export.base.ExportQueryParameters;
import com.ontimize.jee.webclient.export.exception.ExportException;

import java.io.File;

/**
 */
public interface ExportService {

    enum ExportExtensionTypes {
        xlsx, pdf
    }
    
    File export(ExportQueryParameters exportParam) throws ExportException;

}
