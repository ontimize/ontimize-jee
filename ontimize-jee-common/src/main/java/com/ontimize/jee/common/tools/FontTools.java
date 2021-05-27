/*
 *
 */
package com.ontimize.jee.common.tools;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidades para trabajar con fuentes.
 */
public class FontTools {

    private static final Logger logger = LoggerFactory.getLogger(FontTools.class);

    /**
     * Instantiates a new font utils.
     */
    private FontTools() {
        super();
    }

    /**
     * Comprueba si una familia de fuentes esta disponible en el sistema operativo.
     * @param fontFamilyName the font family name
     * @throws Exception the exception
     */
    public static void checkFont(String fontFamilyName) throws Exception {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fontNames[] = ge.getAvailableFontFamilyNames();
        if (Arrays.binarySearch(fontNames, fontFamilyName) < 0) {
            throw new Exception("NO_FONT_AVAILABLE");
        }
    }

    /**
     * Imprime las fuentes disponibles.
     */
    public static void printAvailableFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fontNames[] = ge.getAvailableFontFamilyNames();
        for (int i = 0; i < fontNames.length; i++) {
            FontTools.logger.info(fontNames[i]);
        }
    }

    /**
     * Instala las fuentes en el directorio de la maquina virtual.
     * @param fileName the file name
     * @param font the font
     * @throws Exception the exception
     */
    public static void installFont(String fileName, InputStream font) throws Exception {
        String path = System.getProperty("sun.boot.library.path");
        path = path.substring(0, path.lastIndexOf(FileTools.getFileSeparator()));
        path = path + FileTools.getFileSeparator() + "lib" + FileTools.getFileSeparator() + "fonts"
                + FileTools.getFileSeparator();
        File fdest = new File(path + fileName);
        if (fdest.exists()) {
            fdest.delete();
        }
        fdest.createNewFile();
        FileOutputStream fos = new FileOutputStream(fdest);
        FileTools.copyFile(font, fos);
        fos.close();
    }

    /**
     * Registra una fuente en la sesion actual de la maquina virtual.
     * @param fontFormat the font format
     * @param fontFile the font file
     * @throws FontFormatException the font format exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void registerFont(int fontFormat, InputStream fontFile) throws FontFormatException, IOException {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
    }

}
