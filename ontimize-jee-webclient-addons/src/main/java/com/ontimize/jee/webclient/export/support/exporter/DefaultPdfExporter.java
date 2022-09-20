package com.ontimize.jee.webclient.export.support.exporter;

import java.io.File;
import java.io.FileNotFoundException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.ontimize.jee.webclient.export.exception.ExportException;

/**
 * Exportador a documentos PDF
 */
public class DefaultPdfExporter extends BasePdfExporter<Document> {

  public DefaultPdfExporter(final File pdfFile) {
    super(pdfFile);
  }

  @Override
  protected Document buildDocument() throws ExportException {
    try {
      PdfDocument pdfDoc = new PdfDocument(new PdfWriter(this.getPdfFile()));
      return new Document(pdfDoc);
    } catch (FileNotFoundException e) {
      throw new ExportException(e);
    }
  }

}
