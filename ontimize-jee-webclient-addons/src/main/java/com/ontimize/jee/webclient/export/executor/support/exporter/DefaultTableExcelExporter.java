package com.ontimize.jee.webclient.export.executor.support.exporter;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.ontimize.jee.webclient.export.BaseExcelExporter;

/**
 * @author <a href="antonio.vazquez@imatia.com">antoniova</a>
 */
public class DefaultTableExcelExporter extends BaseExcelExporter<SXSSFWorkbook> {

    @Override
    protected SXSSFWorkbook buildWorkBook() {
        return new SXSSFWorkbook();
    }

}
