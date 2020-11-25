package com.ontimize.jee.webclient.excelexport.providers;

import com.ontimize.jee.webclient.excelexport.CellStyleContext;
import com.ontimize.jee.webclient.excelexport.ExportColumnStyle;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public interface ExportStyleProvider<T, D> {

    ExportColumnStyle getColumnStyleByType(Class clazz);

    ExportColumnStyle getColumnStyle(String columnId);

    T getHeaderCellStyle(CellStyleContext<T, D> context);

    T getCellStyle(CellStyleContext<T, D> context);

}
