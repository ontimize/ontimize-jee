package com.ontimize.jee.webclient.excelexport.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.server.rest.ORestController;

public abstract class ExcelExportRestController<S, T extends IExcelExportService> extends ORestController<S>
		implements ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(ExcelExportRestController.class);

	@Autowired
	S excelExportService;

	@Override
	public S getService() {
		return this.excelExportService;
	}

	@Autowired
	private T exportService;

	public ExcelExportRestController() {

	}

	/**
	 * Receive the query parameters, creates the temporal file and call the service
	 * @param exportParam The query parameters
	 * @param fileExtension The extension of the file to export
	 * @param response The HTTP response
	 * @throws Exception
	 */
	@PostMapping(value = { "/{extension}/export" }, consumes = { "application/json" }, produces = { "application/json" })
	public void exportQuery(@PathVariable(name = "extension", required = true) final String fileExtension,
			@RequestBody ExportQueryParameters exportParam, HttpServletResponse response)
			throws Exception {
		logger.debug("Invoked /{}", exportParam);
		
		BufferedInputStream inputStream = null;
		try {
			
			EntityResult entityResult = new EntityResult();
			entityResult.put("cellStyles", exportParam.getCellStyles());
			entityResult.put("excelColumns", exportParam.getExcelColumns());
			entityResult.put("columnStyles", exportParam.getColumnStyles());
			entityResult.put("columnTypes", exportParam.getColumnTypes());
			entityResult.put("dao", exportParam.getDao());
			entityResult.put("rowStyles", exportParam.getRowStyles());
			entityResult.put("service", exportParam.getService());
			entityResult.put("columnTitles", exportParam.getColumnTitles());
			entityResult.put("styles", exportParam.getStyles());

			Map<?, ?> kvQueryParameter = exportParam.getQueryParam().getFilter();
			List<?> avQueryParameter = exportParam.getQueryParam().getColumns();
			HashMap<?, ?> hSqlTypes = exportParam.getQueryParam().getSqltypes();

			File xlsxFile = exportService.queryParameters(entityResult, new ArrayList(),
					this.createKeysValues(kvQueryParameter, hSqlTypes),
					this.createAttributesValues(avQueryParameter, hSqlTypes),
					exportParam.getPageSize(), exportParam.isAdvQuery(), exportParam.getOffset());

			response.setHeader("Content-Type", "application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + xlsxFile.getName() + "\"");

			inputStream = new BufferedInputStream(new FileInputStream(xlsxFile));
			FileCopyUtils.copy(inputStream, response.getOutputStream());

		} catch (Exception e) {
			response.setStatus(500);
		}
	}
}
