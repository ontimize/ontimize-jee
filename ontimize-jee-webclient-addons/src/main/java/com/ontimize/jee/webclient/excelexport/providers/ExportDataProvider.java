package com.ontimize.jee.webclient.excelexport.providers;

import com.ontimize.jee.webclient.excelexport.HeadExportColumn;

/**
 * Provider que proporciona los datos de la exportación.
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */

public interface ExportDataProvider {

    int getNumberOfRows();

    int getNumberOfColumns();

    int getColumnIndex(HeadExportColumn column);

    Object getCellValue(int row, int column);

}
