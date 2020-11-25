package com.ontimize.jee.webclient.excelexport.rule;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public interface CellSelectionRule {

    boolean match(int row, int col);
}
