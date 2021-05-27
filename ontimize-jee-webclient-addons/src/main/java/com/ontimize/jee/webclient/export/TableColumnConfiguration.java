package com.ontimize.jee.webclient.export;

import java.text.DecimalFormatSymbols;

import javafx.collections.ObservableList;

/**
 * @author <a href="antonio.vazquez@imatia.com">Antonio Vazquez Araújo</a>
 */
public interface TableColumnConfiguration {

    Class<?> getType();

    void setType(Class<?> type);

    String getId();

    void setId(String id);

    String getText();

    void setText(String text);

    int getOrder();

    void setOrder(int order);

    boolean isVisible();

    void setVisible(boolean visible);

    boolean isResizable();

    void setResizable(boolean resizable);

    boolean isSortable();

    void setSortable(boolean sortable);

    Class<?> getCellValueFactoryClass();

    void setCellValueFactoryClass(Class<?> cellValueFactoryClass);

    Class<?> getCellFactoryClass();

    void setCellFactoryClass(Class<?> cellFactoryClass);

    double getMinWidth();

    void setMinWidth(double minWidth);

    double getWidth();

    void setWidth(double width);

    double getMaxWidth();

    void setMaxWidth(double maxWidth);

    ObservableList<TableColumnConfiguration> getColumns();

    void setColumns(ObservableList<TableColumnConfiguration> columns);

    String getStyle();

    void setStyle(String style);

    String getPattern();

    void setPattern(String pattern);

    String getSymbol();

    void setSymbol(String symbol);

    DecimalFormatSymbols getDecimalSymbols();

    void setDecimalSymbols(DecimalFormatSymbols decimalSymbols);

}
