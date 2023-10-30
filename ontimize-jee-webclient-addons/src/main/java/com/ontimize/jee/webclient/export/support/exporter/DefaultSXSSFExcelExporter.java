package com.ontimize.jee.webclient.export.support.exporter;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.util.ExportOptions;

/**
 * Exportador que utiliza libros de tipo SXSFF, que usan streams en lugar de mantener todo el libro
 * en memoria. Es el que se usa por defecto
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */
public class DefaultSXSSFExcelExporter extends BaseExcelExporter<SXSSFWorkbook> {

    @Override
    protected SXSSFWorkbook buildWorkBook() {
        return new SXSSFWorkbook();
    }

	@Override
	public SXSSFWorkbook export(ExportColumnProvider columnProvider, ExportDataProvider dataProvider,
			ExportStyleProvider styleProvider, ExportOptions exportOptions, boolean lanscape) throws ExportException {
		// TODO Auto-generated method stub
		return null;
	}

}
