package com.ontimize.jee.webclient.export.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.rest.ORestController;

public abstract class ExcelExportRestController extends ORestController<IExcelExportService>

        implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportRestController.class);

    @Autowired
    ExcelExportService exportService;

    @Override
    public ExcelExportService getService() {
        return this.exportService;
    }

    public ExcelExportRestController() {

    }

    /**
     * Receive the query parameters, creates the temporal file and call the service
     * @param exportParam The query parameters
     * @param fileExtension The extension of the file to export
     * @param response The HTTP response
     * @throws Exception
     */
    @PostMapping(value = { "/{extension}" }, consumes = { "application/json" }, produces = { "application/json" })
    public void exportQuery(@PathVariable(name = "extension", required = true) final String fileExtension,
            @RequestBody ExcelExportQueryParameters exportParam, HttpServletResponse response) throws Exception {
        logger.debug("Invoked /{}", exportParam);

        BufferedInputStream inputStream = null;
        try {

            EntityResult entityResult = new EntityResultMapImpl();
            entityResult.put("cellStyles", exportParam.getCellStyles());
            entityResult.put("excelColumns", exportParam.getExcelColumns());
            entityResult.put("columnStyles", exportParam.getColumnStyles());
            entityResult.put("columnTypes", exportParam.getColumnTypes());
            entityResult.put("dao", exportParam.getDao());
            entityResult.put("path", exportParam.getPath());
            entityResult.put("rowStyles", exportParam.getRowStyles());
            entityResult.put("service", exportParam.getService());
            entityResult.put("columnTitles", exportParam.getColumnTitles());
            entityResult.put("styles", exportParam.getStyles());

            Map<?, ?> kvQueryParameter = exportParam.getQueryParam().getFilter();
            List<?> avQueryParameter = exportParam.getQueryParam().getColumns();
            HashMap<?, ?> hSqlTypes = exportParam.getQueryParam().getSqltypes();
            
            int pageSize = exportParam.getQueryParam().getPageSize();
            int offset = exportParam.getQueryParam().getOffset();

            File exportFile = getService().queryParameters(entityResult, new ArrayList(),
                    this.createKeysValues(kvQueryParameter, hSqlTypes),
                    this.createAttributesValues(avQueryParameter, hSqlTypes), pageSize,
                    exportParam.isAdvQuery(), offset);

            response.setHeader("Content-Type", "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + exportFile.getName() + "\"");

            inputStream = new BufferedInputStream(new FileInputStream(exportFile));
            FileCopyUtils.copy(inputStream, response.getOutputStream());

        } catch (Exception e) {
            response.setStatus(500);
        }
    }

}
