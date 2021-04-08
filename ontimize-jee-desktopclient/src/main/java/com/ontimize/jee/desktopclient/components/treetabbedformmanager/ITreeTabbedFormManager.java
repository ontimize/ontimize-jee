/**
 *
 */
package com.ontimize.jee.desktopclient.components.treetabbedformmanager;

import java.awt.Component;

import com.ontimize.gui.Form;
import com.ontimize.gui.manager.IFormManager;

/**
 * The Interface ITreeTabbedFormManager.
 */
public interface ITreeTabbedFormManager extends IFormManager {

    /** The tree table builder. */
    String TREE_TABLE_BUILDER = "treetablebuilder";

    /** XML Attribute. */
    String TREE = "tree";

    /**
     * Index of.
     * @param searchedForm the searched form
     * @return the int
     */
    int indexOf(TreeTabbedDetailForm searchedForm);

    /**
     * Index of component.
     * @param component the component
     * @return the int
     */
    int indexOfComponent(Component component);

    /**
     * Removes the tab.
     * @param index the index
     */
    void removeTab(int index);

    /**
     * Show tab.
     * @param index the index
     */
    void showTab(int index);

    /**
     * Sets the title at.
     * @param index the index
     * @param text the text
     */
    void setTitleAt(int index, String text);

    /**
     * Gets the main form.
     * @return the main form
     */
    Form getMainForm();

}
