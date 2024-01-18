package com.ontimize.jee.common.tools;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EntityResultToolsTest {

    @Test
    void renameColumn() {
        EntityResult res = new EntityResultMapImpl();
        Map <String, Object> record = new HashMap<>();
        Map <String, Object> sqlTypes = new HashMap<>();
        String oldColumn1 = "column1";
        String oldColumn2 = "column2";
        String oldColumn3 = "column3";
        record.put(oldColumn1, "value1");
        record.put(oldColumn2, "value2");
        record.put(oldColumn3, "value3");
        res.addRecord(record);
        sqlTypes.put(oldColumn1, 12);
        sqlTypes.put(oldColumn2, 12);
        sqlTypes.put(oldColumn3, 12);
        res.setColumnSQLTypes(sqlTypes);
        EntityResultTools.renameColumn(res, oldColumn1, oldColumn1.toUpperCase());
        EntityResultTools.renameColumn(res, oldColumn2, oldColumn2.toUpperCase());
        EntityResultTools.renameColumn(res, oldColumn3, oldColumn3.toUpperCase());
        assertTrue(res.getRecordValues(0).containsKey(oldColumn1.toUpperCase()));
        assertTrue(res.getRecordValues(0).containsKey(oldColumn2.toUpperCase()));
        assertTrue(res.getRecordValues(0).containsKey(oldColumn3.toUpperCase()));
        assertTrue(res.getColumnSQLTypes().containsKey(oldColumn1.toUpperCase()));
        assertTrue(res.getColumnSQLTypes().containsKey(oldColumn2.toUpperCase()));
        assertTrue(res.getColumnSQLTypes().containsKey(oldColumn3.toUpperCase()));
    }
}