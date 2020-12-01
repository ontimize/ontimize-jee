package com.ontimize.jee.webclient.excelexport.base;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ontimize.db.EntityResult;
import com.ontimize.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.webclient.export.IExportService;

/**
 * @author <a href="antonio.vazquez@imatia.com">Antonio Vázquez Araújo</a>
 */
public interface IExcelExportService extends IExportService {

	File queryParameters(EntityResult data, List<String> orderColumns, Map<Object, Object> keysValues,
			List<Object> attributesValues, int pageSize, boolean advQuery, int offSet)
			throws OntimizeJEERuntimeException, IOException;

}
