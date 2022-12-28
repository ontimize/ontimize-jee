package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;
import com.ontimize.jee.webclient.export.support.BaseExportColumnProvider;
import com.ontimize.jee.webclient.export.support.exporter.DefaultXSSFExcelExporter;
import com.ontimize.jee.webclient.export.support.sheetnameprovider.DefaultSheetNameProvider;
import com.ontimize.jee.webclient.export.support.styleprovider.DefaultExcelExportStyleProvider;
import com.ontimize.jee.webclient.export.util.ExportOptions;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio de exportación en formato Excel.
 *
 * @author <a href="antonio.vazquez@imatia.com">Antonio Vazquez Araujo</a>
 */
@Service("ExcelExportService")
public class ExcelExportService extends BaseExportService implements IExcelExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportService.class);

    // ColumnProvider
    private ExportColumnProvider columnProvider;

    // StyleProvider
    private ExportStyleProvider styleProvider;

    private SheetNameProvider sheetNameProvider;

    public ExportColumnProvider getColumnProvider() {
        return columnProvider;
    }

    public ExportStyleProvider getStyleProvider() {
        return styleProvider;
    }

    public SheetNameProvider getSheetNameProvider() {
        return sheetNameProvider;
    }

    @Override
    public File generateFile(final ExportQueryParameters exportParam) throws ExportException {
        File xlsxFile = null;
        try {
            if (!(exportParam instanceof AdvancedExportQueryParameters)) {
                throw new IllegalArgumentException();
            }
            AdvancedExportQueryParameters excelExportParam = (AdvancedExportQueryParameters) exportParam;
            xlsxFile = createTempFile(".xlsx");
            generateExcel(excelExportParam, xlsxFile);
        } catch (final ExportException e) {
            throw e;
        } catch (IOException e) {
            throw new ExportException("Error creating xlsx file!", e);
        } catch (IllegalArgumentException e) {
            throw new ExportException("Invalid export configuration parameters", e);
        }
        return xlsxFile;
    }

    /**
     * Create all the providers and generate the book
     *
     * @param exportParam The export configuration parameters
     * @param xlsxFile    The tempfile
     */
    public void generateExcel(final AdvancedExportQueryParameters exportParam, File xlsxFile) throws ExportException {
        try (final FileOutputStream fileOutputStream = new FileOutputStream(xlsxFile);) {
            // ColumnProvider
            ExportColumnProvider columnProvider = getColumnProvider();

            // DataProvider
            ExportDataProvider dataProvider = getDataProvider();

            // StyleProvider
            ExportStyleProvider styleProvider = getStyleProvider();

            // SheetNameProvider
            SheetNameProvider sheetNameProvider = getSheetNameProvider();

            // ExportOptions
            ExportOptions exportOptions = this.createExportOptions(exportParam, new ArrayList<>() /*orderColumns*/);

            final Workbook book = generateBook(columnProvider, dataProvider, styleProvider, sheetNameProvider,
                    exportOptions);

            book.write(fileOutputStream);
        } catch (final ExportException e) {
            logger.error("{}", e.getMessage(), e);
            throw e;
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
            throw new ExportException("Error filling export file", e);
        }
    }

    /**
     * Calls the exporter and send it the providers to build the excel file
     *
     * @param columnProvider    The column provider
     * @param dataProvider      The data provider
     * @param styleProvider     The styles provider
     * @param sheetNameProvider The sheet name provider
     * @param exportOptions     Is null, it creates it in the BaseExcelExporter
     * @return
     * @throws Exception
     */
    public Workbook generateBook(ExportColumnProvider columnProvider, ExportDataProvider dataProvider,
                                 ExportStyleProvider<XSSFCellStyle, DataFormat> styleProvider, SheetNameProvider sheetNameProvider,
                                 ExportOptions exportOptions) throws ExportException {
        return new DefaultXSSFExcelExporter().export(columnProvider, dataProvider, styleProvider, sheetNameProvider,
                exportOptions);
    }

    @Override
    protected void createProviders(ExportQueryParameters exportParam) throws ExportException {
        super.createProviders(exportParam);

        if (exportParam instanceof AdvancedExportQueryParameters) {
            AdvancedExportQueryParameters excelExportParam = (AdvancedExportQueryParameters) exportParam;
            // create specific providers...
            this.createXlsxProviders(excelExportParam);
        }
    }

    public void createXlsxProviders(final AdvancedExportQueryParameters excelExportParam) {
        this.columnProvider = createColumnProvider(excelExportParam);
        this.styleProvider = createStyleProvider(excelExportParam);
        this.sheetNameProvider = createDefaultSheetNameProvider();
    }

    /**
     * Creamos un columnProvider que extrae las columnas de la cabecera por una parte y las columnas de
     * datos por otra. Para ello usa el algoritmo recursivo addParentColumn, que agrega a la lista una
     * columna y todas sus hijas en profundidad.
     */
    protected ExportColumnProvider createColumnProvider(final AdvancedExportQueryParameters exportParam) {
        List<HeadExportColumn> headerColumns = new ArrayList<>();
        List<ExportColumn> bodyColumns = new ArrayList<>();
        Map<String, Object> columns = exportParam.getColumns();
        Map<String, String> columnTitles = exportParam.getColumnTitles();
        addChildrenColumns(bodyColumns, headerColumns, columns, columnTitles);
        ExportColumnProvider ret = new BaseExportColumnProvider(headerColumns, bodyColumns);
        return ret;
    }

    protected ExportStyleProvider createStyleProvider(final AdvancedExportQueryParameters exportParam) {
        return new DefaultExcelExportStyleProvider(exportParam);
    }

    protected SheetNameProvider createDefaultSheetNameProvider() {
        return new DefaultSheetNameProvider();
    }

    protected ExportOptions createExportOptions(final AdvancedExportQueryParameters exportParam, List<String> columns) {
        // TODO: de momento no hay opciones
        return null;
    }

}
