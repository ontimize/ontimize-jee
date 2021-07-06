package com.ontimize.jee.webclient.export;

import javafx.collections.ObservableList;

/**
 * @author <a href="antonio.vazquez@imatia.com">Antonio Vazquez Araújo</a>
 */
public interface TableConfiguration {

    boolean isEditable();

    void setEditable(boolean editable);

    ObservableList<TableColumnConfiguration> getColumns();

    TableColumnConfiguration getColumnById(String id);

    void setColumns(ObservableList<TableColumnConfiguration> columns);

    TableConfiguration addColumn(TableColumnConfiguration column);

    ObservableList<String> getExcludeColumns();

    void setExcludeColumns(String[] excludeColumns);

    ObservableList<String> getSortColumns();

    void setSortColumns(String[] sortColumns);

}
