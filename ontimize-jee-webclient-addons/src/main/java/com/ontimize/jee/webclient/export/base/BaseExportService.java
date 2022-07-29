package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.common.dto.EntityResult;
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

        ExportDataProvider<? extends EntityResult> dataProvider0 = null;
        if(exportParam.isAdvQuery()) {
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
}
