package com.ontimize.jee.webclient.export.rule;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public class CellRangeRule implements CellSelectionRule {

    int fromRow = -1;

    int fromCol = -1;

    int toRow = -1;

    int toCol = -1;

    public CellRangeRule(int fromRow, int fromCol, int toRow, int toCol) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
    }

    @Override
    public boolean match(int row, int col) {
        boolean lowerCondition = true;
        boolean upperCondition = true;
        if (fromRow != -1) {
            lowerCondition = row >= fromRow;
        }
        if (fromCol != -1) {
            lowerCondition &= col >= fromCol;
        }
        if (toRow != -1) {
            upperCondition = row <= toRow;
        }
        if (toCol != -1) {
            upperCondition &= col <= toCol;
        }
        return lowerCondition && upperCondition;
    }

}
