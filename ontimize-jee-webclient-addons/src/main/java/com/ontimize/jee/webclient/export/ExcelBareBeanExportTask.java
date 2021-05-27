package com.ontimize.jee.webclient.export;

import org.apache.poi.ss.usermodel.Workbook;

import com.ontimize.jee.webclient.export.asyncexec.AbstractBaseExportCallableTask;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;

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
