package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.annotation.FormComponent;
import com.ontimize.db.EntityResult;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.BasicInteractionManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.MainApplication;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.field.MemoDataField;
import com.ontimize.gui.field.TextComboDataField;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.common.tools.MapTools;
import com.ontimize.jee.desktopclient.components.servermanagement.window.list.ListSql;
import com.ontimize.jee.desktopclient.components.servermanagement.window.list.SentenceSql;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class IMSqlQuery.
 */
public class IMSqlManager extends BasicInteractionManager {

    private static final Logger logger = LoggerFactory.getLogger(IMSqlManager.class);

    /** The connections button. */
    @FormComponent(attr = "B_REFRESH")
    protected Button bRefresh;

    /** The query field. */
    @FormComponent(attr = "QUERY")
    protected MemoDataField queryField;

    /** The query button. */
    @FormComponent(attr = "B_QUERY")
    protected Button bQuery;

    /** The results. */
    @FormComponent(attr = "RESULTS")
    protected Table tableResults;

    /** The history. */
    @FormComponent(attr = "SQL_LIST")
    protected ListSql sqlList;

    /** The ListSql buttons. */
    @FormComponent(attr = "SaveEntry")
    protected Button saveEntry;

    @FormComponent(attr = "SaveAsEntry")
    protected Button saveAsEntry;

    @FormComponent(attr = "DeleteEntry")
    protected Button deleteEntry;

    @FormComponent(attr = "DeleteAll")
    protected Button deleteAll;

    @FormComponent(attr = "CONNECTION_NAME")
    protected TextComboDataField comboConnectionName;

    private final String user = "DEV";

    public IMSqlManager() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.utilmize.client.fim.UBasicFIM#registerInteractionManager(com.ontimize.gui.Form,
     * com.ontimize.gui.FormManager)
     */
    @Override
    public void registerInteractionManager(Form f, IFormManager gf) {
        super.registerInteractionManager(f, gf);
        this.managedForm.setFormTitle("Sql manager");

        this.bRefresh.addActionListener(new ConnectionButtonListener());
        this.bQuery.addActionListener(new QueryButtonListener());
        this.configureRecentList();

        // Sql list Buttons
        this.saveEntry.addActionListener(new SaveEntry());
        this.saveAsEntry.addActionListener(new SaveAsEntry());
        this.deleteEntry.addActionListener(new DeletryEntry());
        this.deleteAll.addActionListener(new DeleteAll());
    }

    /**
     * Configure recent list.
     */
    protected void configureRecentList() {
        this.sqlList.addListMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    SentenceSql sentence = (SentenceSql) IMSqlManager.this.sqlList.getSelectedEntry();
                    IMSqlManager.this.queryField.deleteData();
                    IMSqlManager.this.queryField.setValue(sentence.getSQLSentence());
                }
            }
        });
        this.sqlList.setCellRenderer(new SentenceListRenderer());
    }

    /**
     * Load history list.
     */
    protected void loadHistoryList() {
        MainApplication application = (MainApplication) ApplicationManager.getApplication();
        if (this.user == null) {
            // this.user = application.getUser();//FIXME: DEV user by default
        }
        // Por defecto cargamos las 100 primeras
        this.sqlList.deleteData();
        for (int i = 0; i < 1000; i++) {
            String preference = application.getPreferences().getPreference(this.user, "SQLSentence" + i);
            if (preference != null) {
                this.sqlList.addEntry(new SentenceSql(preference, true, this.user));
            }
        }
    }

    /*******************
     * AUXILIAR CLASSES
     */

    /**
     * Install connections button listener.
     */
    public class ConnectionButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // Query to server connections available
                    try {
                        List<String> availableDataSources = BeansFactory.getBean(IServerManagementService.class)
                            .getAvailableDataSources();
                        if (availableDataSources == null) {
                            IMSqlManager.this.comboConnectionName.setValues(new Vector<String>());
                        } else {
                            IMSqlManager.this.comboConnectionName.setValues(new Vector<>(availableDataSources));
                        }
                    } catch (Exception ex) {
                        IMSqlManager.logger.error(null, ex);
                        IMSqlManager.this.comboConnectionName.setValues(new Vector<String>());
                    }
                }
            }).start();
        }

    }

    /**
     * Install query button listener.
     */
    public class QueryButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    String sql = (String) IMSqlManager.this.queryField.getValue();
                    String connectionName = (String) IMSqlManager.this.comboConnectionName.getValue();

                    if (sql != null) {
                        try {
                            EntityResult res = null;
                            if ((connectionName == null) || connectionName.trim().isEmpty()) {
                                IMSqlManager.this.managedForm.message("Seleccione el data source",
                                        Form.WARNING_MESSAGE);
                                return;
                            }
                            res = BeansFactory.getBean(IServerManagementService.class).executeSql(sql, connectionName);
                            if (res.getCode() == EntityResult.OPERATION_WRONG) {
                                IMSqlManager.this.tableResults.deleteData();
                                IMSqlManager.this.managedForm.message("Error en la consulta. " + res.getMessage(),
                                        Form.ERROR_MESSAGE);
                                return;
                            }
                            if (res.isEmpty()) {
                                IMSqlManager.this.managedForm.message("No ha habido resultados", Form.WARNING_MESSAGE);
                            }
                            QueryButtonListener.this.printEntityResult(res);
                            IMSqlManager.this.tableResults.deleteData();
                            IMSqlManager.this.tableResults.setValue(res);
                        } catch (Exception ex) {
                            IMSqlManager.logger.error(null, ex);
                            EntityResult res = new EntityResult();
                            Hashtable<String, String> data = new Hashtable<>();
                            data.put("EXCEPTION", ex.getClass().getName());
                            MapTools.safePut(data, "MESSAGE", ex.getMessage());
                            res.addRecord(data);
                            IMSqlManager.this.tableResults.setValue(res);
                        }
                    } else {
                        IMSqlManager.this.managedForm.message("Escriba su consulta SQL", Form.WARNING_MESSAGE);
                    }
                }
            }).start();
        }

        /**
         * Prints the entity result.
         * @param res the res
         */
        public void printEntityResult(EntityResult res) {
            switch (res.getCode()) {
                case EntityResult.OPERATION_SUCCESSFUL:
                    IMSqlManager.logger.info("Operacion realizada con exito.");
                    break;
                case EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE:
                    IMSqlManager.logger.info("Operacion devolvio un aviso: {}", res.getMessage());
                    break;
                case EntityResult.OPERATION_WRONG:
                    IMSqlManager.logger.info("Operacion erronea: ", res.getMessage());
                    break;
                default:
                    break;
            }
            if (res.isEmpty()) {
                IMSqlManager.logger.info("No se encontraron resultados");
            }
        }

    }

    public class SaveEntry implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    if ((IMSqlManager.this.queryField.getValue() != null)
                            && !"".equals(IMSqlManager.this.queryField.getValue())) {
                        SentenceSql selectedEntry = (SentenceSql) IMSqlManager.this.sqlList.getSelectedEntry();
                        if (selectedEntry != null) {
                            selectedEntry.setSqlSentence((String) IMSqlManager.this.queryField.getValue());
                            IMSqlManager.this.sqlList.updateCurrentSelectedEntry(selectedEntry);
                        } else {
                            selectedEntry = new SentenceSql((String) IMSqlManager.this.queryField.getValue(),
                                    IMSqlManager.this.user);
                            IMSqlManager.this.sqlList.addEntry(selectedEntry);
                        }
                        MainApplication application = (MainApplication) ApplicationManager.getApplication();
                        application.getPreferences()
                            .setPreference(IMSqlManager.this.user, "SQLSentence" + selectedEntry.getCode(),
                                    selectedEntry.toString());
                        application.getPreferences().savePreferences();
                    }
                }
            }).start();
        }

    }

    public class SaveAsEntry implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    if ((IMSqlManager.this.queryField.getValue() != null)
                            && !"".equals(IMSqlManager.this.queryField.getValue())) {
                        SentenceSql sentence = new SentenceSql((String) IMSqlManager.this.queryField.getValue(),
                                IMSqlManager.this.user);
                        IMSqlManager.this.sqlList.addEntry(sentence);
                        MainApplication application = (MainApplication) ApplicationManager.getApplication();
                        application.getPreferences()
                            .setPreference(IMSqlManager.this.user, "SQLSentence" + sentence.getCode(),
                                    sentence.toString());
                        application.getPreferences().savePreferences();
                    }
                }
            }).start();
        }

    }

    public class DeletryEntry implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Object selectedEntry = IMSqlManager.this.sqlList.getSelectedEntry();
                    if ((selectedEntry != null) && IMSqlManager.this.sqlList.deleteCurrentSelectedEntry()) {
                        MainApplication application = (MainApplication) ApplicationManager.getApplication();
                        application.getPreferences()
                            .setPreference(IMSqlManager.this.user,
                                    "SQLSentence" + ((SentenceSql) selectedEntry).getCode(), null);
                        application.getPreferences().savePreferences();
                    }
                }
            }).start();
        }

    }

    public class DeleteAll implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    MainApplication application = (MainApplication) ApplicationManager.getApplication();
                    if (IMSqlManager.this.user == null) {
                        // this.user = application.getUser();//FIXME
                    }

                    for (int i = 0; i < 1000; i++) {
                        String preference = application.getPreferences()
                            .getPreference(IMSqlManager.this.user, "SQLSentence" + i);
                        if (preference != null) {
                            application.getPreferences().setPreference(IMSqlManager.this.user, "SQLSentence" + i, null);

                        }
                        application.getPreferences().savePreferences();
                    }
                    IMSqlManager.this.sqlList.deleteData();
                    SentenceSql.resetCode();
                }
            }).start();
        }

    }

    /**
     * The Class SentenceListRenderer.
     */
    public static class SentenceListRenderer extends JLabel implements ListCellRenderer {

        /** The Constant BASIC_BGCOLOR_COLOR. */
        private static final Color BASIC_BGCOLOR_COLOR = Color.decode("#e9e9e9");

        /** The Constant HIGHLIGHT_BGCOLOR_COLOR. */
        private static final Color HIGHLIGHT_BGCOLOR_COLOR = Color.decode("#e9d2a5");

        /**
         * Instantiates a new sentence list renderer.
         */
        public SentenceListRenderer() {
            this.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            SentenceSql entry = (SentenceSql) value;
            String sentence = entry.toString();
            sentence = sentence.replaceAll("\n", "   ");
            if (sentence.length() > 46) {
                sentence = sentence.substring(0, Math.min(sentence.length(), 44)) + "...";
            }
            this.setText(sentence);
            this.setToolTipText(entry.getSQLSentence());
            if (isSelected) {
                this.setBackground(SentenceListRenderer.HIGHLIGHT_BGCOLOR_COLOR);
                this.setForeground(Color.black);
            } else {
                this.setBackground(SentenceListRenderer.BASIC_BGCOLOR_COLOR);
                this.setForeground(Color.black);
            }
            return this;
        }

    }

}
