package com.ontimize.jee.webclient.export;

import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;
import com.ontimize.jee.webclient.export.util.ExportOptions;

/**
 * Exportador que realiza la exportación a partir de los elementos que recibe
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */

public interface ExcelExporter<T> extends Exporter<T> {

    T export(
            final ExportColumnProvider columnProvider,
            final ExportDataProvider dataProvider,
            final ExportStyleProvider styleProvider,
            final SheetNameProvider sheetNameProvider,
            final ExportOptions exportOptions) throws ExportException;

}
