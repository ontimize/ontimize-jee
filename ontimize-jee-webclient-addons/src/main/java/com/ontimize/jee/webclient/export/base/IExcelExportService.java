package com.ontimize.jee.webclient.export.base;

/**
 * @author <a href="antonio.vazquez@imatia.com">Antonio V�zquez Ara�jo</a>
 */
public interface IExcelExportService extends ExportService {

    void createXlsxProviders(final ExcelExportQueryParameters excelExportParam);

}
