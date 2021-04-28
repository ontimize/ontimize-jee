package com.ontimize.jee.webclient.export.executor.support;


import org.apache.poi.ss.usermodel.CellStyle;

import com.ontimize.jee.webclient.export.ExportColumnStyle;

public class DefaultExportColumnStyle implements ExportColumnStyle {

    private String dataFormatString;

    private HorizontalAlignment alignment;

    private VerticalAlignment verticalAlignment;

    private CellColor fillBackgroundColor;

    private int width = -1;

    @Override
    public DefaultExportColumnStyle set(final ExportColumnStyle style) {
        if (style == null) {
            return this;
        }
        if (style.getAlignment() != null) {
            this.setAlignment(style.getAlignment());
        }
        if (style.getFillBackgroundColor() != null) {
            this.setFillBackgroundColor(style.getFillBackgroundColor());
        }
        if (style.getDataFormatString() != null) {
            this.setDataFormatString(style.getDataFormatString());
        }
        if (style.getVerticalAlignment() != null) {
            this.setVerticalAlignment(style.getVerticalAlignment());
        }
        if (style.getWidth() != -1) {
            this.setWidth(style.getWidth());
        }
        return this;
    }

    @Override
    public void reset() {
        this.setDataFormatString(null);
        this.setAlignment(null);
        this.setVerticalAlignment(null);
        this.setFillBackgroundColor(null);
    }

    @Override
    public String getDataFormatString() {
        return this.dataFormatString;
    }

    @Override
    public void setDataFormatString(final String dataFormat) {
        this.dataFormatString = dataFormat;
    }

    @Override
    public HorizontalAlignment getAlignment() {
        return this.alignment;
    }

    @Override
    public void setAlignment(final HorizontalAlignment align) {
        this.alignment = align;
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        return this.verticalAlignment;
    }

    @Override
    public void setVerticalAlignment(final VerticalAlignment align) {
        this.verticalAlignment = align;
    }

    @Override
    public CellColor getFillBackgroundColor() {
        return this.fillBackgroundColor;
    }

    @Override
    public void setFillBackgroundColor(final CellColor bg) {
        this.fillBackgroundColor = bg;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public void setWidth(final int width) {
        this.width = width;
    }

    public static ExportColumnStyle createFromCellstyle(CellStyle style) {
        DefaultExportColumnStyle ret = new DefaultExportColumnStyle();
        ret.setAlignment(HorizontalAlignment.forInt(style.getAlignment().getCode()));
        ret.setVerticalAlignment(VerticalAlignment.forInt(style.getAlignment().getCode()));
        ret.setDataFormatString(style.getDataFormatString());
        short colIndex = style.getFillBackgroundColor();
        CellColor cellColor = ExportColumnStyle.CellColor.forInt((colIndex));
        ret.setFillBackgroundColor(ExportColumnStyle.CellColor.forInt(cellColor.getIndex()));
        // TODO Pending size??
        return ret;
    }

}
