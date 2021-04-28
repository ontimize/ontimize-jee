package com.ontimize.jee.webclient.export.util;

import java.util.ArrayList;
import java.util.List;

import javafx.stage.FileChooser.ExtensionFilter;

/**
 * The Class ExtensionFilterUtils.
 *
 * @author <a href="daniel.grana@imatia.com">Daniel Grana</a>
 */
public class ExtensionFilterUtils {

    /**
     * Extensi&oacute;n para un archivo excel .xls.
     */
    public static final String XLS_FILE_EXTENSION = ".xls";

    /**
     * Extensi&oacute;n para un archivo excel .xlsx.
     */
    public static final String XLSX_FILE_EXTENSION = ".xlsx";

    /** Extensi&oacute;n para un archivo pdf .pdf. */
    public static final String PDF_FILE_EXTENSION = ".pdf";

    /*
     * Images
     */
    /** Extensi&oacute;n para un archivo imagen .png. */
    public static final String PNG_FILE_EXTENSION = ".png";

    /** Extensi&oacute;n para un archivo imagen .jpg. */
    public static final String JPG_FILE_EXTENSION = ".jpg";

    /** Extensi&oacute;n para un archivo imagen .gif. */
    public static final String GIF_FILE_EXTENSION = ".gif";

    /**
     * Extensi&oacute;n para un archivo de texto plano .txt.
     */
    public static final String TXT_FILE_EXTENSION = ".txt";

    /**
     * The Enum ExtensionFilterTypes.
     *
     * @author <a href="daniel.grana@imatia.com">Daniel Grana</a>
     */
    public static enum ExtensionFilterTypes {

        /** The text files. */
        TEXT_FILES,
        /** The excel files. */
        EXCEL_FILES,
        /** The pdf files. */
        PDF_FILES,
        /** The image files. */
        IMAGE_FILES,
        /** The all files. */
        ALL_FILES;

    }

    private static final String FILTER_EXTENSION_PREFIX = "*";

    /** The Constant ALL_FILES_FILTER_EXTENSION. */
    public static final String ALL_FILES_FILTER_EXTENSION = "*.*";

    /** The Constant allExtensionsFilter. */
    public static final ExtensionFilter allExtensionsFilter = new ExtensionFilter("All Files", "*.*");

    /**
     * Obtiene extension filter.
     * @param type type
     * @return extension filter
     */
    public static ExtensionFilter[] getExtensionFilter(final ExtensionFilterTypes type) {
        ExtensionFilter[] filters;
        switch (type) {
            case TEXT_FILES:
                filters = new ExtensionFilter[] {
                        new ExtensionFilter("Txt Files", getExtensions(ExtensionFilterTypes.TEXT_FILES)),
                        allExtensionsFilter
                };
                break;
            case EXCEL_FILES:
                filters = new ExtensionFilter[] {
                        new ExtensionFilter("Excel Files", getExtensions(ExtensionFilterTypes.EXCEL_FILES)),
                        allExtensionsFilter
                };
                break;
            case PDF_FILES:
                filters = new ExtensionFilter[] {
                        new ExtensionFilter("Pdf Files", getExtensions(ExtensionFilterTypes.PDF_FILES)),
                        allExtensionsFilter
                };
                break;
            case IMAGE_FILES:
                filters = new ExtensionFilter[] {
                        new ExtensionFilter("Image Files", getExtensions(ExtensionFilterTypes.IMAGE_FILES)),
                        allExtensionsFilter
                };
                break;
            default:
                filters = new ExtensionFilter[] {
                        allExtensionsFilter
                };
                break;
        }

        return filters;
    }

    /**
     * Obtiene extensions.
     * @param type type
     * @return extensions
     */
    static List<String> getExtensions(final ExtensionFilterTypes type) {
        final List<String> extensions = new ArrayList<>();
        switch (type) {
            case TEXT_FILES:
                extensions.add(FILTER_EXTENSION_PREFIX.concat(TXT_FILE_EXTENSION));
                break;
            case EXCEL_FILES:
                extensions.add(FILTER_EXTENSION_PREFIX.concat(XLSX_FILE_EXTENSION));
                break;
            case PDF_FILES:
                extensions.add(FILTER_EXTENSION_PREFIX.concat(PDF_FILE_EXTENSION));
                break;
            case IMAGE_FILES:
                extensions.add(FILTER_EXTENSION_PREFIX.concat(PNG_FILE_EXTENSION));
                extensions.add(FILTER_EXTENSION_PREFIX.concat(JPG_FILE_EXTENSION));
                extensions.add(FILTER_EXTENSION_PREFIX.concat(GIF_FILE_EXTENSION));
                break;
            default:
                extensions.add(ALL_FILES_FILTER_EXTENSION);
                break;
        }
        return extensions;
    }

}
