package com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager;

import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JComponent;

import com.ontimize.gui.Form;

public interface LevelManager {

    // available params
    String PARENTFORM = "parentform";

    String SECTIONS = "sections";

    String BUTTONS = "buttons";

    String PATH_MANAGER_CLASS = "pathmanagerclass";

    Level getLevel(String id);

    Level getFirstLevel();

    void show(String levelId);

    Form getParentForm();

    void openFormInMainTab(Level sourceLevel, int mode);

    void openFormInTab(Level sourceLevel, int mode);

    void add(Level level);

    JComponent getGUIComponent();

    void setResourceBundle(ResourceBundle resources);

    ResourceBundle getResourceBundle();

    void setComponentLocale(Locale locale);

    Collection<?> getTextsToTranslate();

}
