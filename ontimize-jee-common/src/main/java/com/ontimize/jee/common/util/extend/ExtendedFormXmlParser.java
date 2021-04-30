package com.ontimize.jee.common.util.extend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ExtendedFormXmlParser extends ExtendedXmlParser {

    static final Logger logger = LoggerFactory.getLogger(ExtendedFormXmlParser.class);

    @Override
    protected void executeOperation(Document originalForm, ExtendedXmlOperation operation) throws Exception {

        Node foundNode = null;
        Node formNode = originalForm.getDocumentElement();

        if (operation.getOperation().equals(ExtendedXmlParser.MODIFY_FORM)) {
            foundNode = formNode;
        } else {
            foundNode = this.searchNode(formNode, operation.getAttrName());
        }

        if (foundNode != null) {
            Node parentInLevel;
            if (operation.levelsToParent != 0) {
                parentInLevel = this.searchParentInLevel(foundNode, operation.getParentContainerType(),
                        operation.getLevelsToParent());
            } else {
                parentInLevel = foundNode;
            }

            if (parentInLevel != null) {
                String operationType = operation.getOperation();
                if (operationType != null) {
                    // Parent of founded reference node
                    Node parent = parentInLevel.getParentNode();

                    if (parent != null) {

                        // Tries to insert new nodes after reference node
                        if (operationType.equals(ExtendedXmlParser.INSERT_AFTER_OP)) {
                            insertNewNodesAfterReferenceNode(originalForm, operation, parentInLevel, parent);
                        }

                        // tries to insert new nodes before reference node
                        if (operationType.equals(ExtendedXmlParser.INSERT_BEFORE_OP)) {
                            insertNewNodesBeforeReferenceNode(originalForm, operation, parentInLevel, parent);

                        }

                        // tries to remove the reference node in document
                        if (operationType.equals(ExtendedXmlParser.REMOVE_OP)) {
                            parent.removeChild(parentInLevel);
                        }

                        // tries to replace reference node with new nodes
                        if (operationType.equals(ExtendedXmlParser.REPLACE_OP)) {

                            insertNewNodesBeforeReferenceNode(originalForm, operation, parentInLevel, parent);

                            // In last removes the reference node
                            parent.removeChild(parentInLevel);
                        }

                        if (operationType.equals(ExtendedXmlParser.MODIFY_FORM)) {

                            modifyForm(operation, foundNode);
                        }

                        if (operationType.equals(ExtendedXmlParser.MODIFY_ATTRIBUTE)) {
                            modifyAttribute(originalForm, operation, parentInLevel);
                        }
                    }
                }
            }
        }
    }

    protected void modifyAttribute(Document originalForm, ExtendedXmlOperation operation, Node parentInLevel) {
        NodeList nodes = operation.getExtendCode();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node actualNode = nodes.item(i);
            if (actualNode.getNodeType() == Node.ELEMENT_NODE) {
                Element current = (Element) actualNode;
                String nodeName = current.getAttribute(ExtendedXmlParser.MODIFY_ATTRIBUTE_NAME);
                String nodeValue = current.getAttribute(ExtendedXmlParser.MODIFY_ATTRIBUTE_VALUE);
                String nodeSeparator = current.getAttribute(ExtendedXmlParser.MODIFY_ATTRIBUTE_SEPARATOR);
                String nodeOperationType = current.getAttribute(ExtendedXmlParser.MODIFY_ATTRIBUTE_OPERATION);

                NamedNodeMap mapAtributes = parentInLevel.getAttributes();
                if (mapAtributes.getLength() > 0) {
                    Node attrToModify = mapAtributes.getNamedItem(nodeName);
                    if (attrToModify != null) {
                        if (nodeOperationType.equalsIgnoreCase(ExtendedXmlParser.MODIFY_ATTRIBUTE_OPERATOR_ADD)) {
                            StringBuilder buffer = new StringBuilder();
                            buffer.append(attrToModify.getNodeValue());
                            buffer.append(nodeSeparator);
                            buffer.append(nodeValue);
                            attrToModify.setNodeValue(buffer.toString());
                        }

                        if (nodeOperationType.equalsIgnoreCase(ExtendedXmlParser.MODIFY_ATTRIBUTE_OPERATOR_REMOVE)) {
                            if (nodeValue.equals("")) {
                                mapAtributes.removeNamedItem(attrToModify.getNodeName());
                            } else {
                                String originalValue = attrToModify.getNodeValue();
                                int removeLength = nodeValue.length();
                                int index = originalValue.indexOf(nodeValue);
                                if (originalValue.toLowerCase().contains(nodeValue.toLowerCase())) {
                                    if (originalValue.length() == removeLength) {
                                        attrToModify.setNodeValue("");
                                    } else if (index == 0) {
                                        attrToModify.setNodeValue(originalValue.replace(nodeValue + nodeSeparator, ""));
                                    } else if (originalValue.length() == (index + removeLength)) {
                                        attrToModify.setNodeValue(originalValue.replace(nodeSeparator + nodeValue, ""));
                                    } else {
                                        attrToModify.setNodeValue(
                                                originalValue.replace(nodeSeparator + nodeValue + nodeSeparator, ";"));
                                    }
                                }
                            }
                        }

                        if (nodeOperationType.equalsIgnoreCase("")) {
                            attrToModify.setNodeValue(nodeValue);
                        }
                    } else if (!nodeOperationType
                        .equalsIgnoreCase(ExtendedXmlParser.MODIFY_ATTRIBUTE_OPERATOR_REMOVE)) {
                        Node newNode;
                        newNode = originalForm.createAttribute(nodeName);
                        newNode.setNodeValue(nodeValue);
                        mapAtributes.setNamedItem(newNode);
                    }
                } else if (!nodeOperationType.equalsIgnoreCase(ExtendedXmlParser.MODIFY_ATTRIBUTE_OPERATOR_REMOVE)) {
                    Node newNode;
                    newNode = originalForm.createAttribute(nodeName);
                    newNode.setNodeValue(nodeValue);
                    mapAtributes.setNamedItem(newNode);
                }
            }
        }
    }

    protected void modifyForm(ExtendedXmlOperation operation, Node foundNode) {
        NodeList nodes = operation.getExtendCode();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node actualNode = nodes.item(i);
            if (actualNode.getNodeType() == Node.ELEMENT_NODE) {
                Element current = (Element) actualNode;
                String nodeName = current.getAttribute(ExtendedXmlParser.MODIFY_ATTRIBUTE_NAME);
                String nodeValue = current.getAttribute(ExtendedXmlParser.MODIFY_ATTRIBUTE_VALUE);

                NamedNodeMap mapAtributes = foundNode.getAttributes();
                if (mapAtributes.getLength() > 0) {
                    Node attrToModify = mapAtributes.getNamedItem(nodeName);
                    if (attrToModify != null) {
                        attrToModify.setNodeValue(nodeValue);
                    }
                }

            }
        }
    }

    protected void insertNewNodesBeforeReferenceNode(Document originalForm, ExtendedXmlOperation operation,
            Node parentInLevel, Node parent) {
        // Inserts new nodes before the reference node using
        // insertBefore method of father
        NodeList nodesToAdd = operation.getExtendCode();
        for (int i = 0; i < nodesToAdd.getLength(); i++) {
            Node node = nodesToAdd.item(i);
            if (node != null) {
                Node importedNode = originalForm.importNode(node, true);
                parent.insertBefore(importedNode, parentInLevel);
            }
        }
    }

    protected void insertNewNodesAfterReferenceNode(Document originalForm, ExtendedXmlOperation operation,
            Node parentInLevel, Node parent) {
        // If founded reference node has a node in right,
        // use it to
        // insert the new nodes after (with insertBefore
        // method)
        Node nextSiblingNode = parentInLevel.getNextSibling();
        if (nextSiblingNode != null) {
            insertNewNodesBeforeReferenceNode(originalForm, operation, nextSiblingNode, parent);
        } else {
            // If founded reference node has not a node in
            // right,
            // the try to append new nodes in father
            NodeList nodesToAdd = operation.getExtendCode();
            for (int i = 0; i < nodesToAdd.getLength(); i++) {
                Node node = nodesToAdd.item(i);
                if (node != null) {
                    Node importedNode = originalForm.importNode(node, true);
                    parent.appendChild(importedNode);
                }
            }
        }
    }

    /**
     * Recursive method that searches in a node an element of type parentType and it's in the exact
     * level beginning the count of levels in the node. The method makes an inverse deep search of an
     * element in a node
     * @param node The node in which search operation is made and in which the search begins
     * @param parentType Type of element to search
     * @param level Number of elements of type parentType than exist before the searched element
     * @return the searched node or null if it's not found
     */
    protected Node searchParentInLevel(Node node, String parentType, int level) {

        if (level == 0) {
            if (parentType == null) {
                return node;
            } else if (node.getNodeName().equals(parentType)) {
                return node;
            } else {
                return null;
            }
        }

        Node parentNode = node.getParentNode();
        if (parentNode != null) {
            if (parentNode.getNodeName().equals(parentType)) {
                return this.searchParentInLevel(parentNode, parentType, level - 1);
            } else {
                return this.searchParentInLevel(parentNode, parentType, level);
            }
        }
        return null;
    }

    /**
     * The method makes a deep search of a node the by attr attribute.
     * @param node The node in that the search is made
     * @param attr attr attribute of node searched
     * @return the searched node or null if the node is not found
     */
    protected Node searchNode(Node node, String attr) {
        NamedNodeMap map = node.getAttributes();
        if (map != null) {
            Node attrNode = map.getNamedItem("attr");
            if (attrNode == null) {
                attrNode = map.getNamedItem("entity");
            }
            if (attrNode == null) {
                attrNode = map.getNamedItem("key");
            }

            if (attrNode != null) {
                if (attrNode.getNodeValue().equals(attr)) {
                    return node;
                }
            }

        }

        NodeList nodeList = node.getChildNodes();
        if (nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node childNode = nodeList.item(i);
                Node foundNode = this.searchNode(childNode, attr);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }

        return null;
    }

}
