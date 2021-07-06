package com.ontimize.jee.webclient.export.executor.support.exporter;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ontimize.jee.webclient.export.BaseExcelExporter;

/**
 * @author <a href="antonio.vazquez@imatia.com">antoniova</a>
 */
public class DefaultXSSFExcelExporter extends BaseExcelExporter<XSSFWorkbook> {

    @Override
    protected XSSFWorkbook buildWorkBook() {
        return new XSSFWorkbook();
    }

}
