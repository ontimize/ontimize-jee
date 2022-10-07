package com.ontimize.jee.webclient.export.support.styleprovider;

import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ontimize.jee.webclient.export.CellStyleContext;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.base.AdvancedExportQueryParameters;
import com.ontimize.jee.webclient.export.rule.CellSelectionRule;
import com.ontimize.jee.webclient.export.rule.RowSelectionRule;
import com.ontimize.jee.webclient.export.style.PdfCellStyle;
import com.ontimize.jee.webclient.export.style.PdfDataFormat;
import com.ontimize.jee.webclient.export.style.util.PdfCellStyleUtils;
import com.ontimize.jee.webclient.export.support.DefaultExportColumnStyle;

public class DefaultPdfExportStyleProvider extends AbstractExportStyleProvider<PdfCellStyle, PdfDataFormat> {

  protected Map<String, PdfCellStyle> pdfCellStylesMap;

  public DefaultPdfExportStyleProvider(AdvancedExportQueryParameters exportParam) {
    super(exportParam);
  }

  @Override
  public PdfCellStyle getHeaderCellStyle(CellStyleContext<PdfCellStyle, PdfDataFormat> context) {
    if (pdfCellStylesMap == null) {
      pdfCellStylesMap = new HashMap<>();
      getStyles().forEach((name, s) -> {
        PdfCellStyle style = context.getCellStyleCreator().get();
        applyExportStyleToITextStyle(context, s, style);
        pdfCellStylesMap.put(name, style);
      });
    }
    List<String> finalCombinedStyleNames = new ArrayList<>();
    combineColumnStyles(context, finalCombinedStyleNames);

    if (!finalCombinedStyleNames.isEmpty()) {
      return getOrStoreCombinedStyleFromCache(context, finalCombinedStyleNames);
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
  public PdfCellStyle getCellStyle(CellStyleContext<PdfCellStyle, PdfDataFormat> context) {
    List<String> finalCombinedStyleNames = new ArrayList<>();
    // La primera vez, creamos todos los estilos poi usados y los guardamos
    if (pdfCellStylesMap == null) {
      pdfCellStylesMap = new HashMap<>();
      getStyles().forEach((name, s) -> {
        PdfCellStyle style = context.getCellStyleCreator().get();
        applyExportStyleToITextStyle(context, s, style);
        pdfCellStylesMap.put(name, style);
      });
    }

    // Si existe un grupo de estilos para esa fila, primero los agregamos a
    // pdfCellStyles combinados en uno solo con el nombre a+b+c. Luego lo usamos
    for (Map.Entry<RowSelectionRule, List<String>> entry : getRowStyles().entrySet()) {
      // For keeping compatibility with Excel exporting because of in Excel the header is included in row count but
      // here the header is another component.
      if (entry.getKey().match(context.getRow() + 1)) {
        List<String> stylesForRow = entry.getValue();
        String combinedStyleName = stylesForRow.stream().collect(Collectors.joining("+"));
        // Si no existe el estilo combinado como estilo iText lo agregamos ahora
        if (!pdfCellStylesMap.containsKey(combinedStyleName)) {
          PdfCellStyle combined = context.getCellStyleCreator().get();
          for (String style : stylesForRow) {
            applyExportStyleToITextStyle(context, getStyles().get(style), combined);
          }
          pdfCellStylesMap.put(combinedStyleName, combined);
        }
        finalCombinedStyleNames.add(combinedStyleName);
      }
    }

    // Si existe un grupo de estilos para esa columna, primero los agregamos a
    // pdfCellStyles combinados en uno solo con el nombre a+b+c. Luego lo usamos
    combineColumnStyles(context, finalCombinedStyleNames);

    // Si existe un grupo de estilos para esa celda en particular,
    for (Map.Entry<CellSelectionRule, List<String>> cellStyleEntrySet : getCellStyles().entrySet()) {
      // For keeping compatibility with Excel exporting because of in Excel the header is included in row count but
      // here the header is another component.
      if (cellStyleEntrySet.getKey().match(context.getRow() + 1, context.getCol())) {
        List<String> cellStyleNamesOfCell = cellStyleEntrySet.getValue();
        String combinedStyleName = cellStyleNamesOfCell.stream().collect(Collectors.joining("+"));
        // Si no existe el estilo combinado como estilo iText lo agregamos ahora
        if (!pdfCellStylesMap.containsKey(combinedStyleName)) {
          PdfCellStyle combined = context.getCellStyleCreator().get();
          for (String style : cellStyleNamesOfCell) {
            applyExportStyleToITextStyle(context, getStyles().get(style), combined);
          }
          pdfCellStylesMap.put(combinedStyleName, combined);
        }
        finalCombinedStyleNames.add(combinedStyleName);
      }
    }

    // Si se han encontrado varios estilos se vuelven a combinar en uno solo y se
    // agrega a pdfCellStyles
    if (!finalCombinedStyleNames.isEmpty()) {
      return getOrStoreCombinedStyleFromCache(context, finalCombinedStyleNames);
    }
    return null;
  }

  protected void applyExportStyleToITextStyle(CellStyleContext<PdfCellStyle, PdfDataFormat> context,
      ExportColumnStyle columnStyle, PdfCellStyle cellStyle) {
    if (columnStyle == null) {
      return;
    }

    if (columnStyle.getAlignment() != null) {
      cellStyle.setHorizontalAlignment(PdfCellStyleUtils.getHorizontalAlignment(columnStyle.getHorizontalAlignment()));
    }
    if (columnStyle.getVerticalAlignment() != null) {
      cellStyle.setVerticalAlignment(PdfCellStyleUtils.getVerticalAlignment(columnStyle.getVerticalAlignment()));
    }
    if (columnStyle.getDataFormatString() != null) {
      PdfDataFormat dataFormat = context.getDataFormatCreator().get();
      Format format =
          dataFormat.getFormat(columnStyle.getDataFormatString(), context.getValue() != null ? context.getValue().getClass() : Void.class);
      if (format != null) {
        cellStyle.setDataFormatter(format);
      }
    }
    if (columnStyle.getFillBackgroundColor() != null) {
      cellStyle.setBackgroundColor(PdfCellStyleUtils.getBackgroundColor(columnStyle.getFillBackgroundColor()));
    }
  }

  protected void combineColumnStyles(CellStyleContext<PdfCellStyle, PdfDataFormat> context, List<String> finalCombinedStyleNames) {
    // Si existe un grupo de estilos para esa columna, primero los agregamos a
    // pdfCellStyles combinados en uno solo con el nombre a+b+c. Luego lo usamos
    for (Map.Entry<String, List<String>> entry : getColumnStyles().entrySet()) {
      if (entry.getKey().equals(context.getColumnId())) {
        List<String> stylesForColumn = entry.getValue();
        String combinedStyleName = stylesForColumn.stream().collect(Collectors.joining("+"));
        // Si no existe el estilo combinado como estilo iText lo agregamos ahora
        if (!pdfCellStylesMap.containsKey(combinedStyleName)) {
          PdfCellStyle combined = context.getCellStyleCreator().get();
          for (String style : stylesForColumn) {
            applyExportStyleToITextStyle(context, getStyles().get(style), combined);
          }
          pdfCellStylesMap.put(combinedStyleName, combined);
        }
        finalCombinedStyleNames.add(combinedStyleName);
      }
    }
  }

  protected PdfCellStyle getOrStoreCombinedStyleFromCache(CellStyleContext<PdfCellStyle, PdfDataFormat> context,
      List<String> finalCombinedStyleNames) {
    String combinedStyleName = finalCombinedStyleNames.stream().collect(Collectors.joining("+"));
    if (!pdfCellStylesMap.containsKey(combinedStyleName)) {
      for (String styleName : finalCombinedStyleNames) {
        PdfCellStyle combined = context.getCellStyleCreator().get();
        for (String style : finalCombinedStyleNames) {
          applyExportStyleToITextStyle(context, getStyles().get(style), combined);
        }
        pdfCellStylesMap.put(combinedStyleName, combined);
      }
    }
    return pdfCellStylesMap.get(combinedStyleName);
  }

}
