package com.ontimize.jee.webclient.excelexport.providers;

import com.ontimize.jee.server.rest.QueryParameter;
import com.ontimize.jee.webclient.excelexport.HeadExportColumn;

/**
 * Provider que proporciona los datos de la exportacion.
 *
 * @author enrique.alvarez@imatia.com Enrique AÅlvarez Pereira
 */

public interface ExcelExportDataProvider {
	
    int getNumberOfRows();

    int getNumberOfColumns();

    int getColumnIndex(HeadExportColumn column);
    
    Object getCellValue(int row, int column);
	
	String getService();
	
	String getDao();
		
	QueryParameter getQueryParameters();
	
}
