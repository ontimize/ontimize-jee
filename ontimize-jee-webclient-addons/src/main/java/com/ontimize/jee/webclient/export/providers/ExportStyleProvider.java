package com.ontimize.jee.webclient.export.providers;

import com.ontimize.jee.webclient.export.CellStyleContext;
import com.ontimize.jee.webclient.export.ExportColumnStyle;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public interface ExportStyleProvider<T, D> {

    ExportColumnStyle getColumnStyleByType(Class clazz);

    ExportColumnStyle getColumnStyle(String columnId);

    T getHeaderCellStyle(CellStyleContext<T, D> context);

    T getCellStyle(CellStyleContext<T, D> context);

}
