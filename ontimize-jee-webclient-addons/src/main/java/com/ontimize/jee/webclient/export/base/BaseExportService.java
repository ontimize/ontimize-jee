package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.support.dataprovider.DefaultAdvancedEntityResultExportDataProvider;
import com.ontimize.jee.webclient.export.support.dataprovider.DefaultEntityResultExportDataProvider;
import com.ontimize.jee.webclient.export.util.ApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.File;

public abstract class BaseExportService implements ExportService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportService.class);

    private ExportDataProvider dataProvider;
    
    private ApplicationContext appContext;

    private ApplicationContextUtils applicationContextUtils;
    
    
    @Override
    public File export(final ExportQueryParameters exportParam) throws ExportException {
        // Create providers...
        createProviders(exportParam);
        
        // Generate export file
        File file = generateFile(exportParam);
        return file;
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
        if(this.applicationContextUtils == null){
            this.applicationContextUtils = new ApplicationContextUtils();
            ((ApplicationContextAware) this.applicationContextUtils).setApplicationContext(this.appContext);
        }
        return applicationContextUtils;
    }
    
    protected void createProviders(final ExportQueryParameters exportParam) throws ExportException {
        this.dataProvider = createDataProvider(exportParam);
    }
        
    
    protected ExportDataProvider createDataProvider(final ExportQueryParameters exportParam) throws ExportException {

        ExportDataProvider dataProvider = null;
        if(exportParam.isAdvQuery()) {
            dataProvider = new DefaultAdvancedEntityResultExportDataProvider(exportParam);
        } else {
            dataProvider = new DefaultEntityResultExportDataProvider(exportParam);
        }
        
        if(dataProvider != null) {
            Object serviceBean = this.getApplicationContextUtils().getServiceBean(exportParam.getService(), exportParam.getPath());
            dataProvider.setServiceBean(serviceBean);
        }
        
        return dataProvider;
    }
}
