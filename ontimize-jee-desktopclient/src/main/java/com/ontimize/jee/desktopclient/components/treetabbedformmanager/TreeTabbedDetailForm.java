package com.ontimize.jee.desktopclient.components.treetabbedformmanager;

import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.NullValue;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.BaseDetailForm;
import com.ontimize.gui.BasicInteractionManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.InteractionManager;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.Level;
import com.ontimize.util.FormatPattern;

public class TreeTabbedDetailForm extends BaseDetailForm {

    private static final Logger logger = LoggerFactory.getLogger(TreeTabbedDetailForm.class);

    private boolean isMain = false;

    private final ITreeTabbedFormManager mainFormManager;

    private final Level level;

    private Hashtable levelKeys;

    private final Hashtable levelValues;

    public TreeTabbedDetailForm(Form form, Level level, boolean isMain, ITreeTabbedFormManager mainFormManager) {
        this.mainFormManager = mainFormManager;
        this.isMain = isMain;
        this.form = form;
        this.setLayout(new BorderLayout());
        this.add(form, BorderLayout.CENTER);
        Map<?, ?> keysValues = level.getKeysValues();
        if (keysValues != null) {
            this.levelKeys = this.valuesToForm(new Hashtable<Object, Object>(keysValues));
        } else {
            this.levelKeys = new Hashtable<>();
        }

        Map<String, List<?>> selectedData = level.getSelectedData();
        this.levelValues = new Hashtable<>();
        if (selectedData != null) {
            for (Entry<String, List<?>> entry : selectedData.entrySet()) {
                List<?> value = entry.getValue();
                if ((value != null) && (value.size() > 0) && (value.get(0) != null)) {
                    this.levelValues.put(entry.getKey(), value.get(0));
                }
            }
        }
        this.level = level;

        this.form.setDetailForm(this);
        this.form.disableDataFields();

        if (form.getInteractionManager() instanceof BasicInteractionManager) {
            ReflectionTools.invoke(form.getInteractionManager(), "setDetailForm", true);
        }

        List<?> parentKeys2 = level.getParentKeys();
        if (parentKeys2 != null) {
            for (Object c : parentKeys2) {
                form.setModifiable(c.toString(), false);
            }
            form.setParentKeys(new Vector<>(parentKeys2));
        } else {
            form.setParentKeys(new Vector<>());
        }

        String previousLevelId = level.getPreviousLevelId();
        Hashtable<Object, Object> pk = new Hashtable<>();
        if (previousLevelId != null) {
            Map<String, List<?>> selectedData2 = level.getLevelManager().getLevel(previousLevelId).getSelectedData();
            for (Entry<String, List<?>> entry : selectedData2.entrySet()) {
                if ((entry.getValue() != null) && (entry.getValue().size() > 0)) {
                    if (entry.getValue().get(0) == null) {
                        pk.put(entry.getKey(), new NullValue());
                    } else {
                        pk.put(entry.getKey(), entry.getValue().get(0));
                    }
                }
            }

        }
        form.setParentKeyValues(pk);

        this.vectorIndex = 0;
        this.setResourceBundle(level.getResourceBundle());
        if (level instanceof Table) {
            this.table = (Table) level;
        }
    }

    private Hashtable adaptKeys(Hashtable<Object, Object> levelKeys2) {
        Hashtable<Object, Vector<?>> result = new Hashtable<>();
        for (Entry<?, ?> entry : levelKeys2.entrySet()) {
            if (entry.getValue() != null) {
                Vector<Object> value = new Vector<>();
                value.add(entry.getValue());
                result.put(entry.getKey(), value);
            }

        }
        return result;
    }

    public void updateTitle() {
        int index = this.mainFormManager.indexOfComponent(this);
        if (index > 0) {
            int currentMode = this.form.getInteractionManager().getCurrentMode();
            String descriptionText = this.form.getArchiveName();
            if (InteractionManager.INSERT == currentMode) {
                descriptionText = ApplicationManager.getTranslation(this.level.getEntityName(),
                        this.form.getResourceBundle()) + "("
                        + ApplicationManager.getTranslation("insert",
                                this.form.getResourceBundle())
                        + ")";
            } else if (InteractionManager.QUERY == currentMode) {
                descriptionText = ApplicationManager.getTranslation(this.level.getEntityName(),
                        this.form.getResourceBundle()) + "("
                        + ApplicationManager.getTranslation("query",
                                this.form.getResourceBundle())
                        + ")";
            } else {
                if (this.level.getDetailFormatPattern() != null) {
                    FormatPattern formatPattern = this.level.getDetailFormatPattern();
                    descriptionText = formatPattern.parse(0, this.levelValues);
                } else {
                    StringBuilder buffer = new StringBuilder();
                    Vector keys = this.form.getKeys();
                    for (Object current : keys) {
                        Object value = this.form.getDataFieldValue(current.toString());
                        if (buffer.length() > 0) {
                            buffer.append(" ");
                        }
                        buffer.append(value.toString());
                    }
                    descriptionText = buffer.toString();
                }
            }
            this.mainFormManager.setTitleAt(index, descriptionText);
        }
    }

    @Override
    public void showDetailForm() {
        int index = 0;
        if (this.isMain && (this.mainFormManager.getMainForm() != null)) {
            this.mainFormManager.showTab(index);
        } else if (!this.isMain) {
            index = this.mainFormManager.indexOf(this);

            if (index >= 0) {
                // If tab already exits.
                this.mainFormManager.showTab(index);
                return;
            }
        }
        int currentMode = this.form.getInteractionManager().getCurrentMode();
        String descriptionText = this.form.getArchiveName();
        if (InteractionManager.INSERT == currentMode) {
            descriptionText = ApplicationManager.getTranslation(this.level.getEntityName(),
                    this.form.getResourceBundle()) + "("
                    + ApplicationManager.getTranslation("insert",
                            this.form.getResourceBundle())
                    + ")";
        } else if (InteractionManager.QUERY == currentMode) {
            descriptionText = ApplicationManager.getTranslation(this.level.getEntityName(),
                    this.form.getResourceBundle()) + "("
                    + ApplicationManager.getTranslation("query",
                            this.form.getResourceBundle())
                    + ")";
        } else {
            if (this.level.getDetailFormatPattern() != null) {
                FormatPattern formatPattern = this.level.getDetailFormatPattern();
                descriptionText = formatPattern.parse(0, this.levelValues);
            } else {
                StringBuilder buffer = new StringBuilder();
                Vector keys = this.form.getKeys();
                for (Object current : keys) {
                    Object value = this.form.getDataFieldValue(current.toString());
                    if (buffer.length() > 0) {
                        buffer.append(" ");
                    }
                    buffer.append(value.toString());
                }
                descriptionText = buffer.toString();
            }
        }
        this.mainFormManager.addFormToContainer(this, descriptionText);
    }

    public Hashtable getKeysValues() {
        return this.levelKeys;
    }

    public String getEntityName() {
        return this.level.getEntityName();
    }

    @Override
    public void hideDetailForm() {
        int index = this.mainFormManager.indexOfComponent(this);
        if ((this.mainFormManager.getMainForm() == null) || (index > 0)) {
            this.mainFormManager.removeTab(index);
        }
    }

    public int getMode() {
        return this.form.getInteractionManager().getCurrentMode();
    }

    @Override
    public void setInsertMode() {
        super.setInsertMode();
        if (this.form.clearDataFieldButton != null) {
            this.form.clearDataFieldButton.setVisible(false);
        }

        Button queryButton = this.form.getButton(InteractionManager.QUERY_KEY);
        if (queryButton != null) {
            queryButton.setVisible(false);
        }

        Button updateButton = this.form.getButton(InteractionManager.UPDATE_KEY);
        if (updateButton != null) {
            updateButton.setVisible(false);
        }

        Button deleteButton = this.form.getButton(InteractionManager.DELETE_KEY);
        if (deleteButton != null) {
            deleteButton.setVisible(false);
        }

        Button insertButton = this.form.getButton(InteractionManager.INSERT_KEY);
        if (insertButton != null) {
            insertButton.setVisible(true);
        }
    }

    @Override
    public void setQueryMode() {
        this.form.getInteractionManager().setQueryMode();

        if (this.form.clearDataFieldButton != null) {
            this.form.clearDataFieldButton.setVisible(false);
        }

        Button queryButton = this.form.getButton(InteractionManager.QUERY_KEY);
        if (queryButton != null) {
            queryButton.setVisible(true);
        }

        Button insertButton = this.form.getButton(InteractionManager.INSERT_KEY);
        if (insertButton != null) {
            insertButton.setVisible(false);
        }

    }

    @Override
    public void setUpdateMode() {
        super.setUpdateMode();
        if (this.form.clearDataFieldButton != null) {
            this.form.clearDataFieldButton.setVisible(false);
        }

        Button queryButton = this.form.getButton(InteractionManager.QUERY_KEY);
        if (queryButton != null) {
            queryButton.setVisible(false);
        }

        Button insertButton = this.form.getButton(InteractionManager.INSERT_KEY);
        if (insertButton != null) {
            insertButton.setVisible(false);
        }
    }

    @Override
    public void setResourceBundle(ResourceBundle resourceBundle) {
        super.setResourceBundle(resourceBundle);
    }

    public boolean isMain() {
        return this.isMain;
    }

    public void setIsMain(boolean isMain) {
        this.isMain = isMain;

    }

}
