package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.annotation.FormComponent;
import com.ontimize.gui.BasicInteractionManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.container.Row;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.table.ExtendedTableModel;
import com.ontimize.gui.table.Table;
import com.ontimize.gui.table.TableSorter;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService.OntimizeJEELogger;
import com.ontimize.jee.desktopclient.spring.BeansFactory;
import com.ontimize.util.logging.Level;
import com.ontimize.util.logging.LevelCellEditor;
import com.ontimize.util.logging.LevelCellRenderer;

/**
 * The Class IMLiveLogConsole.
 */
public class IMSetupLogLevel extends BasicInteractionManager {
	private static final Logger logger = LoggerFactory.getLogger(IMSetupLogLevel.class);

	@FormComponent(attr = "B_REFRESH")
	protected Button	bRefresh;
	@FormComponent(attr = "RESULTS")
	protected Row		rowTable;

	@FormComponent(attr = "DETAILS")
	protected Table table;

	protected IServerManagementService serverManagement;

	protected LoggerModel tableModel;

	public IMSetupLogLevel() {
		super();
	}

	@Override
	public void registerInteractionManager(Form f, IFormManager gf) {
		super.registerInteractionManager(f, gf);
		this.managedForm.setFormTitle("Setup log level");
		this.serverManagement = BeansFactory.getBean(IServerManagementService.class);

		this.tableModel = new LoggerModel(this.serverManagement, this.table.getVisibleColumns());
		this.table.getJTable().setModel(this.tableModel);

		// this.table.getJTable().getColumnModel().getColumn(1).setMinWidth(60);// TODO select column index by name
		this.table.getJTable().setDefaultEditor(Level.class, new LevelCellEditor());
		this.table.getJTable().setDefaultRenderer(Level.class, new LevelCellRenderer());
		this.table.setRowNumberColumnVisible(false);

		// TODO: fix
		Enumeration<TableColumn> columns = this.table.getJTable().getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			TableColumn tableColumn = columns.nextElement();
			String headerValue = (String) tableColumn.getHeaderValue();
			if ("Logger".equals(headerValue)) {
				tableColumn.setMinWidth(350);
			} else if ("Level".equals(headerValue)) {
				tableColumn.setMinWidth(60);
			} else if (!"ROW_NUMBERS_COLUMN".equals(headerValue)) {
				tableColumn.setMinWidth(50);
			}
		}

		this.bRefresh.addActionListener(new RefreshSetupLogLevelListener(this.tableModel));
	}

	class LoggerModel extends TableSorter {
		public LoggerModel(IServerManagementService manager, Vector columns) {
			super(new CustomLoggerExtendedTableModel(new Hashtable<>(), columns, new Hashtable<>(), true, manager));
		}

		public void setList(List<OntimizeJEELogger> list) {
			((CustomLoggerExtendedTableModel) ((FilterTableModel) ((GroupTableModel) this.getModel()).getModel()).getModel()).setList(list);
		}
	}

	public class CustomLoggerExtendedTableModel extends ExtendedTableModel {
		protected IServerManagementService		manager;
		final protected List<OntimizeJEELogger>	list	= new ArrayList<>();

		public CustomLoggerExtendedTableModel(Hashtable<Object, Object> hashtable, Vector columns, Hashtable<Object, Object> hashtable2, boolean b,
				IServerManagementService manager) {
			super(hashtable, columns, hashtable2, b);
			this.manager = manager;
		}

		@Override
		public int getRowCount() {
			if (this.list == null) {
				return 0;
			}
			return this.list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			OntimizeJEELogger logger = this.list.get(rowIndex);
			Level level = logger.getLoggerLevel();

			Object columnName = this.columnTexts.get(columnIndex);
			if ("Logger".equals(columnName)) {
				return logger.getLoggerName();
			} else if ("Level".equals(columnName)) {
				return level;
			} else if ("Trace".equals(columnName)) {
				return Level.TRACE.equals(level);
			} else if ("Debug".equals(columnName)) {
				return Level.DEBUG.equals(level);
			} else if ("Info".equals(columnName)) {
				return Level.INFO.equals(level);
			} else if ("Warn".equals(columnName)) {
				return Level.WARN.equals(level);
			} else if ("Error".equals(columnName)) {
				return Level.ERROR.equals(level);
			} else if ("Off".equals(columnName)) {
				return Level.OFF.equals(level);
			} else if ("Inherit".equals(columnName)) {
				return null;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if ("Level".equals(this.columnTexts.get(columnIndex))) {
				return Level.class;
			} else if (!"Logger".equals(this.columnTexts.get(columnIndex))) {
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex != 0) {
				return true;
			}
			return false;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			try {
				OntimizeJEELogger logger = this.list.get(rowIndex);
				Object columnName = this.columnTexts.get(columnIndex);
				if ("Level".equals(columnName)) {
					this.setLevel(logger, (Level) value);
					this.fireTableDataChanged();
					return;
				} else if ("Trace".equals(columnName)) {
					this.setLevel(logger, Level.TRACE);
				} else if ("Debug".equals(columnName)) {
					this.setLevel(logger, Level.DEBUG);
				} else if ("Info".equals(columnName)) {
					this.setLevel(logger, Level.INFO);
				} else if ("Warn".equals(columnName)) {
					this.setLevel(logger, Level.WARN);
				} else if ("Error".equals(columnName)) {
					this.setLevel(logger, Level.ERROR);
				} else if ("Off".equals(columnName)) {
					this.setLevel(logger, Level.OFF);
				} else if ("Inherit".equals(columnName)) {
					this.setLevel(logger, null);
				}
				this.getValueAt(rowIndex, 1);
			} catch (Exception e) {
				IMSetupLogLevel.logger.error("setValueAt", e);
			}
			this.fireTableDataChanged();
		}

		public Level getLevel(OntimizeJEELogger logger) {
			if (this.manager != null) {
				try {
					return this.manager.getLevel(logger);
				} catch (Exception e) {
					IMSetupLogLevel.logger.error("remote getLevel exception", e);
				}
			}
			return null;
		}

		public void setLevel(OntimizeJEELogger logger, Level level) throws Exception {
			if (this.manager != null) {
				logger.setLoggerLevel(level);
				this.manager.setLevel(logger);
			}
		}

		public void setList(List<OntimizeJEELogger> list) {
			this.list.clear();
			this.list.addAll(list);
			this.fireTableDataChanged();
		}

	}

	public class RefreshSetupLogLevelListener implements ActionListener {
		private final LoggerModel tableModel;

		public RefreshSetupLogLevelListener(LoggerModel tableModel) {
			super();
			this.tableModel = tableModel;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					List<OntimizeJEELogger> loggers = RefreshSetupLogLevelListener.this.getLoggerList();
					RefreshSetupLogLevelListener.this.tableModel.setList(loggers);
				}
			}).start();
		}

		protected List<OntimizeJEELogger> getLoggerList() {
			if (IMSetupLogLevel.this.serverManagement != null) {
				try {
					return IMSetupLogLevel.this.serverManagement.getLoggerList();
				} catch (Exception e) {
					IMSetupLogLevel.logger.error("getLoggerList exception", e);
				}
			}
			return new ArrayList<OntimizeJEELogger>();
		}
	}
}
