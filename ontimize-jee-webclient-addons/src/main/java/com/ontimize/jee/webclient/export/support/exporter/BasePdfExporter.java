package com.ontimize.jee.webclient.export.support.exporter;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.ontimize.jee.webclient.export.CellStyleContext;
import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.Exporter;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.style.PdfCellStyle;
import com.ontimize.jee.webclient.export.style.PdfDataFormat;
import com.ontimize.jee.webclient.export.style.support.DefaultPdfDataFormat;
import com.ontimize.jee.webclient.export.style.support.DefaultPdfPCellStyle;
import com.ontimize.jee.webclient.export.style.util.PdfCellStyleUtils;
import com.ontimize.jee.webclient.export.support.DefaultExportColumnStyle;
import com.ontimize.jee.webclient.export.util.ExportOptions;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Format;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Exportador base, que exporta a partir de un contexto y unos providers.
 */
public abstract class BasePdfExporter<T extends Document> implements Exporter<T> {

    private final ExportColumnStyle exportColumnStyle = new DefaultExportColumnStyle();

    protected final PdfDataFormat pdfDataFormat;

    protected final File pdfFile;

    public BasePdfExporter(final File pdfFile) {
        this.pdfFile = pdfFile;
        this.pdfDataFormat = new DefaultPdfDataFormat();
    }

    public File getPdfFile() {
        return this.pdfFile;
    }

    public ExportColumnStyle getExportColumnStyle() {
        return this.exportColumnStyle;
    }

    /**
     * Obtenemos primero los posibles estilos que pueden venir con las definiciones de columnas. La forma de asignar estilo es mediante el
     * StyleProvider. Estos estilos complementan a los que tuvieran las columnas. En el StyleProvider hay tres estilos: - Cabecera: estilo por
     * defecto para las cabeceras. - Columna: estilo de todas las celdas de una columna. - Celda: estilo para celdas concretas.
     * DefaultStyleProvider proporciona por defecto unos estilos para todas las cabeceras y además unos por tipo para todas las columnas. Se
     * pueden sobreescribir
     */
    @Override
    public T export(
            final ExportColumnProvider exportColumnProvider,
            final ExportDataProvider dataProvider,
            final ExportStyleProvider styleProvider,
            ExportOptions exportOptions) throws ExportException {

        if (exportOptions == null) {
            exportOptions = new ExportOptions();
        }
        try (final FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);) {
            final T document = this.buildDocument();
            Table pdfTable = this.createPdfTable(exportColumnProvider);

            //TODO
            final Map<String, Object> headerCellStylesById = new HashMap<>();
            //        final Map<String, CellStyle> headerCellStylesById = this.addHeaderStyles(
            //                this.templateSheet,
            //                exportColumnProvider.getHeaderColumns(),
            //                styleProvider);
            this.addHeader(
                    dataProvider,
                    pdfTable,
                    exportColumnProvider.getHeaderColumns(),
                    headerCellStylesById,
                    exportOptions,
                    styleProvider);
            final Map<String, PdfCellStyle> bodyCellStyles = this.addBodyStyles(
                    exportColumnProvider.getBodyColumns(),
                    dataProvider,
                    styleProvider);
            this.addBody(
                    pdfTable,
                    exportColumnProvider.getBodyColumns(),
                    dataProvider,
                    bodyCellStyles,
                    styleProvider,
                    exportColumnProvider.getHeaderColumns(),
                    headerCellStylesById,
                    exportOptions);

            document.add(pdfTable);

            // IMPORTANT! close the document
            document.close();
            return document;
        } catch (IOException e) {
            throw new ExportException("Impossible to create export document", e);
        }
    }

    public Table createPdfTable(final ExportColumnProvider exportColumnProvider) {
        int numCols = Math.max(exportColumnProvider.getHeaderColumns().size(), exportColumnProvider.getBodyColumns().size());
        Table table = new Table(numCols);

        return table;
    }

    public void addHeader(final ExportDataProvider provider, final Table pdfTable,
                          final List<HeadExportColumn> userHeaderColumns,
                          final Map<String, Object> headerCellStylesById,
                          //      final Map<String, CellStyle> headerCellStylesById,
                          final ExportOptions exportOptions,
                          final ExportStyleProvider styleProvider) {
        if (userHeaderColumns != null) {
            this.createHeader(
                    provider,
                    pdfTable,
                    userHeaderColumns,
                    headerCellStylesById,
                    exportOptions,
                    styleProvider);
        }
    }

    public void addCell(
            Table pdfTable,
            final ExportColumn exportColumn,
            final int rowIndex,
            final int colUserIndex,
            final Object value,
            final ExportStyleProvider styleProvider,
            final Map<String, PdfCellStyle> bodyCellStyles) {
        Cell cell = this.createCell(exportColumn, rowIndex, colUserIndex, value, styleProvider, bodyCellStyles);
        if (cell != null) {
            pdfTable.addCell(cell);
        }
    }

    protected void addBody(
            Table pdfTable,
            final List<ExportColumn> userColumns,
            final ExportDataProvider dataProvider,
            final Map<String, PdfCellStyle> bodyCellStyles,
            final ExportStyleProvider styleProvider,
            final List<HeadExportColumn> headerColumns,
            //      final Map<String, CellStyle> headerCellStylesById,
            final Map<String, Object> headerCellStylesById,
            final ExportOptions exportOptions) {

        // Para cada fila...
        IntStream.range(0, dataProvider.getNumberOfRows()).forEach(rowIndex -> {
            userColumns.stream().forEach(column -> {
                final Object value = dataProvider.getCellValue(rowIndex, column.getId());
                this.addCell(
                        pdfTable,
                        column,
                        rowIndex,
                        userColumns.indexOf(column),
                        value,
                        styleProvider,
                        bodyCellStyles);
            });
        });
    }

    protected Cell createCell(
            final ExportColumn exportColumn,
            final int row,
            final int colUserIndex,
            final Object cellValue,
            final ExportStyleProvider styleProvider,
            final Map<String, PdfCellStyle> bodyCellStyles) {

        final Cell cell = new Cell();
        final PdfCellStyle cellStyle = bodyCellStyles.get(exportColumn.getId());
        this.setCellStyle(cell, exportColumn, row, colUserIndex, cellValue, styleProvider, cellStyle);
        this.assignCellValue(cell, cellValue, cellStyle);
        return cell;
    }

    protected void setCellStyle(
            final Cell cell,
            final ExportColumn<ExportColumnStyle> exportColumn,
            final int row,
            final int colUserIndex,
            final Object cellValue,
            final ExportStyleProvider<PdfCellStyle, PdfDataFormat> styleProvider,
            final PdfCellStyle cellStyle) {

        /*
         * Primero asignamos el estilo general de esa columna
         */
        PdfCellStyle cellStyle1 = cellStyle;
        if (cellStyle1 == null) {
            cellStyle1 = new DefaultPdfPCellStyle();
        }
        updateCellStyle(cell, cellStyle1);

        /*
         * Luego, si existe un estilo para la celda en el styleProvider, se asigna
         */
        final PdfCellStyle userStyle = styleProvider.getCellStyle(
                new CellStyleContext<>(
                        row,
                        colUserIndex,
                        exportColumn.getId(),
                        cellValue,
                        cellStyle,
                        () -> {
                            final PdfCellStyle ret = new DefaultPdfPCellStyle();
                            //  TODO check if it is necessary                          ret.set(columnStyle);
                            return ret;
                        },
                        () -> new DefaultPdfDataFormat()));
        if (userStyle != null) {
            updateCellStyle(cell, userStyle);
        }
    }

    protected void createHeader(
            final ExportDataProvider provider,
            final Table pdfTable,
            final List<HeadExportColumn> userHeaderColumns,
            //      final Map<String, CellStyle> headerCellStylesById,
            final Map<String, Object> headerCellStylesById,
            final ExportOptions exportOptions,
            final ExportStyleProvider styleProvider) {

        final AtomicInteger columnIndex = new AtomicInteger(0);
        userHeaderColumns.forEach(exportColumn -> {
            final int width = this.createHeader(
                    pdfTable,
                    exportColumn,
                    0,
                    columnIndex.get(),
                    headerCellStylesById,
                    styleProvider);

            columnIndex.addAndGet(width);
        });

        //        // Después de crear la cabecera, agrupamos hacia abajo las columnas sin hijos
        //        // para que tengan la altura total de la cabecera
        //        // También volvemos a aplicar el estilo de la celda al bloque creado
        //        final int headerHeight = sheet.getLastRowNum();
        //        userHeaderColumns.stream()
        //            .filter(t -> t.getHeadExportColumnCount() == 0)
        //            .forEach(column -> {
        //                if (ExportColumn.class.isAssignableFrom(column.getClass())) {
        //                    final ExportColumn exportColumn = (ExportColumn) column;
        ////                    final int columnNumber = provider.getColumnIndex(exportColumn);
        //                    final int columnNumber = userHeaderColumns.indexOf(column);
        //                    if (headerHeight > 0) {
        //                        final CellRangeAddress region = new CellRangeAddress(
        //                                0,
        //                                headerHeight,
        //                                columnNumber,
        //                                columnNumber);
        //                        sheet.addMergedRegion(region);
        //                        for (int i = 0; i <= headerHeight; i++) {
        //                            if (sheet.getRow(i) == null) {
        //                                sheet.createRow(i);
        //                            }
        //                            Cell cell = sheet.getRow(i).getCell(columnNumber);
        //                            if (cell == null) {
        //                                cell = sheet.getRow(i).createCell(columnNumber);
        //                            }
        //
        //                            this.applyStyleToHeaderCell(sheet, exportColumn, 0, columnNumber, headerCellStylesById,
        //                                    styleProvider,
        //                                    cell);
        //                        }
        //                    }
        //                }
        //            });
        //        if (exportOptions.isFreezeHeaders()) {
        //            sheet.createFreezePane(0, headerHeight + 1);
        //        }
    }

    protected int createHeader(
            final Table pdfTable,
            final HeadExportColumn column,
            final int rowIndex,
            final int columnIndex,
            final Map<String, Object> headerCellStylesById,
            //      final Map<String, CellStyle> headerCellStylesById,
            final ExportStyleProvider styleProvider) {
        //        Row currentRow = sheet.getRow(rowIndex);
        //        if (currentRow == null) {
        //            currentRow = sheet.createRow(rowIndex);
        //        }
        //        final Cell cell = currentRow.createCell(columnIndex);

        Cell headerCell = new Cell();

        // FIXME Hacer el cálculo de width y traspasarlo al objeto pdfTable
        //        if (ExportColumn.class.isAssignableFrom(column.getClass())) {
        //            final int width = ((ExportColumn) column).getWidth();
        //            final short newWidth = ExcelHelper.pixelsToWidthUnits(width);
        //            if (newWidth != sheet.getColumnWidth(columnIndex)) {
        //                sheet.setColumnWidth(columnIndex, newWidth);
        //            }
        //        }
        //        if (((styleProvider != null) && (styleProvider.getColumnStyle(column.getId()) != null))
        //                && (styleProvider.getColumnStyle(column.getId()).getWidth() != -1)) {
        //            sheet.setColumnWidth(columnIndex,
        //                    ExcelHelper.pixelsToWidthUnits(styleProvider.getColumnStyle(column.getId()).getWidth()));
        //        }

        headerCell.add(new Paragraph(column.getTitle()));
        if (styleProvider != null) {
            //FIXME estilos de la cabecera
            //            this.applyStyleToHeaderCell(sheet, column, rowIndex, columnIndex, headerCellStylesById, styleProvider,
            //                    cell);
        }

        //        final int cCount = column.getHeadExportColumnCount();
        int numFields = 1;
        //        if (cCount > 0) {
        //            int width = 0;
        //            int innerColumnIndex = columnIndex;
        //            for (int i = 0; i < cCount; i++) {
        //                final HeadExportColumn current = column.getHeadExportColumn(i);
        //                final int innerWidth = this.createHeader(sheet, current, rowIndex + 1, innerColumnIndex,
        //                        headerCellStylesById, styleProvider);
        //                width += innerWidth;
        //                innerColumnIndex++;
        //            }
        //            if (width > 1) {
        //                sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, columnIndex, (columnIndex + width) - 1));
        //            }
        //            numFields = width;
        //        }
        pdfTable.addHeaderCell(headerCell);
        return numFields;
    }

    protected void updateCellStyle(
            final Cell cell,
            final PdfCellStyle pdfCellStyle) {

        //        final String dataFormatString = pdfCellStyle.getDataFormatString();
        //        if (dataFormatString != null) {
        ////            cellStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat(dataFormatString));
        //        }
        if (pdfCellStyle.getBackgroundColor() != null) {
            cell.setBackgroundColor(pdfCellStyle.getBackgroundColor());
        }

        if (pdfCellStyle.getHorizontalAlignment() != null) {
            cell.setHorizontalAlignment(pdfCellStyle.getHorizontalAlignment());
        }

        if (pdfCellStyle.getVerticalAlignment() != null) {
            cell.setVerticalAlignment(pdfCellStyle.getVerticalAlignment());
        }
    }

    protected abstract T buildDocument() throws ExportException;

    //  private Map<String, CellStyle> addHeaderStyles(
    //      final Sheet sheet,
    //      final List<HeadExportColumn> columns,
    //      final ExportStyleProvider styleProvider) {
    //
    //    final Map<String, CellStyle> headerCellStylesById = new HashMap<>();
    //    for (final HeadExportColumn column : columns) {
    //      this.addHeaderStyles(sheet, column, headerCellStylesById, styleProvider);
    //    }
    //    return headerCellStylesById;
    //  }

    private Map<String, PdfCellStyle> addBodyStyles(
            final List<ExportColumn> bodyColumns,
            final ExportDataProvider dataProvider,
            final ExportStyleProvider styleProvider) {

        final Map<String, PdfCellStyle> bodyCellStyles = new HashMap<>();

        bodyColumns.stream()
                .forEach(column -> {
                    final Object cellValue = dataProvider.getCellValue(0, column.getId());
                    final Class objectClass = cellValue != null ? cellValue.getClass() : Object.class;

                    ExportColumnStyle exportColumnStyle = new DefaultExportColumnStyle();
                    /*
                     * Orden de preferencia de los estilos: Primero se asigna el estilo de la celda por defecto para el
                     * tipo de dato que contiene. Luego el estilo para esa columna según el styleProvider
                     */
                    exportColumnStyle
                            .set(styleProvider.getColumnStyleByType(objectClass))
                            .set(styleProvider.getColumnStyle(column.getId()));
                    final PdfCellStyle cellStyle = this.createCellStyle(exportColumnStyle, objectClass);
                    bodyCellStyles.put(column.getId(), cellStyle);
                    //                }
                });
        return bodyCellStyles;
    }

    //  private void applyStyleToHeaderCell(
    //      final Sheet sheet,
    //      final HeadExportColumn column,
    //      final int rowIndex,
    //      final int columnIndex,
    //      final Map<String, CellStyle> headerCellStylesById,
    //      final ExportStyleProvider styleProvider,
    //      final Cell cell) {
    //
    //    final CellStyle style = headerCellStylesById.get(column.getId());
    //    if (style == null) {
    //      return;
    //    }
    //    cell.setCellStyle(style);
    //    final Supplier<CellStyle> cellStyleSupplier = () -> {
    //      final CellStyle ret = sheet.getWorkbook().createCellStyle();
    //      ret.cloneStyleFrom(style);
    //      return ret;
    //    };
    //    final Supplier<DataFormat> dataFormatSupplier = () -> {
    //      return sheet.getWorkbook().createDataFormat();
    //    };
    //    final CellStyle userStyle = (CellStyle) styleProvider.getHeaderCellStyle(
    //        new CellStyleContext<>(
    //            rowIndex,
    //            columnIndex,
    //            column.getId(),
    //            cell.getStringCellValue(),
    //            style,
    //            cellStyleSupplier,
    //            dataFormatSupplier));
    //    if (userStyle != null) {
    //      cell.setCellStyle(userStyle);
    //    }
    //  }

    private void assignCellValue(final Cell cell, final Object value, final PdfCellStyle columnStyle) {
        if (value == null) {
            return;
        }

        String aux = String.valueOf(value);
        if (columnStyle != null && columnStyle.getDataFormatter() != null) {
            aux = columnStyle.getDataFormatter().format(value);
        }
        cell.add(new Paragraph(aux));

        //        if (Number.class.isAssignableFrom(value.getClass())) {
        //            if (Integer.class.isAssignableFrom(value.getClass())) {
        //                cell.setCellValue(((Integer) value));
        //            } else if (Double.class.isAssignableFrom(value.getClass())) {
        //                cell.setCellValue((Double) value);
        //            } else if (Float.class.isAssignableFrom(value.getClass())) {
        //                cell.setCellValue((Float) value);
        //            } else {
        //                cell.setCellValue(((Number) value).doubleValue());
        //            }
        //
        //            // Dividimos por 100 si es un porcentaje
        //            if (cell.getCellStyle().getDataFormatString().contains("%")) {
        //                BigDecimal bdTest = BigDecimal.valueOf(cell.getNumericCellValue() / 100);
        //                bdTest = bdTest.setScale(6, BigDecimal.ROUND_HALF_UP);
        //                cell.setCellValue(bdTest.doubleValue());
        //            }
        //        } else if (Boolean.class.isAssignableFrom(value.getClass())) {
        //            cell.setCellValue((Boolean) value);
        //        } else {
        //            final Date date = parseStringAsDate(value);
        //            if (date != null) {
        //                cell.setCellValue(date);
        //            } else {
        //                cell.setCellValue(String.valueOf(value));
        //            }
        //        }
    }

    //  private void addHeaderStyles(
    //      final Sheet sheet,
    //      final HeadExportColumn column,
    //      final Map<String, CellStyle> headerCellStylesById,
    //      final ExportStyleProvider styleProvider) {
    //
    //    DefaultHeadExportColumn<ExportColumnStyle> defaultHeadExportColumn = null;
    //    ExportColumnStyle styleFromAnnotations = null;
    //    /*
    //     * Primero extraemos el estilo de las anotaciones de las columnas, si hay alguna
    //     */
    //    if (DefaultHeadExportColumn.class.isAssignableFrom(column.getClass())) {
    //      defaultHeadExportColumn = (DefaultHeadExportColumn<ExportColumnStyle>) column;
    //      styleFromAnnotations = defaultHeadExportColumn.getStyle();
    //    } else if (ExportColumn.class.isAssignableFrom(column.getClass())) {
    //      final ExportColumn exportColumn = (ExportColumn) column;
    //      styleFromAnnotations = (ExportColumnStyle) exportColumn.getStyle();
    //    }
    //
    //    /*
    //     * Si no hay ningún estilo de anotaciones tomamos el estilo de la columna Si hay alguno le asignamos
    //     * los valores del estilo de la columna del styleProvider
    //     */
    //    ExportColumnStyle finalStyle = styleFromAnnotations;
    //    if (styleProvider != null) {
    //      if (finalStyle == null) {
    //        finalStyle = styleProvider.getColumnStyle(column.getId());
    //      } else {
    //        finalStyle.set(styleProvider.getColumnStyle(column.getId()));
    //      }
    //    }
    //
    //    /*
    //     * Guardamos el estilo en un mapa por el id de la columna
    //     */
    //    if (finalStyle != null) {
    //      //TODO REVIEW
    //      //            headerCellStylesById.put(
    //      //                    column.getId(),
    //      //                    this.createCellStyle(
    //      //                            sheet,
    //      //                            finalStyle));
    //    }
    //    /*
    //     * Si la columna tiene columnas anidadas hacemos lo mismo con cada una
    //     */
    //    if (column.getHeadExportColumnCount() > 0) {
    //      for (int n = 0; n < column.getHeadExportColumnCount(); n++) {
    //        this.addHeaderStyles(
    //            sheet,
    //            column.getHeadExportColumn(n),
    //            headerCellStylesById,
    //            styleProvider);
    //      }
    //    }
    //  }

    protected PdfCellStyle createCellStyle(final ExportColumnStyle columnStyle, Class<?> columnClass) {
        PdfCellStyle pdfCellStyle = PdfCellStyleUtils.createPdfCellStyle(columnStyle);
        if (columnStyle != null && !StringUtils.isEmpty(columnStyle.getDataFormatString())) {
            Format format = getPdfDataFormat().getFormat(columnStyle.getDataFormatString(), columnClass);
            if (format != null) {
                pdfCellStyle.setDataFormatter(format);
            }
        }
        return pdfCellStyle;
    }

    protected PdfDataFormat getPdfDataFormat() {
        return this.pdfDataFormat;
    }

}
