package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.webclient.export.exception.ExportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

public class ExportRestController extends BaseExportRestController<ExportService> implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ExportRestController.class);
	
	protected ApplicationContext applicationContext;

    private ExportService exportService;
	
    @Override
    public ExportService getService() {
        return this.exportService;
    }

    public ExportRestController() {
        // no-op
    }
    
    /**
     * Receive the query parameters, creates the temporal file and call the service
     * @param exportParam The query parameters
     * @param fileExtension The extension of the file to export
     * @param response The HTTP response
     * @throws Exception
     */
    @PostMapping(value = {"/{extension}"}, consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<Void> exportQuery(@PathVariable(name = "extension", required = true) final String fileExtension,
                                      @RequestBody ExportQueryParameters exportParam, HttpServletResponse response) {
        try {
            // retrieve specific export service for file extension
            this.exportService = configureExportService(fileExtension);
            // process query parameters (basic expressions, filter expression, etc.)
            processQueryParameter(exportParam);
            // do export
            File exportFile = getService().export(exportParam);
            // add export file to http response
            return doResponse(response, exportFile);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    protected ExportService configureExportService(final String fileExtension) throws ExportException {
        ExportService.ExportExtensionTypes ext = ExportService.ExportExtensionTypes.valueOf(fileExtension);
        ExportService service;
        switch (ext){
            case xlsx:
                service = loadServiceBean("ExcelExportService");
                break;
            default:
                service = null;
        }
        if(service == null){
            throw new ExportException(String.format("Export service for extension %s not found", fileExtension));
        }
        return service;
    }
    
    protected ExportService loadServiceBean(final String beanName) {
        ExportService bean = null;
        Object serviceBean = applicationContext.getBean(beanName);
        if (serviceBean instanceof ExportService) {
            bean = (ExportService) serviceBean;
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
    }
}
