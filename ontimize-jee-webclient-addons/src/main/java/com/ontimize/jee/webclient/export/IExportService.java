package com.ontimize.jee.webclient.export;

import java.io.File;
import java.util.List;

import com.ontimize.dto.EntityResult;

public interface IExportService {

    public File xlsxExport(EntityResult data, List<String> columns) throws Exception;

    public File htmlExport(EntityResult data, List<String> columns) throws Exception;

    public File pdfExport(EntityResult data, List<String> columns) throws Exception;

}
