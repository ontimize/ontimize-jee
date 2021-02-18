/*
 *
 */
package com.ontimize.jee.common.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import com.ontimize.dto.EntityResult;
import com.ontimize.dto.EntityResultMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;


import com.ontimize.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.db.SQLStatementBuilder.BasicField;
import com.ontimize.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.gui.SearchValue;
import com.ontimize.gui.field.ReferenceFieldAttribute;
import com.ontimize.gui.table.TableAttribute;
import com.ontimize.jee.common.tools.ertools.AbstractAggregateFunction;
import com.ontimize.jee.common.tools.ertools.Group;
import com.ontimize.jee.common.tools.ertools.IAggregateFunction;
import com.ontimize.jee.common.tools.ertools.IPartialAggregateValue;

/**
 * Clase de utilidades para EntityResult.
 */
public final class EntityResultTools extends com.ontimize.dto.EntityResultTools {

    private static final Logger logger = LoggerFactory.getLogger(EntityResultTools.class);

    /** The Constant MAX_IN_EXPRESSION_SUPPORT. */
    public static final int MAX_IN_EXPRESSION_SUPPORT = 1000;

    private EntityResultTools() {
        super();
    }

    /**
     * The Enum JoinType.
     */
    public enum JoinType {

        /** The left. */
        LEFT,
        /** The inner. */
        INNER

    }


    /**
     * Hace un join entre dos {@link EntityResult}. El join puede ser LEFT o INNER. En columnKeysA se
     * indican las columnas por las que se hace el join (mismo nombre en los dos EntityResult)
     * @param a the a
     * @param b the b
     * @param columnKeysA the column keys a
     * @param joinType the join type
     * @return the entity result
     */
    public static EntityResult doJoin(EntityResult a, EntityResult b, String[] columnKeysA, JoinType joinType) {
        return EntityResultTools.doJoin(a, b, columnKeysA, columnKeysA, joinType);
    }

    /**
     * Hace un join entre dos {@link EntityResult}. El join puede ser LEFT o INNER. En columnKeysA y
     * columnKeysB se indican las columnas por las que se hace el join (deben estar ordenadas de tal
     * forma que a.columnKeysA[i] = b.columnKeysB[i])
     * @param a the a
     * @param b the b
     * @param columnKeysA the column keys a
     * @param columnKeysB the column keys b
     * @param joinType the join type
     * @return the entity result
     */
    public static EntityResult doJoin(EntityResult a, EntityResult b, String[] columnKeysA, String[] columnKeysB,
            JoinType joinType) {
        EntityResult res = new EntityResultMapImpl();//todo review on new implementations
        List<Object> resColumnsA = new ArrayList<>(a.keySet());
        EntityResultTools.ensureCols(resColumnsA, columnKeysA);
        List<Object> resColumnsB = new ArrayList<>(b.keySet());
        EntityResultTools.ensureCols(resColumnsB, columnKeysB);
        List<Object> resColumns = new ArrayList<>();
        List<Object> resColumnsCommon = new ArrayList<>();
        resColumns.addAll(resColumnsA);
        for (int i = 0; i < resColumnsB.size(); i++) {
            Object col = resColumnsB.get(i);
            if (resColumns.contains(col)) {
                resColumnsCommon.add(col);
                resColumnsB.remove(i);
                resColumnsA.remove(col);
                i--;
            } else {
                resColumns.add(col);
            }
        }
        EntityResultTools.initEntityResult(res, resColumns);

        if (columnKeysA.length == 1) {
            res = EntityResultTools.doFastJoin(a, b, columnKeysA[0], columnKeysB[0], joinType, res, resColumnsA,
                    resColumnsB, resColumns, resColumnsCommon);
        } else {
            int rcount = a.calculateRecordNumber();
            for (int i = 0; i < rcount; i++) {
                Map<Object, Object> row = a.getRecordValues(i);
                EntityResultTools.doJoinForTable(res, resColumns, resColumnsA, resColumnsB, resColumnsCommon, row, b,
                        columnKeysA, columnKeysB, joinType.equals(JoinType.INNER));
            }
            if (res.calculateRecordNumber() == 0) {
                res.clear();
            }
        }
        if (res.get(columnKeysA[0]) == null) {
            return res;
        }
        if (((List) res.get(columnKeysA[0])).size() == 0) {
            return new EntityResultMapImpl();
        } else {
            return res;
        }
    }

    /**
     * Anade a resColumns las columnas de cols que no contenga.
     * @param resColumns the res columns
     * @param cols the cols
     */
    private static void ensureCols(List<Object> resColumns, String... cols) {
        for (String c : cols) {
            if (!resColumns.contains(c)) {
                resColumns.add(c);
            }
        }
    }

    /**
     * Hace un join rapido entre dos entityResult por una columna usando el algoritmo FastQSort antes de
     * hacer el join para acelerar el algoritmo.
     * @param a the a
     * @param b the b
     * @param keyNameA the key name a
     * @param keyNameB the key name b
     * @param joinType the join type
     * @param res the res
     * @param resColumnsA the res columns a
     * @param resColumnsB the res columns b
     * @param resColumns the res columns
     * @param resColumnsCommon the res columns common
     * @return the entity result
     */
    private static EntityResult doFastJoin(EntityResult a, EntityResult b, String keyNameA, String keyNameB,
            JoinType joinType, EntityResult res, List<Object> resColumnsA,
            List<Object> resColumnsB, List<Object> resColumns, List<Object> resColumnsCommon) {

        Object[] keysSortedA = null;
        int[] indexesA = null;
        if (!a.isEmpty()) {
            if (a.get(keyNameA) == null) {
                // No viene la clave --> no hay join
                if (JoinType.INNER == joinType) {
                    return new EntityResultMapImpl();
                } else {
                    return a;
                }
            }
            keysSortedA = ((List) a.get(keyNameA)).toArray();
            indexesA = FastQSortAlgorithm.sort(keysSortedA);
        } else {
            keysSortedA = new Object[0];
            indexesA = new int[0];
        }

        Object[] keysSortedB = null;
        int[] indexesB = null;
        if (!b.isEmpty()) {
            keysSortedB = ((List) b.get(keyNameB)).toArray();
            indexesB = FastQSortAlgorithm.sort(keysSortedB);
        } else {
            keysSortedB = new Object[0];
            indexesB = new int[0];
        }

        int searchIndexB = 0;
        int resIndex = 0;

        for (int i = 0; i < indexesA.length; i++) {
            int found = 0;
            if (keysSortedA[i] != null) {
                for (; searchIndexB < keysSortedB.length; searchIndexB++) {
                    if (keysSortedB[searchIndexB] != null) {
                        int compareResult = ((Comparable) keysSortedA[i]).compareTo(keysSortedB[searchIndexB]);
                        if (compareResult == 0) {
                            found++;
                            // FOUND
                            for (Object col : resColumnsA) {
                                try {
                                    ((List) res.get(col)).add(resIndex, ((List) a.get(col)).get(indexesA[i]));
                                } catch (Exception e) {
                                    EntityResultTools.logger.error(null, e);
                                }
                            }
                            for (Object col : resColumnsB) {
                                ((List) res.get(col)).add(resIndex,
                                        ((List) b.get(col)).get(indexesB[searchIndexB]));
                            }
                            for (Object col : resColumnsCommon) {
                                ((List) res.get(col)).add(resIndex,
                                        ((List) b.get(col)).get(indexesB[searchIndexB]));
                            }
                            resIndex++;
                        } else if (compareResult < 0) {
                            if ((found > 0) && (i < (indexesA.length - 1))
                                    && keysSortedA[i].equals(keysSortedA[i + 1])) {
                                searchIndexB -= found;
                            }
                            break;
                        }
                    }
                }
            }
            // If left join remain A
            if ((found == 0) && joinType.equals(JoinType.LEFT)) {
                for (Object col : resColumnsA) {
                    ((List) res.get(col)).add(resIndex, ((List) a.get(col)).get(indexesA[i]));
                }
                for (Object col : resColumnsCommon) {
                    ((List) res.get(col)).add(resIndex, ((List) a.get(col)).get(indexesA[i]));
                }
                for (Object col : resColumnsB) {
                    ((List) res.get(col)).add(resIndex, null);
                }
                resIndex++;
            } else if ((found > 0) && (searchIndexB == keysSortedB.length) && (i < (indexesA.length - 1))
                    && keysSortedA[i].equals(keysSortedA[i + 1])) {
                searchIndexB -= found;
            }
        }
        return res;
    }

    /**
     * Do join for table.
     * @param res the res
     * @param resColumns the res columns
     * @param resColumnsA the res columns a
     * @param resColumnsB the res columns b
     * @param resColumnsCommon the res columns common
     * @param rowA the row a
     * @param b the b
     * @param columnKeysA the column keys a
     * @param columnKeysB the column keys b
     * @param onlyInnerJoin the only inner join
     */
    private static void doJoinForTable(EntityResult res, List<Object> resColumns, List<Object> resColumnsA,
            List<Object> resColumnsB, List<Object> resColumnsCommon,
            Map<Object, Object> rowA, EntityResult b, String[] columnKeysA, String[] columnKeysB,
            boolean onlyInnerJoin) {
        int rcount = b.calculateRecordNumber();
        boolean match = false;
        int index = res.calculateRecordNumber();
        for (int i = 0; i < rcount; i++) {
            Map<Object, Object> testRow = b.getRecordValues(i);
            if (EntityResultTools.check(rowA, testRow, columnKeysA, columnKeysB)) {
                match = true;
                for (Object col : resColumnsA) {
                    try {
                        ((List) res.get(col)).add(index, rowA.get(col));
                    } catch (Exception e) {
                        EntityResultTools.logger.error(null, e);
                    }
                }
                for (Object col : resColumnsB) {
                    ((List) res.get(col)).add(index, testRow.get(col));
                }
                for (Object col : resColumnsCommon) {
                    ((List) res.get(col)).add(index, testRow.get(col));
                }
                index++;
            }
        }
        if (!match && !onlyInnerJoin) {
            for (Object col : resColumnsA) {
                ((List) res.get(col)).add(index, rowA.get(col));
            }
            for (Object col : resColumnsCommon) {
                ((List) res.get(col)).add(index, rowA.get(col));
            }
            for (Object col : resColumnsB) {
                ((List) res.get(col)).add(index, null);
            }
        }
    }

    /**
     * Comprueba si las columnas son valores para las keys pasadas con iguales.
     * @param ha the ha
     * @param hb the hb
     * @param columnKeysA the column keys a
     * @param columnKeysB the column keys b
     * @return true, if successful
     */
    public static boolean check(Map<Object, Object> ha, Map<Object, Object> hb, String[] columnKeysA,
            String[] columnKeysB) {
        for (int i = 0; i < columnKeysA.length; i++) {
            Object oa = ha.get(columnKeysA[i]);
            Object ob = hb.get(columnKeysB[i]);
            if ((oa == null) && (ob != null)) {
                return false;
            } else if ((oa != null) && !oa.equals(ob)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Inicializa un {@link EntityResult} con las columnas y la longitud indicada.
     * @param res the res
     * @param columns the columns
     * @param length the length
     */
    public static void initEntityResult(EntityResult res, List<?> columns, int length) {
        for (Object col : columns) {
            res.put(col, new ArrayList<>(length > 0 ? length : 10));
        }
    }

    /**
     * Inicializa un {@link EntityResult} con las columnas indicadas.
     * @param res the res
     * @param columns the columns
     */
    public static void initEntityResult(EntityResult res, String... columns) {
        EntityResultTools.initEntityResult(res, Arrays.asList(columns));
    }

    /**
     * Inicializa un {@link EntityResult} con las columnas indicadas.
     * @param res the res
     * @param asList the as list
     */
    public static void initEntityResult(EntityResult res, List<?> asList) {
        EntityResultTools.initEntityResult(res, asList, 0);
    }

    /**
     * Agrupa los valores de un {@link EntityResult}. Se pueden indicar m?ltiples funciones de agregado.
     * TODO: versi?n mejorada de EntityResult doGroup(EntityResult a, String[] groupColumns, GroupType
     * groupType, String columnToGroup, boolean count)
     * @param er the entity result
     * @param groupColumns the group columns
     * @param groupOperations the GroupTypeOperation
     * @return the entity result
     * @throws Exception the exception
     */
    public static EntityResult doGroup(EntityResult er, String[] groupColumns, IAggregateFunction... aggregateFunctions)
            throws Exception {
        if (groupColumns.length <= 0) {
            throw new Exception("GroupColumns are mandatory");
        }

        if (er.isEmpty()) {
            return er;
        }

        List<Group> groups = new ArrayList<>();

        int rcount = er.calculateRecordNumber();
        for (int i = 0; i < rcount; i++) {
            Group group = EntityResultTools.checkGroup(groups, er, i);
            if (group == null) {
                // Add keys and values
                Map<String, Object> ks = new HashMap<>();
                for (String groupCol : groupColumns) {
                    ks.put(groupCol, ((List) er.get(groupCol)).get(i));
                }
                group = new Group(ks, aggregateFunctions);
                groups.add(group);
            }
            group.onNewGroupRecord(er, i);
        }

        EntityResult res = new EntityResultMapImpl();
        List<Object> resultColumns = new ArrayList<Object>(Arrays.asList(groupColumns));
        for (Group group : groups) {
            resultColumns.addAll(group.getAggregatedColumnNames());
        }
        EntityResultTools.initEntityResult(res, resultColumns, groups.size());

        int rowIdx = 0;
        for (Group group : groups) {
            for (Entry<String, Object> groupColEntry : group.getKeys().entrySet()) {
                ((List) res.get(groupColEntry.getKey())).add(rowIdx, groupColEntry.getValue());
            }
            for (Entry<String, Object> aggregateColEntry : group.getAggregateValues().entrySet()) {
                ((List) res.get(aggregateColEntry.getKey())).add(rowIdx, aggregateColEntry.getValue());
            }
            rowIdx++;
        }
        return res;
    }

    /**
     * Check group.
     * @param groups the groups
     * @param recordValues the record values
     * @return the group
     */
    private static Group checkGroup(List<Group> groups, EntityResult er, int index) {
        for (Group group : groups) {
            boolean isEqGroup = true;
            for (Entry<String, Object> entry : group.getKeys().entrySet()) {
                if (!ObjectTools.safeIsEquals(entry.getValue(), ((List) er.get(entry.getKey())).get(index))) {
                    isEqGroup = false;
                    break;
                }
            }
            if (isEqGroup) {
                return group;
            }
        }
        return null;
    }

    /**
     * Cambia el nombre de una columna del {@link EntityResult}.
     * @param er the er
     * @param fromColumn the from column
     * @param toColumn the to column
     */
    public static void renameColumn(EntityResult er, String fromColumn, String toColumn) {
        Object toRename = er.remove(fromColumn);
        if (toRename != null) {
            er.put(toColumn, toRename);
        }
    }


    /**
     * Do fast sort.
     * @param a the a
     * @param col the col
     * @return the entity result
     */
    private static EntityResult doFastSort(EntityResult a, String col) {
        if (a.calculateRecordNumber() < 2) {
            return a;
        }
        Object[] keysSorted = ((List) a.get(col)).toArray();
        int[] indexes = FastQSortAlgorithm.sort(keysSorted);
        List cols = new ArrayList(a.keySet());
        EntityResult res = new EntityResultMapImpl();
        EntityResultTools.initEntityResult(res, cols, indexes.length);
        for (Object key : cols) {
            List vOrig = (List) a.get(key);
            List vDest = (List) res.get(key);
            for (int i = 0; i < indexes.length; i++) {
                vDest.add(i, vOrig.get(indexes[i]));
            }
        }
        return res;
    }

    /**
     * Prints the result.
     * @param res the res
     */
    public static String printResult(EntityResult res) {
        StringBuilder sb = new StringBuilder();
        if ((res == null) || res.isEmpty()) {
            sb.append("Result empty.");
            return sb.toString();
        }
        int numR = res.calculateRecordNumber();
        Set keySet = res.keySet();
        sb.append("\n\n------------------- Printing result -----------------");
        for (Object key : keySet) {
            sb.append(key).append(StringTools.TAB);
        }
        sb.append(StringTools.WEOL);
        for (int i = 0; i < numR; i++) {
            for (Object key : keySet) {
                sb.append(((List) res.get(key)).get(i)).append(StringTools.TAB);
            }
            sb.append(StringTools.WEOL);
        }
        return sb.toString();
    }


    /**
     * Imprime un entityResult.
     * @param er the er
     * @return the string
     */
    public static String toString(EntityResult er) {
        StringBuilder sb = new StringBuilder();
        if (er == null) {
            sb.append("EntityResult is NULL");
        } else {
            int nregs = er.calculateRecordNumber();
            sb.append("Total de registros: ").append(nregs).append("\r\n");
            // Primero las cabeceras
            List<?> keyList = new ArrayList<>(er.keySet());

            for (Object key : keyList) {
                sb.append(key).append("\t");
            }
            sb.append("\r\n");

            for (int i = 0; i < nregs; i++) {
                Map recordValues = er.getRecordValues(i);
                for (Object key : keyList) {
                    sb.append(recordValues.get(key)).append("\t");
                }
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }

    /**
     * Hace un UNION ALL de los {@link EntityResult} que se pasan como parametro. El
     * {@link EntityResult} final tiene las columnas de todos los
     * @param vRes the v res
     * @return the entity result {@link EntityResult} que se han pasado. En caso de que los
     *         {@link EntityResult} tengan las columnas con el mismo identificador, se combinan.
     */
    public static EntityResult doUnionAll(EntityResult... vRes) {
        if ((vRes == null) || (vRes.length == 0)) {
            EntityResult res = new EntityResultMapImpl();
            res.setCode(EntityResult.OPERATION_WRONG);
            return res;
        }

        EntityResult res = new EntityResultMapImpl();
        res.setCode(EntityResult.OPERATION_SUCCESSFUL);
        res.setCompressionThreshold(vRes[0].getCompressionThreshold());
        res.setType(EntityResult.DATA_RESULT);

        // primero sacamos la lista de columnas totales
        List<Object> columnList = EntityResultTools.getColumnList(vRes);
        EntityResultTools.initEntityResult(res, columnList);

        // Combinamos los datos
        int[] numregs = new int[vRes.length];
        for (int i = 0; i < vRes.length; i++) {
            numregs[i] = vRes[i].calculateRecordNumber();
        }
        for (Object ob : columnList) {
            List<Object> vtotal = (List<Object>) res.get(ob);
            int curIndex = 0;
            for (int i = 0; i < vRes.length; i++) {
                EntityResult er = vRes[i];
                List<Object> vpart = (List<Object>) er.get(ob);
                if (vpart == null) {
                    Object[] tmp = new Object[numregs[i]];
                    Arrays.fill(tmp, null);
                    vtotal.addAll(curIndex, Arrays.asList(tmp));
                } else {
                    vtotal.addAll(curIndex, vpart);
                }
                curIndex += numregs[i];
            }
        }
        return res;
    }

    /**
     * Hace un UNION (elimina duplicados) de los {@link EntityResult} que se pasan como parametro. El
     * {@link EntityResult} final tiene las columnas de todos los
     * @param vRes the v res
     * @return the entity result {@link EntityResult} que se han pasado. En caso de que los
     *         {@link EntityResult} tengan las columnas con el mismo identificador, se combinan.
     */
    public static EntityResult doUnion(EntityResult... vRes) {
        EntityResult doUnionAll = EntityResultTools.doUnionAll(vRes);

        // Now clean duplicated entries
        EntityResult newResult = new EntityResultMapImpl();
        newResult.setCode(EntityResult.OPERATION_SUCCESSFUL);
        newResult.setCompressionThreshold(vRes[0].getCompressionThreshold());
        newResult.setType(EntityResult.DATA_RESULT);

        List<Object> columnList = EntityResultTools.getColumnList(doUnionAll);
        EntityResultTools.initEntityResult(newResult, columnList);

        int num = doUnionAll.calculateRecordNumber();
        for (int i = 0; i < num; i++) {
            Map currentValues = doUnionAll.getRecordValues(i);
            if (!EntityResultTools.checkRecordExists(newResult, currentValues, columnList)) {
                EntityResultTools.fastAddRecord(newResult, doUnionAll, i);
            }
        }
        return newResult;
    }

    private static boolean checkRecordExists(EntityResult newResult, Map currentValues, List<Object> columnList) {
        EntityResult dofilter = EntityResultTools.dofilter(newResult, currentValues);

        // Moreover is required to check for this fields that sattisfy filter if is exactly the same value
        // in all columns
        // because if currentvalues hasn`t some value in some column, will be not applied filter
        for (int i = 0; i < dofilter.calculateRecordNumber(); i++) {
            Map filterValues = dofilter.getRecordValues(i);

            boolean match = true;
            for (Object col : columnList) {
                Object toLookForValue = currentValues.get(col);
                Object matchValue = filterValues.get(col);
                if (!ObjectTools.safeIsEquals(toLookForValue, matchValue)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }

        return false;
    }

    /**
     * Obtiene la lista de columnas de todos los {@link EntityResult}.
     * @param vRes the v res
     * @return the column list
     */
    private static List<Object> getColumnList(EntityResult... vRes) {
        List<Object> list = new ArrayList<>();
        for (EntityResult er : vRes) {
            Enumeration keys = er.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                if (!list.contains(key)) {
                    list.add(key);
                }
            }
        }
        return list;
    }

    /**
     * Filtra un {@link EntityResult} a partir del filtro establecido en keysValues. Acepta SearchValue
     * AUN NO ESTA MUY PROBADO!!!!!!
     * @param er the er
     * @param keysValues the keys values
     * @return the entity result
     */
    public static EntityResult dofilter(EntityResult er, Map<?, ?> keysValues) {
        return EntityResultTools.dofilter(er, keysValues, false);
    }

    public static EntityResult dofilter(EntityResult er, Map<?, ?> keysValues, boolean remove) {
        EntityResult res = new EntityResultMapImpl();
        res.setCode(EntityResult.OPERATION_SUCCESSFUL);
        res.setCompressionThreshold(er.getCompressionThreshold());
        res.setType(EntityResult.DATA_RESULT);
        // primero sacamos la lista de columnas totales
        List<Object> columnList = EntityResultTools.getColumnList(er);
        EntityResultTools.initEntityResult(res, columnList);
        int nregs = er.calculateRecordNumber();
        for (int i = 0; i < nregs; i++) {
            if (EntityResultTools.checkFilter(er, i, keysValues)) {
                EntityResultTools.fastAddRecord(res, er, i);
                if (remove) {
                    er.deleteRecord(i);
                    i--;
                    nregs--;
                }
            }
        }

        return res;
    }

    public static void fastAddRecord(EntityResult target, EntityResult source, int sourceIndex) {
        for (Entry entry : (Set<Entry>) target.entrySet()) {
            List v = (List) entry.getValue();
            v.add(((List) source.get(entry.getKey())).get(sourceIndex));
        }
    }

    public static void fastAddRecord(EntityResult target, Map<Object, Object> source) {
        for (Entry entry : (Set<Entry>) target.entrySet()) {
            List v = (List) entry.getValue();
            v.add((source.get(entry.getKey())));
        }
    }

    public static void fastAddRecord(EntityResult target, int targetIndex, EntityResult source, int sourceIndex) {
        for (Entry entry : (Set<Entry>) target.entrySet()) {
            List v = (List) entry.getValue();
            v.add(targetIndex, ((List) source.get(entry.getKey())).get(sourceIndex));
        }
    }

    /**
     * Comprueba si una fila del {@link EntityResult} cumple el filtro establecido por keysValues.
     * @param er the er
     * @param index the index
     * @param keysValues the keys values
     * @return true, if successful
     */
    private static boolean checkFilter(EntityResult er, int index, Map<?, ?> keysValues) {
        if (keysValues == null) {
            return true;
        }
        Enumeration<?> keys = Collections.enumeration(keysValues.keySet());
        boolean isOk = true;
        while (keys.hasMoreElements() && isOk) {
            Object filterKey = keys.nextElement();
            Object filterValue = keysValues.get(filterKey);
            Object test = ((List) er.get(filterKey)).get(index);

            if (filterValue instanceof SearchValue) {
                SearchValue sv = (SearchValue) filterValue;
                switch (sv.getCondition()) {
                    case SearchValue.BETWEEN:
                        if (test instanceof Comparable) {
                            Comparable n = (Comparable) test;
                            Comparable nMin = (Comparable) ((List) sv.getValue()).get(0);
                            Comparable nMax = (Comparable) ((List) sv.getValue()).get(1);
                            isOk = (n.compareTo(nMin) >= 0) && (n.compareTo(nMax) <= 0);
                        } else {
                            isOk = false;
                        }
                        break;
                    case SearchValue.NOT_BETWEEN:
                        if (test instanceof Comparable) {
                            Comparable n = (Comparable) test;
                            Comparable nMin = (Comparable) ((List) sv.getValue()).get(0);
                            Comparable nMax = (Comparable) ((List) sv.getValue()).get(1);
                            isOk = (n.compareTo(nMin) < 0) || (n.compareTo(nMax) > 0);
                        } else {
                            isOk = false;
                        }
                        break;
                    case SearchValue.EQUAL:
                        isOk = sv.getValue().equals(test);
                        break;
                    case SearchValue.NOT_EQUAL:
                        isOk = !sv.getValue().equals(test);
                        break;
                    case SearchValue.IN:
                    case SearchValue.OR:
                        List v = (List) sv.getValue();
                        isOk = v.contains(test);
                        break;
                    case SearchValue.NOT_IN:
                        v = (List) sv.getValue();
                        isOk = !v.contains(test);
                        break;
                    case SearchValue.LESS:
                        if (test instanceof Comparable) {
                            Comparable n = (Comparable) test;
                            Comparable m = (Comparable) sv.getValue();
                            isOk = n.compareTo(m) < 0;
                        } else {
                            isOk = false;
                        }
                        break;
                    case SearchValue.LESS_EQUAL:
                        if (test instanceof Comparable) {
                            Comparable n = (Comparable) test;
                            Comparable m = (Comparable) sv.getValue();
                            isOk = n.compareTo(m) <= 0;
                        } else {
                            isOk = false;
                        }
                        break;
                    case SearchValue.MORE:
                        if (test instanceof Comparable) {
                            Comparable n = (Comparable) test;
                            Comparable m = (Comparable) sv.getValue();
                            isOk = n.compareTo(m) > 0;
                        } else {
                            isOk = false;
                        }
                        break;
                    case SearchValue.MORE_EQUAL:
                        if (test instanceof Comparable) {
                            Comparable n = (Comparable) test;
                            Comparable m = (Comparable) sv.getValue();
                            isOk = n.compareTo(m) >= 0;
                        } else {
                            isOk = false;
                        }
                        break;
                    case SearchValue.NOT_NULL:
                        isOk = test != null;
                        break;
                    case SearchValue.NULL:
                        isOk = test == null;
                        break;

                    case SearchValue.EXISTS:
                    default:
                        break;
                }
            } else if (filterValue instanceof IRecordFilter) {
                isOk = ((IRecordFilter) filterValue).evaluate(test);
            } else {
                isOk = filterValue.equals(test);
            }
        }
        return isOk;
    }

    /**
     * Anade una columna al {@link EntityResult} con los valores indicados en columnDefaultValue.
     * @param er the er
     * @param columnKey the column key
     * @param columnDefaultValue the column default value
     */
    public static void addColumn(EntityResult er, Object columnKey, Object columnDefaultValue) {
        int nreg = er.calculateRecordNumber();
        Object[] array = new Object[nreg];
        Arrays.fill(array, columnDefaultValue);
        er.put(columnKey, new ArrayList(Arrays.asList(array)));
    }

    /**
     * Permite actualizar filas de un {@link EntityResult} que cumplen un criterio.
     * @param er the er
     * @param updater the updater
     * @param criteria the criteria
     */
    public static void update(EntityResult er, IRowUpdater updater, Map<?, ?> criteria) {
        if (er == null) {
            return;
        }

        int nres = er.calculateRecordNumber();
        for (int i = 0; i < nres; i++) {
            if (EntityResultTools.checkFilter(er, i, criteria)) {
                updater.updateRow(er, i);
            }
        }
    }

    /**
     * Reemplaza en la columna el valueToReplace por el newValue.
     * @param rs the rs
     * @param columnName the column name
     * @param valueToReplace the value to replace
     * @param newValue the new value
     */
    public static void replaceValue(EntityResult rs, String columnName, Object valueToReplace, Object newValue) {
        List testList = (List) rs.get(columnName);
        if (testList != null) {
            for (int i = 0; i < testList.size(); i++) {
                Object test = testList.get(i);
                if ((test == null) && (valueToReplace == null)) {
                    testList.set(i, newValue);
                } else if ((test != null) && test.equals(valueToReplace)) {
                    testList.set(i, newValue);
                }
            }
        }
    }

    /**
     * Convierte a mayusculas los datos de la columna indicada.
     * @param res the res
     * @param columnNames the column names
     */
    public static void doUpper(EntityResult res, String... columnNames) {
        if ((columnNames == null) || (res == null)) {
            return;
        }
        for (String columnName : columnNames) {
            List v = (List) res.get(columnName);
            if (v != null) {
                for (int i = 0; i < v.size(); i++) {
                    Object ob = v.get(i);
                    if (ob instanceof String) {
                        v.set(i, ((String) ob).toUpperCase());
                    }
                }
            }
        }
    }

    /**
     * Compose a complex BasicExpresion based on IN exresion, consideing to separate large list in
     * shorter blocks, to build a valid sentence.
     * @param listValues the list values
     * @param field the field
     * @return the basic expression
     */
    public static BasicExpression cutThousandsInExpresions(List<?> listValues, String field) {
        if (listValues == null) {
            return null;
        }

        List<Object> vThousands = new ArrayList<>();
        BasicExpression be = null;
        int counter = 0;
        for (Object item : listValues) {
            counter++;
            vThousands.add(item);
            if (counter == (EntityResultTools.MAX_IN_EXPRESSION_SUPPORT - 1)) {
                BasicExpression be1 = new BasicExpression(new BasicField(field), BasicOperator.IN_OP, vThousands);
                if (be == null) {
                    be = be1;
                } else {
                    be = new BasicExpression(be, BasicOperator.OR_OP, be1);
                }
                counter = 0;
                vThousands = new ArrayList<>();
            }
        }
        if (!vThousands.isEmpty()) {
            BasicExpression be1 = new BasicExpression(new BasicField(field), BasicOperator.IN_OP, vThousands);
            if (be == null) {
                be = be1;
            } else {
                be = new BasicExpression(be, BasicOperator.OR_OP, be1);
            }
        }
        return be;
    }

    /**
     * Metodo auxiliar para crear rapidamente un registro en base a las claves y los valores.
     * @param keys the keys
     * @param values the values
     * @return the Map
     */
    public static Map<String, Object> createRecord(String[] keys, Object[] values) {
        Map<String, Object> record = new HashMap<>();

        for (int i = 0; i < keys.length; i++) {
            if ((values[i] != null) && (keys[i] != null)) {
                record.put(keys[i], values[i]);
            }
        }

        return record;
    }

    /**
     * Interfaz para poder realizar updates sobre un {@link EntityResult}.
     */
    public interface IRowUpdater {

        /**
         * Update row.
         * @param er the er
         * @param row the row
         */
        void updateRow(EntityResult er, int row);

    }

    public static List<String> attributes(String... attributes) {
        if ((attributes == null) || (attributes.length == 0)) {
            return new ArrayList<>();
        }
        return new ArrayList(Arrays.asList(attributes));
    }

    /**
     * Utility to use with Ontimize entities, to specify filters(keys-values) more fast. <br>
     * Will be received a sequence of objects with <key><value>[<key><value>...]. <br>
     * Example: query(EntityResultTools.keysvalues("AA", new Integer(1), "BB", identifier), av, ses,
     * con).
     * @param objects
     * @return
     */
    public static Map<Object, Object> keysvalues(Object... objects) {
        if (objects == null) {
            return new HashMap<>();
        }
        if ((objects.length % 2) != 0) {
            throw new RuntimeException("Review filters, it is mandatory to set dual <key><value>.");
        }

        Map<Object, Object> res = new HashMap<>();
        int i = 0;
        while (i < objects.length) {
            Object key = objects[i++];
            Object value = objects[i++];
            MapTools.safePut(res, key, value);
            if ((key == null) || (value == null)) {
                EntityResultTools.logger.debug("skipping pair with null value <{},{}>", key, value);
            }
        }
        return res;
    }

    /**
     * Remove duplicates from an EntityResult.
     * @param res the res
     * @return the entity result
     */
    public static EntityResult doRemoveDuplicates(EntityResult in) {
        EntityResult res = new EntityResultMapImpl();
        EntityResultTools.initEntityResult(res, new ArrayList(in.keySet()));
        int nInRegs = in.calculateRecordNumber();
        int nResRegs = 0;
        for (int i = 0; i < nInRegs; i++) {
            Map<?, ?> row = in.getRecordValues(i);
            if (!EntityResultTools.containsRow(res, nResRegs, row)) {
                res.addRecord((Map) row);
                nResRegs++;
            }
        }
        return res;
    }

    /**
     * Contains row.
     * @param res the res
     * @param row the row
     * @return true, if successful
     */
    public static boolean containsRow(EntityResult res, Map<?, ?> row) {
        return EntityResultTools.containsRow(res, res.calculateRecordNumber(), row);
    }

    /**
     * Contains row.
     * @param res the res
     * @param nregs the nregs
     * @param row the row
     * @return true, if successful
     */
    public static boolean containsRow(EntityResult res, int nregs, Map<?, ?> row) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < nregs; i++) {
            Map<?, ?> record = res.getRecordValues(i);
            if (row.equals(record)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterates <code>attr</code> list and delete {@link TableAttribute} and
     * {@link ReferenceFieldAttribute} if it's contained in <code>toDelete</code> list
     * @param toDelete names to delete
     * @param attr atributes
     */
    public static void cleanNonRequiredTableAttributes(List<String> toDelete, List<String> attr) {
        for (int i = 0; i < attr.size(); i++) {
            Object ob = attr.get(i);
            String nameToDelete = null;
            if (ob instanceof TableAttribute) {
                nameToDelete = ((TableAttribute) ob).getEntity();
            } else if (ob instanceof String) {
                nameToDelete = (String) ob;
            } else if (ob instanceof ReferenceFieldAttribute) {
                nameToDelete = ((ReferenceFieldAttribute) ob).getEntity();
            }

            if ((nameToDelete != null) && toDelete.contains(nameToDelete)) {
                attr.remove(i);
                i--;
            }
        }
    }

    /**
     * Compare both ERs, that can have different order.
     * @param erBackup
     * @param er
     * @return true if exact match, false in other case
     */
    public static boolean compare(EntityResult erBackup, EntityResult er) {
        if ((erBackup == null) && (er == null)) {
            return true;
        } else if (((erBackup == null) || erBackup.isEmpty()) && (er != null) && !er.isEmpty()) {
            return false;
        } else if (((er == null) || er.isEmpty()) && (erBackup != null) && !erBackup.isEmpty()) {
            return false;
        } else if (er.calculateRecordNumber() != erBackup.calculateRecordNumber()) {
            return false;
        } else {

            int num = erBackup.calculateRecordNumber();
            for (int i = 0; i < num; i++) {
                if (!EntityResultTools.containsRow(er, erBackup.getRecordValues(i))) {
                    return false;
                }
            }
            return true;
        }

    }

    public static interface IRecordFilter {

        boolean evaluate(Object test);

    }

    public static class RecordLikeFilter implements IRecordFilter {

        protected String regEx;

        protected Pattern p;

        public RecordLikeFilter(String regEx) {
            this.regEx = regEx;
            this.p = Pattern.compile(regEx);

        }

        @Override
        public boolean evaluate(Object test) {
            if (test == null) {
                return false;
            }
            Matcher m = this.p.matcher(test.toString());
            return m.matches();
        }

    }

    public static EntityResult pivot(EntityResult er, String pivotColumn, List<String> otherColumns,
            IAggregateFunction operation) throws Exception {
        EntityResult res = new EntityResultMapImpl();
        if ((er == null) || (pivotColumn == null) || (operation == null)) {
            return res;
        }
        HashSet<Object> setPivotColumn = new HashSet<>((List) er.get(pivotColumn));
        List<String> resColumns = new ArrayList<>();
        resColumns.addAll(Arrays.asList(setPivotColumn.toArray(new String[] {})));
        resColumns.addAll(otherColumns);
        EntityResultTools.initEntityResult(res, resColumns);

        for (Object pivotValue : setPivotColumn) {
            EntityResult filteredRes = EntityResultTools.dofilter(er,
                    EntityResultTools.keysvalues(pivotColumn, pivotValue));
            EntityResult group = EntityResultTools.doGroup(filteredRes, otherColumns.toArray(new String[] {}),
                    operation);

            List<String> aggregatedColumnNames = operation.getAggregatedColumnNames();
            if (aggregatedColumnNames.size() > 1) {
                EntityResultTools.logger.warn("Operation with aggregated values more than one not supported");
            }
            EntityResultTools.renameColumn(group, aggregatedColumnNames.get(0), pivotValue.toString());

            res = EntityResultTools.doUnionAll(res, group);
        }
        List<IAggregateFunction> groupOperations = new ArrayList<>();
        for (Object pivotValue : setPivotColumn) {
            groupOperations.add(new PivotGroupOperation(pivotValue.toString(), pivotValue.toString()));
        }
        return EntityResultTools.doGroup(res, otherColumns.toArray(new String[] {}),
                groupOperations.toArray(new IAggregateFunction[] {}));
    }

    public static class PivotPartialAggregateValue implements IPartialAggregateValue {

        Object value = null;

    }

    public static class PivotGroupOperation extends AbstractAggregateFunction<PivotPartialAggregateValue> {

        public PivotGroupOperation(String opColumnName, String resultColumnName) {
            super(opColumnName, resultColumnName);
        }

        @Override
        public Map<String, Object> computeAggregatedGroupValue(PivotPartialAggregateValue partialValue) {
            Map<String, Object> res = new HashMap<>();
            res.put(this.getResultColumn(), partialValue == null ? null : partialValue.value);
            return res;
        }

        @Override
        public PivotPartialAggregateValue onNewGroupRecord(PivotPartialAggregateValue partialValue, EntityResult res,
                int idx) {
            if (partialValue == null) {
                partialValue = new PivotPartialAggregateValue();
            }
            List<?> val = (List<?>) res.get(this.getOpColumn());
            if (val != null) {
                Object nb = val.get(idx);
                if (nb != null) {
                    if (partialValue.value != null) {
                        EntityResultTools.logger.warn("more than one not null value grouping in pivot");
                    }
                    partialValue.value = nb;
                }
            }
            return partialValue;
        }

    }

}
