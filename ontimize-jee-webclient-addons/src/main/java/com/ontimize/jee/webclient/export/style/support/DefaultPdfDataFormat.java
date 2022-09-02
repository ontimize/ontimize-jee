package com.ontimize.jee.webclient.export.style.support;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import com.ontimize.jee.webclient.export.style.PdfDataFormat;
import com.ontimize.jee.webclient.export.util.ColumnCellUtils;
import org.apache.commons.lang3.StringUtils;

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
      // FIXME
    } else if (ColumnCellUtils.isDate(columnClass)) {
      if (java.util.Date.class.isAssignableFrom(columnClass)
          || java.sql.Date.class.isAssignableFrom(columnClass)
          || java.sql.Time.class.isAssignableFrom(columnClass)
          || java.sql.Timestamp.class.isAssignableFrom(columnClass)
          || java.time.LocalDateTime.class.isAssignableFrom(columnClass)) {
        return new SimpleDateFormat(pattern);
      } else {
        return new SimpleDateFormat(pattern);
      }
    }
    return null;
  }
}
