package com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager;

import java.awt.Component;
import java.util.List;
import java.util.ResourceBundle;

public interface PathManager {

    void showPath(List<Level> pathLevels);

    void setLevelManager(LevelManager levelManager);

    Component createGUIComponent();

    void setResourceBundle(ResourceBundle resourceBundle);

}
