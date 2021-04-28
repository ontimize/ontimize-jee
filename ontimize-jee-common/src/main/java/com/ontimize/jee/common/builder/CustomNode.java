package com.ontimize.jee.common.builder;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.HashMap;

public class CustomNode {

    protected Node xMLDocumentNode;

    public static final int PANEL = 0;

    public static final int FIELD = 1;

    public static final int BUTTON = 2;

    public static final int ARROW = 3;

    public static final int LINE = 4;

    // Constructor
    public CustomNode(Node node) {
        this.xMLDocumentNode = node;
    }

    /**
     * If it is a tag returns true. Other case return false
     * @return
     */
    public boolean isTag() {
        return this.xMLDocumentNode.getNodeType() == Node.ELEMENT_NODE;
    }

    /**
     * Creates a text based on the type of node
     *
     */
    public String getNodeInfo() {
        switch (this.xMLDocumentNode.getNodeType()) {
            case Node.ELEMENT_NODE:
                return this.xMLDocumentNode.getNodeName().trim();
            case Node.CDATA_SECTION_NODE:
                return this.xMLDocumentNode.getNodeValue().trim();
            case Node.TEXT_NODE:
                return this.xMLDocumentNode.getNodeValue().trim();
            default:
                return "";
        }

    }

    @Override
    public String toString() {
        return this.getNodeInfo().trim();
    }

    public boolean hasChildren() {
        return this.xMLDocumentNode.hasChildNodes();
    }

    public NamedNodeMap attributeList() {
        return this.xMLDocumentNode.getAttributes();
    }

    public Map<String, String> hashtableAttribute() {
        // Gets the attribute list
        NamedNodeMap attributeList = this.attributeList();
        Map<String, String> attributeTable = new HashMap<String, String>();
        for (int i = 0; i < attributeList.getLength(); i++) {
            Node node = attributeList.item(i);
            attributeTable.put(node.getNodeName(), node.getNodeValue());
        }
        return attributeTable;
    }

    public CustomNode child(int index) {
        Node node = this.xMLDocumentNode.getChildNodes().item(index);
        return new CustomNode(node);

    }

    public int index(CustomNode child) {
        int childNumber = this.xMLDocumentNode.getChildNodes().getLength();
        for (int i = 0; i < childNumber; i++) {
            CustomNode node = this.child(i);
            if (child == node) {
                return i;
            }
        }
        return -1;
    }

    public int getChildrenNumber() {
        return this.xMLDocumentNode.getChildNodes().getLength();
    }

    public Node getXMLDocumentNode() {
        return this.xMLDocumentNode;
    }

}
