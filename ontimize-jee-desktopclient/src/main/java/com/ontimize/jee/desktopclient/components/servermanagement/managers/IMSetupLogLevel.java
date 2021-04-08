package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.annotation.FormComponent;
import com.ontimize.db.EntityResult;
import com.ontimize.gui.BasicInteractionManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.container.Row;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService.OntimizeJEELogger;
import com.ontimize.jee.common.tools.EntityResultTools;
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
    protected Button bRefresh;

    @FormComponent(attr = "RESULTS")
    protected Row rowTable;

    @FormComponent(attr = "DETAILS")
    protected Table table;

    protected IServerManagementService serverManagement;

    public IMSetupLogLevel() {
        super();
    }

    @Override
    public void registerInteractionManager(Form f, IFormManager gf) {
        super.registerInteractionManager(f, gf);
        this.managedForm.setFormTitle("Setup log level");
        this.serverManagement = BeansFactory.getBean(IServerManagementService.class);

        this.table.getJTable().setDefaultEditor(Level.class, new LevelCellEditor());
        this.table.getJTable().setDefaultRenderer(Level.class, new LevelCellRenderer());
        this.table.setRowNumberColumnVisible(false);

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

        this.bRefresh.addActionListener(new RefreshSetupLogLevelListener(this.table));
    }

    public class RefreshSetupLogLevelListener implements ActionListener {

        private final Table table;

        public RefreshSetupLogLevelListener(Table table) {
            super();
            this.table = table;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    List<OntimizeJEELogger> loggers = RefreshSetupLogLevelListener.this.getLoggerList();
                    RefreshSetupLogLevelListener.this.table.setValue(this.convertListToER(loggers));
                }

                private Object convertListToER(List<OntimizeJEELogger> loggers) {
                    EntityResult res = new EntityResult(RefreshSetupLogLevelListener.this.table.getAttributeList());
                    if (loggers != null) {
                        for (OntimizeJEELogger oJEELogger : loggers) {
                            EntityResultTools.fastAddRecord(res,
                                    EntityResultTools.keysvalues("OntimizeLogger", oJEELogger));
                        }
                    }
                    return res;
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
            return new ArrayList<>();
        }

    }

}
