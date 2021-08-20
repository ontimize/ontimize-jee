package com.ontimize.jee.common.dto;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class EntityResultTools {

    public static int getValuesKeysIndex(EntityResult entityResult, Map kv) {

        // Check fast
        if (kv.isEmpty()) {
            return -1;
        }
        List vKeys = new ArrayList();
        Enumeration enumKeys = Collections.enumeration(kv.keySet());
        while (enumKeys.hasMoreElements()) {
            vKeys.add(enumKeys.nextElement());
        }
        // Now get the first data arraylist. Look for all indexes with the
        // specified key
        // and for each one check the other keys
        Object vData = entityResult.get(vKeys.get(0));
        if ((vData == null) || (!(vData instanceof ArrayList))) {
            return -1;
        }
        int currentValueIndex = -1;

        if (vKeys.size() == 1) {
            return ((ArrayList) vData).indexOf(kv.get(vKeys.get(0)));
        }

        while ((currentValueIndex = ((ArrayList) vData).indexOf(kv.get(vKeys.get(currentValueIndex + 1)))) >= 0) {
            boolean allValuesCoincidence = true;
            for (int i = 1; i < vKeys.size(); i++) {
                Object requestValue = kv.get(vKeys.get(i));
                Object vDataAux = entityResult.get(vKeys.get(i));
                if ((vDataAux == null) || (!(vDataAux instanceof ArrayList))) {
                    return -1;
                }
                if (!requestValue.equals(((ArrayList) vDataAux).get(currentValueIndex))) {
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

    public static void updateRecordValues(EntityResult entityResult, Map recordValue, int index) {
        Enumeration keysToUpdate = Collections.enumeration(recordValue.keySet());
        while (keysToUpdate.hasMoreElements()) {
            Object currentKey = keysToUpdate.nextElement();
            if (entityResult.containsKey(currentKey)) {
                ArrayList columnRecords = (ArrayList) entityResult.get(currentKey);
                columnRecords.set(index, recordValue.get(currentKey));
            } else {
                ArrayList columnRecords = new ArrayList(entityResult.calculateRecordNumber());
                columnRecords.set(index, recordValue.get(currentKey));
                entityResult.put(currentKey, columnRecords);
            }
        }
    }

    /**
     * Creates an empty <code>EntityResult</code> with structure of columns passed.
     * @param columns columns of <code>EntityResult</code>
     * @return an <code>EntityResult</code> with result or null when <code>columns</code> parameter is
     *         null
     *
     * @Deprecated public static EntityResult createEmptyEntityResult(List columns) { if (columns !=
     *             null) { return new EntityResult(columns); } return null; }
     */

}
