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
import org.springframework.context.annotation.Configuration;
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
	 * Receives the reference to the temporary file and creates it
	 * @param fileExtension The file extension
	 * @param fileId Id of the tempfile
	 * @param response authentication response
	 */

	@GetMapping({ "/{extension}/{id}" })
	public void downloadFile(@PathVariable(name = "extension", required = true) final String fileExtension,
			@PathVariable(name = "id", required = true) final String fileId, HttpServletResponse response) {

		BufferedInputStream inputStream = null;

		try {
			File tmpDir = new File(System.getProperty("java.io.tmpdir"));
			File[] matchingfiles = tmpDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith(fileId) && name.endsWith(fileExtension);
				}
			});
			if (matchingfiles.length == 1 && matchingfiles[0].exists()) {
				File file = matchingfiles[0];
				response.setHeader("Content-Type", "application/octet-stream");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
//                response.setContentLengthLong(file.length());
				inputStream = new BufferedInputStream(new FileInputStream(file));
				FileCopyUtils.copy(inputStream, response.getOutputStream());
			} else {
				response.setStatus(404);
			}
		} catch (IOException e) {
			logger.error("{}", e.getMessage(), e);
			response.setStatus(500);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("{}", e.getMessage(), e);
				}
			}

		}

	}

	
	/**
	 * Receive the query parameters, create the file and call the service
	 * @param exportParam The query parameters
	 * @return Returns the EntityResult of the file
	 * @throws Exception
	 */
	@PostMapping(value = { "/export" }, consumes = { "application/json" }, produces = { "application/json" })
	public ResponseEntity<EntityResult> exportQuery(@RequestBody ExportQueryParameters exportParam)

			throws Exception {
		logger.debug("Invoked /{}", exportParam);
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

			ExcelExportService exportService = new ExcelExportService();
			File xslxFile = exportService.queryParameters(entityResult, new ArrayList(),
					this.createKeysValues(kvQueryParameter, hSqlTypes),
					this.createAttributesValues(avQueryParameter, hSqlTypes));

			Hashtable<String, Object> erResult = new Hashtable();

			String filename = xslxFile.getName();

			erResult.put("xlsx" + "Id", filename.substring(0, filename.indexOf('.')));
			EntityResult result = new EntityResult(0, 1);
			result.addRecord(erResult);

			return new ResponseEntity(result, HttpStatus.OK);
		} catch (Exception e) {
			return this.processError(e);
		}

	}
}
