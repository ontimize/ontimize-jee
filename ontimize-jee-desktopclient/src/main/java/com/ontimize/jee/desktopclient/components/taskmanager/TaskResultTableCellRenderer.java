package com.ontimize.jee.desktopclient.components.taskmanager;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import com.ontimize.gui.images.ImageManager;

/**
 * This class renders a JProgressBar in a table cell.
 */
public class TaskResultTableCellRenderer extends JButton implements TableCellRenderer {

    private static final long serialVersionUID = -4716351834035301680L;

    public static final ImageIcon iconResult = ImageManager.getIcon("ontimize-jee-images/details_16x16.png");

    /**
     * Constructor for ProgressRenderer.
     */
    public TaskResultTableCellRenderer() {
        super();
        this.setOpaque(false);
        this.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Returns this JProgressBar as the renderer for the given table cell.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Icon icon = null;
        String tooltip = "";
        if (((ITask) value).isFinished() && ((ITask) value).hasResultDetails()) {
            tooltip = "task.seeDetails";
            icon = TaskResultTableCellRenderer.iconResult;
        } else {
            return new JLabel();
        }
        this.setIcon(icon);
        this.setToolTipText(tooltip);
        return this;
    }

}
