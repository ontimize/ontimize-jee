package com.ontimize.jee.desktopclient.components.taskmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.ontimize.jee.common.tools.ObjectTools;

/**
 * This class manages the download table's data.
 *
 */
public class TaskTableModel extends AbstractTableModel implements Observer {

    private static final long serialVersionUID = 2376138748556807794L;

    public static final String COLUMN_DESCRIPTION = "task.DESC";

    public static final String COLUMN_SYNC_STATUS = "task.SYNC_STATUS";

    public static final String COLUMN_EXEC_DETAILS = "task.EXEC_DETAILS";

    public static final String COLUMN_PROGRESS = "task.PROGRESS";

    public static final String COLUMN_NAME = "task.NAME";

    // These are the names for the table's columns.
    private static final String[] columnNames = { TaskTableModel.COLUMN_NAME, TaskTableModel.COLUMN_DESCRIPTION,
            TaskTableModel.COLUMN_PROGRESS, TaskTableModel.COLUMN_SYNC_STATUS, TaskTableModel.COLUMN_EXEC_DETAILS };

    // These are the classes for each column's values.
    private static final Class<?>[] columnClasses = { String.class, Object.class, Number.class, TaskStatus.class,
            Object.class };

    private final transient List<ITask> taskList;

    public TaskTableModel() {
        super();
        this.taskList = new ArrayList<>();
    }

    /**
     * Remove a download from the list.
     */
    public void removeRow(int row) {
        if (row >= 0) {
            this.taskList.remove(row);
            // Fire table row deletion notification to table.
            this.fireTableRowsDeleted(row, row);
        }
    }

    public void removeTask(ITask task) {
        this.removeRow(this.taskList.indexOf(task));
    }

    /**
     * Clean all.
     */
    public void cleanFinishedTasks() {
        for (int i = 0; i < this.taskList.size(); i++) {
            ITask task = this.taskList.get(i);
            if (task.isFinished()) {
                this.taskList.remove(i);
                i--;
            }
        }
        this.fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return TaskTableModel.COLUMN_EXEC_DETAILS.equals(TaskTableModel.columnNames[columnIndex])
                && this.taskList.get(rowIndex).hasResultDetails();
    }

    /**
     * Get table's column count.
     */
    @Override
    public int getColumnCount() {
        return TaskTableModel.columnNames.length;
    }

    /**
     * Get a column's name.
     */
    @Override
    public String getColumnName(int col) {
        return TaskTableModel.columnNames[col];
    }

    /**
     * Get a column's class.
     */
    @Override
    public Class<?> getColumnClass(int col) {
        return TaskTableModel.columnClasses[col];
    }

    /**
     * Get table's row count.
     */
    @Override
    public int getRowCount() {
        return this.taskList.size();
    }

    /**
     * Get value for a specific row and column combination.
     */
    @Override
    public Object getValueAt(int row, int col) {
        // Get download from download list
        ITask task = this.taskList.get(row);

        switch (col) {
            case 0: // Filename
                return task.getName();
            case 1: // Size
                return ObjectTools.coalesce(task.getSize(), task.getDescription());
            case 2: // Progress
                return task.getProgress();
            case 3: // Sync Status
                return task.getStatus();
            case 4:
                return task;
        }
        return "";
    }

    /**
     * Update is called when a Download notifies its observers of any changes
     */
    @Override
    public void update(Observable observable, Object arg) {
        SwingUtilities.invokeLater(() -> {
            int index = this.indexOfObservable(observable);
            if (index < 0) {
                return;
            }
            // Fire table row update notification to table.
            this.fireTableRowsUpdated(index, index);
        });
    }

    private int indexOfObservable(Observable o) {
        if (o != null) {
            for (int i = 0, size = this.taskList.size(); i < size; i++) {
                if (o.equals(this.taskList.get(i).getObservable())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void addTask(ITask task) {
        if (task == null) {
            return;
        }
        this.taskList.add(0, task);
        task.getObservable().addObserver(this);
        this.fireTableRowsInserted(this.getRowCount() - 1, this.getRowCount() - 1);
    }

    public ITask getRow(int index) {
        if ((index >= 0) && (index < this.taskList.size())) {
            return this.taskList.get(index);
        }
        return null;
    }

}
