package com.ontimize.jee.webclient.export.style.support;

import java.text.Format;

import com.itextpdf.text.BaseColor;
import com.ontimize.jee.webclient.export.style.PdfCellStyle;

public class DefaultPdfPCellStyle implements PdfCellStyle {

  private Integer horizontalAlignment;

  private Integer verticalAlignment;

  private BaseColor backgroundColor;

  private Format formatter;

  @Override
  public void setHorizontalAlignment(Integer horizontalAlignment) {
    this.horizontalAlignment = horizontalAlignment;
  }

  @Override
  public Integer getHorizontalAlignment() {
    return this.horizontalAlignment;
  }

  @Override
  public void setVerticalAlignment(Integer verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
  }

  @Override
  public Integer getVerticalAlignment() {
    return this.verticalAlignment;
  }

  @Override
  public void setBackgroundColor(BaseColor backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  @Override
  public BaseColor getBackgroundColor() {
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
