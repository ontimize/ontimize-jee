package com.ontimize.jee.webclient.export.base;

import com.itextpdf.layout.Document;
import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.style.PdfCellStyle;
import com.ontimize.jee.webclient.export.style.PdfDataFormat;
import com.ontimize.jee.webclient.export.support.BaseExportColumnProvider;
import com.ontimize.jee.webclient.export.support.DefaultHeadExportColumn;
import com.ontimize.jee.webclient.export.support.exporter.DefaultPdfExporter;
import com.ontimize.jee.webclient.export.support.styleprovider.DefaultPdfExportStyleProvider;
import com.ontimize.jee.webclient.export.util.ExportOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio de exportación en formato CSV.
 */
@Service("PdfExportService")
public class PdfExportService extends BaseExportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);

    // ColumnProvider
    private ExportColumnProvider columnProvider;

    // StyleProvider
    private ExportStyleProvider styleProvider;

    public ExportColumnProvider getColumnProvider() {
        return columnProvider;
    }

    public ExportStyleProvider getStyleProvider() {
        return styleProvider;
    }

    @Override
    public File generateFile(final ExportQueryParameters exportParam) throws ExportException {
        File pdfFile = null;
        try {
            if (!(exportParam instanceof ExcelExportQueryParameters)) {
                throw new IllegalArgumentException();
            }
            //TODO refactorizar el nombre a ExcelExportQueryParameters
            ExcelExportQueryParameters excelExportParam = (ExcelExportQueryParameters) exportParam;
            pdfFile = createTempFile(".pdf");
            generatePdf(excelExportParam, pdfFile);
        } catch (final ExportException e) {
            throw e;
        } catch (IOException e) {
            logger.error("{}", e.getMessage(), e);
            throw new ExportException("Error creating PDF file!", e);
        } catch (IllegalArgumentException e) {
            throw new ExportException("Invalid export configuration parameters", e);
        }
        return pdfFile;
    }

    /**
     * Create all the providers and generate the book
     *
     * @param exportParam The export configuration parameters
     * @param pdfFile     The tempfile
     */
    public void generatePdf(final ExcelExportQueryParameters exportParam, File pdfFile) throws ExportException {
        try {
            // ColumnProvider
            ExportColumnProvider columnProvider = getColumnProvider();

            // DataProvider
            ExportDataProvider dataProvider = getDataProvider();

            // StyleProvider
            ExportStyleProvider styleProvider = getStyleProvider();

            // ExportOptions
            ExportOptions exportOptions = this.createExportOptions(exportParam, new ArrayList<>() /*orderColumns*/);

            generatePdfDocument(pdfFile, columnProvider, dataProvider, styleProvider, exportOptions);

        } catch (final ExportException e) {
            logger.error("{}", e.getMessage(), e);
            throw e;
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
            throw new ExportException("Error filling export file", e);
        }
    }

    /**
     * Calls the exporter and send it the providers to build the pdf file
     *
     * @param pdfFile        The file
     * @param columnProvider The column provider
     * @param dataProvider   The data provider
     * @param styleProvider  The styles provider
     * @param exportOptions  Is null, it creates it in the BaseExcelExporter
     */
    public Document generatePdfDocument(File pdfFile, ExportColumnProvider columnProvider, ExportDataProvider dataProvider,
                                        ExportStyleProvider<PdfCellStyle, PdfDataFormat> styleProvider, ExportOptions exportOptions) throws ExportException {
        return new DefaultPdfExporter(pdfFile).export(columnProvider, dataProvider, styleProvider, exportOptions);
    }

    @Override
    protected void createProviders(ExportQueryParameters exportParam) throws ExportException {
        super.createProviders(exportParam);

        if (exportParam instanceof ExcelExportQueryParameters) {
            ExcelExportQueryParameters excelExportParam = (ExcelExportQueryParameters) exportParam;
            // create specific providers...
            this.createPdfProviders(excelExportParam);
        }
    }

    public void createPdfProviders(final ExcelExportQueryParameters excelExportParam) {
        this.columnProvider = createColumnProvider(excelExportParam);
        this.styleProvider = createStyleProvider(excelExportParam);
    }

    /**
     * Creamos un columnProvider que extrae las columnas de la cabecera por una parte y las columnas de datos por otra. Para ello usa el
     * algoritmo recursivo addParentColumn, que agrega a la lista una columna y todas sus hijas en profundidad.
     */
    protected ExportColumnProvider createColumnProvider(final ExcelExportQueryParameters exportParam) {
        List<HeadExportColumn> headerColumns = new ArrayList<>();
        List<ExportColumn> bodyColumns = new ArrayList<>();
        Map<String, Object> columns = exportParam.getExcelColumns();
        Map<String, String> columnTitles = exportParam.getColumnTitles();
        addChildrenColumns(bodyColumns, headerColumns, columns, columnTitles);
        ExportColumnProvider ret = new BaseExportColumnProvider(headerColumns, bodyColumns);
        return ret;
    }

    protected ExportStyleProvider createStyleProvider(final ExcelExportQueryParameters exportParam) {
        return new DefaultPdfExportStyleProvider(exportParam);
    }

    //FIXME duplicado de ExcelService
    static void addParentColumn(List<ExportColumn> bodyColumns, List<HeadExportColumn> columns, String id, Object value,
                                Map<String, String> columnTitles) {

        HeadExportColumn column = new DefaultHeadExportColumn(id, columnTitles.get(id));
        Map<String, Object> children = (Map<String, Object>) value;
        // Si la columna no tiene hijos, la agregamos directamente
        if (value == null || ((Map<String, Object>) value).size() == 0) {
            bodyColumns.add(new ExportColumn(id, columnTitles.get(id), 0, null));
        } else {
            // Si la columna tiene hijos, le agregamos todos sus hijos
            column.getColumns().addAll(addChildrenColumns(bodyColumns, new ArrayList(), children, columnTitles));
        }
        columns.add(column);
    }

    static List<HeadExportColumn> addChildrenColumns(List<ExportColumn> bodyColumns, List<HeadExportColumn> columns,
                                                     Map<String, Object> children, Map<String, String> columnTitles) {
        children.entrySet().forEach(entry -> {
            addParentColumn(bodyColumns, columns, entry.getKey(), entry.getValue(), columnTitles);
        });
        return columns;
    }

    protected ExportOptions createExportOptions(final ExcelExportQueryParameters exportParam, List<String> columns) {
        // TODO: de momento no hay opciones
        return null;
    }
}
