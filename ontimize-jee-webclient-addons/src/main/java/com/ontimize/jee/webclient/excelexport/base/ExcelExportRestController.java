package com.ontimize.jee.webclient.excelexport.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ontimize.db.EntityResult;
import com.ontimize.db.NullValue;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.rest.ORestController;
import com.ontimize.jee.webclient.export.ExportParameter;
import com.ontimize.jee.webclient.export.OExportRestController;

//public abstract class ExcelExportRestController<S, T extends IExcelExportService> extends ORestController<S>
public abstract class ExcelExportRestController extends ORestController<IExcelExportService>

		implements ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(ExcelExportRestController.class);

	@Autowired
	ExcelExportService exportService;

	@Override
	public ExcelExportService getService() {
		return this.exportService;
	}

	@Autowired
	private ApplicationContext context;

	public ExcelExportRestController() {

	}

	/**
	 * Receive the query parameters, creates the temporal file and call the service
	 * 
	 * @param exportParam   The query parameters
	 * @param fileExtension The extension of the file to export
	 * @param response      The HTTP response
	 * @throws Exception
	 */
	@PostMapping(value = { "/{extension}" }, consumes = { "application/json" }, produces = { "application/json" })
	public void exportQuery(@PathVariable(name = "extension", required = true) final String fileExtension,
			@RequestBody ExportQueryParameters exportParam, HttpServletResponse response) throws Exception {
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

			File exportFile = getService().queryParameters(entityResult, new ArrayList(),
					this.createKeysValues(kvQueryParameter, hSqlTypes),
					this.createAttributesValues(avQueryParameter, hSqlTypes), exportParam.getPageSize(),
					exportParam.isAdvQuery(), exportParam.getOffset());

			response.setHeader("Content-Type", "application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + exportFile.getName() + "\"");

			inputStream = new BufferedInputStream(new FileInputStream(exportFile));
			FileCopyUtils.copy(inputStream, response.getOutputStream());

		} catch (Exception e) {
			response.setStatus(500);
		}
	}

	@Deprecated
	@PostMapping(value = "/{name}/{extension}/exp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Hashtable<String, Object>> export(@PathVariable("name") String name,
			@PathVariable("extension") String extension, @RequestBody ExportParameter exportParam,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
		StringBuilder buffer = new StringBuilder();
		buffer.append(name).append(ORestController.QUERY);

		Map<?, ?> kvQueryParameter = exportParam.getFilter();
		List<?> avQueryParameter = exportParam.getColumns();
		Map<?, ?> columnNames = exportParam.getColumnNames();
		Hashtable<String, Integer> sqlTypes = new Hashtable<>();
		sqlTypes.putAll(exportParam.getSqlTypes());

		Map<Object, Object> keysValues = this.createKeysValues(kvQueryParameter, sqlTypes);
		List<Object> attributesValues = this.createAttributesValues(avQueryParameter, sqlTypes);

		// TODO reparar servicio

		EntityResult erQuery = (EntityResult) ReflectionTools.invoke(context.getBean("CustomerService"),
				buffer.toString(), keysValues, attributesValues);
		erQuery = this.replaceEntityResultColumnNames(erQuery, sqlTypes, avQueryParameter, columnNames);

		StringBuilder exportMethod = new StringBuilder();
		exportMethod.append(extension).append(OExportRestController.EXPORT);

		File exportFile = (File) ReflectionTools.invoke(this.exportService, exportMethod.toString(), erQuery,
				new ArrayList<>(exportParam.getColumnNames().values()));

		String filename = exportFile.getName();
		Hashtable<String, Object> fileId = new Hashtable<String, Object>();
		fileId.put(extension + "Id", filename.substring(0, filename.indexOf('.')));

		return new ResponseEntity<>(fileId, HttpStatus.OK);
	}

	@Deprecated
	protected EntityResult replaceEntityResultColumnNames(EntityResult data, Map<?, ?> sqlTypes, List<?> columns,
			Map<?, ?> columnNames) {
		List<?> columnNamesArray = new ArrayList<>(columnNames.values());

		// Create EntityResult with column names
		EntityResult er = new EntityResult(columnNamesArray);
		er.setColumnOrder(columnNamesArray);
		er.setColumnSQLTypes((Hashtable) sqlTypes);

		// Add data to EntityResult
		int index = 0;
		for (int i = 0; i < data.calculateRecordNumber(); i++) {
			Hashtable rowData = data.getRecordValues(i);
			Hashtable<Object, Object> record = new Hashtable<>();
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
