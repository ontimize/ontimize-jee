package com.ontimize.jee.common.util.extend;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtendedMenuXmlParser extends ExtendedFormXmlParser {

    public static final String REMOVE_SEPARATOR_OP = "RemoveSeparator";

    public static final String POSITION_ATRIBUTE = "position";

    public static final String MENU_SEPARATOR = "MenuSeparator";

    public static final String TOOLBAR_SEPARATOR = "ApToolBarSeparator";

    public static final String MENU_ELEMENT = "MenuBar";

    public static final String TOOLBAR_ELEMENT = "ApplicationToolBar";

    /**
     * Implementation of REMOVE_SEPARATOR_OP extending super class
     *
     * @author Imatia Innovation
     */
    protected class ExtendedMenuXmlOperation extends ExtendedXmlOperation {

        protected int position = 0;

        public ExtendedMenuXmlOperation(String operation, String attrName, int position) {
            this.operation = operation;
            this.attrName = attrName;
            this.parentContainerType = null;
            this.levelsToParent = 1;
            this.extendCode = null;
            this.position = position;
        }

        public int getPosition() {
            return this.position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

    }

    /**
     * This method extends functionality of super method adding REMOVE_SEPARATOR_OP. This operation
     * removes the element <SeparadorMenu /> or <ToolBarSeparator /> without attr in original Xml
     */
    @Override
    protected void executeOperation(Document originalForm, ExtendedXmlOperation operation) throws Exception {

        String operationType = operation.getOperation();
        if (operationType != null) {
            if (operationType.equals(ExtendedMenuXmlParser.REMOVE_SEPARATOR_OP)) {
                Node firstNode = originalForm.getDocumentElement();
                Node foundNode = null;
                String nodeName = firstNode.getNodeName();

                // Trys to remove separator in root node
                if (nodeName.equalsIgnoreCase(ExtendedMenuXmlParser.TOOLBAR_ELEMENT)) {
                    foundNode = firstNode;
                } else {
                    // If is not root node search the container node (in Menu
                    // cases)
                    foundNode = this.searchNode(firstNode, operation.getAttrName());
                }

                // If the container node is founded
                if (foundNode != null) {
                    // The position of separator to remove
                    int position = ((ExtendedMenuXmlOperation) operation).getPosition();

                    NodeList nodeList = foundNode.getChildNodes();
                    int numOfSeparators = 0;
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node nodeToRemove = nodeList.item(i);
                        // if child is menu separator or toolbar separator class
                        // and
                        // is in position removes it
                        if (nodeToRemove.getNodeName().equals(ExtendedMenuXmlParser.MENU_SEPARATOR)
                                || nodeToRemove.getNodeName().equals(ExtendedMenuXmlParser.TOOLBAR_SEPARATOR)) {
                            numOfSeparators++;
                            if (numOfSeparators == position) {
                                foundNode.removeChild(nodeToRemove);
                                break;
                            }
                        }
                    }
                }
            } else {
                super.executeOperation(originalForm, operation);
            }
        }
    }

    /**
     * Extends super method to add REMOVE_SEPARATOR_OP functionality
     */
    @Override
    protected ExtendedXmlOperation getNodeOperation(Node node) throws Exception {

        String nodeName = node.getNodeName();
        if (nodeName.equalsIgnoreCase(ExtendedMenuXmlParser.REMOVE_SEPARATOR_OP)) {
            NamedNodeMap mapAtributes = node.getAttributes();
            Node attrNode = mapAtributes.getNamedItem(ExtendedXmlParser.ATTR_ATRIBUTE);
            Node position = mapAtributes.getNamedItem(ExtendedMenuXmlParser.POSITION_ATRIBUTE);

            if (attrNode == null) {
                throw new Exception("Missing " + ExtendedXmlParser.ATTR_ATRIBUTE + " in " + node.getNodeName());
            }
            if (position == null) {
                throw new Exception("Missing " + ExtendedMenuXmlParser.POSITION_ATRIBUTE + " in " + node.getNodeName());
            }

            return new ExtendedMenuXmlOperation(nodeName, attrNode.getNodeValue(),
                    Integer.parseInt(position.getNodeValue()));
        } else {
            return super.getNodeOperation(node);
        }
    }

    /**
     * The REMOVE_SEPARATOR_OP is an extend operation too
     */
    @Override
    protected boolean isExtendedNode(Node node) {

        String nodeName = node.getNodeName();

        if (nodeName.equalsIgnoreCase(ExtendedMenuXmlParser.REMOVE_SEPARATOR_OP)) {
            return true;
        } else {
            return super.isExtendedNode(node);
        }
    }

}
