package com.ontimize.jee.desktopclient.components.taskmanager;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.ontimize.gui.table.CellRenderer;
import com.ontimize.jee.common.tools.FileTools;

/**
 * The Class ByteSizeTableCellRenderer.
 */
public class ByteSizeTableCellRenderer extends CellRenderer {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new byte size table cell renderer.
     */
    public ByteSizeTableCellRenderer() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.table.CellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     * java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus,
            int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, null, selected, hasFocus, row, column);
        if ((value != null) && (value instanceof Number)) {
            ((JLabel) comp).setText(FileTools.readableFileSize(((Number) value).longValue()));
            ((JLabel) comp).setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return comp;
    }

}
