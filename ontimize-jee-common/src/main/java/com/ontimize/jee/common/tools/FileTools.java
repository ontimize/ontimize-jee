package com.ontimize.jee.common.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * The Class FileTools.
 */
public final class FileTools {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(FileTools.class);

    private static final int BUFFER_SIZE = 4096;
    public static final String SOURCE = "Source '";
    public static final String ENCODING = "UTF-8";

    /**
     * Instantiates a new file tools.
     */
    private FileTools() {
        super();
    }

    /**
     * Delete file quitely.
     * @param file the file
     */
    public static void deleteQuitely(Path file) {
        if (file == null) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            FileTools.logger.error("could not remove file {}", file, e);
            file.toFile().deleteOnExit();
        }
    }

    /**
     * To path.
     * @param resFile the res file
     * @return the list
     */
    public static List<Path> toPath(List<File> resFile) {
        List<Path> res = new ArrayList<>(resFile.size());
        for (File file : resFile) {
            res.add(file.toPath());
        }
        return res;
    }

    /**
     * Copia un fichero en otro destino.
     * @param org the org
     * @param dst the dst
     * @throws IOException
     * @throws Exception the exception
     */
    public static void copyFile(String org, String dst) throws IOException {
        File fcorig = new File(org);
        File fcdst = new File(dst);
        FileTools.copyFile(fcorig, fcdst);
    }

    /**
     * Copia un fichero en otro destino.
     * @param fcorig the fcorig
     * @param fcdst the fcdst
     * @throws IOException
     * @throws Exception the exception
     */
    public static void copyFile(File fcorig, File fcdst) throws IOException {
        FileTools.copyFile(fcorig, fcdst, true);
    }

    public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcFile.exists()) {
            throw new FileNotFoundException(SOURCE + srcFile + "' does not exist");
        }
        if (srcFile.isDirectory()) {
            throw new IOException(SOURCE + srcFile + "' exists but is a directory");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException(SOURCE + srcFile + "' and destination '" + destFile + "' are the same");
        }
        if ((destFile.getParentFile() != null) && !destFile.getParentFile().exists()
                && !destFile.getParentFile().mkdirs()) {
            throw new IOException("Destination '" + destFile + "' directory cannot be created");
        }
        if (destFile.exists() && !destFile.canWrite()) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        try (InputStream is = new FileInputStream(srcFile); OutputStream os = new FileOutputStream(destFile)) {
            FileTools.copyFile(is, os);
            if (srcFile.length() != destFile.length()) {
                throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
            }

            if (preserveFileDate && !destFile.setLastModified(srcFile.lastModified())) {
                logger.error("Could not set last modified to {}", destFile.getName());
            }
        }
    }

    /**
     * Envia un {@link InputStream} a un {@link OutputStream}.
     * @param is the is
     * @param os the os
     * @throws IOException
     * @throws Exception the exception
     */
    public static void copyFile(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[FileTools.BUFFER_SIZE];
        int leido = 0;
        while ((leido = is.read(buffer)) != -1) {
            os.write(buffer, 0, leido);
        }
    }

    /**
     * Envia un {@link InputStream} a un {@link OutputStream}.
     * @param is the is
     * @param os the os
     * @throws IOException
     * @throws Exception the exception
     */
    public static void copyFile(InputStream is, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        FileTools.copyFile(is, fos);
        fos.close();
    }

    /**
     * Borra un directorio y todos los ficheros que contenga.
     * @param path the path
     * @return true, if successful
     */
    public static boolean deleteDirectory(File path) throws IOException {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    FileTools.deleteDirectory(files[i]);
                } else {
                    Files.deleteIfExists(files[i].toPath());
                }
            }
        }
        return Files.deleteIfExists(path.toPath());
    }

    /**
     * Mueve un fichero a otra localizacion.
     * @param org the org
     * @param dst the dst
     * @throws IOException
     * @throws Exception the exception
     */
    public static void moveFile(String org, String dst) throws IOException {
        FileTools.copyFile(org, dst);
        File fcorig = new File(org);
        Files.deleteIfExists(fcorig.toPath());
    }

    /**
     * Mueve un fichero a otra localizacion.
     * @param fcorg the fcorg
     * @param fcdst the fcdst
     * @throws Exception the exception
     */
    public static void moveFile(File fcorg, File fcdst) throws IOException {
        FileTools.copyFile(fcorg, fcdst);
        Files.deleteIfExists(fcorg.toPath());
    }

    /**
     * Devuelve el sepadarod de directorios del sistema operativo.
     * @return the file separator
     */
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }

    /**
     * Carga un fichero en un array de bytes.
     * @param f the f
     * @return the bytes from file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static byte[] getBytesFromFile(File f) throws IOException {
        try (InputStream in = new FileInputStream(f)){
            return FileTools.getBytesFromFile(in);
        }
    }

    /**
     * Carga un inputStream en un array de bytes.
     * @param in the in
     * @return the bytes from file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static byte[] getBytesFromFile(InputStream in) throws IOException {
        byte[] buffer = new byte[2048];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int leidos = 0;
        while ((leidos = in.read(buffer)) != -1) {
            out.write(buffer, 0, leidos);
        }
        in.close();
        out.close();
        return out.toByteArray();
    }

    /**
     * Marca un fichero/directorio para borrarse al cerrar la aplicacion.
     * @param path the path
     */
    public static void deleteDirectoryOnExit(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    FileTools.deleteDirectoryOnExit(files[i]);
                } else {
                    files[i].deleteOnExit();
                }
            }
        }
        path.deleteOnExit();
    }

    /**
     * To temporary file.
     * @param is the is
     * @param string
     * @return the file
     * @throws Exception the exception
     */
    public static File toTemporaryFile(InputStream is, String extension) throws IOException {
        File file = File.createTempFile("utilmize", extension);
        FileOutputStream fos = new FileOutputStream(file);
        FileTools.copyFile(is, fos);
        fos.close();
        file.deleteOnExit();
        return file;
    }

    /**
     * Creates the temporary folder.
     * @param prefix the prefix
     * @return the file
     * @throws IOException
     */
    public static File createTemporaryFolder(String prefix) throws IOException {
        File tmpFolder = File.createTempFile(prefix, "");
        Files.deleteIfExists(tmpFolder.toPath());
        tmpFolder.mkdirs();
        tmpFolder.deleteOnExit();
        return tmpFolder;
    }

    /**
     * Change extension.
     * @param name the name
     * @param ext the ext
     * @return the string
     */
    public static String changeExtension(String name, String ext) {
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf >= 0) {
            return name.substring(0, lastIndexOf) + (ext.charAt(0) == '.' ? "" : ".") + ext;
        }
        return name + (ext.charAt(0) == '.' ? "" : ".") + ext;
    }

    public static String[] listFilesByExtension(File dirOut, final String ext) throws IOException {
        if (!dirOut.isDirectory()) {
            throw new IOException("NOT_DIRECTORY_FILE");
        }
        return dirOut.list((dir, name) -> name.toLowerCase().endsWith(ext.toLowerCase()));
    }

    public static String readExternalFile(File file) throws IOException {
        if ((file == null) || !file.exists()) {
            throw new IOException("FILE_NOT_EXISTS");
        }
        return new String(FileTools.getBytesFromFile(file));
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[] { " B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.00").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Return the JAR file URL that contains received class, or null if it is not allocated in JAR file
     * @param classToLookFor
     * @return
     */
    public static URL getJarURL(Class<?> classToLookFor) {
        try {
            URL location = classToLookFor.getProtectionDomain().getCodeSource().getLocation();
            FileTools.logger.debug("location=\"{}\"", location);
            URI uri = location.toURI();
            FileTools.logger.debug("uri=\"{}\"", uri);
            return uri.toURL();
        } catch (Exception e) {
            FileTools.logger.error("Error detecting jar file URL. Detail: ", e);
            URL thePathEncoded = getUrl(classToLookFor);
            if (thePathEncoded != null) return thePathEncoded;
            return null;
        }
    }

    /**
     * Method extracted to improve readability of {@link #getJarURL(Class)}
     * @param classToLookFor
     * @return
     */
    private static URL getUrl(Class<?> classToLookFor) {
        try {
            URL url = classToLookFor.getProtectionDomain().getCodeSource().getLocation();
            String thePathEncoded = URLEncoder.encode(url.toString(), ENCODING);
            return getUrl(thePathEncoded);
        } catch (Exception e2) {
            FileTools.logger.error("Error detecting jar file URL encoded. Detail: ", e2);
        }
        return null;
    }

    /**
     * Method extracted to improve readability of {@link #getUrl(Class)}
     * @param thePathEncoded
     * @return
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    private static URL getUrl(String thePathEncoded) throws MalformedURLException, UnsupportedEncodingException {
        try {
            return new URL(URLEncoder.encode(thePathEncoded, ENCODING));
        } catch (Exception ex2) {
            FileTools.logger.trace("Error detecting jar file URL - 2. Detail: ", ex2);
            return new URL(URLDecoder.decode(thePathEncoded, ENCODING));
        }
    }

    /**
     * Return values (cast over String) of all properties allocated in JAR file MANIFEST. If some error
     * occurs empty values is returned.
     * @param jarFile
     * @return
     * @throws IOException
     */
    public static Map<String, String> readManifest(File jarFile) {
        Map<String, String> values = new HashMap<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Manifest mf = jar.getManifest();
            FileTools.catchValuesFromManifest(values, mf);
        } catch (Exception ex) {
            FileTools.logger.error("Error reading JAR file MANIFEST. Detail:", ex);
        }

        return values;
    }

    /**
     * Return values (cast over String) of all properties allocated in JAR file MANIFEST. If some error
     * occurs empty values is returned.
     * @param jarFile
     * @return
     * @throws IOException
     */
    public static Map<String, String> readManifest(InputStream jarFileStream) {
        Map<String, String> values = new HashMap<>();
        try {
            // Java 7+ ----------------------
            try (JarInputStream jarIS = new JarInputStream(jarFileStream)) {
                Manifest mf = jarIS.getManifest();
                FileTools.catchValuesFromManifest(values, mf);
            }
        } catch (Exception ex) {
            FileTools.logger.error("Error reading JAR file MANIFEST. Detail:", ex);
        }
        return values;
    }

    private static void catchValuesFromManifest(Map<String, String> values, Manifest mf) {
        if (mf != null) {
            // Parse main attributes
            final Attributes mainattr = mf.getMainAttributes();
            if (mainattr != null) {
                for (Entry<Object, Object> entry : mainattr.entrySet())
                    parseEntries(entry.getKey(), entry.getValue(), values);
            }

            // Parse another entries
            Map<String, Attributes> entries = mf.getEntries();
            for (Entry<String, Attributes> entry : entries.entrySet())
                parseEntries(entry.getKey(), entry.getValue(), values);
        } else {
            FileTools.logger.warn("Null manifest.");
        }
    }

    /**
     * Method to reduce cognitive complexity of {@link #catchValuesFromManifest(Map, Manifest)}
     * @param entry
     * @param value
     * @param values
     */
    private static void parseEntries(Object entry, Object value, Map<String, String> values) {
        if ((entry != null) && (value != null)) {
            values.put(String.valueOf(entry), String.valueOf(value));
        }
    }

}
