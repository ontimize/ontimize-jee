package com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.ListSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.InteractionManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.table.Table;
import com.ontimize.gui.table.TableSorter;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.ITreeTabbedFormManager;
import com.ontimize.util.FormatPattern;
import com.ontimize.util.ParseUtils;

public class DefaultLevel extends Table implements Level {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLevel.class);

    private String nextLevelId;

    private String previousLevelId;

    private String id;

    private boolean showInMainFormAvailable;

    private String displayTextFormat;

    protected FormatPattern displayTextFormatPattern;

    private LevelManager levelManager;

    private final Map<Object, Object> lastSelectedKeys = new HashMap<>();

    private Map<String, List<?>> lastSelectedRowData = new Hashtable<>();

    public DefaultLevel(Hashtable params) throws Exception {
        super(params);
        this.setParentForm(this.levelManager.getParentForm());
        this.getJTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getJTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                IFormManager formManager = DefaultLevel.this.getParentForm().getFormManager();
                DefaultLevel.this.updateLastSelected(DefaultLevel.this.getSelectedRow());
                if (formManager instanceof ITreeTabbedFormManager) {
                    if (e.getClickCount() == 2) {
                        // show linked table
                        if (DefaultLevel.this.nextLevelId != null) {

                            DefaultLevel.this.levelManager.show(DefaultLevel.this.getNextLevelId());

                        }
                        DefaultLevel.this.openDetailForm(DefaultLevel.this.getSelectedRow());
                    } else {
                        if (DefaultLevel.this.showInMainFormAvailable) {
                            DefaultLevel.this.openFormInMainTab(DefaultLevel.this.getSelectedRow());
                        }
                    }

                }
            }

        });
    }

    @Override
    public Map<String, List<?>> getSelectedData() {
        return this.lastSelectedRowData;
    }

    @Override
    public void reload() {
        this.refreshEDT(true);
    }

    @Override
    public String getDisplayText() {
        if ((this.displayTextFormatPattern != null) && (this.getSelectedRowsNumber() == 1)) {
            Hashtable<Object, Object> tableKeys = new Hashtable<>();
            Map<String, List<?>> selectedRowData = this.lastSelectedRowData;
            for (Entry<String, List<?>> entry : selectedRowData.entrySet()) {
                if ((entry.getValue() != null) && (entry.getValue().size() == 1)) {
                    if (entry.getValue().get(0) == null) {
                        tableKeys.put(entry.getKey().toString(), "-");
                    } else {
                        tableKeys.put(entry.getKey().toString(), entry.getValue().get(0));
                    }
                }
            }
            return this.displayTextFormatPattern.parse(0, tableKeys);
        }
        return ApplicationManager.getTranslation(this.getEntityName(), this.getResourceBundle());
    }

    @Override
    public Hashtable getParentKeyValues(boolean applyEquivalences) {
        Level level = this.levelManager.getLevel(this.previousLevelId);

        Map<String, List<?>> parentSelectedRowData = level != null ? level.getSelectedData() : null;
        Hashtable kv = new Hashtable();
        if ((this.parentkeys != null) && (parentSelectedRowData != null)) {
            for (int i = 0; i < this.parentkeys.size(); i++) {
                List<?> v = parentSelectedRowData.get(this.parentkeys.get(i).toString());
                if ((v != null) && (v.size() == 1)) {
                    Object pkName = this.parentkeys.get(i);
                    if (applyEquivalences) {
                        pkName = this.getParentkeyEquivalentValue(pkName);
                    }
                    kv.put(pkName, v.get(0));
                } else if ((v != null) && (v.size() > 1)) {
                    throw new IllegalArgumentException("DefaultLevel: only one row can be selected");
                } else {
                    DefaultLevel.logger.debug(
                            "DefaultLevel: Parentkey {} is null. It won't be included in the query.Check the xml file in which the table is defined to ensure that the field has a value",
                            this.parentkeys.get(i));
                    if (DefaultLevel.logger.isTraceEnabled()) {
                        MessageDialog.showErrorMessage(this.parentFrame, "DEBUG: DefaultLevel: Parentkey "
                                + this.parentkeys.get(
                                        i)
                                + " is null. It won't be included in the query. "
                                + "Check the xml file in which the table is defined to ensure that the field has a value");
                    }
                }
            }

        }
        return kv;
    }

    @Override
    public void init(Hashtable parameters) throws Exception {
        parameters.put(Table.MINROWHEIGHT, "45");
        parameters.put(Table.FONT_SIZE, "35");
        parameters.put(Table.CONTROLS_VISIBLE, "no");
        parameters.put(Table.DISABLE_INSERT, "yes");
        parameters.put(Table.QUICK_FILTER_VISIBLE, "yes");
        super.init(parameters);

        this.levelManager = (LevelManager) parameters.get(Level.LEVEL_MANAGER);
        if (this.levelManager == null) {
            throw new IllegalArgumentException("levelmanager must not be null");
        }

        this.showInMainFormAvailable = ParseUtils.getBoolean((String) parameters.get(Level.SHOW_IN_MAIN_FORM_AVAILABLE),
                true);
        this.displayTextFormat = (String) parameters.get(Level.DISPLAY_TEXT_FORMAT);
        if (this.displayTextFormat != null) {
            this.displayTextFormatPattern = new FormatPattern(this.displayTextFormat);

            String oDateFormat = ParseUtils.getString((String) parameters.get(Level.DISPLAY_TEXT_DATE_FORMAT), null);
            if (oDateFormat != null) {
                this.displayTextFormatPattern.setDateFormat(oDateFormat);
            }
        }
        if (parameters.containsKey(Level.NEXT_LEVEL)) {
            this.setNextLevelId(parameters.get(Level.NEXT_LEVEL).toString());
        }
        if (parameters.containsKey(Level.PREVIOUS_LEVEL)) {
            this.setPreviousLevelId(parameters.get(Level.PREVIOUS_LEVEL).toString());
        }
        if (parameters.containsKey(Level.ID)) {
            this.id = (String) parameters.get(Level.ID);
        } else {
            throw new IllegalArgumentException("'id' attibute is mandatory");
        }
    }

    @Override
    public String getNextLevelId() {
        return this.nextLevelId;
    }

    @Override
    public void setNextLevelId(String linkedTable) {
        this.nextLevelId = linkedTable;
    }

    @Override
    public String getPreviousLevelId() {
        return this.previousLevelId;
    }

    @Override
    public void setPreviousLevelId(String previousLevelId) {
        this.previousLevelId = previousLevelId;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LevelManager getlLevelManager() {
        return this.levelManager;
    }

    @Override
    public Vector getParentKeys() {
        return super.getParentKeys(true);
    }

    public void openFormInMainTab(int rowIndex) {
        boolean bPemission = this.checkQueryPermission();
        if ((this.formName != null) && bPemission && !this.isGrouped()) {
            this.checkRefreshThread();

            // If row is a sum row then the detail form is not open
            TableSorter ts = this.getTableSorter();
            for (int i = 0; i < ts.getColumnCount(); i++) {
                if (ts.isSumCell(rowIndex, i)) {
                    return;
                }
            }

            if (this.parentForm.getFormManager() instanceof ITreeTabbedFormManager) {

                this.levelManager.openFormInMainTab(this, InteractionManager.UPDATE);
            }
        } else {
            DefaultLevel.logger.debug(
                    "Form is NULL. Or 'form' tag was not especified, or the user has not permission to open the form to query, or the table is grouped.");
        }
    }

    @Override
    public void openDetailForm(int rowIndex) {
        boolean bPemission = this.checkQueryPermission();
        if ((this.formName != null) && bPemission && !this.isGrouped()) {
            this.checkRefreshThread();

            // If row is a sum row then the detail form is not open
            TableSorter ts = this.getTableSorter();
            for (int i = 0; i < ts.getColumnCount(); i++) {
                if (ts.isSumCell(rowIndex, i)) {
                    return;
                }
            }

            if (this.parentForm.getFormManager() instanceof ITreeTabbedFormManager) {
                this.levelManager.openFormInTab(this, InteractionManager.UPDATE);
            }
        } else {
            DefaultLevel.logger.debug(
                    "Form is NULL. Or 'form' tag was not especified, or the user has not permission to open the form to query, or the table is grouped.");
        }
    }

    @Override
    public Map<?, ?> getKeysValues() {
        Map<Object, Object> keysValues = new HashMap<>();
        Vector<?> keys2 = this.getKeys();
        Map<String, List<?>> selectedData = this.getSelectedData();

        if ((keys2 != null) && (selectedData != null)) {
            for (Object o : keys2) {
                List<?> list = selectedData.get(o);
                if ((list != null) && (list.size() > 0)) {
                    keysValues.put(o, list.get(0));
                }
            }
        }
        return keysValues;
    }

    private void updateLastSelected(int selectedRow) {
        // Return a new hastable with the data
        int[] selectedRows = { selectedRow };
        Vector attributes = this.getAttributeList();
        Hashtable hData = new Hashtable();
        for (int i = 0; i < selectedRows.length; i++) {
            int row = selectedRows[i];
            Hashtable hRowData = this.getRowData(row);
            if (hRowData == null) {
                continue;
            }
            for (int j = 0; j < attributes.size(); j++) {
                Object oKey = attributes.get(j);
                Object oValue = hRowData.get(oKey);
                Vector v = (Vector) hData.get(oKey);
                if (v == null) {
                    Vector vAux = new Vector();
                    vAux.add(0, oValue);
                    hData.put(oKey, vAux);
                } else {
                    v.add(i, oValue);
                }
            }
        }
        this.lastSelectedRowData = hData;
        this.lastSelectedKeys.clear();
        this.lastSelectedKeys.putAll(this.getKeysValues());

    }

    @Override
    protected void setInnerValue(Object value, boolean autoSizeColumns) {
        super.setInnerValue(value, autoSizeColumns);
        if (!this.lastSelectedKeys.isEmpty()) {
            int rowForKeys = this.getRowForKeys(new Hashtable<>(this.lastSelectedKeys));
            if (rowForKeys != -1) {
                this.table.addRowSelectionInterval(rowForKeys, rowForKeys);
            }
        }
    }

    @Override
    public LevelManager getLevelManager() {
        return this.levelManager;
    }

}
