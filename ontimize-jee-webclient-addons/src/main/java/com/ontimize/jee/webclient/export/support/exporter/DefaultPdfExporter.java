package com.ontimize.jee.webclient.export.support.exporter;

import com.itextpdf.text.Document;
import com.ontimize.jee.webclient.export.exception.ExportException;

import java.io.File;

/**
 * Exportador a documentos PDF
 */
public class DefaultPdfExporter extends BasePdfExporter<Document> {

    public DefaultPdfExporter(final File pdfFile) {
        super(pdfFile);
    }

    @Override
    protected Document buildDocument() throws ExportException {
        return new Document();
    }

}
