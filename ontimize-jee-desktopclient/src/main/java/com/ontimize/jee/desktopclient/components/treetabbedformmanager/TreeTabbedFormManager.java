package com.ontimize.jee.desktopclient.components.treetabbedformmanager;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.builder.ApplicationBuilder;
import com.ontimize.builder.FormBuilder;
import com.ontimize.builder.xml.XMLFormBuilder;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.DataNavigationEvent;
import com.ontimize.gui.DynamicFormManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.InteractionManager;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.manager.BaseFormManager;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.manager.TabbedFormManager.ButtonTabComponent;
import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.gui.tree.OTreeNode;
import com.ontimize.gui.tree.Tree;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.LevelManager;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.builder.LevelManagerBuilder;
import com.ontimize.locator.ClientReferenceLocator;

/**
 * The <code>FormManager</code> is used to create, register and manage a group of form.
 *
 * @author Imatia Innovation
 */

public class TreeTabbedFormManager extends BaseFormManager implements ITreeTabbedFormManager {

    private static final Logger logger = LoggerFactory.getLogger(TreeTabbedFormManager.class);

    /**
     * <code>SplitPane</code> reference used when a <code>Tree</code> is defined. If the
     * <code>FormManager</code> hasn't defined a tree, this reference is null.
     */
    protected JSplitPane splitPane;

    /**
     * <code>JScrollPane</code> reference. Only if the tree is defined, this reference will have a
     * value.
     */
    protected JScrollPane treeTableScrollPane;

    /**
     * The <code>Tree</code> reference that will be displayed in this <code>FormManager</code>
     */
    protected LevelManager managedTreeTableColumn;

    /** The form name that is visible in this <code>FormManager</code> */
    protected String currentForm = "";

    /** TreeBuilder reference. */
    protected LevelManagerBuilder treeTableBuilder;

    private JPanel auxPanel;

    private boolean checkModified = true;

    private Form activeForm = null;

    /** If this condition is true, the <code>FormManager</code> will be loaded. */
    protected boolean loadedTreeAndForm;

    /** The file name of the tree xml description. */
    protected String treeFileName;

    protected FormTabbedPane tabbedPane;

    protected Form mainForm;

    /**
     * Creates a FormManager instance with the parameters establishes in <code>Hastable</code>. This
     * constructor is called from {@link ApplicationBuilder}
     * <p>
     * The parameters
     * <code>Hashtable<code> contains the attribute values set in the Application XML. <p>
     *
     * <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=
    BOX> <tr> <td><b>attribute</td> <td><b>values</td> <td><b>default</td> <td><b>required</td> <td><b>meaning</td>
     * </tr>
     *
     * <tr> <td>id</td> <td></td> <td></td> <td>yes</td> <td> Establishes the unique identifier for this <code>FormManager</code>
     * </td>
     * </tr>
     * <tr>
     * <td>useclasspath</td>
     * <td>yes/no</td>
     * <td>no</td>
     * <td>no</td>
     * <td>Establishes if the form files are loaded using a location relative to the classpath.</td>
     * </tr>
     * <tr>
     * <td>locator</td>
     * <td></td>
     * <td></td>
     * <td>no</td>
     * <td>A <code>EntityReferenceLocator</code> reference.</td>
     * </tr>
     * <tr>
     * <td>application</td>
     * <td></td>
     * <td></td>
     * <td>no</td>
     * <td>An <code>Application</code> reference.</td>
     * </tr>
     * <tr>
     * <td>formbuilder</td>
     * <td></td>
     * <td></td>
     * <td>yes</td>
     * <td>A <code>FormBuilder</code> reference.</td>
     * </tr>
     * <tr>
     * <td>treebuilder</td>
     * <td></td>
     * <td></td>
     * <td>yes</td>
     * <td>A <code>TreeBuilder</code> reference.</td>
     * </tr>
     * <tr>
     * <td>tree</td>
     * <td></td>
     * <td></td>
     * <td>no</td>
     * <td>Established the path of tree xml description file.</td>
     * </tr>
     * <tr>
     * <td>treeclass</td>
     * <td></td>
     * <td></td>
     * <td>no</td>
     * <td>Established the class name that will be instanced for create a <code>Tree</code></td>
     * </tr>
     * <tr>
     * <td>frame</td>
     * <td></td>
     * <td></td>
     * <td>yes</td>
     * <td>Established the application <code>Frame</code> reference.</td>
     * </tr>
     * <tr>
     * <td>container</td>
     * <td></td>
     * <td></td>
     * <td>yes</td>
     * <td>Established container where this <code>FormManager</code> will be layered out in.</td>
     * </tr>
     * <tr>
     * <td>form</td>
     * <td></td>
     * <td></td>
     * <td>yes</td>
     * <td>Established the initial <code>Form</code> that will be loaded at first.</td>
     * </tr>
     * <tr>
     * <td>imanager</td>
     * <td></td>
     * <td></td>
     * <td>no</td>
     * <td>Established the <code>InteractionManager</code> that will be registered to initial
     * <code>Form</code></td>
     * </tr>
     * <tr>
     * <td>imloader</td>
     * <td></td>
     * <td></td>
     * <td>no</td>
     * <td>Established the <code>InteractionManagerLoader</code> that will be registered</td>
     * </tr>
     * <tr>
     * <td>resources</td>
     * <td></td>
     * <td></td>
     * <td>no</td>
     * <td>Established the <code>ResourceBundle</code> that will be used in this
     * <code>FormManager</code></td>
     * </tr>
     * <tr>
     * <td>dynamicform</td>
     * <td></td>
     * <td></td>
     * <td>no</td>
     * <td>Established the <code>DynamicFormManager</code> class name that will be used.</td>
     * </tr>
     * <tr>
     * <td>delayedload</td>
     * <td>yes/no/background</td>
     * <td>no</td>
     * <td>no</td>
     * <td>Established the {@link #delayedLoad} condition.</td>
     * </tr>
     * </Table>
     */
    public TreeTabbedFormManager(Hashtable parameters) throws Exception {
        super(parameters);
    }

    @Override
    public void init(Hashtable parameters) throws Exception {
        String useclasspath = (String) parameters.get(IFormManager.USE_CLASS_PATH);
        if ("yes".equalsIgnoreCase(useclasspath)) {
            this.useClasspath = true;
        }

        Object oTreeBuilder = parameters.get(ITreeTabbedFormManager.TREE_TABLE_BUILDER);
        if ((oTreeBuilder != null) && (oTreeBuilder instanceof LevelManagerBuilder)) {
            this.treeTableBuilder = (LevelManagerBuilder) oTreeBuilder;
        } else {
            throw new IllegalArgumentException(
                    "'" + ITreeTabbedFormManager.TREE_TABLE_BUILDER + " doesn't implement TreeBuilder or is NULL");
        }

        Object tree = parameters.get(ITreeTabbedFormManager.TREE);
        if ((tree != null) && (tree instanceof String)) {
            this.treeFileName = tree.toString();
            if (this.useClasspath) {
                URL url = this.getClass().getClassLoader().getResource(this.treeFileName);
                if (url == null) {
                    TreeTabbedFormManager.logger.warn("{} -> Tree was not found: {}" + this.getId(), this.treeFileName);
                } else {
                    this.treeFileName = url.toString();
                }
            }
        } else {
            TreeTabbedFormManager.logger.debug("{}  parameter was not found: Tree cannot be established",
                    ITreeTabbedFormManager.TREE);
        }

        super.init(parameters);
    }

    /**
     * Loads the <code>FormManager</code>. This method must be called from
     * <code>EventDispatchThread</code>.
     *
     * @see #load
     */
    @Override
    public synchronized void loadInEDTh() {
        try {
            if (!this.isLoaded()) {
                if (this.formInteractionManagerClassNameList != null) {
                    Enumeration enumKeys = this.formInteractionManagerClassNameList.keys();
                    while (enumKeys.hasMoreElements()) {
                        String form = (String) enumKeys.nextElement();
                        String imClassName = this.formInteractionManagerClassNameList.get(form);
                        this.applyInteractionManager(imClassName, form);
                    }
                }
                if (this.setResourceBundleOnLoad) {
                    this.setResourceBundle_internal();
                }

                this.loadedTreeAndForm = true;
                try {

                    if (this.treeFileName != null) {
                        try {
                            Form mockForm = new Form(new Hashtable<>());
                            mockForm.setFormManager(this);
                            mockForm.setResourceBundle(this.getResourceBundle());
                            this.managedTreeTableColumn = this.treeTableBuilder.buildLevelManager(this.treeFileName,
                                    mockForm, this.getResourceBundle());
                        } catch (Exception error) {
                            TreeTabbedFormManager.logger.error(null, error);
                        }
                    }

                    this.registerApplicationPreferencesListener();
                } catch (Exception error) {
                    TreeTabbedFormManager.logger.error(null, error);
                }
                if (this.managedTreeTableColumn == null) {
                    this.createLayout(this.applicationFrame, this.parent, this.formBuilder, this.initialForm,
                            this.initialInteractionManager, this.resourceFile, false);
                } else {
                    this.createLayoutWithTree(this.applicationFrame, this.parent, this.formBuilder, this.initialForm,
                            this.initialInteractionManager, this.resourceFile,
                            this.managedTreeTableColumn, false);
                    this.managedTreeTableColumn.show(this.managedTreeTableColumn.getFirstLevel().getId());
                }
            } else if (ApplicationManager.DEBUG) {
                TreeTabbedFormManager.logger.debug("FormManager has already done the initial load");
            }

        } catch (Exception ex) {
            throw new RuntimeException(null, ex);
        }

    }

    /**
     * Builds a <code>FormManager</code>
     * @param frame the Application Frame
     * @param container <code>Container</code> where is layered out the <code>FormManager</code>in.
     * @param builder <code>FormBuilder</code> used to create the <code>Form</code> instances.
     * @param formURI URI where is stored the xml description in.
     * @param resource ResourceBundle where is the language resources in.
     * @param northPanel true if new components can be added in <code>FormManager</code> using the
     *        {@link #addTopComponent} method.
     */

    public TreeTabbedFormManager(Frame frame, Container container, FormBuilder builder, String formURI,
            ResourceBundle resource, boolean northPanel) {
        super(frame, container, builder, formURI, resource, northPanel);
    }

    /**
     * Builds a <code>FormManager</code>
     * @param frame the Application Frame
     * @param container <code>Container</code> where is layered out the <code>FormManager</code>in.
     * @param builder <code>FormBuilder</code> used to create the <code>Form</code> instances.
     * @param formURI URI where is stored the xml description in.
     * @param interactionManager <code>InteractionManager</code> reference.
     * @param resource ResourceBundle where is the language resources in.
     * @param northPanel true if new components can be added in <code>FormManager</code> using the
     *        {@link #addTopComponent} method.
     */

    public TreeTabbedFormManager(Frame frame, Container container, FormBuilder builder, String formURI,
            InteractionManager interactionManager, ResourceBundle resource,
            boolean northPanel) {
        super(frame, container, builder, formURI, interactionManager, resource, northPanel);
    }

    /**
     * Builds a <code>FormManager</code>
     * @param frame the Application Frame
     * @param container <code>Container</code> where is layered out the <code>FormManager</code>in.
     * @param builder <code>FormBuilder</code> used to create the <code>Form</code> instances.
     * @param formURI URI where is stored the xml description in.
     * @param resource ResourceBundle where is the language resources in.
     */
    public TreeTabbedFormManager(Frame frame, Container container, FormBuilder builder, String formURI,
            ResourceBundle resource) {
        super(frame, container, builder, formURI, null, resource, false);
    }

    /**
     * Builds a <code>FormManager</code>
     * @param frame the Application Frame
     * @param container <code>Container</code> where is layered out the <code>FormManager</code>in.
     * @param builder <code>FormBuilder</code> used to create the <code>Form</code> instances.
     * @param formURI URI where is stored the xml description in.
     * @param interactionManager <code>InteractionManager</code> reference.
     * @param resource ResourceBundle where is the language resources in.
     */
    public TreeTabbedFormManager(Frame frame, Container container, FormBuilder builder, String formURI,
            InteractionManager interactionManager, ResourceBundle resource) {
        super(frame, container, builder, formURI, interactionManager, resource, false);
    }

    @Override
    protected JComponent createCenterPanel() {
        this.tabbedPane = new FormTabbedPane();

        if (this.managedTreeTableColumn == null) {
            return this.tabbedPane;
        } else {
            this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT) {

                @Override
                public void updateUI() {
                    super.updateUI();
                    this.setDividerSize(12);
                }
            };
            this.splitPane.setOneTouchExpandable(true);
            this.splitPane.setDividerSize(12);
            this.treeTableScrollPane = new JScrollPane();
            this.splitPane.add(this.treeTableScrollPane, JSplitPane.LEFT);
            this.splitPane.setDividerLocation(0.25);
            this.treeTableScrollPane.getViewport().add(this.managedTreeTableColumn.getGUIComponent());
            return this.splitPane;
        }
    }

    private void createLayoutWithTree(Frame frame, Container container, FormBuilder builder, String formURI,
            InteractionManager interactionManager, ResourceBundle resources,
            LevelManager treeTableColumn, boolean controls) {

        this.managedTreeTableColumn = treeTableColumn;
        this.managedTreeTableColumn.setResourceBundle(resources);

        if ((resources != null) && (resources.getLocale() != null)) {
            this.managedTreeTableColumn.setComponentLocale(resources.getLocale());
        } else {
            this.managedTreeTableColumn.setComponentLocale(this.locale);
        }

        this.createLayout(frame, container, builder, formURI, interactionManager, resources, controls);
        try {
            // Control panel
            if (controls) {
                this.auxPanel = new JPanel(new BorderLayout());
                this.auxPanel.add(this.panelTop, BorderLayout.NORTH);
                this.auxPanel.add(this.tabbedPane, BorderLayout.CENTER);
                this.splitPane.add(this.auxPanel, JSplitPane.RIGHT);
            } else {
                this.splitPane.add(this.tabbedPane, JSplitPane.RIGHT);
            }
        } catch (Exception e) {
            if (com.ontimize.gui.ApplicationManager.DEBUG) {
                TreeTabbedFormManager.logger.debug("Error adding form manager to the container. ", e);
            }
            return;
        }

        this.revalidate();

        if (Tree.PREFERRED_WIDTH == -1) {
            this.managedTreeTableColumn.getGUIComponent()
                .setPreferredSize(new Dimension(treeTableColumn.getGUIComponent().getPreferredSize().width + 60,
                        this.treeTableScrollPane.getPreferredSize().height));
        } else {
            this.managedTreeTableColumn.getGUIComponent()
                .setPreferredSize(
                        new Dimension(Tree.PREFERRED_WIDTH, this.treeTableScrollPane.getPreferredSize().height));
        }
    }

    @Override
    public Vector getTextsToTranslate() {
        Vector v = super.getTextsToTranslate();
        // And for tree when exists
        if (this.managedTreeTableColumn != null) {
            v.addAll(this.managedTreeTableColumn.getTextsToTranslate());
        }
        return v;
    }

    @Override
    public void setResourceBundle(ResourceBundle resources) {
        super.setResourceBundle(resources);
        // If custom resource file is defined, it will be used and passed
        // parameter will be obviated
        if (this.resourceFileName == null) {
            // For all forms, we establish resource file.
            // And for tree
            if (this.managedTreeTableColumn != null) {
                this.managedTreeTableColumn.setResourceBundle(resources);
            }
        } else {
            if (resources != null) {
                if ((this.delayedLoad) && !this.isLoaded()) {
                    this.setResourceBundleOnLoad = true;
                    return;
                }
                try {
                    // And for tree when it exists
                    if (this.managedTreeTableColumn != null) {
                        this.managedTreeTableColumn.setResourceBundle(this.resourceFile);
                    }
                } catch (Exception e) {
                    TreeTabbedFormManager.logger.debug("{}", e.getMessage(), e);
                }
            }

        }

        if (this.mainForm != null) {
            String title = this.mainForm.getFormTitle() != null ? this.mainForm.getFormTitle()
                    : this.mainForm.getEntityName();
            if (title == null) {
                title = this.mainForm.getArchiveName();
            }
            if (resources != null) {
                this.setTitleAt(0, ApplicationManager.getTranslation(title, resources));
            } else {
                this.setTitleAt(0, title);
            }
        }
    }

    @Override
    protected void setResourceBundle_internal() {
        super.setResourceBundle_internal();
        if (this.resourceFileName == null) {
            return;
        }
        try {
            // And for tree when it exists
            if (this.managedTreeTableColumn != null) {
                this.managedTreeTableColumn.setResourceBundle(this.resourceFile);
            }
        } catch (Exception e) {
            TreeTabbedFormManager.logger.debug("{}", e.getMessage(), e);
        }
    }

    @Override
    public void setComponentLocale(Locale l) {
        super.setComponentLocale(l);
        // And for tree when it exists
        if (this.managedTreeTableColumn != null) {
            this.managedTreeTableColumn.setComponentLocale(l);
        }
    }

    /**
     * Fits the space to the tree.
     */
    protected void fitSpaceToTree() {
        if (SwingUtilities.isEventDispatchThread()) {
            this.managedTreeTableColumn.getGUIComponent().revalidate();
            this.splitPane
                .setDividerLocation(this.managedTreeTableColumn.getGUIComponent().getPreferredSize().width + 30);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        TreeTabbedFormManager.this.managedTreeTableColumn.getGUIComponent().revalidate();
                        TreeTabbedFormManager.this.splitPane
                            .setDividerLocation(TreeTabbedFormManager.this.managedTreeTableColumn.getGUIComponent()
                                .getPreferredSize().width + 30);
                    }
                });
            } catch (Exception e) {
                TreeTabbedFormManager.logger.trace(null, e);
            }
        }
    }

    /**
     * Gets the JSplitPane reference. If the form manager doesn't have tree this method return null.
     * @return
     */
    public JSplitPane getJSPlitPane() {
        return this.splitPane;
    }

    /**
     * Gets the <code>Form</code> name that is currently visible.
     */

    @Override
    public String getCurrentForm() {
        return this.currentForm;
    }

    /**
     * Gets the <code>Tree</code> reference that is associated to this <code>FormManager</code>
     * @return <code>Tree</code> reference.
     */
    public LevelManager getTreeTable() {
        if (!this.isLoaded()) {
            this.load();
        }
        return this.managedTreeTableColumn;
    }

    @Override
    public void free() {
        super.free();
        try {
            if (this.managedTreeTableColumn != null) {
                this.remove(this.splitPane);
                this.splitPane.remove(this.treeTableScrollPane);
                this.treeTableScrollPane.remove(this.managedTreeTableColumn.getGUIComponent());
                this.treeTableScrollPane.getViewport().remove(this.managedTreeTableColumn.getGUIComponent());
                this.splitPane = null;
                this.treeTableScrollPane = null;
            }
        } catch (Exception e) {
            TreeTabbedFormManager.logger.debug("Exception while trying to free the tree: {}", e.getMessage(), e);
        }

        // Frees resources
        this.interactionManagers = null;
        this.managedTreeTableColumn = null;
        this.resourceFile = null;
        this.locator = null;
        this.formBuilder = null;
        this.currentForm = null;
        this.tabbedPane = null;
        this.loadedList = null;
        this.interactionManagerList = null;
        this.formReferenceList = null;
        this.applicationFrame = null;
        this.parent = null;
        this.auxPanel = null;
        this.panelTop = null;
    }

    @Override
    public void initPermissions() {
        // Nothing
    }

    /**
     * Gets the attribute value into the <code>OTreeNode</code> that is passed as entry parameter. This
     * method search this attribute into the <code>OTreeNode</code> and its parents.
     * @param attribute the attribute to be found.
     * @param node <code>OTreeNode</code> where the search of the attribute value will start in.
     * @return a attribute value.
     */

    @Override
    protected Object getAttributeValue(Object attribute, OTreeNode node) {
        Object oAttributeValue = null;
        while (!node.isRoot()) {
            if (!node.isOrganizational()) {
                boolean bContainsAttribute = false;
                for (int i = 0; i < node.getAttributes().length; i++) {
                    if (node.getAttributes()[i].equals(attribute)) {
                        bContainsAttribute = true;
                        break;
                    }
                }
                if (bContainsAttribute) {
                    oAttributeValue = node.getValueForAttribute(attribute);
                    break;
                }
            }
            node = (OTreeNode) node.getParent();
        }
        return oAttributeValue;
    }

    /**
     * Enables/disables the check of modified form data
     * @param check true the check of modifed form data is enabled.
     */
    @Override
    public void setCheckModifiedFormData(boolean check) {
        this.checkModifiedDataForms = check;
    }

    @Override
    public String getLabelFileURI() {
        if (this.formBuilder instanceof XMLFormBuilder) {
            return ((XMLFormBuilder) this.formBuilder).getLabelFileURI();
        } else {
            return null;
        }
    }

    @Override
    public void setActiveForm(Form f) {
        this.activeForm = f;
    }

    /**
     * Returns the active <code>Form</code> reference. This method takes into account both the forms
     * displayed in container and the forms layered out in <code>DetailForms</code>. Returns the
     * <code>Form</code> reference established in method {@link #setActiveForm(Form)}. If the
     * <code>Form</code> reference hasn't been established then this method returns the
     * <code>Form</code> reference that is visible into the FormManager container.
     * @return
     */

    @Override
    public Form getActiveForm() {
        if (!this.isLoaded()) {
            this.load();
        }
        if (this.activeForm == null) {
            return this.getFormReference(this.currentForm);
        } else {
            return this.activeForm;
        }
    }

    @Override
    public boolean dataWillChange(DataNavigationEvent e) {
        return true;
    }

    @Override
    public void dataChanged(DataNavigationEvent e) {
        if (!this.processDataChangeEvents) {
            return;
        }
        Form f = e.getForm();
        if (f == null) {
            return;
        }
        // Checks if other forms must be showed
        DynamicFormManager dfm = f.getDynamicFormManager();
        if (dfm != null) {
            // Form to show.
            String fShowForm = dfm.getForm(e.getData());
            if (fShowForm != null) {
                if (ApplicationManager.DEBUG) {
                    TreeTabbedFormManager.logger.debug("DataChanged: Form to show: " + fShowForm);
                }
                if (!this.interactionManagerList.containsKey(fShowForm)) {
                    this.setInteractionManager(dfm.getFormInteractionManagerClass(fShowForm), fShowForm);
                }
                // Now, we have to show the new form. This one must share the
                // data
                // list
                this.showForm(fShowForm);
                Form fNewForm = this.getFormReference(fShowForm);
                if (fNewForm.getAssociatedTreePath() == null) {
                    fNewForm.setLinkedTreePath(f.getAssociatedTreePath());
                    fNewForm.setAssociatedNode(f.getAssociatedNode());
                }
                if (!this.formRegisteredDataNavigationListener.contains(fShowForm)) {
                    // Registers the listener
                    if (fNewForm != null) {
                        fNewForm.addDataNavigationListener(this);
                        fNewForm.setDynamicFormManager(dfm);
                        this.formRegisteredDataNavigationListener.add(fShowForm);
                    }
                }
                if ((fNewForm != null) && (fNewForm != f)) {
                    if (ApplicationManager.DEBUG) {
                        TreeTabbedFormManager.logger.debug("Setting the form data list to show...");
                    }
                    // We must configure the non-modifiable fields and values
                    // for
                    // fields that they are not
                    // included in data list.
                    Hashtable hFieldsValues = f.getDataFieldValues(false);
                    fNewForm.setDataFieldValues(hFieldsValues);
                    Vector v = f.getDataFieldAttributeList();
                    for (int i = 0; i < v.size(); i++) {
                        DataComponent c = f.getDataFieldReference(v.get(i).toString());
                        if ((c != null) && !c.isModifiable()) {
                            fNewForm.setModifiable(v.get(i).toString(), false);
                        }
                    }

                    try {
                        // Data list.
                        this.processDataChangeEvents = false;
                        if (fNewForm.getInteractionManager() != null) {
                            fNewForm.getInteractionManager().setDataChangedEventProcessing(false);
                        }
                        fNewForm.updateDataFields(f.getDataList());

                        // Selects the record
                        fNewForm.updateDataFields(e.getIndex());
                        if (ApplicationManager.DEBUG) {
                            TreeTabbedFormManager.logger
                                .debug("The form data list setted. Selected index: " + e.getIndex());
                        }
                    } catch (Exception ex) {
                        TreeTabbedFormManager.logger.error(null, ex);
                    } finally {
                        this.processDataChangeEvents = true;
                        if (fNewForm.getInteractionManager() != null) {
                            fNewForm.getInteractionManager().setDataChangedEventProcessing(true);
                        }
                    }
                } else {
                    // If form is the same, we do nothing
                }
            }
        } else {
            TreeTabbedFormManager.logger.debug("DynamicFormManager is null for the form: " + f.getArchiveName());
        }
    }

    @Override
    public void setApplicationPreferences(ApplicationPreferences ap) {
        this.aPreferences = ap;
        this.registerApplicationPreferencesListener();
        Enumeration enumKeys = this.formReferenceList.keys();
        while (enumKeys.hasMoreElements()) {
            Object oKey = enumKeys.nextElement();
            Form f = this.formReferenceList.get(oKey);
            if (f != null) {
                f.registerApplicationPreferencesListener();
                String user = null;
                if ((this.locator != null) && (this.locator instanceof ClientReferenceLocator)) {
                    user = ((ClientReferenceLocator) this.locator).getUser();
                }
                f.initPreferences(ap, user);
            }
        }
    }

    @Override
    protected void updateInteractionDynamicFormMode(Form form) {
        // Now, state of interaction manager
        Form fNewForm = this.getFormReference(this.getCurrentForm());
        if (fNewForm != form) {
            InteractionManager formInteractionManager = fNewForm.getInteractionManager();
            if (formInteractionManager != null) {
                int currentMode = form.getInteractionManager().currentMode;
                switch (currentMode) {
                    case InteractionManager.INSERT:
                        formInteractionManager.setInsertMode();
                        break;
                    case InteractionManager.QUERY:
                        formInteractionManager.setQueryMode();
                        break;
                    case InteractionManager.QUERYINSERT:
                        formInteractionManager.setQueryInsertMode();
                        break;
                    case InteractionManager.UPDATE:
                        formInteractionManager.setUpdateMode();
                        break;
                    default:
                        formInteractionManager.setUpdateMode();
                        break;
                }
            }
        }
    }

    public void setCheckModified(boolean enabled) {
        this.checkModified = enabled;
    }

    /**
     * Reload the <code>Form</code> that is passed as entry parameter.
     * @param form <code>Form</code> reference to be reloaded.
     */
    @Override
    public void reload(Form form) {
        if (form == null) {
            return;
        }
        try {
            if (this.formReferenceList.containsValue(form)) {
                String sFileName = form.getArchiveName();
                this.loadedList.remove(sFileName);
                if (form.getInteractionManager() != null) {
                    form.getInteractionManager().free();
                    form.free();
                }
                this.formReferenceList.remove(sFileName);
                Container c = form.getParent();
                if (c != null) {
                    Object constraints = null;
                    LayoutManager l = c.getLayout();
                    if (l instanceof CardLayout) {
                        constraints = form.getArchiveName();
                    }
                    try {
                        Form fNewForm = this.getFormCopy(sFileName);
                        c.remove(form);
                        c.add(fNewForm, constraints);
                        c.validate();
                        c.repaint();
                    } catch (Exception e) {
                        form.message("Error reloading the form.  Maybe the xml is incorrect", Form.ERROR_MESSAGE, e);
                    }

                }
            }
        } catch (Exception e) {
            TreeTabbedFormManager.logger.error(null, e);
        }
    }

    protected boolean restricted = false;

    @Override
    public boolean isRestricted() {
        return this.restricted;
    }

    protected void registerApplicationPreferencesListener() {
        // Nothing
    }

    @Override
    public synchronized boolean isLoaded() {

        return this.loadedTreeAndForm;
    }

    public LevelManagerBuilder getTreeBuilder() {
        return this.treeTableBuilder;
    }

    /**
     * Shows the form which file name is passed as entry parameter If this form isn't loaded, this
     * method will load the form. Before the form will be shown, this method checks if the current form
     * has modified data. This method call the {@link #showFormInEDTh} from EventDispatchThread.
     * @param formName a <code>String</code> with the file name.
     * @return true if the form is showed.
     */

    @Override
    public boolean showForm(final String formName) {
        final Vector res = new Vector();
        if (SwingUtilities.isEventDispatchThread()) {
            return this.showFormInEDTh(formName);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        boolean bIn = TreeTabbedFormManager.this.showFormInEDTh(formName);
                        if (bIn) {
                            res.add(Boolean.TRUE);
                        }
                    }
                });
                return res.isEmpty() ? false : true;
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void addFormToContainer(JPanel panelForm, String formFileName) {
        if ((panelForm instanceof TreeTabbedDetailForm) && ((TreeTabbedDetailForm) panelForm).isMain()) {
            this.tabbedPane.replaceTab(0, formFileName, panelForm);
            this.tabbedPane.setSelectedIndex(0);
            if (this.formReferenceList.containsKey(formFileName)) {
                this.setMainForm(this.formReferenceList.get(formFileName));
            }
        } else {
            int index = this.tabbedPane.getTabCount();
            this.tabbedPane.addTab(formFileName, panelForm);
            this.tabbedPane.setSelectedIndex(index);
            if ((index == 0) && this.formReferenceList.containsKey(formFileName)) {
                this.setMainForm(this.formReferenceList.get(formFileName));
            }
        }
    }

    /**
     * Shows the form which file name is passed as entry parameter
     * @param form
     * @see #showForm
     * @return true if the form is showed.
     */
    @Override
    public boolean showFormInEDTh(String form) {
        if (!this.isLoaded()) {
            this.load();
        }
        if (this.checkVisiblePermission(form)) {
            if (!this.loadedList.contains(form)) {
                this.loadFormInEDTh(form);
            }
            this.tabbedPane.setSelectedIndex(0);
            this.currentForm = form;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int indexOf(TreeTabbedDetailForm searchedForm) {
        Hashtable keyValues = searchedForm.getKeysValues();
        String entityName = searchedForm.getEntityName();
        int size = this.tabbedPane.getTabCount();
        // i==0 is use by result table.
        for (int i = 1; i < size; i++) {
            Component component = this.tabbedPane.getComponentAt(i);
            if (component instanceof TreeTabbedDetailForm) {
                TreeTabbedDetailForm detailForm = (TreeTabbedDetailForm) component;
                if ((detailForm.getEntityName() != null) && detailForm.getEntityName().equals(entityName)) {
                    if ((detailForm.getMode() == InteractionManager.UPDATE)
                            && (searchedForm.getMode() == InteractionManager.UPDATE)) {
                        Form currentForm = detailForm.getForm();
                        Vector keyAttrs = currentForm.getKeys();
                        boolean check = true;
                        for (Object key : keyAttrs) {
                            if (keyValues.containsKey(key)) {
                                Object keyValue = keyValues.get(key);
                                if ((keyValue instanceof Vector) && (((Vector) keyValue).size() > 0)) {
                                    keyValue = ((Vector) keyValues.get(key)).firstElement();
                                }
                                if (!keyValue.equals(currentForm.getDataFieldValue(key.toString()))) {
                                    check = false;
                                    break;
                                }
                            } else {
                                check = false;
                                break;
                            }
                        }
                        if (check) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public int indexOfComponent(Component component) {
        return this.tabbedPane.indexOfComponent(component);
    }

    @Override
    public void removeTab(int index) {
        this.tabbedPane.remove(index);

    }

    @Override
    public void showTab(int index) {
        this.tabbedPane.setSelectedIndex(index);

    }

    @Override
    public void setTitleAt(int index, String text) {
        this.tabbedPane.setTitleAt(index, text);

    }

    @Override
    public Form getMainForm() {
        return this.mainForm;
    }

    protected void setMainForm(Form form) {
        this.mainForm = form;
        String title = this.mainForm.getFormTitle() != null ? this.mainForm.getFormTitle()
                : this.mainForm.getEntityName();
        if (title == null) {
            title = this.mainForm.getArchiveName();
        }
        this.setTitleAt(0, ApplicationManager.getTranslation(title, this.getResourceBundle()));
    }

    protected static class FormTabbedPane extends JTabbedPane {

        public FormTabbedPane() {
            KeyStroke altF = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, java.awt.event.InputEvent.CTRL_DOWN_MASK, false);
            Action control_tab_action = new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = FormTabbedPane.this.getSelectedIndex();
                    int total = FormTabbedPane.this.getTabCount();
                    index++;
                    if (index >= total) {
                        index = 0;
                    }
                    FormTabbedPane.this.setSelectedIndex(index);
                }
            };
            this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(altF, "CONTROL_TAB");
            this.getActionMap().put("CONTROL_TAB", control_tab_action);
        }

        @Override
        public void addTab(String title, Component component) {
            int index = this.getTabCount();
            super.addTab(title, component);
            if (index > 0) {
                this.setTabComponentAt(index, new ButtonTabComponent(this));
            }
        }

        public void replaceTab(int index, String title, Component component) {
            if (index == this.getTabCount()) {
                this.addTab(title, component);
            } else {
                this.setComponentAt(index, component);
                this.setTitleAt(index, title);
                if (index > 0) {
                    this.setTabComponentAt(index, new ButtonTabComponent(this));
                }

            }
        }

    }

}
