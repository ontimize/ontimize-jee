package com.ontimize.jee.webclient.export.providers;

import com.ontimize.jee.server.rest.QueryParameter;
import com.ontimize.jee.webclient.export.HeadExportColumn;

/**
 * Provider que proporciona los datos de la exportacion.
 *
 * @author enrique.alvarez@imatia.com Enrique A�lvarez Pereira
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
