package com.ontimize.jee.webclient.export.style;

import java.text.Format;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

public interface PdfCellStyle {

  void setHorizontalAlignment(HorizontalAlignment horizontalAlignment);

  HorizontalAlignment getHorizontalAlignment();

  void setVerticalAlignment(VerticalAlignment verticalAlignment);

  VerticalAlignment getVerticalAlignment();

  void setBackgroundColor(Color backgroundColor);

  Color getBackgroundColor();

  Format getDataFormatter();

  void setDataFormatter(final Format formatter);
}
