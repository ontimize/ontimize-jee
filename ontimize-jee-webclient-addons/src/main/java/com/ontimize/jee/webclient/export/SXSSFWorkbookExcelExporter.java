package com.ontimize.jee.webclient.export;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Exportador que utiliza libros de tipo SXSFF, que usan streams en lugar de mantener todo el libro
 * en memoria. Es el que se usa por defecto
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */

public class SXSSFWorkbookExcelExporter extends BaseExcelExporter<SXSSFWorkbook> {

    @Override
    protected SXSSFWorkbook buildWorkBook() {
        return new SXSSFWorkbook();
    }

}
