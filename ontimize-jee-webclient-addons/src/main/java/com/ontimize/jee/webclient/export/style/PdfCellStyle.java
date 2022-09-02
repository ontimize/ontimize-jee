package com.ontimize.jee.webclient.export.style;

import java.text.Format;

import com.itextpdf.text.BaseColor;

public interface PdfCellStyle {

  void setHorizontalAlignment(Integer horizontalAlignment);

  Integer getHorizontalAlignment();

  void setVerticalAlignment(Integer verticalAlignment);

  Integer getVerticalAlignment();

  void setBackgroundColor(BaseColor backgroundColor);

  BaseColor getBackgroundColor();

  Format getDataFormatter();

  void setDataFormatter(final Format formatter);
}
