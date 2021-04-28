package com.ontimize.jee.webclient.export.rule;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public class RowModuleRule implements RowSelectionRule {

    int module;

    public RowModuleRule(int module) {
        this.module = module;
    }

    @Override
    public boolean match(int row) {
        return row % module == 0;
    }

}
