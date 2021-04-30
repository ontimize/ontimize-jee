package com.ontimize.jee.webclient.export.rule;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public class RowRangeRule implements RowSelectionRule {

    int fromRow = -1;

    int toRow = -1;

    public RowRangeRule(int fromRow, int toRow) {
        this.fromRow = fromRow;
        this.toRow = toRow;
    }

    @Override
    public boolean match(int row) {
        boolean lowerCondition = true;
        boolean upperCondition = true;
        if (fromRow != -1) {
            lowerCondition = row >= fromRow;
        }
        if (toRow != -1) {
            upperCondition = row <= toRow;
        }
        return lowerCondition && upperCondition;
    }

}
