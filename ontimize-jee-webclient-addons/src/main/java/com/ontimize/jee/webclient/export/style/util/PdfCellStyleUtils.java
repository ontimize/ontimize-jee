package com.ontimize.jee.webclient.export.style.util;

import com.itextpdf.text.Element;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.style.PdfCellStyle;
import com.ontimize.jee.webclient.export.style.support.DefaultPdfPCellStyle;

public class PdfCellStyleUtils {

  public static PdfCellStyle createPdfCellStyle(final ExportColumnStyle columnStyle) {
    DefaultPdfPCellStyle cellStyle = new DefaultPdfPCellStyle();
    if (columnStyle == null) {
      return null;
    }
    if (columnStyle.getAlignment() != null) {
      cellStyle.setHorizontalAlignment(PdfCellStyleUtils.getHorizontalAlignment(columnStyle.getHorizontalAlignment()));
    }
    if (columnStyle.getVerticalAlignment() != null) {
      cellStyle.setVerticalAlignment(PdfCellStyleUtils.getVerticalAlignment(columnStyle.getVerticalAlignment()));
    }
    //FIXME Añadir lógica para el background color
    return cellStyle;
  }

  public static int getHorizontalAlignment(final ExportColumnStyle.HorizontalAlignment alignment) {

    switch (alignment) {
      case LEFT:
        return Element.ALIGN_LEFT;
      case CENTER:
        return Element.ALIGN_CENTER;
      case RIGHT:
        return Element.ALIGN_RIGHT;
      case JUSTIFY:
        return Element.ALIGN_JUSTIFIED;
      default:
        return Element.ALIGN_UNDEFINED;
    }
  }

  public static int getVerticalAlignment(final ExportColumnStyle.VerticalAlignment alignment) {

    switch (alignment) {
      case TOP:
        return Element.ALIGN_TOP;
      case CENTER:
        return Element.ALIGN_MIDDLE;
      case BOTTOM:
        return Element.ALIGN_BOTTOM;
      case JUSTIFY:
        return Element.ALIGN_JUSTIFIED;
      default:
        return Element.ALIGN_UNDEFINED;
    }
  }
}
