package com.ontimize.jee.core.common.dto;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;

public interface EntityResult {

    boolean DEBUG = false;

    boolean LIMIT_SPEED = false;

    int DATA_RESULT = 0;

    int NODATA_RESULT = 1;

    int OPERATION_SUCCESSFUL = 0;

    int OPERATION_WRONG = 1;

    int OPERATION_SUCCESSFUL_SHOW_MESSAGE = 2;

    int DEFAULT_COMPRESSION_THRESHOLD = Integer.MAX_VALUE;

    int DEFAULT_COMPRESSION = Deflater.DEFAULT_COMPRESSION;

    int NO_COMPRESSION = Deflater.NO_COMPRESSION;

    int BEST_COMPRESSION = Deflater.BEST_COMPRESSION;

    int BEST_SPEED = Deflater.BEST_SPEED;

    int HUFFMAN_ONLY = Deflater.HUFFMAN_ONLY;

    int MIN_PERCENT_PROGRESS = 3;

    int type = EntityResult.NODATA_RESULT;

    int code = EntityResult.OPERATION_SUCCESSFUL;

    void deleteRecord(int index);

    void setCode(int operationCode);

    String getMessage();

    void setMessage(String operationMessage);

    void addRecord(Map data);

    void addRecord(Map data, int s);

    void setColumnSQLTypes(Map types);

    void setColumnOrder(List l);

    void clear();

    Object get(Object key);

    Object put(Object key, Object value);

    void putAll(Map m);

    Object remove(Object key);

    EntityResult clone();

    Map getColumnSQLTypes();

    Map getRecordValues(int i);

    List getOrderColumns();

    int calculateRecordNumber();

    int getCode();

    int getBytesNumber();

    long getStreamTime();

    Object[] getMessageParameter();

    boolean containsValue(Object value);

    boolean containsKey(Object key);

    Set keySet();

    Enumeration keys();

    boolean isEmpty();

    boolean contains(Object value);

    int getCompressionThreshold();

    void setCompressionThreshold(int threshold);

    Enumeration elements();

    void setType(int operationType);

    Set entrySet();

    int getRecordIndex(Map kv);

    boolean isWrong();

    int getColumnSQLType(String col);

    class TimeUtil {

        long time = 0;

        public void setTime(long t) {
            this.time = t;
        }

        public long getTime() {
            return this.time;
        }

    }

    default int getValuesKeysIndex(EntityResult entityResult, Map kv) {

        // Check fast
        if (kv.isEmpty()) {
            return -1;
        }
        List vKeys = new ArrayList();
        Enumeration enumKeys = (Enumeration) kv.keySet();
        while (enumKeys.hasMoreElements()) {
            vKeys.add(enumKeys.nextElement());
        }

        Object vData = entityResult.get(vKeys.get(0));
        if ((vData == null) || (!(vData instanceof List))) {
            return -1;
        }
        int currentValueIndex = -1;

        if (vKeys.size() == 1) {
            return ((List) vData).indexOf(kv.get(vKeys.get(0)));
        }

        while ((currentValueIndex = ((ArrayList) vData)
            .indexOf(kv.get(vKeys.get(currentValueIndex + 1)))) >= 0) {
            boolean allValuesCoincidence = true;
            for (int i = 1; i < vKeys.size(); i++) {
                Object requestValue = kv.get(vKeys.get(i));
                Object vDataAux = entityResult.get(vKeys.get(i));
                if ((vDataAux == null) || (!(vDataAux instanceof List))) {
                    return -1;
                }
                if (!requestValue.equals(((List) vDataAux).get(currentValueIndex))) {
                    allValuesCoincidence = false;
                    break;
                }
            }

            if (allValuesCoincidence) {
                return currentValueIndex;
            }
        }
        return -1;
    }

    /*
     * default void main(String[] args) { List<String> columns = new ArrayList<String>();
     * columns.add("test"); EntityResultMapImpl eR = new EntityResultMapImpl(columns); Map record = new
     * HashMap<String, String>(); record.put("test", "value"); int total = 1000000;
     * System.out.println("Creating " + total + " records"); long startTime = System.nanoTime(); for
     * (int i = 0; i < total; i++) { eR.addRecord(record); } long estimatedTime = System.nanoTime() -
     * startTime; System.out.println("Time to create the entity result  ->" + estimatedTime);
     *
     * }
     *
     */

}
