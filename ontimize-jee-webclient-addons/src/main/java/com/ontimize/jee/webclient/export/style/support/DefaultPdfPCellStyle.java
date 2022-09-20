package com.ontimize.jee.webclient.export.style.support;

import java.text.Format;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.ontimize.jee.webclient.export.style.PdfCellStyle;

public class DefaultPdfPCellStyle implements PdfCellStyle {

  private HorizontalAlignment horizontalAlignment;

  private VerticalAlignment verticalAlignment;

  private Color backgroundColor;

  private Format formatter;

  @Override
  public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
    this.horizontalAlignment = horizontalAlignment;
  }

  @Override
  public HorizontalAlignment getHorizontalAlignment() {
    return this.horizontalAlignment;
  }

  @Override
  public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
  }

  @Override
  public VerticalAlignment getVerticalAlignment() {
    return this.verticalAlignment;
  }

  @Override
  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  @Override
  public Color getBackgroundColor() {
    return this.backgroundColor;
  }

  @Override
  public Format getDataFormatter() {
    return this.formatter;
  }

  @Override
  public void setDataFormatter(final Format formatter) {
    this.formatter = formatter;
  }
}
