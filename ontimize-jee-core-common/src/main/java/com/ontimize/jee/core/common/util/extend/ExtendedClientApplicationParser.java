package com.ontimize.jee.core.common.util.extend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ExtendedClientApplicationParser {

    private static final Logger logger = LoggerFactory.getLogger(ExtendedClientApplicationParser.class);

    // OPERATIONS
    public static final String INSERT_OP = "Insert";

    public static final String MODIFY_OP = "Modify";

    public static final String REPLACE_OP = "Replace";

    public static final String DELETE_OP = "Delete";

    public static final String INSERT_MANAGER_OP = "InsertManager";

    public static final String DELETE_MANAGER_OP = "DeleteManager";

    public static final String MODIFY_MANAGER_OP = "ModifyManager";

    public static final String ATRIBUTE_OP = "Atribute";

    // APLICATION CLIENT
    public static final String PARENT_ATRIBUTE = "parent";

    public static final String NAME_ATRIBUTE = "name";

    public static final String VALUE_ATRIBUTE = "value";

    public static final String FORM_ATRIBUTE = "form";

    public static final String ID_ATRIBUTE = "id";

    // Values for Attribute
    public static final String TAGNAME = "tagname";

    public Document parseExtendedXml(Document originalForm, Document extendDocument) {
        return this.parse(originalForm, extendDocument);
    }

    protected void executeOperation(Document originalForm, ExtendedClientApplicationOperation operation)
            throws Exception {

        Node firstNode = originalForm.getDocumentElement();

        String operationType = operation.getOperation();
        if (operationType != null) {

            // REPLACE UNIQUE OPERATION
            if (operationType.equalsIgnoreCase(ExtendedClientApplicationParser.REPLACE_OP)) {
                doReplaceOperation(originalForm, operation, firstNode);
            }

            // MODIFY UNIQUE OPERATION
            if (operationType.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_OP)) {
                doModifyOperation(operation, firstNode);
            }

            // INSERT MANAGER OPERATION
            if (operationType.equalsIgnoreCase(ExtendedClientApplicationParser.INSERT_MANAGER_OP)) {
                doInsertManagerOperation(originalForm, operation, firstNode);
            }

            // DELETE MANAGER OPERATION
            if (operationType.equalsIgnoreCase(ExtendedClientApplicationParser.DELETE_MANAGER_OP)) {

                doDeleteManagerOperation(operation, firstNode);
            }

            // MODIFY MANAGER OPERATION
            if (operationType.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_MANAGER_OP)) {

                doModifyManagerOperation(originalForm, operation, firstNode);
            }
        }

    }

    protected void doModifyManagerOperation(Document originalForm, ExtendedClientApplicationOperation operation,
            Node firstNode) {
        String managerId = operation.getIdAttr();
        if (managerId != null) {
            Node managerNode = this.searchNodeByAttr(firstNode, ExtendedClientApplicationParser.ID_ATRIBUTE, managerId);
            if (managerNode != null) {
                List internalOperations = operation.getInternalOperations();
                if (internalOperations.size() > 0) {

                    for (int i = 0; i < internalOperations.size(); i++) {
                        InnerExtendedClientApplicationOperation internalOperation = (InnerExtendedClientApplicationOperation) internalOperations
                            .get(i);
                        String intOperationName = internalOperation.getOperation();

                        // Modify manager ATRIBUTE
                        if (intOperationName.equalsIgnoreCase(ExtendedClientApplicationParser.ATRIBUTE_OP)) {
                            String nameAttr = internalOperation.getNameAttr();
                            String valueAttr = internalOperation.getValueAttr();

                            NamedNodeMap mapAtributes = managerNode.getAttributes();
                            if (mapAtributes.getLength() > 0) {
                                Node attrToModify = mapAtributes.getNamedItem(nameAttr);
                                if (attrToModify != null) {
                                    attrToModify.setNodeValue(valueAttr);
                                }
                            }
                        }

                        // Insert new Manager forms or GFORMS
                        if (intOperationName.equalsIgnoreCase(ExtendedClientApplicationParser.INSERT_OP)) {
                            NodeList nodesToAdd = internalOperation.getExtendCode();
                            if (nodesToAdd != null) {
                                for (int j = 0; j < nodesToAdd.getLength(); j++) {
                                    Node node = nodesToAdd.item(j);
                                    if (node != null) {
                                        Node importedNode = originalForm.importNode(node, true);
                                        managerNode.appendChild(importedNode);
                                    }
                                }
                            }
                        }

                        // Delete form in Manager
                        if (intOperationName.equalsIgnoreCase(ExtendedClientApplicationParser.DELETE_OP)) {
                            String form = internalOperation.getFormAttr();
                            if (form != null) {
                                Node formNode = this.searchNodeByAttr(managerNode,
                                        ExtendedClientApplicationParser.FORM_ATRIBUTE, form);
                                if (formNode != null) {
                                    managerNode.removeChild(formNode);
                                }
                            }
                        }

                        // Modify form element in Manager
                        if (intOperationName.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_OP)) {

                            String form = internalOperation.getFormAttr();
                            if (form != null) {
                                Node formNode = this.searchNodeByAttr(managerNode,
                                        ExtendedClientApplicationParser.FORM_ATRIBUTE, form);
                                if (formNode != null) {
                                    List iOps = internalOperation.getInternalOperations();
                                    if (iOps.size() > 0) {
                                        for (int j = 0; j < iOps.size(); j++) {
                                            InnerExtendedClientApplicationOperation iOp = (InnerExtendedClientApplicationOperation) iOps
                                                .get(j);
                                            String iOpName = iOp.getOperation();

                                            // Only Modify for ATRIBUTE
                                            // allowed
                                            if (iOpName.equalsIgnoreCase(ExtendedClientApplicationParser.ATRIBUTE_OP)) {
                                                String nameAttr = iOp.getNameAttr();
                                                String valueAttr = iOp.getValueAttr();

                                                NamedNodeMap mapAtributes = formNode.getAttributes();
                                                if (mapAtributes.getLength() > 0) {
                                                    Node attrToModify = mapAtributes.getNamedItem(nameAttr);
                                                    if (attrToModify != null) {
                                                        attrToModify.setNodeValue(valueAttr);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    protected void doDeleteManagerOperation(ExtendedClientApplicationOperation operation, Node firstNode) {
        String managerId = operation.getIdAttr();
        if (managerId != null) {
            Node managerNode = this.searchNodeByAttr(firstNode, ExtendedClientApplicationParser.ID_ATRIBUTE, managerId);
            if (managerNode != null) {
                firstNode.removeChild(managerNode);
            }
        }
    }

    protected void doInsertManagerOperation(Document originalForm, ExtendedClientApplicationOperation operation,
            Node firstNode) {
        NodeList nodesToAdd = operation.getExtendCode();
        if (nodesToAdd != null) {
            for (int i = 0; i < nodesToAdd.getLength(); i++) {
                Node node = nodesToAdd.item(i);
                if (node != null) {
                    Node importedNode = originalForm.importNode(node, true);
                    firstNode.appendChild(importedNode);
                }
            }
        }
    }

    protected void doModifyOperation(ExtendedClientApplicationOperation operation, Node firstNode) {
        String parent = operation.getParentAttr();
        if (parent != null) {
            Node uniqueNode = this.searchNodeByElementName(firstNode, parent);
            if (uniqueNode != null) {

                List internalOperations = operation.getInternalOperations();
                if (!internalOperations.isEmpty()) {
                    for (int i = 0; i < internalOperations.size(); i++) {
                        InnerExtendedClientApplicationOperation internalOperation = (InnerExtendedClientApplicationOperation) internalOperations
                            .get(i);
                        String intOperationName = internalOperation.getOperation();

                        // Only works with ATRIBUTE modify operations
                        if (intOperationName.equalsIgnoreCase(ExtendedClientApplicationParser.ATRIBUTE_OP)) {
                            String nameAttr = internalOperation.getNameAttr();
                            String valueAttr = internalOperation.getValueAttr();

                            NamedNodeMap mapAtributes = uniqueNode.getAttributes();
                            if (mapAtributes.getLength() > 0) {
                                Node attrToModify = mapAtributes.getNamedItem(nameAttr);
                                if (attrToModify != null) {
                                    attrToModify.setNodeValue(valueAttr);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void doReplaceOperation(Document originalForm, ExtendedClientApplicationOperation operation,
            Node firstNode) {
        String parent = operation.getParentAttr();
        if (parent != null) {
            Node uniqueNode = this.searchNodeByElementName(firstNode, parent);
            if (uniqueNode != null) {
                Node parentNode = uniqueNode.getParentNode();
                if (parentNode != null) {
                    if (!parentNode.equals(originalForm)) {
                        // First inserts new nodes before reference node
                        NodeList nodesToAdd = operation.getExtendCode();
                        for (int i = 0; i < nodesToAdd.getLength(); i++) {
                            Node node = nodesToAdd.item(i);
                            if (node != null) {
                                Node importedNode = originalForm.importNode(node, true);
                                parentNode.insertBefore(importedNode, uniqueNode);
                            }
                        }

                        // In last removes the reference node
                        parentNode.removeChild(uniqueNode);
                    }
                }
            }
        }
    }

    /**
     * Thes method makes a deep search of a node the by attr atribute.
     * @param node The node in that the search is made
     * @param attrName name of attribute of node searched
     * @param attrValue value of attribute of node searched
     * @return the searched node or null if the node is not found
     */
    protected Node searchNodeByAttr(Node node, String attrName, String attrValue) {

        NamedNodeMap map = node.getAttributes();
        if (map != null) {
            Node attrNode = map.getNamedItem(attrName);
            if (attrNode != null) {
                if (attrNode.getNodeValue().equals(attrValue)) {
                    return node;
                }
            }
        }

        NodeList nodeList = node.getChildNodes();
        if (nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node childNode = nodeList.item(i);

                Node foundNode = this.searchNodeByAttr(childNode, attrName, attrValue);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }

        return null;
    }

    /**
     * Thes method makes a deep search of a node the by element Name.
     * @param node The node in that the search is made
     * @param name element name of node searched
     * @return the searched node or null if the node is not found
     */
    protected Node searchNodeByElementName(Node node, String name) {

        String nodeName = node.getNodeName();

        if (nodeName.equalsIgnoreCase(name)) {
            return node;
        } else {
            NodeList nodeList = node.getChildNodes();
            if (nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node childNode = nodeList.item(i);
                    Node foundNode = this.searchNodeByElementName(childNode, name);
                    if (foundNode != null) {
                        return foundNode;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Executes all operations in extendDocument to modify the xml code in originalDocument
     * @param originalForm - The xml of the original file
     * @param extendDocument - The xml containing the operations to apply in originalDocument
     * @return
     */
    protected Document parse(Document originalForm, Document extendDocument) {

        ArrayList operationNodes = this.extractOperationNodes(extendDocument);
        for (int i = 0; i < operationNodes.size(); i++) {
            Node operationNode = (Node) operationNodes.get(i);
            if (operationNode != null) {
                try {
                    ExtendedClientApplicationOperation operation = this.getNodeOperation(operationNode);
                    this.executeOperation(originalForm, operation);
                } catch (Exception e) {
                    ExtendedClientApplicationParser.logger.error(null, e);
                }
            }
        }

        return originalForm;
    }

    /**
     * This method iterate along the extend document extracting all nodes of operations
     * @param extendDocument
     * @return
     */
    protected ArrayList extractOperationNodes(Document extendDocument) {

        ArrayList extendNodes = new ArrayList();

        Node rootExtendsNode = extendDocument.getFirstChild();
        if (rootExtendsNode != null) {
            // Get childs of root extend node
            NodeList nodes = rootExtendsNode.getChildNodes();
            int lenght = nodes.getLength();
            for (int i = 0; i < lenght; i++) {
                Node node = nodes.item(i);
                if (this.isExtendNode(node)) {
                    // logger.debug(node.getNodeName());
                    extendNodes.add(node);
                }
            }
        }

        return extendNodes;
    }

    /**
     * This method processes a node that contains an operaton and returns an instance of class
     * XmlExtendsOperation with necessary information to apply this operation in original xml document
     * @param node The node of operation
     * @return An instance of class XmlExtendsOperation
     * @throws Exception
     */
    protected ExtendedClientApplicationOperation getNodeOperation(Node node) throws Exception {

        ExtendedClientApplicationOperation operation = null;

        NamedNodeMap mapAtributes = node.getAttributes();

        Node parentNode = mapAtributes.getNamedItem(ExtendedClientApplicationParser.PARENT_ATRIBUTE);
        Node idNode = mapAtributes.getNamedItem(ExtendedClientApplicationParser.ID_ATRIBUTE);

        String operationName = node.getNodeName();
        NodeList containedCode = node.getChildNodes();

        // If its an unique operation
        if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.REPLACE_OP)
                || operationName.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_OP)) {

            if (parentNode == null) {
                throw new Exception(
                        "Missing " + ExtendedClientApplicationParser.PARENT_ATRIBUTE + " in " + node.getNodeName());
            }

            if (containedCode.getLength() <= 0) {
                throw new Exception("Empty xml code in " + node.getNodeName());
            }

            // The unique operation is created
            operation = new ExtendedClientApplicationOperation(ExtendedClientApplicationOperation.UNIQUE_OP,
                    operationName, parentNode.getNodeValue());

            // If operation is REPLACE, the new code to replace is added in the
            // operation
            if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.REPLACE_OP)) {
                operation.setExtendCode(containedCode);
            }

            // If operation is MODIFY, the new code may be internal ops
            if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_OP)) {

                int lenght = containedCode.getLength();
                for (int i = 0; i < lenght; i++) {
                    Node internalNode = containedCode.item(i);

                    if (this.isInternalOperationNode(internalNode)) {
                        InnerExtendedClientApplicationOperation internalOp = this
                            .getInternalNodeOperation(internalNode);
                        if (internalOp != null) {
                            operation.addInternalOperation(internalOp);
                        }
                    }
                }
            }
        }

        // If its an manager operation
        if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.INSERT_MANAGER_OP) || operationName
            .equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_MANAGER_OP)
                || operationName.equalsIgnoreCase(ExtendedClientApplicationParser.DELETE_MANAGER_OP)) {

            // Insert a new manager
            if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.INSERT_MANAGER_OP)) {

                if (containedCode.getLength() <= 0) {
                    throw new Exception("Empty xml code in " + node.getNodeName());
                }

                operation = new ExtendedClientApplicationOperation(ExtendedClientApplicationOperation.MANAGER_OP,
                        operationName, null);
                operation.setExtendCode(containedCode);
            }

            if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_MANAGER_OP) || operationName
                .equalsIgnoreCase(ExtendedClientApplicationParser.DELETE_MANAGER_OP)) {

                if (idNode == null) {
                    throw new Exception(
                            "Missing " + ExtendedClientApplicationParser.ID_ATRIBUTE + " in " + node.getNodeName());
                }

                operation = new ExtendedClientApplicationOperation(ExtendedClientApplicationOperation.MANAGER_OP,
                        operationName, idNode.getNodeValue());

                if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_MANAGER_OP)) {

                    if (containedCode.getLength() <= 0) {
                        throw new Exception("Empty xml code in " + node.getNodeName());
                    }

                    int lenght = containedCode.getLength();
                    for (int i = 0; i < lenght; i++) {
                        Node internalNode = containedCode.item(i);

                        if (this.isInternalOperationNode(internalNode)) {
                            InnerExtendedClientApplicationOperation internalOp = this
                                .getInternalNodeOperation(internalNode);
                            if (internalOp != null) {
                                operation.addInternalOperation(internalOp);
                            }
                        }
                    }
                }
            }
        }

        return operation;
    }

    protected InnerExtendedClientApplicationOperation getInternalNodeOperation(Node node) throws Exception {

        InnerExtendedClientApplicationOperation operation = null;

        NamedNodeMap mapAtributes = node.getAttributes();

        Node nameNode = mapAtributes.getNamedItem(ExtendedClientApplicationParser.NAME_ATRIBUTE);
        Node valueNode = mapAtributes.getNamedItem(ExtendedClientApplicationParser.VALUE_ATRIBUTE);
        Node formNode = mapAtributes.getNamedItem(ExtendedClientApplicationParser.FORM_ATRIBUTE);

        String operationName = node.getNodeName();
        NodeList containedCode = node.getChildNodes();

        if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.ATRIBUTE_OP)) {

            if (nameNode == null) {
                throw new Exception(
                        "Missing " + ExtendedClientApplicationParser.NAME_ATRIBUTE + " in " + node.getNodeName());
            }

            if (valueNode == null) {
                throw new Exception(
                        "Missing " + ExtendedClientApplicationParser.VALUE_ATRIBUTE + " in " + node.getNodeName());
            }

            operation = new InnerExtendedClientApplicationOperation(operationName, nameNode.getNodeValue(),
                    valueNode.getNodeValue(), null);
        }

        if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.INSERT_OP)) {

            if (containedCode.getLength() <= 0) {
                throw new Exception("Empty xml code in " + node.getNodeName());
            }

            operation = new InnerExtendedClientApplicationOperation(operationName, null, null, null);
            operation.setExtendCode(containedCode);

        }

        if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.DELETE_OP)
                || operationName.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_OP)) {

            if (formNode == null) {
                throw new Exception(
                        "Missing " + ExtendedClientApplicationParser.FORM_ATRIBUTE + " in " + node.getNodeName());
            }

            operation = new InnerExtendedClientApplicationOperation(operationName, null, null, formNode.getNodeValue());

            if (operationName.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_OP)) {

                if (containedCode.getLength() <= 0) {
                    throw new Exception("Empty xml code in " + node.getNodeName());
                }

                int lenght = containedCode.getLength();
                for (int i = 0; i < lenght; i++) {
                    Node internalNode = containedCode.item(i);

                    if (this.isInternalOperationNode(internalNode)) {
                        InnerExtendedClientApplicationOperation internalOp = this
                            .getInternalNodeOperation(internalNode);
                        if (internalOp != null) {
                            operation.addInternalOperation(internalOp);
                        }
                    }
                }
            }
        }

        return operation;
    }

    /**
     * Returns true or false if the node parameter is a operation node
     * @param node the node to checl
     * @return true or falsse
     */
    protected boolean isExtendNode(Node node) {

        String nodeName = node.getNodeName();

        if (nodeName.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_MANAGER_OP)
                || nodeName.equalsIgnoreCase(ExtendedClientApplicationParser.DELETE_MANAGER_OP) || nodeName
                    .equalsIgnoreCase(ExtendedClientApplicationParser.INSERT_MANAGER_OP)
                || nodeName
                    .equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_OP)
                || nodeName.equalsIgnoreCase(ExtendedClientApplicationParser.REPLACE_OP)) {
            return true;
        }
        return false;
    }

    protected boolean isInternalOperationNode(Node node) {

        String nodeName = node.getNodeName();

        if (nodeName.equalsIgnoreCase(ExtendedClientApplicationParser.MODIFY_OP)
                || nodeName.equalsIgnoreCase(ExtendedClientApplicationParser.DELETE_OP) || nodeName
                    .equalsIgnoreCase(ExtendedClientApplicationParser.INSERT_OP)
                || nodeName.equalsIgnoreCase(ExtendedClientApplicationParser.ATRIBUTE_OP)) {
            return true;
        }
        return false;
    }

    public Document getDocumentModel(File f) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Primero el archivo de etiquetas
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(f);

            Node n;

            return document;
        } catch (Exception e) {
            ExtendedClientApplicationParser.logger.error(
                    this.getClass().toString() + ": " + e.getClass().toString() + " in " + this.getClass().toString()
                            + ": " + e.getMessage(),
                    e);
            return null;
        }
    }

    public String printDocument(Document document) {

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            Writer writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            ExtendedClientApplicationParser.logger.error(null, e);
        }

        return null;
    }

}
