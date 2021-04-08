package com.ontimize.jee.desktopclient.components.taskmanager;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.ontimize.gui.table.CellRenderer;

/**
 * This class renders a JProgressBar in a table cell.
 */
public class ProgressRenderer extends CellRenderer implements TableCellRenderer {

    private static final long serialVersionUID = -4716351834035301680L;

    /**
     * Constructor for ProgressRenderer.
     */
    public ProgressRenderer() {
        super();
        this.component = new JProgressBar(0, 10000);
        ((JProgressBar) this.component).setStringPainted(true); // show progress text
    }

    /**
     * Returns this JProgressBar as the renderer for the given table cell.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Component component = super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
        if (value == null) {
            ((JProgressBar) component).setIndeterminate(true);
        } else {
            ((JProgressBar) component).setIndeterminate(false);
            ((JProgressBar) component).setValue((int) (((Number) value).floatValue() * 10000));
        }
        return component;
    }

}
