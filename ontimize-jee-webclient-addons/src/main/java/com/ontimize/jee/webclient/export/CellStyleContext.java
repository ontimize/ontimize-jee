package com.ontimize.jee.webclient.export;

import java.util.function.Supplier;

/**
 * Contexto que se envía al usuario para que pueda decidir qué estilo tiene una celda. Lleva
 * información de la celda y su valor y también un creador de estilos excel y otro de formatos.
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */

public class CellStyleContext<T, D> {

    private final int row;

    private final int col;

    private final String columnId;

    private final Object value;

    T excelStyle;

    Supplier<T> cellStyleCreator;

    Supplier<D> dataFormatCreator;

    public CellStyleContext(
            final int row, final int colUserIndex, final String columnId, final Object cellValue, final T excelStyle,
            final Supplier<T> cellStyleCreator,
            final Supplier<D> dataFormatCreator) {
        this.row = row;
        this.col = colUserIndex;
        this.columnId = columnId;
        this.value = cellValue;
        this.excelStyle = excelStyle;
        this.cellStyleCreator = cellStyleCreator;
        this.dataFormatCreator = dataFormatCreator;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public String getColumnId() {
        return this.columnId;
    }

    public Object getValue() {
        return this.value;
    }

    public T getExcelStyle() {
        return this.excelStyle;
    }

    public Supplier<T> getCellStyleCreator() {
        return this.cellStyleCreator;
    }

    public Supplier<D> getDataFormatCreator() {
        return this.dataFormatCreator;
    }

}
