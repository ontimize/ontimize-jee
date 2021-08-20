package com.ontimize.jee.webclient.export.rule;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public class CellCoordinatesRule implements CellSelectionRule {

    int row = -1;

    int col = -1;

    public CellCoordinatesRule(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean match(int row, int col) {
        return row == this.row && col == this.col;
    }

}
