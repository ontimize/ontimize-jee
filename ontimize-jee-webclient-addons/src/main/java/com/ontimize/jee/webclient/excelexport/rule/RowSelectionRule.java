package com.ontimize.jee.webclient.excelexport.rule;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public interface RowSelectionRule {

    boolean match(int row);
}
