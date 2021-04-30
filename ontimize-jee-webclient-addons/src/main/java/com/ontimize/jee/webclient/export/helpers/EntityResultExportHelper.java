package com.ontimize.jee.webclient.export.helpers;

import java.sql.JDBCType;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.executor.support.DefaultExportColumnStyle;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.support.BaseExportColumnProvider;
import com.ontimize.jee.webclient.export.support.DefaultHeadExportColumn;
import com.ontimize.jee.webclient.export.util.ColumnCellUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EntityResultExportHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityResultExportHelper.class);

    private EntityResultExportHelper() {

    }

    public static <T> ExportColumnProvider getExportContextFromEntityResult(final EntityResult entityResult) {
        return getExportContextFromEntityResult(entityResult, null);
    }

    public static <T> ExportColumnProvider getExportContextFromEntityResult(final EntityResult entityResult,
            List orderColumns) {
        final List<HeadExportColumn> headerColumns = createHeaderColumnsFromEntityResult(entityResult, orderColumns);
        final List<ExportColumn> bodyColumns = createBodyColumnsFromEntityResult(entityResult);
        return new BaseExportColumnProvider(headerColumns, bodyColumns);
    }

    private static List<HeadExportColumn> createHeaderColumnsFromEntityResult(EntityResult entityResult,
            List orderColumns) {
        final ObservableList<HeadExportColumn> columns = FXCollections.observableArrayList();
        Iterator i = orderColumns != null
                ? orderColumns.iterator()
                : ((List) entityResult.get("columns")).iterator();
        while (i.hasNext()) {
            String nextColumn = (String) (i.next());
            columns.add(
                    new DefaultHeadExportColumn(
                            nextColumn,
                            (String) (((Map) (entityResult.get("columnNames"))).get(nextColumn)),
                            createDefaultHeaderCellStyle()));
        }
        return columns;
    }

    private static <T> List<ExportColumn> createBodyColumnsFromEntityResult(final EntityResult entityResult) {
        return createBodyColumnsFromEntityResult(entityResult, null);
    }

    private static <T> List<ExportColumn> createBodyColumnsFromEntityResult(final EntityResult entityResult,
            List orderColumns) {
        final ObservableList<ExportColumn> columns = FXCollections.observableArrayList();
        Iterator i = orderColumns != null
                ? orderColumns.iterator()
                : ((List) entityResult.get("columns")).iterator();
        while (i.hasNext()) {
            String columnName = (String) i.next();
            int columnSQLType = getColumnSQLType(entityResult, columnName);
            Class clazz = SQLTypeMap.toClass(columnSQLType);
            int type = JDBCType.valueOf(columnSQLType).getVendorTypeNumber();
            columns.add(new ExportColumn(
                    columnName,
                    columnName,
                    columnName.length(),
                    createBodyCellStyleFromClass(clazz)));
        }
        return columns;
    }

    private static ExportColumnStyle createDefaultHeaderCellStyle() {
        final ExportColumnStyle style = new DefaultExportColumnStyle();
        style.setAlignment(ExportColumnStyle.HorizontalAlignment.CENTER);
        style.setVerticalAlignment(ExportColumnStyle.VerticalAlignment.CENTER);
        return style;
    }

    private static <T> ExportColumnStyle createBodyCellStyleFromClass(Class type) {
        final ExportColumnStyle style = new DefaultExportColumnStyle();
        if (ColumnCellUtils.isNumber(type)) {
            style.setAlignment(ExportColumnStyle.HorizontalAlignment.RIGHT);
            if (Double.class.isAssignableFrom(type)
                    || double.class.isAssignableFrom(type)
                    || Float.class.isAssignableFrom(type)
                    || float.class.isAssignableFrom(type)) {
                style.setDataFormatString("#,##0.00");
            } else if (Long.class.isAssignableFrom(type)
                    || long.class.isAssignableFrom(type)
                    || Integer.class.isAssignableFrom(type)
                    || int.class.isAssignableFrom(type)) {
                style.setDataFormatString("#,##0");
            }
        } else if (ColumnCellUtils.isBoolean(type) || boolean.class.isAssignableFrom(type)) {
            style.setDataFormatString("text");

        } else if (ColumnCellUtils.isDate(type)) {
            style.setDataFormatString("dd/mm/yyyy");
        } else {
            style.setAlignment(ExportColumnStyle.HorizontalAlignment.LEFT);
        }
        return style;
    }

    private static <T> ExportColumnStyle createBodyCellStyleFromSQLType(int sqlType) {
        Class type = JDBCType.valueOf(sqlType).getDeclaringClass();
        final ExportColumnStyle style = new DefaultExportColumnStyle();
        if (ColumnCellUtils.isNumber(type)) {
            style.setAlignment(ExportColumnStyle.HorizontalAlignment.RIGHT);
            if (Double.class.isAssignableFrom(type)
                    || double.class.isAssignableFrom(type)
                    || Float.class.isAssignableFrom(type)
                    || float.class.isAssignableFrom(type)) {
                style.setDataFormatString("#,##0.00");
            } else if (Long.class.isAssignableFrom(type)
                    || long.class.isAssignableFrom(type)
                    || Integer.class.isAssignableFrom(type)
                    || int.class.isAssignableFrom(type)) {
                style.setDataFormatString("#,##0");
            }
        } else if (ColumnCellUtils.isBoolean(type) || boolean.class.isAssignableFrom(type)) {
            style.setDataFormatString("text");

        } else if (ColumnCellUtils.isDate(type)) {
            style.setDataFormatString("dd/mm/yyyy");
        } else {
            style.setAlignment(ExportColumnStyle.HorizontalAlignment.LEFT);
        }
        return style;
    }

    private static int getColumnSQLType(EntityResult entityResult, String columnName) {
        Map types = (Map) (entityResult.get("sqlTypes"));
        return (int) (types.get(columnName));
    }

}
