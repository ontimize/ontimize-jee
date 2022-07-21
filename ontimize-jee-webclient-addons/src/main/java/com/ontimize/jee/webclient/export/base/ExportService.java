package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

import java.io.File;
import java.io.IOException;

/**
 */
public interface ExportService {

    enum ExportExtensionTypes {
        xlsx, pdf
    }
    
    File export(ExportQueryParameters exportParam) throws OntimizeJEERuntimeException, IOException;

}
