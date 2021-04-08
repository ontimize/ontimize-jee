/*
 *
 */
package com.ontimize.jee.desktopclient.tools;

import java.util.Vector;

import javax.swing.table.TableModel;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.tools.FastQSortAlgorithm;
import com.ontimize.report.TableSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidades para EntityResult.
 */
public final class EntityResultTools {

    private static final Logger logger = LoggerFactory.getLogger(EntityResultTools.class);

    private EntityResultTools() {
        super();
    }

    /**
     * Do sort.
     * @param res the res
     * @param cols the cols
     * @return the entity result
     */
    public static EntityResult doSort(EntityResult res, String... cols) {
        if (cols.length == 1) {
            return EntityResultTools.doFastSort(res, cols[0]);
        }
        return EntityResultTools.doSlowSort(res, cols);
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
        Object[] keysSorted = ((Vector) a.get(col)).toArray();
        int[] indexes = FastQSortAlgorithm.sort(keysSorted);
        Vector cols = new Vector(a.keySet());
        EntityResult res = new EntityResult();
        com.ontimize.jee.common.tools.EntityResultTools.initEntityResult(res, cols, indexes.length);
        for (Object key : cols) {
            Vector vOrig = (Vector) a.get(key);
            Vector vDest = (Vector) res.get(key);
            for (int i = 0; i < indexes.length; i++) {
                vDest.add(i, vOrig.get(indexes[i]));
            }
        }
        return res;
    }

    /**
     * Do slow sort.
     * @param res the res
     * @param cols the cols
     * @return the entity result
     */
    protected static EntityResult doSlowSort(EntityResult res, String... cols) {
        if (res != null) {
            TableModel model = com.ontimize.db.EntityResultUtils.createTableModel(res, new Vector(res.keySet()), false,
                    false, false);
            TableSorter sorter = new TableSorter(model) {

                @Override
                public int compareRowsByColumn(int row1, int row2, int column) {
                    Class type = this.model.getColumnClass(column);
                    TableModel data = this.model;
                    if (type == java.lang.String.class) {
                        String s1 = (String) data.getValueAt(row1, column);
                        String s2 = (String) data.getValueAt(row2, column);
                        if (s1 == null) {
                            return -1;
                        }
                        if (s2 == null) {
                            return 1;
                        }
                        return s1.compareToIgnoreCase(s2);
                    } else {
                        return super.compareRowsByColumn(row1, row2, column);
                    }
                }
            };
            for (String column : cols) {
                sorter.sortByColumn(EntityResultTools.getColumnIndex(sorter, column));
            }
            return EntityResultTools.getOrderedEntityResult(sorter, res);
        } else {
            return res;
        }

    }

    /**
     * Gets the ordered entity result.
     * @param sorter the sorter
     * @param er the er
     * @return the ordered entity result
     */
    protected static EntityResult getOrderedEntityResult(TableSorter sorter, EntityResult er) {
        EntityResult res = new EntityResult();
        com.ontimize.jee.common.tools.EntityResultTools.initEntityResult(res, new Vector(er.keySet()));
        int j = 0;
        for (int i : sorter.getIndexes()) {
            com.ontimize.jee.common.tools.EntityResultTools.fastAddRecord(res, j, er, i);
            j++;
        }
        return res;
    }

    /**
     * Gets the column index.
     * @param model the model
     * @param column the column
     * @return the column index
     */
    protected static int getColumnIndex(TableModel model, String column) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (column.equals(model.getColumnName(i))) {
                return i;
            }
        }
        return -1;
    }

}
