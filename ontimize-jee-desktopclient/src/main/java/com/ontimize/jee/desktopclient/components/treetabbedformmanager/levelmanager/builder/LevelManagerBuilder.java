/**
 *
 */
package com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.builder;

import java.util.ResourceBundle;

import com.ontimize.gui.Form;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.LevelManager;

/**
 * The Interface LevelManagerBuilder.
 */
public interface LevelManagerBuilder {

    /**
     * Builds the level manager.
     * @param uriFile the uri file
     * @param formManager the form manager
     * @param resourceBundle the resource bundle
     * @return the level manager
     */
    LevelManager buildLevelManager(String uriFile, Form formManager, ResourceBundle resourceBundle);

    /**
     * Builds the level manager.
     * @param content the content
     * @param formManager the form manager
     * @param resourceBundle the resource bundle
     * @return the level manager
     */
    LevelManager buildLevelManager(StringBuffer content, Form formManager, ResourceBundle resourceBundle);

}
