package com.ontimize.jee.webclient.export.support.styleprovider;


import org.apache.poi.ss.usermodel.DataFormat;

import com.ontimize.jee.webclient.export.CellStyleContext;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.support.DefaultExportColumnStyle;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.util.ColumnCellUtils;

public class DefaultExcelExportStyleProvider<T> implements ExportStyleProvider<T, DataFormat> {

    @Override
    public ExportColumnStyle getColumnStyleByType(final Class columnClass) {
        final ExportColumnStyle style = new DefaultExportColumnStyle();
        if (ColumnCellUtils.isNumber(columnClass)) {
            style.setAlignment(ExportColumnStyle.HorizontalAlignment.RIGHT);
            if (Double.class.isAssignableFrom(columnClass)
                    || double.class.isAssignableFrom(columnClass)
                    || Float.class.isAssignableFrom(columnClass)
                    || float.class.isAssignableFrom(columnClass)) {
                style.setDataFormatString("#,##0.00");
            } else if (Long.class.isAssignableFrom(columnClass)
                    || long.class.isAssignableFrom(columnClass)
                    || Integer.class.isAssignableFrom(columnClass)
                    || int.class.isAssignableFrom(columnClass)) {
                style.setDataFormatString("#,##0");
            }
        } else if (ColumnCellUtils.isBoolean(columnClass) || boolean.class.isAssignableFrom(columnClass)) {
            style.setDataFormatString("text");

        } else if (ColumnCellUtils.isDate(columnClass)) {
            if (java.util.Date.class.isAssignableFrom(columnClass)
                    || java.sql.Date.class.isAssignableFrom(columnClass)
                    || java.sql.Time.class.isAssignableFrom(columnClass)
                    || java.sql.Timestamp.class.isAssignableFrom(columnClass)
                    || java.time.LocalDateTime.class.isAssignableFrom(columnClass)) {
                style.setDataFormatString("dd/mm/yyyy hh:mm:ss");
            } else {
                style.setDataFormatString("dd/mm/yyyy");
            }

        } else {
            style.setAlignment(ExportColumnStyle.HorizontalAlignment.LEFT);
        }
        return style;
    }

    @Override
    public ExportColumnStyle getColumnStyle(final String columnId) {
        return null;
    }

    @Override
    public T getHeaderCellStyle(final CellStyleContext<T, DataFormat> context) {
        return null;
    }

    @Override
    public T getCellStyle(final CellStyleContext<T, DataFormat> context) {
        return null;
    }

}
