package com.ontimize.jee.webclient.export;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ontimize.dto.EntityResult;
import com.ontimize.dto.EntityResultMapImpl;
import com.ontimize.db.NullValue;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.rest.ORestController;

public abstract class OExportRestController<S, T extends IExportService> extends ORestController<S> {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(OExportRestController.class);

    public static final String EXPORT = "Export";

    @Autowired
    private T exportService;

    /**
     * Returns the export service.
     * @return
     */
    public T getExportService() {
        return this.exportService;
    }

    @GetMapping(value = "/{extension}/{id}")
    public void downloadFile(@PathVariable(name = "extension", required = true) final String fileExtension,
            @PathVariable(name = "id", required = true) final String fileId, HttpServletResponse response) {
        InputStream fis = null;
        try {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            File[] matchingfiles = tmpDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(fileId) && name.endsWith(fileExtension);
                }

            });

            if (matchingfiles.length == 1 && matchingfiles[0].exists()) {
                File file = matchingfiles[0];
                response.setHeader("Content-Type", "application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
                response.setContentLengthLong(file.length());
                fis = new BufferedInputStream(new FileInputStream(file));
                FileCopyUtils.copy(fis, response.getOutputStream());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IOException e) {
            logger.error("{}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error("{}", e.getMessage(), e);
                }
            }
        }
    }

    @PostMapping(value = "/{name}/{extension}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> export(@PathVariable("name") String name,
            @PathVariable("extension") String extension, @RequestBody ExportParameter exportParam) throws Exception {
        OExportRestController.logger.debug("Invoked /{}/{}", name, extension);
        CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(name).append(ORestController.QUERY);

            Map<?, ?> kvQueryParameter = exportParam.getFilter();
            List<?> avQueryParameter = exportParam.getColumns();
            Map<?, ?> columnNames = exportParam.getColumnNames();
            Map<String, Integer> sqlTypes = new HashMap<>();
            sqlTypes.putAll(exportParam.getSqlTypes());

            Map<Object, Object> keysValues = this.createKeysValues(kvQueryParameter, sqlTypes);
            List<Object> attributesValues = this.createAttributesValues(avQueryParameter, sqlTypes);

            EntityResult er = (EntityResultMapImpl) ReflectionTools.invoke(this.getService(), buffer.toString(), keysValues,
                    attributesValues);
            er = this.replaceEntityResultColumnNames(er, sqlTypes, avQueryParameter, columnNames);

            StringBuilder exportMethod = new StringBuilder();
            exportMethod.append(extension).append(OExportRestController.EXPORT);

            File xslxFile = (File) ReflectionTools.invoke(this.exportService, exportMethod.toString(), er,
                    new ArrayList<>(exportParam.getColumnNames().values()));
            Map<String, Object> erResult = new HashMap<>();
            String filename = xslxFile.getName();
            erResult.put(extension + "Id", filename.substring(0, filename.indexOf('.')));
            EntityResult result = new EntityResultMapImpl(EntityResult.OPERATION_SUCCESSFUL, EntityResult.NODATA_RESULT);
            result.addRecord(erResult);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return this.processError(e);
        }
    }

    protected EntityResult replaceEntityResultColumnNames(EntityResult data, Map<?, ?> sqlTypes, List<?> columns,
            Map<?, ?> columnNames) {
        List<?> columnNamesArray = new ArrayList<>(columnNames.values());

        // Create EntityResult with column names
        EntityResult er = new EntityResultMapImpl(columnNamesArray);
        er.setColumnOrder(columnNamesArray);
        er.setColumnSQLTypes((Map) sqlTypes);

        // Add data to EntityResult
        int index = 0;
        for (int i = 0; i < data.calculateRecordNumber(); i++) {
            Map rowData = data.getRecordValues(i);
            Map<Object, Object> record = new HashMap<>();
            for (Object key : columns) {
                if (rowData.get(key) != null) {
                    record.put(columnNames.get(key), rowData.get(key));
                } else {
                    record.put(columnNames.get(key), new NullValue());
                }
            }
            er.addRecord(record, index++);
        }

        return er;
    }

}
