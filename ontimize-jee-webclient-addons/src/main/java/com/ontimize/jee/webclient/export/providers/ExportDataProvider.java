package com.ontimize.jee.webclient.export.providers;

import com.ontimize.jee.server.rest.QueryParameter;
import com.ontimize.jee.webclient.export.HeadExportColumn;

/**
 * Provider que proporciona los datos de la exportación.
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */

public interface ExportDataProvider {

    String getService();
    
    void setServiceBean(Object serviceBean);
    
    Object getServiceBean();

    String getDao();

    QueryParameter getQueryParameters();

    int getNumberOfRows();

    int getNumberOfColumns();

    int getColumnIndex(HeadExportColumn column);

    Object getCellValue(int row, int column);
    Object getCellValue(int row, String colId);

}
