package com.ontimize.jee.webclient.export.rule;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public interface CellSelectionRule {

    boolean match(int row, int col);

}
