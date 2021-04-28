package com.ontimize.jee.webclient.export;

import org.apache.poi.ss.usermodel.CellStyle;

public class ExcelExportColumn extends ExportColumn<CellStyle> {

    public ExcelExportColumn(final String id, final String title, final int width, final CellStyle style) {
        super(id, title, width, style);
    }

}
