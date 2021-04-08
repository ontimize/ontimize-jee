/*
 *
 */
package com.ontimize.jee.desktopclient.components.servermanagement.window.list;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Form;
import com.ontimize.gui.ValueChangeDataComponent;
import com.ontimize.gui.ValueChangeListener;
import com.ontimize.gui.field.AccessForm;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.jee.common.tools.ReflectionTools;

/**
 * TODO Este componente esta muy picado add-hoc para las consulas SQL, pero se podria mejorar de
 * cara a poder utilizar listas en las aplicaciones. TODO Copiado de UList de utilmize
 */
public class ListSql extends JScrollPane implements DataComponent, AccessForm, ValueChangeDataComponent {

    private static final Logger logger = LoggerFactory.getLogger(ListSql.class);

    /** The list. */
    protected JList list;

    /** The attr. */
    protected String attr;

    /**
     * Instantiates a new u list.
     * @param params the params
     * @throws Exception the exception
     */
    public ListSql(Hashtable params) throws Exception {
        super();
        this.list = new JList(new DefaultListModel());
        this.setViewportView(this.list);
        this.init(params);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#deleteData()
     */
    @Override
    public void deleteData() {
        if (this.list.getModel() instanceof DefaultListModel) {
            ((DefaultListModel) this.list.getModel()).removeAllElements();
        } else {
            ListSql.logger.error("UList: The delete method is not available to this model list: {}",
                    this.list.getModel().getClass().getName());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#getLabelComponentText()
     */
    @Override
    public String getLabelComponentText() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#getSQLDataType()
     */
    @Override
    public int getSQLDataType() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#getValue()
     */
    @Override
    public Object getValue() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#isHidden()
     */
    @Override
    public boolean isHidden() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#isModifiable()
     */
    @Override
    public boolean isModifiable() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#isModified()
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#isRequired()
     */
    @Override
    public boolean isRequired() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#setModifiable(boolean)
     */
    @Override
    public void setModifiable(boolean flag) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#setRequired(boolean)
     */
    @Override
    public void setRequired(boolean flag) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.DataComponent#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Object obj) {
        // do nothing
        if (obj == null) {
            this.deleteData();
        } else if (obj instanceof List) {
            this.deleteData();
            for (Object o : (List) obj) {
                this.addEntry(o);
            }
        } else {
            this.deleteData();
            this.addEntry(obj);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.FormComponent#getConstraints(java.awt.LayoutManager)
     */
    @Override
    public Object getConstraints(LayoutManager layoutmanager) {
        if (layoutmanager instanceof GridBagLayout) {
            return new GridBagConstraints(-1, -1, 1, 1, 1.0d, 1.0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.FormComponent#init(java.util.Hashtable)
     */
    @Override
    public void init(Hashtable hashtable) throws Exception {
        this.attr = (String) hashtable.get("attr");
        if (this.attr == null) {
            this.attr = "list";
        }

        if (hashtable.containsKey("renderer")) {
            ListCellRenderer cellRenderer = ReflectionTools.newInstance((String) hashtable.get("renderer"),
                    ListCellRenderer.class, this);
            this.list.setCellRenderer(cellRenderer);
        } else {
            this.list.setCellRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                        boolean cellHasFocus) {
                    Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index,
                            isSelected, cellHasFocus);

                    if (listCellRendererComponent instanceof JComponent) {
                        ((JLabel) listCellRendererComponent).setHorizontalAlignment(SwingConstants.CENTER);
                    }

                    return listCellRendererComponent;
                }
            });
        }

        Object selection = hashtable.get("selection");
        if (selection != null) {
            if ("no".equals(selection)) {
                this.list.setSelectionModel(new DefaultListSelectionModel() {

                    @Override
                    public void setSelectionInterval(int index0, int index1) {
                        super.setSelectionInterval(-1, -1);
                    }
                });
            } else if ("single".equals(selection)) {
                this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            } else {
                this.list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            }
        }

        this.list.setLayoutOrientation(JList.VERTICAL);
        this.list.setVisibleRowCount(-1);

        if ("no".equals(hashtable.get("opaque"))) {
            this.getViewport().setOpaque(false);
            this.list.setOpaque(false);
            ((DefaultListCellRenderer) this.list.getCellRenderer()).setOpaque(false);
        }
        if ("no".equals(hashtable.get("scrollh"))) {
            this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        if ("no".equals(hashtable.get("scrollv"))) {
            this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.i18n.Internationalization#getTextsToTranslate()
     */
    @Override
    public Vector getTextsToTranslate() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.i18n.Internationalization#setComponentLocale(java.util.Locale)
     */
    @Override
    public void setComponentLocale(Locale locale) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.i18n.Internationalization#setResourceBundle(java.util.ResourceBundle)
     */
    @Override
    public void setResourceBundle(ResourceBundle resourcebundle) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.IdentifiedElement#getAttribute()
     */
    @Override
    public Object getAttribute() {
        return this.attr;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.SecureElement#initPermissions()
     */
    @Override
    public void initPermissions() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.SecureElement#isRestricted()
     */
    @Override
    public boolean isRestricted() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.field.AccessForm#setParentForm(com.ontimize.gui.Form)
     */
    @Override
    public void setParentForm(Form form) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.ValueChangeDataComponent#addValueChangeListener(com.ontimize.gui.
     * ValueChangeListener)
     */
    @Override
    public void addValueChangeListener(ValueChangeListener valuechangelistener) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.ValueChangeDataComponent#removeValueChangeListener(com.ontimize.gui.
     * ValueChangeListener)
     */
    @Override
    public void removeValueChangeListener(ValueChangeListener valuechangelistener) {
        // do nothing
    }

    /**
     * Adds the entry.
     * @param value the value
     */
    public void addEntry(Object value) {
        ((DefaultListModel) this.list.getModel()).addElement(value);
    }

    /**
     * Update current selected entry.
     * @param value the value
     * @return true, if successful
     */
    public boolean updateCurrentSelectedEntry(Object value) {
        if (this.list.getSelectedIndex() >= 0) {
            // Update
            DefaultListModel model = (DefaultListModel) this.list.getModel();
            model.set(this.list.getSelectedIndex(), value);
            return true;
        } else {
            this.addEntry(value);
            return false;
        }
    }

    /**
     * Delete current selected entry.
     * @return true, if successful
     */
    public boolean deleteCurrentSelectedEntry() {
        if (this.list.getSelectedIndex() >= 0) {
            // Update
            DefaultListModel model = (DefaultListModel) this.list.getModel();
            model.remove(this.list.getSelectedIndex());
            return true;
        }
        return false;
    }

    /**
     * Gets the selected entry.
     * @return the selected entry
     */
    public Object getSelectedEntry() {
        return this.list.getSelectedValue();
    }

    /**
     * Adds the list selection listener.
     * @param listener the listener
     */
    public void addListSelectionListener(ListSelectionListener listener) {
        this.list.addListSelectionListener(listener);
    }

    /**
     * Adds the list mouse listener.
     * @param listener the listener
     */
    public void addListMouseListener(MouseListener listener) {
        this.list.addMouseListener(listener);
    }

    /**
     * Sets the cell renderer.
     * @param render the new cell renderer
     */
    public void setCellRenderer(ListCellRenderer render) {
        this.list.setCellRenderer(render);
    }

}
