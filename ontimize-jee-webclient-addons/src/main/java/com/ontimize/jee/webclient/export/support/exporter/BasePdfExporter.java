package com.ontimize.jee.webclient.export.support.exporter;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
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
import com.ontimize.jee.webclient.export.support.DefaultHeadExportColumn;
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
	 * Obtenemos primero los posibles estilos que pueden venir con las definiciones
	 * de columnas. La forma de asignar estilo es mediante el StyleProvider. Estos
	 * estilos complementan a los que tuvieran las columnas. En el StyleProvider hay
	 * tres estilos: - Cabecera: estilo por defecto para las cabeceras. - Columna:
	 * estilo de todas las celdas de una columna. - Celda: estilo para celdas
	 * concretas. DefaultStyleProvider proporciona por defecto unos estilos para
	 * todas las cabeceras y además unos por tipo para todas las columnas. Se pueden
	 * sobreescribir
	 */
	@Override
	public T export(final ExportColumnProvider exportColumnProvider, final ExportDataProvider dataProvider,
			final ExportStyleProvider styleProvider, ExportOptions exportOptions, boolean landscape) throws ExportException {

		if (exportOptions == null) {
			exportOptions = new ExportOptions();
		}
		try (final FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);) {
			T document = this.buildDocument(landscape);			

			Table pdfTable = this.createPdfTable(exportColumnProvider);
			document.add(pdfTable);

			final Map<String, PdfCellStyle> headerCellStylesById = this
					.addHeaderStyles(exportColumnProvider.getHeaderColumns(), styleProvider);
			this.addHeader(pdfTable, exportColumnProvider.getHeaderColumns(), headerCellStylesById, styleProvider,
					exportOptions);
			final Map<String, PdfCellStyle> bodyCellStyles = this.addBodyStyles(exportColumnProvider.getBodyColumns(),
					dataProvider, styleProvider);
			this.addBody(pdfTable, exportColumnProvider.getBodyColumns(), dataProvider, bodyCellStyles, styleProvider,
					exportOptions);

			// Tell the table that there are not more records
			pdfTable.complete();

			// IMPORTANT! close the document
			document.close();
			return document;
		} catch (IOException e) {
			throw new ExportException("Impossible to create export document", e);
		}
	}

	public Table createPdfTable(final ExportColumnProvider exportColumnProvider) {
		int numCols = Math.max(exportColumnProvider.getHeaderColumns().size(),
				exportColumnProvider.getBodyColumns().size());
		Table table = new Table(UnitValue.createPercentArray(numCols), true);
		table.setBorder(Border.NO_BORDER);

		return table;
	}

	public void addHeader(final Table pdfTable, final List<HeadExportColumn> userHeaderColumns,
			final Map<String, PdfCellStyle> headerCellStylesById, final ExportStyleProvider styleProvider,
			final ExportOptions exportOptions) {
		if (userHeaderColumns != null) {
			this.createHeader(pdfTable, userHeaderColumns, headerCellStylesById, styleProvider, exportOptions);
		}
	}

	public void addCell(Table pdfTable, final ExportColumn exportColumn, final int rowIndex, final int colUserIndex,
			final Object value, final ExportStyleProvider styleProvider,
			final Map<String, PdfCellStyle> bodyCellStyles) {
		Cell cell = this.createCell(exportColumn, rowIndex, colUserIndex, value, styleProvider, bodyCellStyles);
		if (cell != null) {
			pdfTable.addCell(cell);
		}
	}

	protected void addBody(Table pdfTable, final List<ExportColumn> userColumns, final ExportDataProvider dataProvider,
			final Map<String, PdfCellStyle> bodyCellStyles, final ExportStyleProvider styleProvider,
			final ExportOptions exportOptions) {

		// Para cada fila...
		IntStream.range(0, dataProvider.getNumberOfRows()).forEach(rowIndex -> {
			userColumns.stream().forEach(column -> {
				if (rowIndex % 50 == 0) {
					// Flushes the current content, e.g. places it on the document.
					// Please bear in mind that the method (alongside complete()) make sense only
					// for 'large tables'
					pdfTable.flush();
				}
				final Object value = dataProvider.getCellValue(rowIndex, column.getId());
				this.addCell(pdfTable, column, rowIndex, userColumns.indexOf(column), value, styleProvider,
						bodyCellStyles);
			});
		});
	}

	protected Cell createCell(final ExportColumn exportColumn, final int row, final int colUserIndex,
			final Object cellValue, final ExportStyleProvider styleProvider,
			final Map<String, PdfCellStyle> bodyCellStyles) {

		final Cell cell = new Cell();
		final PdfCellStyle cellStyle = bodyCellStyles.get(exportColumn.getId());
		this.setCellStyle(cell, exportColumn, row, colUserIndex, cellValue, styleProvider, cellStyle);
		this.assignCellValue(cell, cellValue, cellStyle);
		return cell;
	}

	protected void setCellStyle(final Cell cell, final ExportColumn<ExportColumnStyle> exportColumn, final int row,
			final int colUserIndex, final Object cellValue,
			final ExportStyleProvider<PdfCellStyle, PdfDataFormat> styleProvider, final PdfCellStyle cellStyle) {

		cell.setBorder(Border.NO_BORDER);
		cell.setPaddingLeft(8.0f);
		cell.setPaddingRight(8.0f);

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
				new CellStyleContext<>(row, colUserIndex, exportColumn.getId(), cellValue, cellStyle1, () -> {
					final PdfCellStyle ret = new DefaultPdfPCellStyle();
					ret.cloneStyleFrom(cellStyle);
					return ret;
				}, () -> new DefaultPdfDataFormat()));
		if (userStyle != null) {
			updateCellStyle(cell, userStyle);
		}
	}

	protected void createHeader(final Table pdfTable, final List<HeadExportColumn> userHeaderColumns,
			final Map<String, PdfCellStyle> headerCellStylesById, final ExportStyleProvider styleProvider,
			final ExportOptions exportOptions) {

		final AtomicInteger columnIndex = new AtomicInteger(0);
		userHeaderColumns.forEach(exportColumn -> {
			final int width = this.createHeaderCell(pdfTable, exportColumn, headerCellStylesById, styleProvider,
					exportOptions);

			columnIndex.addAndGet(width);
		});

		// // Después de crear la cabecera, agrupamos hacia abajo las columnas sin hijos
		// // para que tengan la altura total de la cabecera
		// // También volvemos a aplicar el estilo de la celda al bloque creado
		// final int headerHeight = sheet.getLastRowNum();
		// userHeaderColumns.stream()
		// .filter(t -> t.getHeadExportColumnCount() == 0)
		// .forEach(column -> {
		// if (ExportColumn.class.isAssignableFrom(column.getClass())) {
		// final ExportColumn exportColumn = (ExportColumn) column;
		//// final int columnNumber = provider.getColumnIndex(exportColumn);
		// final int columnNumber = userHeaderColumns.indexOf(column);
		// if (headerHeight > 0) {
		// final CellRangeAddress region = new CellRangeAddress(
		// 0,
		// headerHeight,
		// columnNumber,
		// columnNumber);
		// sheet.addMergedRegion(region);
		// for (int i = 0; i <= headerHeight; i++) {
		// if (sheet.getRow(i) == null) {
		// sheet.createRow(i);
		// }
		// Cell cell = sheet.getRow(i).getCell(columnNumber);
		// if (cell == null) {
		// cell = sheet.getRow(i).createCell(columnNumber);
		// }
		//
		// this.applyStyleToHeaderCell(sheet, exportColumn, 0, columnNumber,
		// headerCellStylesById,
		// styleProvider,
		// cell);
		// }
		// }
		// }
		// });
		// if (exportOptions.isFreezeHeaders()) {
		// sheet.createFreezePane(0, headerHeight + 1);
		// }
	}

	protected int createHeaderCell(final Table pdfTable, final HeadExportColumn column,
			final Map<String, PdfCellStyle> headerCellStylesById, final ExportStyleProvider styleProvider,
			final ExportOptions exportOptions) {

		Cell headerCell = new Cell();

		// FIXME Hacer el cálculo de width y traspasarlo al objeto pdfTable
		// if (ExportColumn.class.isAssignableFrom(column.getClass())) {
		// final int width = ((ExportColumn) column).getWidth();
		// final short newWidth = ExcelHelper.pixelsToWidthUnits(width);
		// if (newWidth != sheet.getColumnWidth(columnIndex)) {
		// sheet.setColumnWidth(columnIndex, newWidth);
		// }
		// }
		// if (((styleProvider != null) && (styleProvider.getColumnStyle(column.getId())
		// != null))
		// && (styleProvider.getColumnStyle(column.getId()).getWidth() != -1)) {
		// sheet.setColumnWidth(columnIndex,
		// ExcelHelper.pixelsToWidthUnits(styleProvider.getColumnStyle(column.getId()).getWidth()));
		// }

		headerCell.add(new Paragraph(column.getTitle()));

		// Default style
		headerCell.setPaddingLeft(8.0f);
		headerCell.setPaddingRight(8.0f);
		headerCell.setBorder(Border.NO_BORDER);
		Border border = new SolidBorder(new DeviceRgb(204, 204, 204), 1);
		headerCell.setBorderBottom(border);
		headerCell.setBold();

		if (styleProvider != null) {
			// FIXME estilos de la cabecera
			// this.applyStyleToHeaderCell(sheet, column, rowIndex, columnIndex,
			// headerCellStylesById, styleProvider,
			// cell);
		}

		// final int cCount = column.getHeadExportColumnCount();
		int numFields = 1;
		// if (cCount > 0) {
		// int width = 0;
		// int innerColumnIndex = columnIndex;
		// for (int i = 0; i < cCount; i++) {
		// final HeadExportColumn current = column.getHeadExportColumn(i);
		// final int innerWidth = this.createHeader(sheet, current, rowIndex + 1,
		// innerColumnIndex,
		// headerCellStylesById, styleProvider);
		// width += innerWidth;
		// innerColumnIndex++;
		// }
		// if (width > 1) {
		// sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, columnIndex,
		// (columnIndex + width) - 1));
		// }
		// numFields = width;
		// }
		pdfTable.addHeaderCell(headerCell);
		return numFields;
	}

	protected void updateCellStyle(final Cell cell, final PdfCellStyle pdfCellStyle) {

		if (pdfCellStyle.getBackgroundColor() != null) {
			cell.setBackgroundColor(pdfCellStyle.getBackgroundColor());
		}

		if (pdfCellStyle.getHorizontalAlignment() != null) {
			cell.setTextAlignment(pdfCellStyle.getHorizontalAlignment());
		}

		if (pdfCellStyle.getVerticalAlignment() != null) {
			cell.setVerticalAlignment(pdfCellStyle.getVerticalAlignment());
		}
	}

	protected abstract T buildDocument(boolean lanscape) throws ExportException;

	protected Map<String, PdfCellStyle> addHeaderStyles(final List<HeadExportColumn> columns,
			final ExportStyleProvider styleProvider) {

		final Map<String, PdfCellStyle> headerCellStylesById = new HashMap<>();
		for (final HeadExportColumn column : columns) {
			this.addHeaderStyles(column, headerCellStylesById, styleProvider);
		}
		return headerCellStylesById;
	}

	protected Map<String, PdfCellStyle> addBodyStyles(final List<ExportColumn> bodyColumns,
			final ExportDataProvider dataProvider, final ExportStyleProvider styleProvider) {

		final Map<String, PdfCellStyle> bodyCellStyles = new HashMap<>();

		bodyColumns.stream().forEach(column -> {
			final Object cellValue = dataProvider.getCellValue(0, column.getId());
			final Class objectClass = cellValue != null ? cellValue.getClass() : Object.class;

			ExportColumnStyle exportColumnStyle = new DefaultExportColumnStyle();
			/*
			 * Orden de preferencia de los estilos: Primero se asigna el estilo de la celda
			 * por defecto para el tipo de dato que contiene. Luego el estilo para esa
			 * columna según el styleProvider
			 */
			exportColumnStyle.set(styleProvider.getColumnStyleByType(objectClass))
					.set(styleProvider.getColumnStyle(column.getId()));
			final PdfCellStyle cellStyle = this.createCellStyle(exportColumnStyle, objectClass);
			bodyCellStyles.put(column.getId(), cellStyle);
			// }
		});
		return bodyCellStyles;
	}

	protected void applyStyleToHeaderCell(final HeadExportColumn column, final int rowIndex, final int columnIndex,
			final Map<String, PdfCellStyle> headerCellStylesById, final ExportStyleProvider styleProvider,
			final Cell cell) {

		final PdfCellStyle cellStyle = headerCellStylesById.get(column.getId());
		if (cellStyle == null) {
			return;
		}
		updateCellStyle(cell, cellStyle);

		final PdfCellStyle userStyle = (PdfCellStyle) styleProvider.getHeaderCellStyle(
				new CellStyleContext<>(rowIndex, columnIndex, column.getId(), null, cellStyle, () -> {
					final PdfCellStyle ret = new DefaultPdfPCellStyle();
					ret.cloneStyleFrom(cellStyle);
					return ret;
				}, () -> new DefaultPdfDataFormat()));
		if (userStyle != null) {
			updateCellStyle(cell, userStyle);
		}
	}

	private void assignCellValue(final Cell cell, final Object value, final PdfCellStyle columnStyle) {
		if (value == null) {
			return;
		}

		String aux = String.valueOf(value);
		if (columnStyle != null && columnStyle.getDataFormatter() != null) {
			aux = columnStyle.getDataFormatter().format(value);
		}
		cell.add(new Paragraph(aux));

		// if (Number.class.isAssignableFrom(value.getClass())) {
		// if (Integer.class.isAssignableFrom(value.getClass())) {
		// cell.setCellValue(((Integer) value));
		// } else if (Double.class.isAssignableFrom(value.getClass())) {
		// cell.setCellValue((Double) value);
		// } else if (Float.class.isAssignableFrom(value.getClass())) {
		// cell.setCellValue((Float) value);
		// } else {
		// cell.setCellValue(((Number) value).doubleValue());
		// }
		//
		// // Dividimos por 100 si es un porcentaje
		// if (cell.getCellStyle().getDataFormatString().contains("%")) {
		// BigDecimal bdTest = BigDecimal.valueOf(cell.getNumericCellValue() / 100);
		// bdTest = bdTest.setScale(6, BigDecimal.ROUND_HALF_UP);
		// cell.setCellValue(bdTest.doubleValue());
		// }
		// } else if (Boolean.class.isAssignableFrom(value.getClass())) {
		// cell.setCellValue((Boolean) value);
		// } else {
		// final Date date = parseStringAsDate(value);
		// if (date != null) {
		// cell.setCellValue(date);
		// } else {
		// cell.setCellValue(String.valueOf(value));
		// }
		// }
	}

	protected void addHeaderStyles(final HeadExportColumn column, final Map<String, PdfCellStyle> headerCellStylesById,
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
		 * Si no hay ningún estilo de anotaciones tomamos el estilo de la columna Si hay
		 * alguno le asignamos los valores del estilo de la columna del styleProvider
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
			headerCellStylesById.put(column.getId(), this.createCellStyle(finalStyle, String.class));
		}
		/*
		 * Si la columna tiene columnas anidadas hacemos lo mismo con cada una
		 */
		if (column.getHeadExportColumnCount() > 0) {
			for (int n = 0; n < column.getHeadExportColumnCount(); n++) {
				this.addHeaderStyles(column.getHeadExportColumn(n), headerCellStylesById, styleProvider);
			}
		}
	}

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
