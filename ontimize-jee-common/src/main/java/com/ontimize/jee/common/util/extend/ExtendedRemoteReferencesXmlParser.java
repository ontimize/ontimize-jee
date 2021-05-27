package com.ontimize.jee.common.util.extend;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.StringTokenizer;

public class ExtendedRemoteReferencesXmlParser extends ExtendedXmlParser {

    @Override
    protected void executeOperation(Document originalForm, ExtendedXmlOperation operation) throws Exception {
        Element foundNode = null;
        Node formNode = originalForm.getDocumentElement();

        if (operation.getOperation().equals(ADD_REMOTE_REFERENCE)) {
            foundNode = (Element) formNode;
        } else {
            foundNode = (Element) this.searchNode(formNode, operation.getAttrName());
        }

        if (foundNode != null) {
            String operationType = operation.getOperation();

            if (operationType.equalsIgnoreCase(ADD_REMOTE_REFERENCE)) {
                appendAddRemoteReference(originalForm, operation, foundNode);
            }

            if (operationType.equalsIgnoreCase(REMOVE_REMOTE_REFERENCE)) {
                Node parentNode = foundNode.getParentNode();
                parentNode.removeChild(foundNode);
            }

            if (operationType.equalsIgnoreCase(MODIFY_REMOTE_REFERENCE)) {
                NodeList operationList = operation.getExtendCode();
                for (int i = 0; i < operationList.getLength(); i++) {
                    Node currentGroupOperationsNode = operationList.item(i);
                    modifyGroupOperationsNode(originalForm, foundNode, currentGroupOperationsNode);
                }
            }
        }
    }

    protected void modifyGroupOperationsNode(Document originalForm, Element foundNode,
            Node currentGroupOperationsNode) {
        if (currentGroupOperationsNode.getNodeName().equalsIgnoreCase(MODIFY_REFERENCE_MODIFY_INIT)) {
            foundNode.setAttribute(MODIFY_REFERENCE_INIT,
                    currentGroupOperationsNode.getAttributes()
                        .getNamedItem(MODIFY_ATTRIBUTE_VALUE)
                        .getNodeValue());
        }
        if ((currentGroupOperationsNode.getNodeType() == Node.ELEMENT_NODE) && !currentGroupOperationsNode.getNodeName()
            .equalsIgnoreCase(MODIFY_REFERENCE_PARAMS)) {
            NodeList foundElementList = foundNode.getChildNodes();
            for (int j = 0; j < foundElementList.getLength(); j++) {
                Node currentElement = foundElementList.item(j);
                if (currentGroupOperationsNode.getNodeName()
                    .equalsIgnoreCase(MODIFY_REFERENCE_MODIFY_NAME)
                        && currentElement.getNodeName()
                            .equals(MODIFY_REFERENCE_NAME)) {
                    Node childElementText = currentElement.getFirstChild();
                    childElementText.setNodeValue(currentGroupOperationsNode.getAttributes()
                        .getNamedItem(MODIFY_ATTRIBUTE_VALUE)
                        .getNodeValue());
                } else if (currentGroupOperationsNode.getNodeName()
                    .equalsIgnoreCase(MODIFY_REFERENCE_MODIFY_CLASS)
                        && currentElement.getNodeName()
                            .equals(MODIFY_REFERENCE_CLASS)) {
                    Node childElementText = currentElement.getFirstChild();
                    childElementText.setNodeValue(currentGroupOperationsNode.getAttributes()
                        .getNamedItem(MODIFY_ATTRIBUTE_VALUE)
                        .getNodeValue());
                }

            }
        }

        if ((currentGroupOperationsNode.getNodeType() == Node.ELEMENT_NODE) && currentGroupOperationsNode.getNodeName()
            .equalsIgnoreCase(MODIFY_REFERENCE_PARAMS)) {
            modifyReferenceParameters(originalForm, foundNode, currentGroupOperationsNode);
        }
    }

    protected void modifyReferenceParameters(Document originalForm, Element foundNode,
            Node currentGroupOperationsNode) {
        NodeList paramNodesOperation = currentGroupOperationsNode.getChildNodes();
        for (int j = 0; j < paramNodesOperation.getLength(); j++) {
            Node paramNodeOperation = paramNodesOperation.item(j);
            if (paramNodeOperation.getNodeType() == Node.ELEMENT_NODE) {

                String nodeName = ((Element) paramNodeOperation).getAttribute(MODIFY_ATTRIBUTE_NAME)
                    .toString();
                String nodeValue = ((Element) paramNodeOperation).getAttribute(MODIFY_ATTRIBUTE_VALUE)
                    .toString();
                String nodeOperation = ((Element) paramNodeOperation)
                    .getAttribute(MODIFY_ATTRIBUTE_OPERATION);
                String nodeSeparator = ((Element) paramNodeOperation)
                    .getAttribute(MODIFY_ATTRIBUTE_SEPARATOR);

                Node paramsToModify = this.searchNodeName(nodeName, foundNode);

                if ((nodeOperation.equalsIgnoreCase(MODIFY_ATTRIBUTE_OPERATOR_ADD)
                        && (paramsToModify == null))
                        || (nodeOperation
                            .equalsIgnoreCase("") && (paramsToModify == null))) {
                    Element newNode = originalForm.createElement("Param");
                    Element name = originalForm.createElement("Name");
                    Element value = originalForm.createElement("Value");
                    name.appendChild(originalForm.createTextNode(nodeName));
                    value.appendChild(originalForm.createTextNode(nodeValue));
                    newNode.appendChild(name);
                    newNode.appendChild(value);
                    foundNode.appendChild(originalForm.importNode(newNode, true));
                } else if (nodeOperation.equalsIgnoreCase(MODIFY_ATTRIBUTE_OPERATOR_ADD)
                        && (paramsToModify != null)) {
                    modifyAttributeOperatorAdd(nodeValue, nodeSeparator, paramsToModify);

                } else if (nodeOperation.equalsIgnoreCase(MODIFY_ATTRIBUTE_OPERATOR_REMOVE)) {
                    modifyAttributeOperatorRemove(nodeValue, nodeSeparator, paramsToModify);

                } else if (nodeOperation.equalsIgnoreCase("")) {
                    paramsToModify = paramsToModify.getParentNode();
                    NodeList valueToModify = ((Element) paramsToModify)
                        .getElementsByTagName(MODIFY_REFERENCE_VALUE);
                    valueToModify.item(0).getFirstChild().setNodeValue(nodeValue);
                }
            }
        }
    }

    protected void modifyAttributeOperatorAdd(String nodeValue, String nodeSeparator, Node paramsToModify) {
        paramsToModify = paramsToModify.getParentNode();
        NodeList valueToModify = ((Element) paramsToModify)
            .getElementsByTagName(MODIFY_REFERENCE_VALUE);
        String oldvalue = valueToModify.item(0).getFirstChild().getNodeValue();
        StringBuilder buffer = new StringBuilder();
        buffer.append(oldvalue);
        buffer.append(nodeSeparator);
        buffer.append(nodeValue);
        valueToModify.item(0).getFirstChild().setNodeValue(buffer.toString());
    }

    protected void modifyAttributeOperatorRemove(String nodeValue, String nodeSeparator, Node paramsToModify) {
        if (nodeValue.equalsIgnoreCase("")) {
            paramsToModify.getParentNode().removeChild(paramsToModify);
        } else {
            NodeList valueToModify = ((Element) paramsToModify)
                .getElementsByTagName(MODIFY_REFERENCE_VALUE);
            String oldvalue = valueToModify.item(0).getFirstChild().getNodeValue();
            StringTokenizer tokens = new StringTokenizer(oldvalue, nodeSeparator);
            StringBuilder buffer = new StringBuilder();
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                if (!token.equalsIgnoreCase(nodeValue)) {
                    buffer.append(token);
                    buffer.append(nodeSeparator);
                }
            }

            if (buffer.length() > 0) {
                buffer.deleteCharAt(buffer.length() - 1);
            }
            valueToModify.item(0).getFirstChild().setNodeValue(buffer.toString());

        }
    }

    protected void appendAddRemoteReference(Document originalForm, ExtendedXmlOperation operation, Element foundNode) {
        NodeList nodes = operation.getExtendCode();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node actualNode = nodes.item(i);
            if (actualNode.getNodeType() == Node.ELEMENT_NODE) {
                foundNode.appendChild(originalForm.importNode(actualNode, true));
            }
        }
    }

    /**
     * The method makes a deep search of a node the by attr attribute.
     * @param node The node in that the search is made
     * @param attr attr attribute of node searched
     * @return the searched node or null if the node is not found
     */
    protected Node searchNode(Node node, String attr) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node actualNode = childNodes.item(i);
            if (actualNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList childNodesFromActual = actualNode.getChildNodes();
                for (int j = 0; j < childNodesFromActual.getLength(); j++) {
                    Node checkNameValue = childNodesFromActual.item(j);
                    if ((checkNameValue.getNodeType() == Node.ELEMENT_NODE)
                            && checkNameValue.getNodeName().equalsIgnoreCase("Name")) {
                        Element current = (Element) checkNameValue;
                        if (current.getTextContent().equalsIgnoreCase(attr)) {
                            return actualNode;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected Node searchNodeName(String name, Element foundNode) {
        NodeList paramsNodesDocument = foundNode.getElementsByTagName(MODIFY_REFERENCE_NAME);
        for (int i = 0; i < paramsNodesDocument.getLength(); i++) {
            Node paramNodeDocument = paramsNodesDocument.item(i);
            if (name.equalsIgnoreCase(paramNodeDocument.getTextContent())) {
                return paramNodeDocument.getParentNode();
            }
        }
        return null;
    }

}
