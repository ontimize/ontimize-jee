package com.ontimize.jee.webclient.export.rule;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public class RowNumberRule implements RowSelectionRule {

    int row = -1;

    public RowNumberRule(int row) {
        this.row = row;
    }

    @Override
    public boolean match(int row) {
        return row == this.row;
    }

}
