package com.ontimize.jee.webclient.export.support.exporter;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.util.ExportOptions;

/**
 * Exportador que mantiene todo el libro en memoria. Sirve para conjuntos de datos de pequeño tamaño
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */
public class DefaultXSSFExcelExporter extends BaseExcelExporter<XSSFWorkbook> {

    @Override
    protected XSSFWorkbook buildWorkBook() {
        return new XSSFWorkbook();
    }

	@Override
	public XSSFWorkbook export(ExportColumnProvider columnProvider, ExportDataProvider dataProvider,
			ExportStyleProvider styleProvider, ExportOptions exportOptions, boolean lanscape) throws ExportException {
		// TODO Auto-generated method stub
		return null;
	}

}
