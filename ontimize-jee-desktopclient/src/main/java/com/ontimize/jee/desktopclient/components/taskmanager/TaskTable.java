package com.ontimize.jee.desktopclient.components.taskmanager;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.table.ObjectCellRenderer;

/**
 * The Class TaskTable.
 */
public class TaskTable extends JTable {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(TaskTable.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private static final int COLUMN_SYNC_STATUS_WIDTH = 24;

    private static final int COLUMN_PROGRESS_WIDTH = 100;

    /**
     * Instantiates a new task table.
     */
    public TaskTable() {
        super(new TaskTableModel());

        // Allow only one row at a time to be selected.
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set up ProgressBar as renderer for progress column.
        ProgressRenderer progressRenderer = new ProgressRenderer();
        this.setDefaultRenderer(Object.class, new ObjectCellRenderer());

        this.getColumn(TaskTableModel.COLUMN_PROGRESS).setCellRenderer(progressRenderer);
        this.getColumn(TaskTableModel.COLUMN_SYNC_STATUS).setCellRenderer(new TaskStatusTableCellRenderer());
        this.getColumn(TaskTableModel.COLUMN_EXEC_DETAILS).setCellRenderer(new TaskResultTableCellRenderer());
        this.getColumn(TaskTableModel.COLUMN_EXEC_DETAILS).setCellEditor(new TaskResultTableCellEditor());
        try {
            this.getColumn(TaskTableModel.COLUMN_DESCRIPTION).setCellRenderer(new DescriptionTableCellRenderer());
        } catch (Exception ex) {
            TaskTable.logger.error(null, ex);
        }
        TableCellRenderer headerRenderer = this.getTableHeader().getDefaultRenderer();
        this.getTableHeader().setDefaultRenderer(new TranslatedTableCellRenderer(headerRenderer));

        // Set table's row height large enough to fit JProgressBar.
        this.setRowHeight((int) progressRenderer.getPreferredSize().getHeight());

        // Column size
        this.getColumn(TaskTableModel.COLUMN_SYNC_STATUS).setMinWidth(TaskTable.COLUMN_SYNC_STATUS_WIDTH);
        this.getColumn(TaskTableModel.COLUMN_SYNC_STATUS).setMaxWidth(TaskTable.COLUMN_SYNC_STATUS_WIDTH);
        this.getColumn(TaskTableModel.COLUMN_SYNC_STATUS).setWidth(TaskTable.COLUMN_SYNC_STATUS_WIDTH);
        this.getColumn(TaskTableModel.COLUMN_SYNC_STATUS).setPreferredWidth(TaskTable.COLUMN_SYNC_STATUS_WIDTH);
        this.getColumn(TaskTableModel.COLUMN_SYNC_STATUS).setResizable(false);
        this.getColumn(TaskTableModel.COLUMN_SYNC_STATUS).setHeaderValue("");

        this.getColumn(TaskTableModel.COLUMN_EXEC_DETAILS).setMinWidth(TaskTable.COLUMN_SYNC_STATUS_WIDTH);
        this.getColumn(TaskTableModel.COLUMN_EXEC_DETAILS).setMaxWidth(TaskTable.COLUMN_SYNC_STATUS_WIDTH);
        this.getColumn(TaskTableModel.COLUMN_EXEC_DETAILS).setWidth(TaskTable.COLUMN_SYNC_STATUS_WIDTH);
        this.getColumn(TaskTableModel.COLUMN_EXEC_DETAILS).setPreferredWidth(TaskTable.COLUMN_SYNC_STATUS_WIDTH);
        this.getColumn(TaskTableModel.COLUMN_EXEC_DETAILS).setResizable(false);
        this.getColumn(TaskTableModel.COLUMN_EXEC_DETAILS).setHeaderValue("");

        this.getColumn(TaskTableModel.COLUMN_PROGRESS).setWidth(TaskTable.COLUMN_PROGRESS_WIDTH);
        this.getColumn(TaskTableModel.COLUMN_PROGRESS).setPreferredWidth(TaskTable.COLUMN_PROGRESS_WIDTH);
    }

    /**
     * Gets the task model.
     * @return the task model
     */
    public TaskTableModel getTaskModel() {
        return (TaskTableModel) this.getModel();
    }

    /**
     * Adds the task.
     * @param task the task
     */
    public void addTask(ITask task) {
        this.getTaskModel().addTask(task);
    }

}
