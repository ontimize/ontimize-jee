package com.ontimize.jee.webclient.export.base;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;
import com.ontimize.jee.webclient.export.support.BaseExportColumnProvider;
import com.ontimize.jee.webclient.export.support.DefaultHeadExportColumn;
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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio de exportación en formato CSV. 
 *
 */
@Service("CsvExportService")
public class CsvExportService extends BaseExportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvExportService.class);

    @Override
    public File generateFile(final ExportQueryParameters exportParam) throws ExportException {
        File csvFile = null;
        try {
            csvFile = createTempFile(".csv");
            generateCsv(exportParam, csvFile);
        } catch (IOException e) {
            logger.error("{}", e.getMessage(), e);
            throw new ExportException("Error creating csv file!", e);
        }
        return csvFile;
    }
    
    

    /**
     * Create all the providers and generate the book
     * @param exportParam The export configuration parameters
     * @param csvFile The tempfile
     */
    public void generateCsv(final ExportQueryParameters exportParam, File csvFile) throws ExportException {
        try {
            // DataProvider
            ExportDataProvider<EntityResult> dataProvider = getDataProvider();

            dataProvider.doQuery();
            EntityResult data = dataProvider.getData();
            List<Object> columns = exportParam.getQueryParam().getColumns();
            String content = getCsvContent(data,columns);

            Files.write(Paths.get(csvFile.getAbsolutePath()), content.getBytes(StandardCharsets.UTF_8));
            
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
            throw new ExportException("Error filling export file", e);
        }
    }

    public String getCsvContent(EntityResult data, List<Object> columns) throws Exception {
        StringBuilder body = new StringBuilder();

        if(data != null) {
            for (int j = 0; j < data.calculateRecordNumber(); j++) {
                Map record = data.getRecordValues(j);
                StringBuilder sbRow = new StringBuilder();
                for (int i = 0; i < columns.size(); i++) {
                    Object sText = record.get(columns.get(i));
                    sbRow.append(sText != null ? sText : "");
                    sbRow.append(";");
                }
                sbRow.append("\n");
                body.append(sbRow.toString());
            }
        }
        return body.toString();
    }

}
