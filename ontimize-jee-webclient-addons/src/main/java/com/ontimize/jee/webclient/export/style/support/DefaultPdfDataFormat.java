package com.ontimize.jee.webclient.export.style.support;

import com.ontimize.jee.webclient.export.style.PdfDataFormat;
import com.ontimize.jee.webclient.export.util.ColumnCellUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

public class DefaultPdfDataFormat implements PdfDataFormat {

    @Override
    public Format getFormat(String pattern, Class<?> columnClass) {
        if (StringUtils.isEmpty(pattern)) {
            return null;
        }
        if (columnClass == null || Void.class.isAssignableFrom(columnClass)) {
            return null;
        }

        if (ColumnCellUtils.isNumber(columnClass)) {
            if (Double.class.isAssignableFrom(columnClass)
                    || double.class.isAssignableFrom(columnClass)
                    || Float.class.isAssignableFrom(columnClass)
                    || float.class.isAssignableFrom(columnClass)) {
                return new DecimalFormat(pattern);
            } else if (Long.class.isAssignableFrom(columnClass)
                    || long.class.isAssignableFrom(columnClass)
                    || Integer.class.isAssignableFrom(columnClass)
                    || int.class.isAssignableFrom(columnClass)) {
                return new DecimalFormat(pattern);
            }
        } else if (ColumnCellUtils.isBoolean(columnClass) || boolean.class.isAssignableFrom(columnClass)) {
            // TODO Create an specific format to transform boolean values
            return null;
        } else if (ColumnCellUtils.isDate(columnClass)) {
            return new SimpleDateFormat(pattern);
        }
        return null;
    }
}
