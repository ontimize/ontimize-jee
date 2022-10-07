package com.ontimize.jee.webclient.export.style;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.text.Format;

public interface PdfCellStyle {

    void setHorizontalAlignment(TextAlignment horizontalAlignment);

    TextAlignment getHorizontalAlignment();

    void setVerticalAlignment(VerticalAlignment verticalAlignment);

    VerticalAlignment getVerticalAlignment();

    void setBackgroundColor(Color backgroundColor);

    Color getBackgroundColor();

    Format getDataFormatter();

    void setDataFormatter(final Format formatter);
}
