package com.ontimize.jee.webclient.export.style.util;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
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
        if (columnStyle.getFillBackgroundColor() != null) {
            cellStyle.setBackgroundColor(PdfCellStyleUtils.getBackgroundColor(columnStyle.getFillBackgroundColor()));
        }
        return cellStyle;
    }

    public static TextAlignment getHorizontalAlignment(final ExportColumnStyle.HorizontalAlignment alignment) {

        switch (alignment) {
            case LEFT:
                return TextAlignment.LEFT;
            case CENTER:
                return TextAlignment.CENTER;
            case RIGHT:
                return TextAlignment.RIGHT;
            case JUSTIFY:
                return TextAlignment.JUSTIFIED;
            case FILL:
                return TextAlignment.JUSTIFIED_ALL;
            default:
                return TextAlignment.RIGHT;
        }
    }

    public static VerticalAlignment getVerticalAlignment(final ExportColumnStyle.VerticalAlignment alignment) {

        switch (alignment) {
            case TOP:
                return VerticalAlignment.TOP;
            case CENTER:
                return VerticalAlignment.MIDDLE;
            case BOTTOM:
                return VerticalAlignment.BOTTOM;
            default:
                return VerticalAlignment.MIDDLE;
        }
    }

    public static Color getBackgroundColor(final ExportColumnStyle.CellColor color) {
        switch (color) {
            case AQUA:
                return new DeviceRgb(51, 204, 205);
            case AUTOMATIC:
            case BLACK:
            case BLACK1:
                return new DeviceRgb(0, 0, 0);
            case BLUE:
            case BLUE1:
                return new DeviceRgb(0, 0, 255);
            case BLUE_GREY:
                return new DeviceRgb(102, 102, 153);
            case BRIGHT_GREEN:
            case BRIGHT_GREEN1:
                return new DeviceRgb(0, 255, 0);
            case BROWN:
                return new DeviceRgb(150, 50, 0);
            case CORAL:
                return new DeviceRgb(255, 128, 128);
            case CORNFLOWER_BLUE:
                return new DeviceRgb(150, 150, 255);
            case DARK_BLUE:
                return new DeviceRgb(0, 0, 128);
            case DARK_GREEN:
                return new DeviceRgb(0, 50, 0);
            case DARK_RED:
                return new DeviceRgb(128, 0, 0);
            case DARK_TEAL:
                return new DeviceRgb(0, 50, 100);
            case DARK_YELLOW:
                return new DeviceRgb(128, 128, 0);
            case GOLD:
                return new DeviceRgb(255, 200, 0);
            case GREEN:
                return new DeviceRgb(0, 128, 0);
            case GREY_25_PERCENT:
                return new DeviceRgb(192, 192, 192);
            case GREY_40_PERCENT:
                return new DeviceRgb(150, 150, 150);
            case GREY_50_PERCENT:
                return new DeviceRgb(128, 128, 128);
            case GREY_80_PERCENT:
                return new DeviceRgb(51, 51, 51);
            case INDIGO:
                return new DeviceRgb(51, 51, 154);
            case LAVENDER:
                return new DeviceRgb(204, 153, 254);
            case LEMON_CHIFFON:
                return new DeviceRgb(255, 255, 205);
            case LIGHT_BLUE:
                return new DeviceRgb(51, 103, 255);
            case LIGHT_CORNFLOWER_BLUE:
                return new DeviceRgb(204, 204, 255);
            case LIGHT_GREEN:
                return new DeviceRgb(204, 255, 204);
            case LIGHT_ORANGE:
                return new DeviceRgb(255, 154, 0);
            case LIGHT_TURQUOISE:
            case LIGHT_TURQUOISE1:
                return new DeviceRgb(204, 255, 255);
            case LIGHT_YELLOW:
                return new DeviceRgb(255, 255, 151);
            case LIME:
                return new DeviceRgb(151, 203, 0);
            case MAROON:
                return new DeviceRgb(152, 51, 102);
            case OLIVE_GREEN:
                return new DeviceRgb(51, 51, 0);
            case ORANGE:
                return new DeviceRgb(204, 153, 254);
            case ORCHID:
                return new DeviceRgb(102, 0, 102);
            case PALE_BLUE:
                return new DeviceRgb(152, 204, 255);
            case PINK:
            case PINK1:
                return new DeviceRgb(255, 0, 255);
            case PLUM:
                return new DeviceRgb(153, 51, 102);
            case RED:
            case RED1:
                return new DeviceRgb(255, 0, 0);
            case ROSE:
                return new DeviceRgb(255, 153, 205);
            case ROYAL_BLUE:
                return new DeviceRgb(0, 102, 204);
            case SEA_GREEN:
                return new DeviceRgb(51, 151, 102);
            case SKY_BLUE:
                return new DeviceRgb(0, 204, 255);
            case TAN:
                return new DeviceRgb(255, 204, 153);
            case TEAL:
                return new DeviceRgb(0, 128, 128);
            case TURQUOISE:
            case TURQUOISE1:
                return new DeviceRgb(0, 255, 255);
            case VIOLET:
                return new DeviceRgb(128, 0, 128);
            case WHITE:
            case WHITE1:
                return new DeviceRgb(255, 255, 255);
            case YELLOW:
            case YELLOW1:
                return new DeviceRgb(255, 255, 0);
        }
        return null;
    }
}
