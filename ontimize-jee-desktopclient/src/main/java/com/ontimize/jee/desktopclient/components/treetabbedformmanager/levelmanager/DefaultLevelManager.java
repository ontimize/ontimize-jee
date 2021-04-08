package com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JComponent;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Form;
import com.ontimize.gui.IDetailForm;
import com.ontimize.gui.InteractionManager;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.container.CardPanel;
import com.ontimize.gui.container.Column;
import com.ontimize.gui.container.Row;
import com.ontimize.jee.common.tools.ConcatTools;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.ITreeTabbedFormManager;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.TreeTabbedDetailForm;
import com.ontimize.util.ParseUtils;

public class DefaultLevelManager extends Column implements LevelManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLevelManager.class);
    static final String COL_TAG = "cp.";

    private static final String HOME_ICON = "com/ontimize/gui/images/24/home.png";

    private static final String PREV_ICON = "com/ontimize/gui/images/24/undo.png";

    private static final String REFRESH_ICON = "com/ontimize/gui/images/24/refresh.png";

    private static final String NEXT_ICON = "com/ontimize/gui/images/24/redo.png";

    private static final String NEW_FORM_ICON = "com/ontimize/gui/images/24/newform.png";

    private static final String SEARCH_ICON = "com/ontimize/gui/images/24/search.png";

    private static final String PREV_KEY = "previous";

    private static final String HOME_KEY = "home";

    private static final String REFRESH_KEY = "refresh";

    private static final String NEXT_KEY = "next";

    private static final String NEW_FORM_KEY = "newForm";

    private static final String SEARCH_KEY = "queryData";

    // buttons keys
    private static final String QUERY_BUTTON = "query";

    private static final String INSERT_BUTTON = "insert";

    private static final String PREVIOUS_BUTTON = "previous";

    private static final String NEXT_BUTTON = "next";

    private static final String HOME_BUTTON = "home";

    private static final String REFRESH_BUTTON = "refresh";

    // sections keys
    private static final String BUTTONS_SECTION = "buttons";

    private static final String PATH_SECTION = "path";

    private static final String MAIN_SECTION = "main";

    private Button bPrevious;

    private Button bHome;

    private Button bRefresh;

    private Button bNext;

    private Button bNewForm;

    private Button bSearch;

    private String buttonsToCreate;

    private String sectionsToCreate;

    private final Vector<String> listLevels = new Vector<>(1);

    private final Map<String, Level> levels = new HashMap<>();

    private CardPanel cardPanel;

    private final List<String> currentPath = new ArrayList<>();

    private PathManager pathManager;

    private String currentShowLevel = null;

    private ResourceBundle resourceBundle;

    public DefaultLevelManager(Hashtable parameters) {
        super(parameters);
    }

    @Override
    public void init(Hashtable parameters) {
        super.init(parameters);
        String pathManager = (String) parameters.get(LevelManager.PATH_MANAGER_CLASS);
        if (pathManager != null) {
            Class<?> classObject;
            try {
                classObject = Class.forName(pathManager);

                Constructor[] constructors = classObject.getConstructors();
                Object[] params = { parameters };
                for (Constructor constructor : constructors) {
                    if (constructor.getParameterTypes().length == 1) {
                        // TODO here we should check it is the correct constructor
                        this.pathManager = (PathManager) constructor.newInstance(params);
                        break;
                    }
                }

            } catch (Exception e) {
                DefaultLevelManager.logger.error(e.getMessage(), e);
            }
        }
        if (this.pathManager == null) {
            this.pathManager = new DefaultPathManager(parameters);
        }
        this.pathManager.setLevelManager(this);

        String defaultButtons = ConcatTools.concat(DefaultLevelManager.QUERY_BUTTON, '|',
                DefaultLevelManager.PREVIOUS_BUTTON, '|', DefaultLevelManager.HOME_BUTTON, '|',
                DefaultLevelManager.REFRESH_BUTTON, '|', DefaultLevelManager.NEXT_BUTTON, '|',
                DefaultLevelManager.INSERT_BUTTON);
        this.buttonsToCreate = ParseUtils.getString((String) parameters.get(LevelManager.BUTTONS), defaultButtons);
        if (this.buttonsToCreate.trim().isEmpty()) {
            this.buttonsToCreate = defaultButtons;
        }

        String defaultSections = ConcatTools.concat(DefaultLevelManager.BUTTONS_SECTION, '|',
                DefaultLevelManager.PATH_SECTION, '|', DefaultLevelManager.MAIN_SECTION);
        this.sectionsToCreate = ParseUtils.getString((String) parameters.get(LevelManager.SECTIONS), defaultSections);
        if (this.sectionsToCreate.trim().isEmpty()) {
            this.sectionsToCreate = defaultSections;
        }

        this.createSections(this.sectionsToCreate);

        Form parentForm = (Form) parameters.get(LevelManager.PARENTFORM);
        this.setParentForm(parentForm);

    }

    private void createSections(String sections) {
        if (sections != null) {
            String[] split = StringUtils.split(sections, '|');
            for (String sectionKey : split) {
                Component section = this.createSection(sectionKey);
                if (section != null) {
                    this.add(section);
                }
            }
        }
    }

    protected Component createSection(String section) {
        if (DefaultLevelManager.BUTTONS_SECTION.equalsIgnoreCase(section)) {
            return this.createButtons(this.buttonsToCreate);
        } else if (DefaultLevelManager.PATH_SECTION.equalsIgnoreCase(section)) {
            return this.createPath();
        } else if (DefaultLevelManager.MAIN_SECTION.equalsIgnoreCase(section)) {
            return this.createMain();
        }
        return null;
    }

    /**
     * @param buttons
     */
    private Component createButtons(String buttons) {
        Row rowButtons = new Row(new Hashtable());
        if (buttons != null) {
            String[] split = StringUtils.split(buttons, '|');
            for (String buttonKey : split) {
                Button b = this.createButton(buttonKey);
                if (b != null) {
                    rowButtons.add(b);
                }
            }
        }

        return rowButtons;

    }

    protected Button createButton(String button) {

        Button b = null;
        if (DefaultLevelManager.QUERY_BUTTON.equalsIgnoreCase(button)) {
            this.bSearch = this.createButton(DefaultLevelManager.SEARCH_KEY, "search", DefaultLevelManager.SEARCH_ICON,
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (DefaultLevelManager.this.currentShowLevel != null) {
                                Level level = DefaultLevelManager.this
                                    .getLevel(DefaultLevelManager.this.currentShowLevel);
                                if (level != null) {
                                    DefaultLevelManager.this.openFormInMainTab(level, InteractionManager.QUERY);
                                }
                            }

                        }
                    });
            b = this.bSearch;
        } else if (DefaultLevelManager.PREVIOUS_BUTTON.equalsIgnoreCase(button)) {

            this.bPrevious = this.createButton(DefaultLevelManager.PREV_KEY, "previous", DefaultLevelManager.PREV_ICON,
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String current = DefaultLevelManager.this.currentShowLevel;
                            DefaultLevelManager.this.show(DefaultLevelManager.this.getPrevPanel(current));
                        }
                    });
            b = this.bPrevious;
        } else if (DefaultLevelManager.HOME_BUTTON.equalsIgnoreCase(button)) {

            this.bHome = this.createButton(DefaultLevelManager.HOME_KEY, "home", DefaultLevelManager.HOME_ICON,
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            DefaultLevelManager.this.show(DefaultLevelManager.this.listLevels.firstElement());
                        }
                    });
            b = this.bHome;
        } else if (DefaultLevelManager.REFRESH_BUTTON.equalsIgnoreCase(button)) {

            this.bRefresh = this.createButton(DefaultLevelManager.REFRESH_KEY, "refresh",
                    DefaultLevelManager.REFRESH_ICON, new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            DefaultLevelManager.logger.debug("refresh actual table.");
                            DefaultLevelManager.this.show(DefaultLevelManager.this.currentShowLevel);
                        }
                    });
            b = this.bRefresh;
        } else if (DefaultLevelManager.NEXT_BUTTON.equalsIgnoreCase(button)) {
            this.bNext = this.createButton(DefaultLevelManager.NEXT_KEY, "next", DefaultLevelManager.NEXT_ICON,
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String next = DefaultLevelManager.this
                                .getNextPanel(DefaultLevelManager.this.currentShowLevel);
                            DefaultLevelManager.this.show(next);
                        }
                    });
            b = this.bNext;
        } else if (DefaultLevelManager.INSERT_BUTTON.equalsIgnoreCase(button)) {
            this.bNewForm = this.createButton(DefaultLevelManager.NEW_FORM_KEY, "newform",
                    DefaultLevelManager.NEW_FORM_ICON, new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (DefaultLevelManager.this.currentShowLevel != null) {
                                Level level = DefaultLevelManager.this
                                    .getLevel(DefaultLevelManager.this.currentShowLevel);
                                if (level != null) {
                                    DefaultLevelManager.this.openFormInTab(level, InteractionManager.INSERT);
                                }
                            }
                        }
                    });
            b = this.bNewForm;
        }
        return b;

    }

    /**
     * @return
     */
    protected Component createPath() {
        return this.pathManager.createGUIComponent();
    }

    protected Component createMain() {
        Hashtable params = new Hashtable();
        params.put("attr", "cardPanel");
        params.put("opaque", "no");
        params.put("enabled", "yes");
        this.cardPanel = new CardPanel(params);
        return this.cardPanel;
    }

    protected Button createButton(String key, String tip, String icon, ActionListener actionListener) {
        Hashtable bP = new Hashtable();
        bP.put("key", key);
        bP.put("tip", tip);
        bP.put("rollover", "yes");
        bP.put("icon", icon);
        bP.put("alttip", tip + "level");

        Button button = new Button(bP);
        button.addActionListener(actionListener);
        button.setFocusable(false);
        return button;
    }

    /**
     * @param actualPanel
     * @return
     */
    private String getPrevPanel(String actualPanel) {
        if (this.listLevels.size() == 1) {
            return this.listLevels.firstElement();
        } else {
            int elementPosition = this.listLevels.indexOf(actualPanel);
            if (elementPosition > 0) {
                return this.listLevels.get(elementPosition - 1);
            }
            return actualPanel;
        }
    }

    /**
     * @param actualPanel
     * @return
     */
    private String getNextPanel(String actualPanel) {
        if (this.listLevels.size() == 1) {
            return this.listLevels.firstElement();
        } else {
            int elementPosition = this.listLevels.indexOf(actualPanel);
            if (elementPosition < (this.listLevels.size() - 1)) {
                return this.listLevels.get(elementPosition + 1);
            }
            return actualPanel;
        }
    }

    /**
     * @param name
     */
    @Override
    public void show(String levelId) {
        Level level = this.getLevel(levelId);
        if (level != null) {
            level.reload();
        }
        this.cardPanel.show(DefaultLevelManager.COL_TAG + levelId);
        this.setButtonsStatus(levelId);
        this.currentShowLevel = levelId;
        this.buildPath(levelId);
        this.updateDisplayPath();
    }

    public void updateDisplayPath() {
        List<Level> paths = new ArrayList<>();
        for (int i = 0; i < this.currentPath.size(); i++) {
            Level linkedTable = this.getLevel(this.currentPath.get(i));
            paths.add(linkedTable);

        }
        this.pathManager.showPath(paths);
    }

    /**
     * @param name
     */
    private void setButtonsStatus(String levelId) {
        int elementPosition = this.listLevels.indexOf(levelId);
        this.updateButtonsOnLevelMoved(elementPosition);
    }

    protected void updateButtonsOnLevelMoved(int levelId) {

        // TODO basar el comportamamiento en el numero de niveles
        this.bRefresh.setEnabled(true);
        this.bPrevious.setEnabled(levelId > 0);
        this.bHome.setEnabled(levelId != 0);
        this.bNext.setEnabled(levelId < (this.listLevels.size() - 1));
    }

    public void setRefreshButtonEnabled() {
        this.bRefresh.setEnabled(true);
    }

    @Override
    public void addImpl(Component comp, Object constraints, int index) {
        if (comp instanceof Level) {

        } else {
            super.addImpl(comp, constraints, index);
        }
    }

    @Override
    public Level getLevel(String levelId) {
        return this.levels.get(levelId);
    }

    @Override
    public Level getFirstLevel() {
        return this.levels.get(this.listLevels.firstElement());
    }

    @Override
    public Form getParentForm() {
        return this.parentForm;
    }

    public void buildPath(String levelId) {
        this.currentPath.clear();
        Level first = this.getFirstLevel();
        Level l = this.getLevel(levelId);
        List<Level> levels = new ArrayList<>();
        while ((l != first) && (l != null)) {
            levels.add(0, l);
            String parentLevel = l.getPreviousLevelId();
            if (parentLevel != null) {
                l = this.getLevel(parentLevel);
            } else {
                l = null;
            }
        }
        if (l != null) {
            levels.add(0, l);
        }
        for (Level level : levels) {
            this.currentPath.add(level.getId());
        }
    }

    /**
     * Creates the detail form for this table. The detail form is used to insert new records to the
     * table and to diplay de information contained buy the table in a more detailed way using a form
     * instead a row.
     * @param iFormManager
     */
    protected IDetailForm createTabbedDetailForm(ITreeTabbedFormManager iFormManager, String levelId) {

        Form formCopy = this.parentForm.getFormManager().getFormCopy(this.getLevel(levelId).getFormName());

        if (formCopy != null) {
            return new TreeTabbedDetailForm(formCopy, this.getLevel(levelId), false, iFormManager);
        }
        return null;
    }

    @Override
    public void openFormInMainTab(Level sourceLevel, int mode) {
        if (this.parentForm.getFormManager() instanceof ITreeTabbedFormManager) {
            IDetailForm detailForm = this
                .createTabbedDetailForm((ITreeTabbedFormManager) this.parentForm.getFormManager(), sourceLevel.getId());
            ((TreeTabbedDetailForm) detailForm).setIsMain(true);
            this.openDetailForm(detailForm, sourceLevel, mode);
        }

    }

    @Override
    public void openFormInTab(Level sourceLevel, int mode) {
        if (this.parentForm.getFormManager() instanceof ITreeTabbedFormManager) {
            IDetailForm detailForm = this
                .createTabbedDetailForm((ITreeTabbedFormManager) this.parentForm.getFormManager(), sourceLevel.getId());
            ((TreeTabbedDetailForm) detailForm).setIsMain(false);
            this.openDetailForm(detailForm, sourceLevel, mode);
        }

    }

    private void openDetailForm(IDetailForm detailForm, Level level, int mode) {
        detailForm.setQueryInsertMode();
        if (mode == InteractionManager.INSERT) {
            detailForm.setInsertMode();
        } else if (mode == InteractionManager.QUERY) {
            ((TreeTabbedDetailForm) detailForm).setQueryMode();
        } else {
            detailForm.setKeys(new Hashtable<>(level.getSelectedData()), 0);
            detailForm.setUpdateMode();
        }
        detailForm.showDetailForm();
    }

    @Override
    public void add(Level level) {
        Hashtable colParams = new Hashtable();
        colParams.put("attr", DefaultLevelManager.COL_TAG + level.getId());
        if (level instanceof JComponent) {
            ((JComponent) level).setEnabled(true);
        }
        if (level instanceof Component) {
            Column column = new Column(colParams);
            column.add((Component) level, null);
            this.cardPanel.add(column, null);
        }
        this.listLevels.add(level.getId());
        this.levels.put(level.getId(), level);
    }

    @Override
    public JComponent getGUIComponent() {
        return this;
    }

    @Override
    public void setResourceBundle(ResourceBundle resources) {
        super.setResourceBundle(resources);
        this.resourceBundle = resources;
        if (this.pathManager != null) {
            this.pathManager.setResourceBundle(resources);
        }
    }

    @Override
    public ResourceBundle getResourceBundle() {
        return this.resourceBundle;
    }

}
