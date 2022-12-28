package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.support.DefaultHeadExportColumn;
import com.ontimize.jee.webclient.export.support.dataprovider.DefaultAdvancedEntityResultExportDataProvider;
import com.ontimize.jee.webclient.export.support.dataprovider.DefaultEntityResultExportDataProvider;
import com.ontimize.jee.webclient.export.util.ApplicationContextUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseExportService implements ExportService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(BaseExportService.class);

    private ExportDataProvider<? extends EntityResult> dataProvider;

    private ApplicationContext appContext;

    private ApplicationContextUtils applicationContextUtils;


    @Override
    public File export(final ExportQueryParameters exportParam) throws ExportException {
        // Create providers...
        createProviders(exportParam);

        // Generate export file
        return generateFile(exportParam);
    }

    public abstract File generateFile(final ExportQueryParameters exportParam) throws ExportException;

    public ExportDataProvider getDataProvider() {
        return this.dataProvider;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    public ApplicationContext getContext() {
        return appContext;
    }

    public ApplicationContextUtils getApplicationContextUtils() {
        if (this.applicationContextUtils == null) {
            this.applicationContextUtils = new ApplicationContextUtils();
            ((ApplicationContextAware) this.applicationContextUtils).setApplicationContext(this.appContext);
        }
        return applicationContextUtils;
    }

    protected void createProviders(final ExportQueryParameters exportParam) throws ExportException {
        this.dataProvider = createDataProvider(exportParam);
    }


    protected ExportDataProvider createDataProvider(final ExportQueryParameters exportParam) throws ExportException {

        ExportDataProvider<? extends EntityResult> dataProvider0 = null;
        if (exportParam.isAdvQuery()) {
            logger.debug("Configuring default AdvancedEntityResult data provider...");
            dataProvider0 = new DefaultAdvancedEntityResultExportDataProvider(exportParam);
        } else {
            logger.debug("Configuring default EntityResult data provider...");
            dataProvider0 = new DefaultEntityResultExportDataProvider(exportParam);
        }

        Object serviceBean = this.getApplicationContextUtils().getServiceBean(exportParam.getService(), exportParam.getPath());
        dataProvider0.setServiceBean(serviceBean);

        return dataProvider0;
    }

    protected File createTempFile(final String fileExtension) throws IOException {

        File tmpExportFile = null;
        String tmpDirsLocation = System.getProperty("java.io.tmpdir");
        if (SystemUtils.IS_OS_UNIX) {
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
            Path tempDirectory = Files.createTempDirectory(Paths.get(tmpDirsLocation), "ontimize.export.", attr);
            tmpExportFile = Files.createTempFile(tempDirectory, String.valueOf(System.currentTimeMillis()), fileExtension, attr).toFile();
        } else {
            File tempDirectory = new File(tmpDirsLocation, "ontimize.export." + String.valueOf(System.currentTimeMillis()));
            if (!tempDirectory.exists()) {
                tempDirectory.mkdir();
            }
            tmpExportFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), fileExtension, tempDirectory);
            if (!tmpExportFile.setReadable(true, true)) {
                throw new IOException("File is not readable");
            }
            if (!tmpExportFile.setWritable(true, true)) {
                throw new IOException("File is not writable");
            }
        }
        return tmpExportFile;
    }

    protected void addParentColumn(List<ExportColumn> bodyColumns, List<HeadExportColumn> columns, String id, Object value,
                                   Map<String, String> columnTitles) {

        String title = columnTitles != null && columnTitles.containsKey(id) ? columnTitles.get(id) : id;

        HeadExportColumn column = new DefaultHeadExportColumn(id, title);
        Map<String, Object> children = (Map<String, Object>) value;
        // Si la columna no tiene hijos, la agregamos directamente
        if (value == null || ((Map<String, Object>) value).size() == 0) {
            bodyColumns.add(new ExportColumn(id, title, 0, null));
        } else {
            // Si la columna tiene hijos, le agregamos todos sus hijos
            column.getColumns().addAll(addChildrenColumns(bodyColumns, new ArrayList(), children, columnTitles));
        }
        columns.add(column);
    }

    protected List<HeadExportColumn> addChildrenColumns(List<ExportColumn> bodyColumns, List<HeadExportColumn> columns,
                                                        Map<String, Object> children, Map<String, String> columnTitles) {
        children.entrySet().forEach(entry -> {
            addParentColumn(bodyColumns, columns, entry.getKey(), entry.getValue(), columnTitles);
        });
        return columns;
    }
}
