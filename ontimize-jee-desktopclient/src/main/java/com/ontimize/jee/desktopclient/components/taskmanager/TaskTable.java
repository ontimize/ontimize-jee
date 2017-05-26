package com.ontimize.jee.desktopclient.components.taskmanager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
	private static final Logger	logger						= LoggerFactory.getLogger(TaskTable.class);

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID			= 1L;

	private final static int	COLUMN_SYNC_STATUS_WIDTH	= 24;

	/**
	 * Instantiates a new task table.
	 */
	public TaskTable() {
		super(new TaskTableModel());

		// Allow only one row at a time to be selected.
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.addMouseListener(new MouseListenerFireActionTask());

		// Set up ProgressBar as renderer for progress column.
		ProgressRenderer progressRenderer = new ProgressRenderer();
		this.setDefaultRenderer(Object.class, new ObjectCellRenderer());

		this.getColumn(TaskTableModel.COLUMN_PROGRESS).setCellRenderer(progressRenderer);
		this.getColumn(TaskTableModel.COLUMN_SYNC_STATUS).setCellRenderer(new IconToolTipRenderer());
		try {
			this.getColumn(TaskTableModel.COLUMN_DESCRIPTION).setCellRenderer(new DescriptionTableCellRenderer()

			);
		} catch (Throwable ex) {
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
	}

	/**
	 * Gets the task model.
	 *
	 * @return the task model
	 */
	public TaskTableModel getTaskModel() {
		return (TaskTableModel) this.getModel();
	}

	/**
	 * Adds the task.
	 *
	 * @param task
	 *            the task
	 */
	public void addTask(ITask task) {
		this.getTaskModel().addTask(task);
	}

	class MouseListenerFireActionTask extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent me) {
			if (me.getClickCount() == 2) {
				int rowAtPoint = TaskTable.this.rowAtPoint(me.getPoint());
				TaskTable.this.getTaskModel().getRow(rowAtPoint).onTaskClicked();
			}
		}

	}
}
