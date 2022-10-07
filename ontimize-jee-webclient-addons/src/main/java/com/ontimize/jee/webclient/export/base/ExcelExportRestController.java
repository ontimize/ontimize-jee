package com.ontimize.jee.webclient.export.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RequestMapping("${ontimize.export.url}/excel")
public class ExcelExportRestController extends BaseExportRestController<IExcelExportService> {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportRestController.class);

    @Autowired
    IExcelExportService exportService;

    @Override
    public IExcelExportService getService() {
        return this.exportService;
    }

    public ExcelExportRestController() {

    }

    /**
     * Receive the query parameters, creates the temporal file and call the service
     *
     * @param exportParam The query parameters
     * @param response    The HTTP response
     * @throws Exception
     */
    @PostMapping(value = {"/xlsx"}, consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<Void> exportQuery(@RequestBody AdvancedExportQueryParameters exportParam,
                                            HttpServletResponse response) {
        logger.debug("Invoked /{}", exportParam);
        try {
            // verify required attributes
            verifyExportParameter(exportParam);
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

}
