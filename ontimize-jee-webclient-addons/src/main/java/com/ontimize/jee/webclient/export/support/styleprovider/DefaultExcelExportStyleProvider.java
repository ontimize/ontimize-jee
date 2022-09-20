package com.ontimize.jee.webclient.export.support.styleprovider;


import com.ontimize.jee.webclient.export.CellStyleContext;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.base.ExcelExportQueryParameters;
import com.ontimize.jee.webclient.export.rule.CellSelectionRule;
import com.ontimize.jee.webclient.export.rule.RowSelectionRule;
import com.ontimize.jee.webclient.export.support.DefaultExportColumnStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultExcelExportStyleProvider extends AbstractExportStyleProvider<XSSFCellStyle, DataFormat> {

    protected Map<String, XSSFCellStyle> poiCellStyles;

    public DefaultExcelExportStyleProvider(ExcelExportQueryParameters exportParam) {
        super(exportParam);
    }

    @Override
    public XSSFCellStyle getHeaderCellStyle(CellStyleContext<XSSFCellStyle, DataFormat> context) {
        if (poiCellStyles == null) {
            poiCellStyles = new HashMap<>();
            getStyles().forEach((name, s) -> {
                XSSFCellStyle style = context.getCellStyleCreator().get();
                applyExportStyleToPoiStyle(context, s, style);
                poiCellStyles.put(name, style);
            });
        }
        List<String> finalCombinedStyleNames = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : getColumnStyles().entrySet()) {
            if (entry.getKey().equals(context.getColumnId())) {
                List<String> stylesForColumn = entry.getValue();
                String combinedStyleName = stylesForColumn.stream().collect(Collectors.joining("+"));
                // Si no existe el estilo combinado como estilo poi lo agregamos ahora
                if (!poiCellStyles.containsKey(combinedStyleName)) {
                    XSSFCellStyle combined = context.getCellStyleCreator().get();
                    for (String style : stylesForColumn) {
                        applyExportStyleToPoiStyle(context, getStyles().get(style), combined);
                    }
                    poiCellStyles.put(combinedStyleName, combined);
                }
                finalCombinedStyleNames.add(combinedStyleName);
            }
        }
        if (!finalCombinedStyleNames.isEmpty()) {
            String combinedStyleName = finalCombinedStyleNames.stream().collect(Collectors.joining("+"));
            if (!poiCellStyles.containsKey(combinedStyleName)) {
                for (String styleName : finalCombinedStyleNames) {
                    XSSFCellStyle combined = context.getCellStyleCreator().get();
                    for (String style : finalCombinedStyleNames) {
                        applyExportStyleToPoiStyle(context, getStyles().get(style), combined);
                    }
                    poiCellStyles.put(combinedStyleName, combined);
                }
            }
            return poiCellStyles.get(combinedStyleName);
        }
        return null;
    }

    @Override
    public ExportColumnStyle getColumnStyle(String columnId) {
        List<String> stylesOfThisColumn = getColumnStyles().get(columnId);
        if (stylesOfThisColumn != null) {
            DefaultExportColumnStyle ret = new DefaultExportColumnStyle();
            stylesOfThisColumn.forEach(t -> ret.set(getStyles().get(t)));
            return ret;
        }
        return null;
    }

    @Override
    public XSSFCellStyle getCellStyle(CellStyleContext<XSSFCellStyle, DataFormat> context) {
        List<String> finalCombinedStyleNames = new ArrayList<>();
        // La primera vez, creamos todos los estilos poi usados y los guardamos
        if (poiCellStyles == null) {
            poiCellStyles = new HashMap<>();
            getStyles().forEach((name, s) -> {
                XSSFCellStyle style = context.getCellStyleCreator().get();
                applyExportStyleToPoiStyle(context, s, style);
                poiCellStyles.put(name, style);
            });
        }

        // Si existe un grupo de estilos para esa fila, primero los agregamos a
        // poiCellStyles combinados en uno solo con el nombre a+b+c. Luego lo usamos
        for (Map.Entry<RowSelectionRule, List<String>> entry : getRowStyles().entrySet()) {
            if (entry.getKey().match(context.getRow())) {
                List<String> stylesForRow = entry.getValue();
                String combinedStyleName = stylesForRow.stream().collect(Collectors.joining("+"));
                // Si no existe el estilo combinado como estilo poi lo agregamos ahora
                if (!poiCellStyles.containsKey(combinedStyleName)) {
                    XSSFCellStyle combined = context.getCellStyleCreator().get();
                    for (String style : stylesForRow) {
                        applyExportStyleToPoiStyle(context, getStyles().get(style), combined);
                    }
                    poiCellStyles.put(combinedStyleName, combined);
                }
                finalCombinedStyleNames.add(combinedStyleName);
            }
        }

        // Si existe un grupo de estilos para esa columna, primero los agregamos a
        // poiCellStyles combinados en uno solo con el nombre a+b+c. Luego lo usamos
        for (Map.Entry<String, List<String>> entry : getColumnStyles().entrySet()) {
            if (entry.getKey().equals(context.getColumnId())) {
                List<String> stylesForColumn = entry.getValue();
                String combinedStyleName = stylesForColumn.stream().collect(Collectors.joining("+"));
                // Si no existe el estilo combinado como estilo poi lo agregamos ahora
                if (!poiCellStyles.containsKey(combinedStyleName)) {
                    XSSFCellStyle combined = context.getCellStyleCreator().get();
                    for (String style : stylesForColumn) {
                        applyExportStyleToPoiStyle(context, getStyles().get(style), combined);
                    }
                    poiCellStyles.put(combinedStyleName, combined);
                }
                finalCombinedStyleNames.add(combinedStyleName);
            }
        }

        // Si existe un grupo de estilos para esa celda en particular,
        for (Map.Entry<CellSelectionRule, List<String>> cellStyleEntrySet : getCellStyles().entrySet()) {
            if (cellStyleEntrySet.getKey().match(context.getRow(), context.getCol())) {
                List<String> cellStyleNamesOfCell = cellStyleEntrySet.getValue();
                String combinedStyleName = cellStyleNamesOfCell.stream().collect(Collectors.joining("+"));
                // Si no existe el estilo combinado como estilo poi lo agregamos ahora
                if (!poiCellStyles.containsKey(combinedStyleName)) {
                    XSSFCellStyle combined = context.getCellStyleCreator().get();
                    for (String style : cellStyleNamesOfCell) {
                        applyExportStyleToPoiStyle(context, getStyles().get(style), combined);
                    }
                    poiCellStyles.put(combinedStyleName, combined);
                }
                finalCombinedStyleNames.add(combinedStyleName);
            }
        }

        // Si se han encontrado varios estilos se vuelven a combinar en uno solo y se
        // agrega a poiCellStyles
        if (!finalCombinedStyleNames.isEmpty()) {
            String combinedStyleName = finalCombinedStyleNames.stream().collect(Collectors.joining("+"));
            if (!poiCellStyles.containsKey(combinedStyleName)) {
                for (String styleName : finalCombinedStyleNames) {
                    XSSFCellStyle combined = context.getCellStyleCreator().get();
                    for (String style : finalCombinedStyleNames) {
                        applyExportStyleToPoiStyle(context, getStyles().get(style), combined);
                    }
                    poiCellStyles.put(combinedStyleName, combined);
                }

            }
            return poiCellStyles.get(combinedStyleName);
        }
        return null;
    }

    protected void applyExportStyleToPoiStyle(CellStyleContext<XSSFCellStyle, DataFormat> context,
                                              ExportColumnStyle s, XSSFCellStyle style) {
        if (s == null) {
            return;
        }
        if (s.getHorizontalAlignment() != null) {
            style.setAlignment(
                    org.apache.poi.ss.usermodel.HorizontalAlignment.forInt(s.getHorizontalAlignment().getCode()));
        }
        if (s.getVerticalAlignment() != null) {
            style.setVerticalAlignment(
                    org.apache.poi.ss.usermodel.VerticalAlignment.forInt(s.getVerticalAlignment().getCode()));
        }
        if (s.getDataFormatString() != null) {
            DataFormat dataFormat = context.getDataFormatCreator().get();
            style.setDataFormat(dataFormat.getFormat(s.getDataFormatString()));
        }
        if (s.getFillBackgroundColor() != null) {
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            String name = s.getFillBackgroundColor().name();
            IndexedColors indexedColor = IndexedColors.valueOf(name);
            short index = indexedColor.getIndex();
            style.setFillForegroundColor(index);
        }
    }

}
