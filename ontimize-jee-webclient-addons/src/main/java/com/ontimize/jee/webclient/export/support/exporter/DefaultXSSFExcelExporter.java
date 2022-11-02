package com.ontimize.jee.webclient.export.support.exporter;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

}
