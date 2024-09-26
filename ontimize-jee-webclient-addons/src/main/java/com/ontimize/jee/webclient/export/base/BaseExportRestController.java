package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.server.rest.FilterParameter;
import com.ontimize.jee.server.rest.ORestController;
import com.ontimize.jee.webclient.export.exception.ExportException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("${ontimize.export.url}")
public abstract class BaseExportRestController<T> extends ORestController<T> {

    private static final Logger logger = LoggerFactory.getLogger(BaseExportRestController.class);

    protected BaseExportRestController() {
        // no-op
    }

    protected void verifyExportParameter(final ExportQueryParameters exportParam) throws ExportException {
        if (exportParam == null) {
            throw new ExportException("Export parameter not found!");
        }

        if (exportParam.getQueryParam() == null) {
            throw new ExportException("'queryParam' attribute not found!");
        }

        if (StringUtils.isEmpty(exportParam.getDao())) {
            throw new ExportException("'dao' attribute not found!");
        }

        if (StringUtils.isEmpty(exportParam.getService()) && StringUtils.isEmpty(exportParam.getPath())) {
            throw new ExportException("No 'service' nor 'path' attribute were found!");
        }

        if (exportParam instanceof AdvancedExportQueryParameters) {
            if (((AdvancedExportQueryParameters) exportParam).getColumns() == null) {
                throw new ExportException("'columns' attribute not found!");
            }
        }
    }

    protected void processQueryParameter(final ExportQueryParameters exportParam) throws ExportException {
        FilterParameter filterParam = exportParam.getQueryParam();
        if (filterParam == null) {
            throw new ExportException("'queryParam' attribute not found!");
        }

        Map<?, ?> kvQueryParameter =  filterParam.getFilter();
        List<?> avQueryParameter =  filterParam.getColumns();
        HashMap<?, ?> hSqlTypes =  filterParam.getSqltypes();

        Map<Object, Object> processedKeysValues = this.createKeysValues(kvQueryParameter, hSqlTypes);
        List<Object> processedAttributesValues = this.createAttributesValues(avQueryParameter, hSqlTypes);
        filterParam.setKv(processedKeysValues);
        filterParam.setColumns(processedAttributesValues);

        exportParam.setQueryParam(filterParam);
    }

    protected ResponseEntity<Void> doResponse(final HttpServletResponse response, final File exportFile) {

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(exportFile));) {
            response.setHeader("Content-Type", "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + exportFile.getName() + "\"");

            FileCopyUtils.copy(inputStream, response.getOutputStream());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("Impossible to add export file to HTTP response", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            //Ensure deleting temp file
            if (exportFile != null) {
                try {
                    Files.deleteIfExists(Paths.get(exportFile.getAbsolutePath()));
                    if (exportFile.getParentFile() != null && exportFile.getParentFile().getName().contains("ontimize.export.")) {
                        Files.deleteIfExists(Paths.get(exportFile.getParentFile().getAbsolutePath()));
                    }
                } catch (IOException ex) {
                    // no-op
                }
            }
        }
    }

}
