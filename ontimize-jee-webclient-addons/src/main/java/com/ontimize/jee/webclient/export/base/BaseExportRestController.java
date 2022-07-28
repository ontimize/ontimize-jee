package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.server.rest.FilterParameter;
import com.ontimize.jee.server.rest.ORestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("${ontimize.export.url}")
public abstract class BaseExportRestController<T> extends ORestController<T> {

    private static final Logger logger = LoggerFactory.getLogger(BaseExportRestController.class);

    public BaseExportRestController() {

    }
    
    protected void processQueryParameter(final ExportQueryParameters exportParam) {
        FilterParameter filterParam = exportParam.getQueryParam();
        
        Map<?, ?> kvQueryParameter = filterParam.getFilter();
        List<?> avQueryParameter = filterParam.getColumns();
        HashMap<?, ?> hSqlTypes = filterParam.getSqltypes();

        Map<Object, Object> processedKeysValues = this.createKeysValues(kvQueryParameter, hSqlTypes);
        List<Object> processedAttributesValues = this.createAttributesValues(avQueryParameter, hSqlTypes);
        filterParam.setKv(processedKeysValues);
        filterParam.setColumns(processedAttributesValues);
        
        exportParam.setQueryParam(filterParam);
    }
    
    protected ResponseEntity<Void> doResponse(final HttpServletResponse response, final File exportFile) {
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + exportFile.getName() + "\"");

        try(BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(exportFile));) {
            FileCopyUtils.copy(inputStream, response.getOutputStream());
            return new ResponseEntity<Void>(HttpStatus.OK);
        } catch (IOException ex) {
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }finally {
            //Ensure deleting temp file
            if(exportFile != null) {
                try {
                    Files.deleteIfExists(Paths.get(exportFile.getAbsolutePath()));
                } catch (IOException ex) {
                    // no-op
                }
            }
        }
    }

}
