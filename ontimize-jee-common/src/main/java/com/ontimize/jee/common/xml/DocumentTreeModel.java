package com.ontimize.jee.common.xml;

import com.ontimize.jee.common.builder.CustomNode;
import org.w3c.dom.Node;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class DocumentTreeModel implements TreeModel {

    CustomNode rootNode;

    public DocumentTreeModel(Node root) {
        this.rootNode = new CustomNode(root);
    }

    @Override
    public Object getRoot() {
        return this.rootNode;
    }

    @Override
    public boolean isLeaf(Object node) {
        CustomNode customNode = (CustomNode) node;
        if (customNode.hasChildren()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Object getChild(Object parent, int index) {
        CustomNode parentNode = (CustomNode) parent;
        return parentNode.child(index);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        CustomNode parentNode = (CustomNode) parent;
        return parentNode.index((CustomNode) child);
    }

    @Override
    public int getChildCount(Object node) {
        CustomNode customNode = (CustomNode) node;
        return customNode.getChildrenNumber();
    }

}
