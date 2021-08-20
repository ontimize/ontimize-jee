/*
 * ColumnCellUtils.java 21-sep-2017
 *
 * Copyright 2017 Imatia.com
 */
package com.ontimize.jee.webclient.export.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

/**
 * @author <a href=""></a>
 *
 */
public class ColumnCellUtils {

    private ColumnCellUtils() {
        // no-op
    }

    private static final Class<?>[] numericTypes = new Class[] { byte.class, Byte.class, short.class, Short.class,
            int.class, Integer.class, long.class, Long.class, float.class, Float.class, double.class, Double.class,
            BigInteger.class, BigDecimal.class };

    private static final Class<?>[] dateTypes = new Class[] { LocalDate.class,
            LocalDateTime.class,
            Timestamp.class, Time.class, Date.class, java.util.Date.class };

    private static final Class<?>[] booleanTypes = new Class[] { boolean.class, Boolean.class };

    /**
     * Chequea si number.
     * @param type type
     * @return true, si number
     */
    public static boolean isNumber(final Class<?> type) {
        if (type == null) {
            return false;
        }
        for (final Class<?> cls : numericTypes) {
            if (type == cls) {
                return true;
            }
        }
        return false;
    }

    /**
     * Chequea si date.
     * @param type type
     * @return true, si date
     */
    public static boolean isDate(final Class<?> type) {
        if (type == null) {
            return false;
        }
        for (final Class<?> cls : dateTypes) {
            if (type == cls) {
                return true;
            }
        }
        return false;
    }

    /**
     * Chequea si boolean.
     * @param type type
     * @return true, si boolean
     */
    public static boolean isBoolean(final Class<?> type) {
        if (type == null) {
            return false;
        }
        for (final Class<?> cls : booleanTypes) {
            if (type == cls) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene type.
     * @param tableConfiguration table configuration
     * @return type
     */
    // public static Class<?> getType(final TableColumnConfiguration tableConfiguration) {
    // if (tableConfiguration == null) {
    // return null;
    // }
    // return tableConfiguration.getType();
    // }

    /***************************************************************************
     * * Private fields * *
     **************************************************************************/

    public static final StringConverter<?> defaultStringConverter = new StringConverter<Object>() {
        @Override
        public String toString(final Object t) {
            return t == null ? null : t.toString();
        }

        @Override
        public Object fromString(final String string) {
            return string;
        }
    };

    public static final StringConverter<?> defaultTreeItemStringConverter = new StringConverter<TreeItem<?>>() {
        @Override
        public String toString(final TreeItem<?> treeItem) {
            return ((treeItem == null) || (treeItem.getValue() == null)) ? "" : treeItem.getValue().toString();
        }

        @Override
        public TreeItem<?> fromString(final String string) {
            return new TreeItem<>(string);
        }
    };

    /***************************************************************************
     * * General convenience *
     *
     * Note: copied from CellUtils *
     **************************************************************************/

    /*
     * Simple method to provide a StringConverter implementation in various cell implementations.
     */
    @SuppressWarnings("unchecked")
    public static <T> StringConverter<T> defaultStringConverter() {
        return (StringConverter<T>) defaultStringConverter;
    }

    /*
     * Simple method to provide a TreeItem-specific StringConverter implementation in various cell
     * implementations.
     */
    @SuppressWarnings("unchecked")
    public static <T> StringConverter<TreeItem<T>> defaultTreeItemStringConverter() {
        return (StringConverter<TreeItem<T>>) defaultTreeItemStringConverter;
    }

    public static <T> String getItemText(final Cell<T> cell, final StringConverter<T> converter) {
        return converter == null ? cell.getItem() == null ? "" : cell.getItem().toString()
                : converter.toString(cell.getItem());
    }

    public static Node getGraphic(final TreeItem<?> treeItem) {
        return treeItem == null ? null : treeItem.getGraphic();
    }

    /***************************************************************************
     * * TextField convenience
     *
     * * Note: copied from CellUtils (methods are not accesible) *
     **************************************************************************/

    public static <T> void updateItem(final Cell<T> cell,
            final StringConverter<T> converter,
            final TextField textField) {
        updateItem(cell, converter, null, null, textField);
    }

    public static <T> void updateItem(final Cell<T> cell,
            final StringConverter<T> converter,
            final HBox hbox,
            final Node graphic,
            final TextField textField) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (textField != null) {
                    textField.setText(getItemText(cell, converter));
                }
                cell.setText(null);

                if ((graphic != null) && (hbox != null)) {
                    hbox.getChildren().setAll(graphic, textField);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(textField);
                }
            } else {
                cell.setText(getItemText(cell, converter));
                cell.setGraphic(graphic);
            }
        }
    }

    public static <T> void startEdit(final Cell<T> cell,
            final StringConverter<T> converter,
            final HBox hbox,
            final Node graphic,
            final TextField textField) {
        if (textField != null) {
            textField.setText(getItemText(cell, converter));
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, textField);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(textField);
        }

        if (textField != null) {
            textField.selectAll();
            // requesting focus so that key input can immediately go into the
            // TextField (see RT-28132)
            textField.requestFocus();
        }
    }

    public static <T> void cancelEdit(final Cell<T> cell, final StringConverter<T> converter, final Node graphic) {
        cell.setText(getItemText(cell, converter));
        cell.setGraphic(graphic);
    }

    public static <T> TextField createTextField(final Cell<T> cell, final StringConverter<T> converter) {
        final TextField textField = new TextField(getItemText(cell, converter));

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        textField.setOnAction(event -> {
            if (converter == null) {
                throw new IllegalStateException(
                        "Attempting to convert text input into Object, but provided "
                                + "StringConverter is null. Be sure to set a StringConverter "
                                + "in your cell factory.");
            }
            cell.commitEdit(converter.fromString(textField.getText()));
            event.consume();
        });
        textField.focusedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(oldValue) && Boolean.FALSE.equals(newValue)) {
                // Acaba de perder el foco...
                if (converter == null) {
                    throw new IllegalStateException(
                            "Attempting to convert text input into Object, but provided "
                                    + "StringConverter is null. Be sure to set a StringConverter "
                                    + "in your cell factory.");
                }
                cell.commitEdit(converter.fromString(textField.getText()));
            }

        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return textField;
    }

}
