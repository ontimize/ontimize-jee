package com.ontimize.jee.webclient.excelexport;

import org.apache.poi.ss.usermodel.Workbook;

import com.ontimize.jee.webclient.excelexport.asyncexec.AbstractBaseExportCallableTask;
import com.ontimize.jee.webclient.excelexport.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.excelexport.providers.ExportDataProvider;
import com.ontimize.jee.webclient.excelexport.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.excelexport.providers.SheetNameProvider;

/**
 * @param <T> Tipo de tabla: ITXTableView ó ITXTreeTableView
 * @param <E> Tipo de WorkBook: XSSFWorkbook ó SXSSFWorkbook
 */
public abstract class ExcelBareBeanExportTask<T, E extends Workbook>
        extends AbstractBaseExportCallableTask<T> {

    protected abstract ExportColumnProvider createExportContext(final T table);

    protected abstract ExportDataProvider createExportDataProvider(final T table);

    protected abstract ExportStyleProvider createExportCellStyleProvider();

    protected abstract Exporter<E> createExporter();

    protected abstract SheetNameProvider createSheetNameProvider();

}
