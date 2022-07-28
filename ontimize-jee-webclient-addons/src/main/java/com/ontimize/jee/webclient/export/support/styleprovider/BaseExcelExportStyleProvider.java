package com.ontimize.jee.webclient.export.support.styleprovider;


import com.ontimize.jee.webclient.export.CellStyleContext;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.base.ExcelExportQueryParameters;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.rule.CellSelectionRule;
import com.ontimize.jee.webclient.export.rule.CellSelectionRuleFactory;
import com.ontimize.jee.webclient.export.rule.RowSelectionRule;
import com.ontimize.jee.webclient.export.rule.RowSelectionRuleFactory;
import com.ontimize.jee.webclient.export.support.DefaultExportColumnStyle;
import com.ontimize.jee.webclient.export.util.ColumnCellUtils;
import org.apache.poi.ss.usermodel.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseExcelExportStyleProvider<T> implements ExportStyleProvider<T, DataFormat> {

    private static final Logger logger = LoggerFactory.getLogger(BaseExcelExportStyleProvider.class);

    private Map<String, ExportColumnStyle> styles;
    
    private Map<String, List<String>> columnStyles;
    
    private Map<RowSelectionRule, List<String>> rowStyles;
    
    private Map<CellSelectionRule, List<String>> cellStyles;
    public BaseExcelExportStyleProvider(final ExcelExportQueryParameters exportParam) {
        this.initialize(exportParam);
    }
    
    protected void initialize(final ExcelExportQueryParameters exportParam) {
        this.styles = createStyles(exportParam);
        this.columnStyles = createColumnStyles(exportParam);
        this.rowStyles = this.createRowStyles(exportParam);
        this.cellStyles = this.createCellStyles(exportParam);
    }

    public Map<String, ExportColumnStyle> getStyles() {
        if(this.styles == null) {
            this.styles = new HashMap<>();
        }
        return styles;
    }

    public Map<String, List<String>> getColumnStyles() {
        if(this.columnStyles == null) {
            this.columnStyles = new HashMap<>();
        }
        return columnStyles;
    }

    public Map<RowSelectionRule, List<String>> getRowStyles() {
        if(this.rowStyles == null) {
            this.rowStyles = new HashMap<>();
        }
        return rowStyles;
    }

    public Map<CellSelectionRule, List<String>> getCellStyles() {
        if(this.cellStyles == null) {
            this.cellStyles = new HashMap<>();
        }
        return cellStyles;
    }

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

    
    protected Map<String, ExportColumnStyle> createStyles(final ExcelExportQueryParameters exportParam) {
        Map<String, Map<String, String>> styles = exportParam.getStyles();
        Map<String, ExportColumnStyle> exportColumnStyles = new HashMap<>();
        styles.entrySet().stream().forEach(m -> {
            Set<Map.Entry<String, String>> entries = m.getValue().entrySet();
            exportColumnStyles.put(m.getKey(), createColumnStyle(entries));
        });
        return exportColumnStyles;
    }

    protected ExportColumnStyle createColumnStyle(Set<Map.Entry<String, String>> entries) {
        ExportColumnStyle style = new DefaultExportColumnStyle();
        Iterator<Map.Entry<String, String>> i = entries.iterator();
        while (i.hasNext()) {
            Map.Entry<String, String> entry = i.next();
            switch (entry.getKey()) {
                case "dataFormatString":
                    style.setDataFormatString(entry.getValue());
                    break;
                case "alignment":
                    style.setAlignment(ExportColumnStyle.HorizontalAlignment.valueOf(entry.getValue()));
                    break;
                case "verticalAlignment":
                    style.setVerticalAlignment(ExportColumnStyle.VerticalAlignment.valueOf(entry.getValue()));
                    break;
                case "fillBackgroundColor":
                    style.setFillBackgroundColor(ExportColumnStyle.CellColor.valueOf(entry.getValue()));
                    break;
                case "width":
                    style.setWidth(Integer.valueOf(entry.getValue()));
                    break;
            }
        }
        return style;
    }

    protected Map<String, List<String>> createColumnStyles(final ExcelExportQueryParameters exportParam) {
        Map<String, Object> styles = exportParam.getColumnStyles();
        Map<String, List<String>> exportColumnStyles = new HashMap<>();
        styles.entrySet().stream().forEach(m -> {
            Object value = m.getValue();
            if (List.class.isAssignableFrom(value.getClass())) {
                List<String> styleNames = (List<String>) value;
                exportColumnStyles.put(m.getKey(), styleNames);
            } else {

                List<String> styleNames = new ArrayList<>();
                styleNames.add((String) value);
                exportColumnStyles.put(m.getKey(), styleNames);
            }
        });
        return exportColumnStyles;
    }

    protected Map<RowSelectionRule, List<String>> createRowStyles(final ExcelExportQueryParameters exportParam) {
        Map<String, Object> styles = exportParam.getRowStyles();
        Map<RowSelectionRule, List<String>> exportRowStyles = new HashMap<>();
        styles.entrySet().stream().forEach(m -> {
            try {
                RowSelectionRule rule = RowSelectionRuleFactory.create(m.getKey());
                Object value = m.getValue();
                if (List.class.isAssignableFrom(value.getClass())) {
                    List<String> styleNames = (List<String>) value;
                    exportRowStyles.put(rule, styleNames);
                } else {
                    List<String> styleNames = new ArrayList<>();
                    styleNames.add((String) value);
                    exportRowStyles.put(rule, styleNames);
                }
            } catch (Exception e) {
                logger.error("Impossible to create row styles", e);
            }
        });
        return exportRowStyles;

    }

    protected Map<CellSelectionRule, List<String>> createCellStyles(final ExcelExportQueryParameters exportParam) {
        Map<String, Object> styles = exportParam.getCellStyles();
        Map<CellSelectionRule, List<String>> exportCellStyles = new HashMap<>();
        styles.entrySet().stream().forEach(m -> {
            try {
                CellSelectionRule rule = CellSelectionRuleFactory.create(m.getKey());
                Object value = m.getValue();
                if (List.class.isAssignableFrom(value.getClass())) {
                    List<String> styleNames = (List<String>) value;
                    exportCellStyles.put(rule, styleNames);
                } else {
                    List<String> styleNames = new ArrayList<>();
                    styleNames.add((String) value);
                    exportCellStyles.put(rule, styleNames);
                }
            } catch (Exception e) {
                logger.error("Impossible to create row styles", e);
            }
        });
        return exportCellStyles;
    }
}
