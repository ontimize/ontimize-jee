package com.ontimize.jee.webclient.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ontimize.dto.EntityResult;
import com.ontimize.util.xls.XLSExporter;
import com.ontimize.util.xls.XLSExporterFactory;

@Service("ExportService")
public class ExportService implements IExportService {

    @Override
    public File xlsxExport(EntityResult data, List<String> columns) throws Exception {
        XLSExporter xlsExporter = XLSExporterFactory.instanceXLSExporter(XLSExporterFactory.POI_3_5);

        File xlsxFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".xlsx");

        xlsExporter.createXLS(data, xlsxFile, null, new HashMap<>(), columns, true, true, false);

        return xlsxFile;
    }

    @Override
    public File htmlExport(EntityResult data, List<String> columns) throws IOException {
        // Code inspired by getHTMLString method from com.ontimize.gui.table.Table
        // component

        // Create an String with the table data
        // Export to HTML:
        String tagEnd = "</TABLE></BODY></HTML>";
        StringBuilder sbHeader = new StringBuilder("<HTML><HEAD></HEAD><BODY><TABLE border='1'><TR>");
        for (int i = 0; i < columns.size(); i++) {
            sbHeader.append("<TH>" + columns.get(i) + "</TH>");
        }

        for (int j = 0; j < data.calculateRecordNumber(); j++) {
            Map record = data.getRecordValues(j);
            StringBuilder sbRow = new StringBuilder("<TR>");
            for (int i = 0; i < columns.size(); i++) {
                Object sText = record.get(columns.get(i));
                if (sText != null) {
                    sbRow.append("<TD>");
                    sbRow.append(sText);
                    sbRow.append("</TD>");
                }
            }
            sbRow.append("</TR>\n");
            sbHeader.append(sbRow.toString());
        }

        String htmlStringValue = sbHeader.toString() + tagEnd;

        File htmlFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".html");
        FileWriter fw = new FileWriter(htmlFile);
        fw.write(htmlStringValue, 0, htmlStringValue.length());
        fw.flush();
        fw.close();

        return htmlFile;
    }

    @Override
    public File pdfExport(EntityResult data, List<String> columns) throws Exception {
        File pdfFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".pdf");

        // Create document on landscape mode
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Create table
        PdfPTable table = new PdfPTable(columns.size());
        table.setWidthPercentage(100);

        // Add data to table
        for (int i = 0; i < columns.size(); i++) {
            PdfPCell cell = new PdfPCell(new Phrase(columns.get(i)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        table.setHeaderRows(1);
        for (int i = 0; i < data.calculateRecordNumber(); i++) {
            Map record = data.getRecordValues(i);
            for (int j = 0; j < columns.size(); j++) {
                String sText = record.get(columns.get(j)).toString();
                PdfPCell cell = new PdfPCell(new Phrase(sText));
                table.addCell(cell);
            }
        }

        // Add table to document and close it
        document.add(table);
        document.close();

        return pdfFile;
    }

}
