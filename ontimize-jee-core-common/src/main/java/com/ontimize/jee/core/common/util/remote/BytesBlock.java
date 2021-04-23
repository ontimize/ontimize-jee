package com.ontimize.jee.core.common.util.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Generic class that represents a <code>Byte</code> array implementing custom
 * <code>Serializable</code> techniques. Moreover, some types of compression are allowed in these
 * processes.
 *
 * @author Imatia Innovation
 */
public class BytesBlock implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BytesBlock.class);

    public static final int NO_COMPRESSION = Deflater.NO_COMPRESSION;

    public static final int BEST_COMPRESSION = Deflater.BEST_COMPRESSION;

    public static final int BEST_SPEED = Deflater.BEST_SPEED;

    public static final int DEFAULT_COMPRESSION = Deflater.DEFAULT_COMPRESSION;

    public static final int HUFFMAN_ONLY = Deflater.HUFFMAN_ONLY;

    public long serializationStartTime = 0;

    public long serializationFinalTime = 0;

    /**
     * Zip compression is used after serialization process.
     */
    private transient byte[] bytes = null;

    private transient InputStream inputStream = null;

    /**
     * This variable indicates the number of bytes of array.
     *
     * <p>
     *
     * <b>Notes:</b> <br>
     * -Number of bytes when is serialized is generally different from number of bytes to read when it
     * is deserialized. So, it is necessary a variable that indicates number of bytes to read. This
     * variable must be serialized in our method to assign the number of bytes after compression. This
     * number will be used to read when it is deserialized.
     */
    private transient int bytesNumber;

    private final int decompressBytesNumber;

    private int compression = Deflater.NO_COMPRESSION;

    /**
     * Builds a <code>BytesBlock</code> with parameter <code>byteArray</code>. Serialization will be
     * executed with default method (with compression). Moreover, object will determine itself if
     * <code>byteArray</code> compression is justified.
     * @param byteArray the byte array
     */
    public BytesBlock(byte[] byteArray) {
        this.bytes = byteArray;
        this.bytesNumber = this.bytes.length;
        this.decompressBytesNumber = this.bytesNumber;
    }

    /**
     * Builds a <code>BytesBlock</code> from an Byte Array. Serialization will be executed with default
     * method and without compression (NO_COMPRESSION). If in parameter <code>compressionLevel</code> is
     * passed NO_COMPRESSION, this constructor will be equivalent to {@link #BytesBlock(byte[])}.
     * <p>
     *
     * When an object is serialized to disk, NO_COMPRESSION is about twenty times better in performance
     * than Compression. However, when serialization is executed over RMI, generally results are
     * inverted.
     * @param byteArray the byte array
     * @param compressionLevel Indicates the level of compression is defined in this class according to
     *        {@link java.util.zip.Deflater}
     */
    public BytesBlock(byte[] byteArray, int compressionLevel) {
        this.bytes = byteArray;
        this.bytesNumber = this.bytes.length;
        this.decompressBytesNumber = this.bytesNumber;
        this.compression = compressionLevel;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // Object serialization
        // Default serialization will write non-static and non-transient fields
        this.serializationStartTime = System.currentTimeMillis();
        // For compression levels different from NO_COMPRESSION
        byte[] compressedBytes = null;
        if (this.compression != Deflater.NO_COMPRESSION) {
            // We have to compress the byte array and write it in output
            // stream
            compressedBytes = this.compressionBytes(this.bytes, this.compression);
            // Checks if compressed byte number is higher than
            // non-compressed byte number.
            // In this case, compression is not produced and level is fixed
            // to NO_COMPRESSION
            if (compressedBytes.length >= this.bytes.length) {
                compressedBytes = this.bytes;
                this.compression = Deflater.NO_COMPRESSION;
            }
        } else {
            compressedBytes = this.bytes;
        }
        // Default serialization
        out.defaultWriteObject();
        // Result byte number
        int numeroBytesComprimido = compressedBytes.length;
        // Writes in object
        out.writeInt(numeroBytesComprimido);
        out.write(compressedBytes);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Deserializes the object
        // By default
        in.defaultReadObject();
        // Number of bytes to read
        int compressBytesNumber = in.readInt();
        byte[] compressBytes = new byte[compressBytesNumber];
        in.readFully(compressBytes);
        // When level is different from NO_COMPRESION
        if (this.compression != Deflater.NO_COMPRESSION) {
            this.bytes = this.decompressionBytes(compressBytes);
        } else {
            this.bytes = compressBytes;
        }
        this.serializationFinalTime = System.currentTimeMillis();
    }

    protected byte[] compressionBytes(byte[] bytesToCompress, int compressionLevel) {
        ZipOutputStream zip = null;
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            zip = new ZipOutputStream(byteStream);
            zip.setLevel(compressionLevel);
            ZipEntry entradaZip = new ZipEntry("BytesBlock");
            zip.putNextEntry(entradaZip);
            zip.write(bytesToCompress);
            zip.flush();
            zip.closeEntry();
            // Bytes result
            return byteStream.toByteArray();
        } catch (Exception e) {
            BytesBlock.logger.trace(null, e);
            return null;
        } finally {
            try {
                zip.close();
            } catch (Exception e) {
                BytesBlock.logger.trace(null, e);
            }
            try {
                byteStream.close();
            } catch (Exception e) {
                BytesBlock.logger.trace(null, e);
            }
        }
    }

    protected byte[] decompressionBytes(byte[] bytesToDecompress) {
        ZipInputStream zip = null;
        ByteArrayInputStream byteStream = null;
        try {
            // Now decompress
            byteStream = new ByteArrayInputStream(bytesToDecompress);
            zip = new ZipInputStream(byteStream);
            ZipEntry entradaZip = zip.getNextEntry();
            byte[] decompressBytes = new byte[this.decompressBytesNumber];
            for (int i = 0; i < this.decompressBytesNumber; i++) {
                decompressBytes[i] = (byte) zip.read();
            }
            return decompressBytes;
        } catch (IOException e) {
            BytesBlock.logger.trace(null, e);
            return null;
        } finally {
            try {
                zip.close();
            } catch (Exception e) {
                BytesBlock.logger.trace(null, e);
            }
            try {
                byteStream.close();
            } catch (Exception e) {
                BytesBlock.logger.trace(null, e);
            }
        }

    }

    /**
     * If byte array is equals in both compared objects returns true. Otherwise, false.
     * @param object Object to compare
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof BytesBlock) {
            byte[] objectBytes = ((BytesBlock) object).getBytes();
            if (objectBytes.length != this.bytes.length) {
                return false;
            } else {
                for (int i = 0; i < this.bytes.length; i++) {
                    if (objectBytes[i] != this.bytes[i]) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
