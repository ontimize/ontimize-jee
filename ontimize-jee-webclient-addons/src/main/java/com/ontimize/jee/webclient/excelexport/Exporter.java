package com.ontimize.jee.webclient.excelexport;

import com.ontimize.jee.webclient.excelexport.providers.ExcelExportDataProvider;
import com.ontimize.jee.webclient.excelexport.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.excelexport.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.excelexport.providers.SheetNameProvider;
import com.ontimize.jee.webclient.excelexport.util.ExportOptions;

/**
 * Exportador que realiza la exportación a partir de los elementos que recibe
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */

public interface Exporter<T> {

    T export(
        final ExportColumnProvider columnProvider,
        final ExcelExportDataProvider dataProvider,
        final ExportStyleProvider styleProvider,
        final SheetNameProvider sheetNameProvider,
        final ExportOptions exportOptions);

}
