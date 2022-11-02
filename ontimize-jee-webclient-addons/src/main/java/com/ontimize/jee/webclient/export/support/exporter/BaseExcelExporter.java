package com.ontimize.jee.webclient.export.support.exporter;

import com.ontimize.jee.webclient.export.CellStyleContext;
import com.ontimize.jee.webclient.export.ExcelExporter;
import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.SheetContext;
import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.helpers.ExcelHelper;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;
import com.ontimize.jee.webclient.export.support.DefaultExportColumnStyle;
import com.ontimize.jee.webclient.export.support.DefaultHeadExportColumn;
import com.ontimize.jee.webclient.export.support.sheetnameprovider.DefaultSheetNameProvider;
import com.ontimize.jee.webclient.export.util.ColumnCellUtils;
import com.ontimize.jee.webclient.export.util.ExportOptions;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Exportador base, que exporta a partir de un contexto y unos providers.
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */

public abstract class BaseExcelExporter<T extends Workbook> implements ExcelExporter<T> {

    // Usamos un nombre improbable para la hoja plantilla
    public static final String TEMPLATE_SHEET_NAME = "____TEMPLATE_SHEET____";

    private final ExportColumnStyle exportColumnStyle = new DefaultExportColumnStyle();

    private Sheet templateSheet;

    public BaseExcelExporter() {

    }

    public T export(
            final ExportColumnProvider exportColumnProvider,
            final ExportDataProvider dataProvider,
            final ExportStyleProvider styleProvider,
            ExportOptions exportOptions) throws ExportException {
        return this.export(exportColumnProvider, dataProvider, styleProvider, new DefaultSheetNameProvider(), exportOptions);
    }

    /**
     * Obtenemos primero los posibles estilos que pueden venir con las definiciones de columnas. Podrían
     * venir de las anotaciones en la tabla. Pero podrían estar todas a null. Si no se usaron
     * anotaciones, la forma de asignar estilo es es mediante el StyleProvider. Estos estilos
     * complementan a los que tuvieran las columnas. En el StyleProvider hay tres estilos: - Cabecera:
     * estilo por defecto para las cabeceras. - Columna: estilo de todas las celdas de una columna. -
     * Celda: estilo para celdas concretas. DefaultStyleProvider proporciona por defecto unos estilos
     * para todas las cabeceras y además unos por tipo para todas las columnas. Se pueden sobreescribir
     */
    @Override
    public T export(
            final ExportColumnProvider exportColumnProvider,
            final ExportDataProvider dataProvider,
            final ExportStyleProvider styleProvider,
            SheetNameProvider sheetNameProvider,
            ExportOptions exportOptions) throws ExportException {

        if (sheetNameProvider == null) {
            sheetNameProvider = new DefaultSheetNameProvider();
        }
        if (exportOptions == null) {
            exportOptions = new ExportOptions();
        }
        final T workBook = this.buildWorkBook();

        // Primero creamos una hoja "plantilla" con las cabeceras para que todas las hojas
        // adicionales
        // tengan las mismas. Cada hoja adicional será una copia de ella.
        // Al final se elimina
        this.templateSheet = null;
        this.templateSheet = this.createTemplateSheet(workBook);

        final Map<String, CellStyle> headerCellStylesById = this.addHeaderStyles(
                this.templateSheet,
                exportColumnProvider.getHeaderColumns(),
                styleProvider);
        this.addHeader(
                dataProvider,
                this.templateSheet,
                exportColumnProvider.getHeaderColumns(),
                headerCellStylesById,
                exportOptions,
                styleProvider);
        final Map<String, CellStyle> bodyCellStyles = this.addBodyStyles(
                this.templateSheet,
                exportColumnProvider.getBodyColumns(),
                dataProvider,
                styleProvider);
        this.addBody(
                exportColumnProvider.getBodyColumns(),
                dataProvider,
                bodyCellStyles,
                styleProvider,
                exportColumnProvider.getHeaderColumns(),
                headerCellStylesById,
                sheetNameProvider,
                exportOptions);
        this.removeTemplateSheet(workBook);
        return workBook;
    }

    public void addHeader(final ExportDataProvider provider, final Sheet sheet,
                          final List<HeadExportColumn> userHeaderColumns,
                          final Map<String, CellStyle> headerCellStylesById,
                          final ExportOptions exportOptions,
                          final ExportStyleProvider styleProvider) {
        if (userHeaderColumns != null) {
            this.createHeader(
                    provider,
                    sheet,
                    userHeaderColumns,
                    headerCellStylesById,
                    exportOptions,
                    styleProvider);
        }
    }

    public void addCell(
            final Sheet sheet,
            final ExportColumn exportColumn,
            final int rowIndex,
            final int colUserIndex,
            final Object value,
            final ExportStyleProvider styleProvider,
            final Map<String, CellStyle> bodyCellStyles) {
        this.createCell(sheet, exportColumn, rowIndex, colUserIndex, value, styleProvider, bodyCellStyles);
    }

    protected void addBody(
            final List<ExportColumn> userColumns,
            final ExportDataProvider dataProvider,
            final Map<String, CellStyle> bodyCellStyles,
            final ExportStyleProvider styleProvider,
            final List<HeadExportColumn> headerColumns,
            final Map<String, CellStyle> headerCellStylesById,
            final SheetNameProvider sheetNameProvider,
            final ExportOptions exportOptions) {

        // Tomamos el contexto de la primera hoja, que es la del nombre que dió el usuario y de
        // índice 0
        final SheetContext firstSheetContext = new SheetContext(
                0,
                0,
                sheetNameProvider.getDefaultSheetName(),
                sheetNameProvider.getDefaultSheetName(),
                0);
        // Obtenemos la primera hoja con ese contexto, que será una hoja nueva con el nombre que dió el
        // usuario
        final Sheet firstSheet = this.selectSheetByName(
                sheetNameProvider.getSheetName().apply(firstSheetContext),
                dataProvider,
                headerColumns,
                headerCellStylesById,
                exportOptions,
                styleProvider);

        final AtomicInteger aLastRowNum = new AtomicInteger(firstSheet.getLastRowNum());
        final AtomicInteger aActualSheetIndex = new AtomicInteger(firstSheet.getWorkbook().getSheetIndex(firstSheet));
        final AtomicReference<String> aSheetName = new AtomicReference<>(firstSheet.getSheetName());
        // Para cada fila, obtenemos el contexto de hoja, y si hay que cambiar de hoja cambiamos
        IntStream.range(0, dataProvider.getNumberOfRows()).forEach(rowIndex -> {
            final SheetContext sheetContext = new SheetContext(
                    rowIndex,
                    aLastRowNum.get(),
                    sheetNameProvider.getDefaultSheetName(),
                    aSheetName.get(),
                    aActualSheetIndex.get());
            final String newSheetName = sheetNameProvider.getSheetName().apply(sheetContext);
            final Sheet actualSheet = this.selectSheetByName(
                    newSheetName,
                    dataProvider,
                    headerColumns,
                    headerCellStylesById,
                    exportOptions, styleProvider);

            aLastRowNum.set(actualSheet.getLastRowNum() + 1);
            aSheetName.set(actualSheet.getSheetName());
            aActualSheetIndex.set(actualSheet.getWorkbook().getSheetIndex(actualSheet));
            userColumns.stream().forEach(column -> {
                final Object value = dataProvider.getCellValue(rowIndex, column.getId());
                this.addCell(
                        actualSheet,
                        column,
                        aLastRowNum.get(),
                        userColumns.indexOf(column),
                        value,
                        styleProvider,
                        bodyCellStyles);
                actualSheet.autoSizeColumn(userColumns.indexOf(column));
            });
        });
    }

    protected Sheet createTemplateSheet(final T workbook) {
        return workbook.createSheet(TEMPLATE_SHEET_NAME);
    }

    protected void createCell(
            final Sheet sheet,
            final ExportColumn exportColumn,
            final int row,
            final int colUserIndex,
            final Object cellValue,
            final ExportStyleProvider styleProvider,
            final Map<String, CellStyle> bodyCellStyles) {
        Row sheetRow = sheet.getRow(row);
        if (sheetRow == null) {
            sheetRow = sheet.createRow(row);
        }
        final Cell cell = sheetRow.createCell(colUserIndex);
        this.setCellStyle(sheet, exportColumn, row, colUserIndex, cellValue, styleProvider, cell, bodyCellStyles);
        this.assignCellValue(cell, cellValue);
    }

    protected void setCellStyle(
            final Sheet sheet,
            final ExportColumn<ExportColumnStyle> exportColumn,
            final int row,
            final int colUserIndex,
            final Object cellValue,
            final ExportStyleProvider<CellStyle, DataFormat> styleProvider,
            final Cell cell,
            final Map<String, CellStyle> bodyCellStyles) {

        /*
         * Primero asignamos el estilo general de esa columna
         */
        final CellStyle columnStyle = bodyCellStyles.get(exportColumn.getId());
        cell.setCellStyle(columnStyle);

        /*
         * Luego, si existe un estilo para la celda en el styleProvider, se asigna
         */
        final CellStyle userStyle = styleProvider.getCellStyle(
                new CellStyleContext<>(
                        row,
                        colUserIndex,
                        exportColumn.getId(),
                        cellValue,
                        columnStyle,
                        () -> {
                            final CellStyle ret = sheet.getWorkbook().createCellStyle();
                            ret.cloneStyleFrom(columnStyle);
                            return ret;
                        },
                        () -> sheet.getWorkbook().createDataFormat()));
        if (userStyle != null) {
            cell.setCellStyle(userStyle);
        }
    }

    protected void createHeader(
            final ExportDataProvider provider,
            final Sheet sheet,
            final List<HeadExportColumn> userHeaderColumns,
            final Map<String, CellStyle> headerCellStylesById,
            final ExportOptions exportOptions,
            final ExportStyleProvider styleProvider) {

        final AtomicInteger columnIndex = new AtomicInteger(0);
        userHeaderColumns.forEach(exportColumn -> {
            final int width = this.createHeader(
                    sheet,
                    exportColumn,
                    0,
                    columnIndex.get(),
                    headerCellStylesById,
                    styleProvider);

            columnIndex.addAndGet(width);
        });

        // Después de crear la cabecera, agrupamos hacia abajo las columnas sin hijos
        // para que tengan la altura total de la cabecera
        // También volvemos a aplicar el estilo de la celda al bloque creado
        final int headerHeight = sheet.getLastRowNum();
        userHeaderColumns.stream()
                .filter(t -> t.getHeadExportColumnCount() == 0)
                .forEach(column -> {
                    if (ExportColumn.class.isAssignableFrom(column.getClass())) {
                        final ExportColumn exportColumn = (ExportColumn) column;
//                    final int columnNumber = provider.getColumnIndex(exportColumn);
                        final int columnNumber = userHeaderColumns.indexOf(column);
                        if (headerHeight > 0) {
                            final CellRangeAddress region = new CellRangeAddress(
                                    0,
                                    headerHeight,
                                    columnNumber,
                                    columnNumber);
                            sheet.addMergedRegion(region);
                            for (int i = 0; i <= headerHeight; i++) {
                                if (sheet.getRow(i) == null) {
                                    sheet.createRow(i);
                                }
                                Cell cell = sheet.getRow(i).getCell(columnNumber);
                                if (cell == null) {
                                    cell = sheet.getRow(i).createCell(columnNumber);
                                }
// FIXME
//                                this.applyStyleToHeaderCell(sheet, exportColumn, 0, columnNumber, headerCellStylesById,
//                                        styleProvider,
//                                        cell);
                            }
                        }
                    }
                });
        if (exportOptions.isFreezeHeaders()) {
            sheet.createFreezePane(0, headerHeight + 1);
        }
    }

    protected int createHeader(
            final Sheet sheet,
            final HeadExportColumn column,
            final int rowIndex,
            final int columnIndex,
            final Map<String, CellStyle> headerCellStylesById,
            final ExportStyleProvider styleProvider) {
        Row currentRow = sheet.getRow(rowIndex);
        if (currentRow == null) {
            currentRow = sheet.createRow(rowIndex);
        }
        final Cell cell = currentRow.createCell(columnIndex);
        if (ExportColumn.class.isAssignableFrom(column.getClass())) {
            final int width = ((ExportColumn) column).getWidth();
            final short newWidth = ExcelHelper.pixelsToWidthUnits(width);
            if (newWidth != sheet.getColumnWidth(columnIndex)) {
                sheet.setColumnWidth(columnIndex, newWidth);
            }
        }
        if (((styleProvider != null) && (styleProvider.getColumnStyle(column.getId()) != null))
                && (styleProvider.getColumnStyle(column.getId()).getWidth() != -1)) {
            sheet.setColumnWidth(columnIndex,
                    ExcelHelper.pixelsToWidthUnits(styleProvider.getColumnStyle(column.getId()).getWidth()));
        }

        cell.setCellValue(column.getTitle());
        if (styleProvider != null) {
            // FIXME
//            this.applyStyleToHeaderCell(sheet, column, rowIndex, columnIndex, headerCellStylesById, styleProvider,
//                    cell);
        }

        final int cCount = column.getHeadExportColumnCount();
        int numFields = 1;
        if (cCount > 0) {
            int width = 0;
            int innerColumnIndex = columnIndex;
            for (int i = 0; i < cCount; i++) {
                final HeadExportColumn current = column.getHeadExportColumn(i);
                final int innerWidth = this.createHeader(sheet, current, rowIndex + 1, innerColumnIndex,
                        headerCellStylesById, styleProvider);
                width += innerWidth;
                innerColumnIndex++;
            }
            if (width > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, columnIndex, (columnIndex + width) - 1));
            }
            numFields = width;
        }
        return numFields;
    }

    protected void updateCellStyle(
            final Sheet sheet,
            final ExportColumnStyle fieldStyle,
            final CellStyle cellStyle) {

        final String dataFormatString = fieldStyle.getDataFormatString();
        if (dataFormatString != null) {
            cellStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat(dataFormatString));
        }
        if (fieldStyle.getFillBackgroundColor() != null) {
            short index = fieldStyle.getFillBackgroundColor().getIndex();
            cellStyle.setFillForegroundColor(index);
        }

        if (fieldStyle.getAlignment() != null) {
            cellStyle.setAlignment(HorizontalAlignment.forInt(fieldStyle.getAlignment().ordinal()));
        }

        if (fieldStyle.getVerticalAlignment() != null) {
            cellStyle.setVerticalAlignment(VerticalAlignment.forInt(fieldStyle.getVerticalAlignment().ordinal()));
        }
    }

    protected abstract T buildWorkBook();

    protected Date parseStringAsDate(Object s) {
        try {
            return new SimpleDateFormat("yyyy/MM/dd").parse(s.toString());
        } catch (ParseException e) {
        }
        return null;
    }

    private Map<String, CellStyle> addHeaderStyles(
            final Sheet sheet,
            final List<HeadExportColumn> columns,
            final ExportStyleProvider styleProvider) {

        final Map<String, CellStyle> headerCellStylesById = new HashMap<>();
        for (final HeadExportColumn column : columns) {
            this.addHeaderStyles(sheet, column, headerCellStylesById, styleProvider);
        }
        return headerCellStylesById;
    }

    private Map<String, CellStyle> addBodyStyles(
            final Sheet sheet,
            final List<ExportColumn> bodyColumns,
            final ExportDataProvider dataProvider,
            final ExportStyleProvider styleProvider) {

        final Map<String, CellStyle> bodyCellStyles = new HashMap<>();

//        IntStream.range(0, bodyColumns.size())
        bodyColumns.stream()
                .forEach(column -> {
//                final int dataProviderColumnIndex = dataProvider.getColumnIndex(bodyColumns.get(columnIndex));
//                if (dataProviderColumnIndex != -1) {
//                    final ExportColumn column = bodyColumns.get(dataProviderColumnIndex);
                    final Object cellValue = dataProvider.getCellValue(0, column.getId());
                    final Class objectClass = cellValue != null ? cellValue.getClass() : Object.class;
                    this.exportColumnStyle.reset();
                    /*
                     * Orden de preferencia de los estilos: Primero se asigna el estilo de la celda por defecto para el
                     * tipo de dato que contiene. Luego el estilo para esa columna según el styleProvider
                     */
                    this.exportColumnStyle
                            .set(styleProvider.getColumnStyleByType(objectClass))
                            .set(styleProvider.getColumnStyle(column.getId()));
                    final CellStyle cellStyle = this.createCellStyle(
                            sheet,
                            this.exportColumnStyle);
                    bodyCellStyles.put(
                            column.getId(),
                            cellStyle);
//                }
                });
        return bodyCellStyles;
    }

    private void removeTemplateSheet(final T workBook) {
        workBook.removeSheetAt(workBook.getSheetIndex(TEMPLATE_SHEET_NAME));
    }

    private Sheet selectSheetByName(
            final String newSheetName,
            final ExportDataProvider provider,
            final List<HeadExportColumn> headerColumns,
            final Map<String, CellStyle> headerCellStylesById,
            final ExportOptions exportOptions,
            final ExportStyleProvider styleProvider) {
        Sheet ret = null;
        ret = this.templateSheet.getWorkbook().getSheet(newSheetName);
        if (ret == null) {
            ret = this.templateSheet.getWorkbook().createSheet(newSheetName);
            if (headerColumns != null) {
                this.createHeader(provider, ret, headerColumns, headerCellStylesById, exportOptions, styleProvider);
            }
        }
        return ret;
    }

    private void applyStyleToHeaderCell(
            final Sheet sheet,
            final HeadExportColumn column,
            final int rowIndex,
            final int columnIndex,
            final Map<String, CellStyle> headerCellStylesById,
            final ExportStyleProvider styleProvider,
            final Cell cell) {

        final CellStyle style = headerCellStylesById.get(column.getId());
        if (style == null) {
            return;
        }
        cell.setCellStyle(style);
        final Supplier<CellStyle> cellStyleSupplier = () -> {
            final CellStyle ret = sheet.getWorkbook().createCellStyle();
            ret.cloneStyleFrom(style);
            return ret;
        };
        final Supplier<DataFormat> dataFormatSupplier = () -> {
            return sheet.getWorkbook().createDataFormat();
        };
        final CellStyle userStyle = (CellStyle) styleProvider.getHeaderCellStyle(
                new CellStyleContext<>(
                        rowIndex,
                        columnIndex,
                        column.getId(),
                        cell.getStringCellValue(),
                        style,
                        cellStyleSupplier,
                        dataFormatSupplier));
        if (userStyle != null) {
            cell.setCellStyle(userStyle);
        }
    }


    private void assignCellValue(final Cell cell, final Object value) {
        if (value == null) {
            return;
        }
        if (Number.class.isAssignableFrom(value.getClass())) {
            if (Integer.class.isAssignableFrom(value.getClass())) {
                cell.setCellValue(((Integer) value));
            } else if (Double.class.isAssignableFrom(value.getClass())) {
                cell.setCellValue((Double) value);
            } else if (Float.class.isAssignableFrom(value.getClass())) {
                cell.setCellValue((Float) value);
            } else {
                cell.setCellValue(((Number) value).doubleValue());
            }

            // Dividimos por 100 si es un porcentaje
            if (cell.getCellStyle().getDataFormatString().contains("%")) {
                BigDecimal bdTest = BigDecimal.valueOf(cell.getNumericCellValue() / 100);
                bdTest = bdTest.setScale(6, BigDecimal.ROUND_HALF_UP);
                cell.setCellValue(bdTest.doubleValue());
            }
        } else if (Boolean.class.isAssignableFrom(value.getClass())) {
            cell.setCellValue((Boolean) value);
        } else if (ColumnCellUtils.isDate(value.getClass())) {
            if (Date.class.isAssignableFrom(value.getClass())) {
                cell.setCellValue((Date) value);
            } else if (LocalDate.class.isAssignableFrom(value.getClass())) {
                cell.setCellValue((LocalDate) value);
            } else if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
                cell.setCellValue((LocalDateTime) value);
            } else if (Calendar.class.isAssignableFrom(value.getClass())) {
                cell.setCellValue((Calendar) value);
            }
        } else {
            final Date date = parseStringAsDate(value);
            if (date != null) {
                cell.setCellValue(date);
            } else {
                cell.setCellValue(String.valueOf(value));
            }
        }
    }

    private void addHeaderStyles(
            final Sheet sheet,
            final HeadExportColumn column,
            final Map<String, CellStyle> headerCellStylesById,
            final ExportStyleProvider styleProvider) {

        DefaultHeadExportColumn<ExportColumnStyle> defaultHeadExportColumn = null;
        ExportColumnStyle styleFromAnnotations = null;
        /*
         * Primero extraemos el estilo de las anotaciones de las columnas, si hay alguna
         */
        if (DefaultHeadExportColumn.class.isAssignableFrom(column.getClass())) {
            defaultHeadExportColumn = (DefaultHeadExportColumn<ExportColumnStyle>) column;
            styleFromAnnotations = defaultHeadExportColumn.getStyle();
        } else if (ExportColumn.class.isAssignableFrom(column.getClass())) {
            final ExportColumn exportColumn = (ExportColumn) column;
            styleFromAnnotations = (ExportColumnStyle) exportColumn.getStyle();
        }

        /*
         * Si no hay ningún estilo de anotaciones tomamos el estilo de la columna Si hay alguno le asignamos
         * los valores del estilo de la columna del styleProvider
         */
        ExportColumnStyle finalStyle = styleFromAnnotations;
        if (styleProvider != null) {
            if (finalStyle == null) {
                finalStyle = styleProvider.getColumnStyle(column.getId());
            } else {
                finalStyle.set(styleProvider.getColumnStyle(column.getId()));
            }
        }

        /*
         * Guardamos el estilo en un mapa por el id de la columna
         */
        if (finalStyle != null) {
            headerCellStylesById.put(
                    column.getId(),
                    this.createCellStyle(
                            sheet,
                            finalStyle));
        }
        /*
         * Si la columna tiene columnas anidadas hacemos lo mismo con cada una
         */
        if (column.getHeadExportColumnCount() > 0) {
            for (int n = 0; n < column.getHeadExportColumnCount(); n++) {
                this.addHeaderStyles(
                        sheet,
                        column.getHeadExportColumn(n),
                        headerCellStylesById,
                        styleProvider);
            }
        }
    }


    private CellStyle createCellStyle(final Sheet sheet, final ExportColumnStyle fieldStyle) {
        final CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        this.updateCellStyle(sheet, fieldStyle, cellStyle);
        return cellStyle;
    }

}
