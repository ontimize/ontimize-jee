package com.ontimize.jee.webclient.export.style.support;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.ontimize.jee.webclient.export.style.PdfCellStyle;

import java.text.Format;

public class DefaultPdfPCellStyle implements PdfCellStyle {

    private TextAlignment horizontalAlignment;

    private VerticalAlignment verticalAlignment;

    private Color backgroundColor;

    private Format formatter;

    @Override
    public void setHorizontalAlignment(TextAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    @Override
    public TextAlignment getHorizontalAlignment() {
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

    @Override
    public void cloneStyleFrom(PdfCellStyle source) {
        if (source != null) {
            this.setBackgroundColor(source.getBackgroundColor());
            this.setHorizontalAlignment(source.getHorizontalAlignment());
            this.setVerticalAlignment(source.getVerticalAlignment());
            this.setDataFormatter(source.getDataFormatter());
        }
    }
}
