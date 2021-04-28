package com.ontimize.jee.common.dto;

import com.ontimize.jee.common.db.CancellableOperationManager;
import com.ontimize.jee.common.gui.field.ReferenceFieldAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class EntityResultMapImpl implements EntityResult, Map {

    Logger logger = LoggerFactory.getLogger(EntityResultMapImpl.class);

    protected String message = "";

    protected Object[] messageParameter = null;

    protected String detail = null;

    protected String operationId = null;

    protected List columnsOrder = null;

    protected int type = NODATA_RESULT;

    protected int code = OPERATION_SUCCESSFUL;

    protected int compressionLevel = Deflater.NO_COMPRESSION;

    protected int compressionThreshold = DEFAULT_COMPRESSION_THRESHOLD;

    protected int dataByteNumber = -1;

    protected long streamTime = 0;

    protected int MIN_BYTE_PROGRESS = 1024 * 50;

    int byteBlock = 40 * 1024;// 40 K

    /**
     * Object needed for the compression mechanism
     */
    protected transient Map data = new HashMap();

    public EntityResultMapImpl() {
    }

    // 5.2062EN-0.1
    public EntityResultMapImpl(List columns) {
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i) != null) {
                    this.put(columns.get(i), new ArrayList());
                }
            }
        }
    }

    public EntityResultMapImpl(HashMap h) {
        if (h != null) {
            this.data = (Map) h.clone();
        }
    }

    public EntityResultMapImpl(int operationCode, int resultType) {
        this.code = operationCode;
        this.type = resultType;
    }

    public EntityResultMapImpl(int operationCode, int resultType, String resultMessage) {
        this.code = operationCode;
        this.type = resultType;
        this.message = resultMessage;
    }

    public int getType() {
        return this.type;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getDetails() {
        return this.detail;
    }

    public void setType(int operationType) {
        this.type = operationType;
    }

    public void setCode(int operationCode) {
        this.code = operationCode;
    }

    public void setMessage(String operationMessage) {
        this.message = operationMessage;
    }

    public void setMessageParameters(Object[] params) {
        this.messageParameter = params;
    }

    public Object[] getMessageParameter() {
        return this.messageParameter;
    }

    public void setMessage(String operationMessage, String operationDetails) {
        this.message = operationMessage;
        this.detail = operationDetails;
    }

    // ////////// PROXY METHODS (COMPATIBILITY) ///////////////////////////
    public void clear() {
        this.data.clear();
    }

    public boolean contains(Object value) { // todo check if is desired result
        return this.data.containsValue(value);
    }

    public boolean containsKey(Object key) {
        return this.data.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.data.containsValue(value);
    }

    public Enumeration elements() {
        return Collections.enumeration(this.data.values());
    }

    public EntityResultMapImpl clone() {
        try {
            return this.deepClone();
        } catch (Exception e) {
            logger.trace(null, e);
            EntityResultMapImpl o = new EntityResultMapImpl();
            o.data = new HashMap(this.data);
            return o;
        }
    }

    private EntityResultMapImpl deepClone() {
        EntityResultMapImpl o = new EntityResultMapImpl();
        Enumeration eKeys = Collections.enumeration((this.data.keySet()));
        while (eKeys.hasMoreElements()) {
            Object oKey = eKeys.nextElement();
            List vValues = (ArrayList) this.data.get(oKey);
            if (vValues != null) {
                ((EntityResultMapImpl) o).data.put(oKey, new ArrayList(vValues));
            }
        }

        return (EntityResultMapImpl) o;
    }

    public Set entrySet() {
        return this.data.entrySet();
    }

    public Object get(Object key) {
        return this.data.get(key);
    }

    public Object get(Object cod, Object attr) {
        Object oValue = null;
        Enumeration eKeys = Collections.enumeration(this.data.keySet());
        while (eKeys.hasMoreElements()) {
            Object oKey = eKeys.nextElement();
            if (oKey instanceof ReferenceFieldAttribute) {
                ReferenceFieldAttribute ar = (ReferenceFieldAttribute) oKey;
                if (ar.getCod().equals(cod) && ar.getAttr().equals(attr)) {
                    oValue = this.data.get(oKey);
                    return oValue;
                }
            }
        }
        return oValue;
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    public Enumeration keys() {
        if (this.columnsOrder != null) {
            ArrayList sortKeys = new ArrayList();
            // First, search in columns order.
            for (int i = 0; i < this.columnsOrder.size(); i++) {
                if (this.data.containsKey(this.columnsOrder.get(i))) {
                    sortKeys.add(this.columnsOrder.get(i));
                }
            }
            // Now, other data columns not in columns order
            Enumeration eKeys = Collections.enumeration(this.data.keySet());
            while (eKeys.hasMoreElements()) {
                Object oKey = eKeys.nextElement();
                if (!sortKeys.contains(oKey)) {
                    sortKeys.add(oKey);
                }
            }
            return Collections.enumeration(sortKeys);
        }
        return Collections.enumeration(this.data.keySet());
    }

    public Set keySet() {
        return this.data.keySet();
    }

    public Object put(Object key, Object value) {
        return this.data.put(key, value);
    }

    public void putAll(Map m) {
        this.data.putAll(m);
    }

    public Object remove(Object key) {
        return this.data.remove(key);
    }

    public int size() {
        return this.data.size();
    }

    @Override
    public String toString() {
        String s = "EntityResult: ";
        if (this.getCode() == OPERATION_WRONG) {
            s = s + " ERROR CODE RETURN: " + this.getMessage();
        }
        s = s + " : " + this.data.toString();
        return s;
    }

    public Collection values() {
        return this.data.values();
    }

    public void setCompressionLevel(int level) {
        this.compressionLevel = level;
    }

    public void setCompressionThreshold(int threshold) {
        logger.debug("EntityResult: Compression threshold sets to: {}", threshold);
        this.compressionThreshold = threshold;
        if (this.compressionThreshold < this.MIN_BYTE_PROGRESS) {
            this.MIN_BYTE_PROGRESS = this.compressionThreshold * 2;
        }
        if (this.compressionThreshold < this.byteBlock) {
            this.byteBlock = this.compressionThreshold * 2;
        }
    }

    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }

    /**
     * Calculates the data size (bytes).NO USE
     */
    int sizeOf() {
        long t = System.currentTimeMillis();
        ByteArrayOutputStream out = null;
        ObjectOutputStream outO = null;
        try {
            out = new ByteArrayOutputStream(65536);
            outO = new ObjectOutputStream(out);
            outO.writeObject(this.data);
            long t2 = System.currentTimeMillis();
            int size = out.size();
            logger.debug("Time calculating EntityResult data size = {}  milliseconds. Size = {}  Bytes.",
                    t2 - t, size);
            return size;
        } catch (IOException e) {
            logger.error(null, e);
            return -1;
        } finally {
            try {
                out.close();
                outO.close();
            } catch (Exception e) {
                logger.trace(null, e);
            }
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // Default serialization. This object is serialized in first place.
        // To serialize the data is sometimes necessary to compress them
        logger.debug("Serializing EntityResult");
        long t = System.currentTimeMillis();
        int thresholdLevel = this.compressionThreshold;
        out.defaultWriteObject();
        logger.debug("Serializing EntityResult: after defaultWriteObject");
        // Now data
        ByteArrayOutputStream bOut = null;
        ObjectOutputStream outAux = null;
        try {
            logger.debug("Serializing EntityResult: entering the try");
            byte[] compressedBytes = null;
            bOut = new ByteArrayOutputStream(512);
            outAux = new ObjectOutputStream(bOut);
            logger.debug("Serializing EntityResult: before outAux");
            outAux.writeObject(this.data);
            outAux.flush();
            int size = bOut.size();
            logger.debug("Object size without compression = {} bytes. Compression threshold = {}", size,
                    thresholdLevel);

            if ((size > thresholdLevel) && (this.compressionLevel == NO_COMPRESSION)) {
                this.compressionLevel = BEST_SPEED;
            }
            // Object is now a byte array, compress it:
            byte[] bytesWithOutCompress = bOut.toByteArray();
            // If compresionLevel is different of NO_COMPRESSION, compress the
            // object
            if (this.compressionLevel != Deflater.NO_COMPRESSION) {
                // Save the object in a byte array
                // Compress the array and write it in the stream
                compressedBytes = this.compressionBytes(bytesWithOutCompress, this.compressionLevel);

                // Evaluate: If compressed byte number are greater than bytes
                // without compress, undo the compression and set the
                // compression
                // level to NO_COMPRESION to save time
                if (compressedBytes.length >= bytesWithOutCompress.length) {
                    compressedBytes = bytesWithOutCompress;
                    this.compressionLevel = Deflater.NO_COMPRESSION;
                }
                logger.debug("Compressed object size = {}  bytes", compressedBytes.length);
            } else {
                // When compression is not necessary.
                compressedBytes = bytesWithOutCompress;
            }

            this.dataByteNumber = compressedBytes.length;
            int priority = Thread.currentThread().getPriority();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            // Now, we have data bytes.
            // First write the compression, then the bytes number and the bytes
            // themselves
            out.writeInt(this.compressionLevel);
            out.writeInt(this.dataByteNumber);
            out.flush();
            int nBytesToWrite = compressedBytes.length;
            int writedBytes = 0;
            long t4 = System.currentTimeMillis();
            while (writedBytes < nBytesToWrite) {

                if (CancellableOperationManager.existCancellationRequest(this.operationId)) {
                    logger.info("Serializing operation canceled: {} . Written: {}", this.operationId,
                            writedBytes);
                    throw new IOException("Serializing operation canceled: " + this.operationId);
                }

                int currentWritedBytes = Math.min(nBytesToWrite - writedBytes, this.byteBlock);
                out.write(compressedBytes, writedBytes, currentWritedBytes);
                writedBytes = writedBytes + currentWritedBytes;
                if (this.operationId != null) {
                    out.flush();
                }
                logger.debug("EntityResult: Written: {}", currentWritedBytes);
                if (LIMIT_SPEED) {
                    long t5 = System.currentTimeMillis();
                    if ((t5 - t4) < 1000) {
                        logger.info("Serialization sleep: {}", 1000 - (t5 - t4));
                        try {
                            Thread.sleep(1000 - (t5 - t4));
                        } catch (Exception e) {
                            logger.trace(null, e);
                        }
                    }
                }
            }
            out.flush();
            this.streamTime = System.currentTimeMillis() - t4;
            logger.debug("STREAM Time EntityResult {}", this.streamTime);
            Thread.currentThread().setPriority(priority);
            out.writeLong(this.streamTime);
            out.flush();
            // Serialization is finished
            logger.debug("Serializing EntityResult time {}", System.currentTimeMillis() - t);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            try {
                outAux.close();
                bOut.close();
            } catch (Exception e) {
                logger.error(null, e);
            }
        }

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Particularities:
        // - Make the default deserialization. Get bytes number and compression
        // Makes the specific deserialization
        ObjectInputStream inAux = null;
        try {
            in.defaultReadObject();
            // Read the compression type
            int nCompression = in.readInt();
            // The number of bytes (compressed or not), is the number to read
            int nBytes = in.readInt();
            // Now bytes themselves. nBytes to read. Best option is read and
            // decompress
            byte[] bytes = null;
            // If compression level is different to NO_COMPRESION is necessary
            // undo
            // the compression
            long t = System.currentTimeMillis();
            long elapsedTime = 0;
            if (nCompression != Deflater.NO_COMPRESSION) {
                EntityResult.TimeUtil time = new EntityResult.TimeUtil();
                bytes = this.uncompressionBytes(in, nBytes);
                elapsedTime = time.getTime();
            } else {
                bytes = new byte[nBytes]; // Bytes of the data
                InputStream input = createApplicationInputStream(in, nBytes);

                int read = 0;
                int readProgress = 0;
                long t0 = System.nanoTime();

                int lastPercent = 0;
                while (read < bytes.length) {

                    int res = input.read(bytes, read, Math.min(bytes.length - read, 65536));

                    read += res;
                    readProgress += res;
                    int currentPercent = (int) ((read * 100.0) / bytes.length);

                }

                long t2 = System.currentTimeMillis();
                logger.debug("Time reading EntityResult: {} without compression", t2 - t);
                elapsedTime = t2 - t;
            }
            logger.debug("EntityResult size not serialized: {}", bytes.length);
            inAux = new ObjectInputStream(new ByteArrayInputStream(bytes));
            // Now read the object
            this.data = (Map) inAux.readObject();
            long tStream = in.readLong();
            this.dataByteNumber = nBytes;
            this.compressionLevel = nCompression;
            this.streamTime = tStream;
            if (elapsedTime > tStream) {
                logger.debug("Stream time < deserialized time: {} < {}", tStream, elapsedTime);
                this.streamTime = elapsedTime;
            }
        } catch (IOException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            throw e;
        } finally {
            try {
                if (inAux != null) {
                    inAux.close();
                }
            } catch (Exception e) {
                logger.error(null, e);
            }
        }
    }

    protected byte[] compressionBytes(byte[] bytesToCompress, int compressionLevel) {
        ZipOutputStream zip = null;
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            zip = new ZipOutputStream(byteStream);
            zip.setLevel(compressionLevel);
            ZipEntry inputZip = new ZipEntry("BytesBlock");
            zip.putNextEntry(inputZip);
            zip.write(bytesToCompress);
            zip.flush();
            zip.closeEntry();
            // Now bytes are in the bytearraystream.
            // Lock at the resultant number of bytes
            return byteStream.toByteArray();
        } catch (Exception e) {
            logger.trace(null, e);
            return null;
        } finally {
            try {
                zip.close();
            } catch (Exception e) {
                logger.trace(null, e);
            }
            try {
                byteStream.close();
            } catch (Exception e) {
                logger.trace(null, e);
            }
        }
    }

    protected byte[] uncompressionBytes(byte[] bytesADescomprimir) {
        ZipInputStream zip = null;
        ByteArrayInputStream byteStream = null;
        try {
            // Undo the compression
            byteStream = new ByteArrayInputStream(bytesADescomprimir);
            zip = new ZipInputStream(byteStream);
            ZipEntry inputZip = zip.getNextEntry();
            List bytesUncompressed = new ArrayList();
            int byt = -1;
            while ((byt = zip.read()) != -1) {
                bytesUncompressed.add(bytesUncompressed.size(), new Byte((byte) byt));
            }
            byte[] bytes = new byte[bytesUncompressed.size()];
            for (int i = 0; i < bytesUncompressed.size(); i++) {
                bytes[i] = ((Byte) bytesUncompressed.get(i)).byteValue();
            }
            return bytes;
        } catch (IOException e) {
            logger.trace(null, e);
            return null;
        } finally {
            try {
                zip.close();
            } catch (Exception e) {
                logger.trace(null, e);
            }
            try {
                byteStream.close();
            } catch (Exception e) {
                logger.trace(null, e);
            }
        }

    }

    private transient static Constructor applicationInputStreamConstructor;

    private boolean checkApplicationClass = false;

    protected InputStream createApplicationInputStream(InputStream in, int size) {

        if (!checkApplicationClass && applicationInputStreamConstructor == null) {
            try {
                Class clazz = Class.forName("com.ontimize.db.ApplicationStatusBarInputStream");
                applicationInputStreamConstructor = clazz.getConstructor(new Class[] { InputStream.class, int.class });
            } catch (Exception e) {
            } finally {
                checkApplicationClass = true;
            }
        }

        InputStream current = null;
        if (applicationInputStreamConstructor != null) {
            try {
                current = (InputStream) applicationInputStreamConstructor.newInstance(new Object[] { in, size });
            } catch (Exception e) {
                logger.error("{}", e.getMessage(), e);
            }
        }

        return current != null ? current : in;
    }

    protected byte[] uncompressionBytes(InputStream in, int byteNumber) {
        ZipInputStream zip = null;

        ByteArrayOutputStream bOut = null;
        logger.debug("EntityResult: Uncompressing : {} bytes", byteNumber);
        try {
            long t = System.currentTimeMillis();
            // Undo the compression
            bOut = new ByteArrayOutputStream(1024 * 50);

            zip = new ZipInputStream(createApplicationInputStream(in, byteNumber)) {
                @Override
                public void close() throws IOException {
                    // super.close();
                }
            };
            ZipEntry inputZip = zip.getNextEntry();

            long uncompressorT = System.nanoTime();;

            int byt = -1;
            byte[] bytes = new byte[50 * 1024];
            while ((byt = zip.read(bytes)) != -1) {
                bOut.write(bytes, 0, byt);
            }
            bOut.flush();
            byte[] res = bOut.toByteArray();

            uncompressorT = System.nanoTime() - uncompressorT;
            logger.debug("Time reading EntityResult: {} COMPRESSED", uncompressorT / 1000000.0);
            logger.trace("Time uncompress: " + (System.currentTimeMillis() - t));
            return res;
        } catch (IOException e) {
            logger.error(null, e);
            return null;
        } finally {
            try {
                bOut.close();
            } catch (Exception e) {
                logger.trace(null, e);
            }
            try {
                zip.close();
            } catch (Exception e) {
                logger.trace(null, e);
            }
        }

    }

    public int calculateRecordNumber() {
        int r = 0;
        Enumeration keys = this.keys();
        while (keys.hasMoreElements()) {
            Object oKey = keys.nextElement();
            Object v = this.get(oKey);
            if ((v != null) && (v instanceof List)) {
                r = ((List) v).size();
                break;
            }
        }
        return r;
    }

    public Map getRecordValues(int i) {
        if (i < 0) {
            return null;
        }
        Map hValues = new HashMap();
        Enumeration keys = this.keys();
        int r = 0;
        while (keys.hasMoreElements()) {
            Object oKey = keys.nextElement();
            List v = (List) this.get(oKey);
            r = v.size();
            if (i >= r) {
                logger.debug("The values List for {} only have {} values", oKey, r);
                continue;
            }
            if (v.get(i) != null) {
                hValues.put(oKey, v.get(i));
            }
        }
        return hValues;
    }

    public Map getRecordValues(int i, List vKeys) {
        if (i < 0) {
            return null;
        }
        Map hValues = new HashMap(vKeys.size() * 2);
        Enumeration keys = this.keys();
        int r = 0;
        while (keys.hasMoreElements()) {
            Object oKey = keys.nextElement();
            if (vKeys.contains(oKey)) {
                List v = (List) this.get(oKey);
                r = v.size();
                if (i >= r) {
                    logger.debug("The values List for {} only have {} values", oKey, r);
                    continue;
                }
                if (v.get(i) != null) {
                    hValues.put(oKey, v.get(i));
                }
            }
        }
        return hValues;
    }

    public long getStreamTime() {
        return this.streamTime;
    }

    public int getBytesNumber() {
        return this.dataByteNumber;
    }

    public void addRecord(Map data) {
        this.addRecord(data, 0);
    }

    public void addRecord(Map data, int s) {
        if (this.isEmpty()) {
            if (s > 0) {
                throw new IllegalArgumentException("is empty -> index must be 0");
            }
            Enumeration keys = Collections.enumeration((this.data.keySet()));
            while (keys.hasMoreElements()) {
                Object oKey = keys.nextElement();
                List v = new ArrayList();
                v.add(0, data.get(oKey));
                this.put(oKey, v);
            }
        } else {
            Enumeration keys = this.keys();
            int nReg = this.calculateRecordNumber();
            if (s >= nReg) {
                s = nReg;
            }
            if (s < 0) {
                s = 0;
            }
            ArrayList modifiedList = new ArrayList();
            while (keys.hasMoreElements()) {
                Object oKey = keys.nextElement();
                List v = (List) this.get(oKey);
                if (modifiedList.contains(v)) {
                    continue;
                }
                if (data.containsKey(oKey)) {
                    Object oValue = data.get(oKey);
                    v.add(s, oValue);
                } else {
                    v.add(s, null);
                }
                modifiedList.add(v);
            }
        }
    }

    public void deleteRecord(int index) {
        if ((index >= 0) && (index < this.calculateRecordNumber())) {
            Enumeration eKeys = this.keys();
            while (eKeys.hasMoreElements()) {
                Object oKey = eKeys.nextElement();
                List vData = (List) this.get(oKey);
                vData.remove(index);
            }
        }
    }

    /**
     * Returns true when code is equals to {@value #OPERATION_WRONG}.
     *
     * @since 5.2068EN-0.1
     * @return condition about successful/wrong state
     */
    public boolean isWrong() {
        return this.code == OPERATION_WRONG;
    }

    public int indexOfData(Map dataKeys) {
        int index = EntityResultTools.getValuesKeysIndex(this, dataKeys);
        return index;
    }

    public int getRecordIndex(Map kv) {
        List vKeys = new ArrayList();
        Enumeration eKeys = Collections.enumeration(kv.keySet());
        while (eKeys.hasMoreElements()) {
            vKeys.add(eKeys.nextElement());
        }
        for (int i = 0; i < this.calculateRecordNumber(); i++) {
            Map recordValues = this.getRecordValues(i);
            boolean found = true;
            for (int j = 0; j < vKeys.size(); j++) {
                Object keyCondition = kv.get(vKeys.get(j));
                if (!keyCondition.equals(recordValues.get(vKeys.get(j)))) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    public Map columnsSQLTypes = null;

    public Map getColumnSQLTypes() {
        return this.columnsSQLTypes;
    }

    public void setColumnSQLTypes(Map types) {
        this.columnsSQLTypes = types;
    }

    public int getColumnSQLType(String col) {
        if ((this.columnsSQLTypes != null) && this.columnsSQLTypes.containsKey(col)) {
            return ((Number) this.columnsSQLTypes.get(col)).intValue();
        } else {
            return java.sql.Types.OTHER;
        }
    }

    public void setOperationId(String opId) {
        this.operationId = opId;
    }

    public List getOrderColumns() {
        if (this.columnsOrder != null) {
            ArrayList l = new ArrayList();
            l.addAll(this.columnsOrder);
            return l;
        } else {
            return this.columnsOrder;
        }
    }

    public void setColumnOrder(List l) {
        this.columnsOrder = l;
    }

}
