package com.ontimize.jee.common.util.extend;

import com.ontimize.jee.common.xml.XMLUtil;
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
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public abstract class ExtendedXmlParser {

    private static final Logger logger = LoggerFactory.getLogger(ExtendedXmlParser.class);

    public static boolean DEBUG = false;

    abstract protected void executeOperation(Document originalForm, ExtendedXmlOperation operation) throws Exception;

    public static final String INSERT_AFTER_OP = "InsertAfter";

    public static final String INSERT_BEFORE_OP = "InsertBefore";

    public static final String REMOVE_OP = "Remove";

    public static final String REPLACE_OP = "Replace";

    public static final String ATTR_ATRIBUTE = "attr";

    public static final String CONTAINER_ATRIBUTE = "container";

    public static final String LEVELS_ATRIBUTE = "levels";

    public static final String FORM_EXTENDS = "FormExtends";

    public static final String MENU_EXTENDS = "MenuExtends";

    public static final String TOOLBAR_EXTENDS = "ToolBarExtends";

    public static final String MODIFY_FORM = "ModifyForm";

    public static final String ORDER = "order";

    public static final String MODIFY_ATTRIBUTE_NAME = "name";

    public static final String MODIFY_ATTRIBUTE_VALUE = "value";

    public static final String MODIFY_ATTRIBUTE_SEPARATOR = "separator";

    public static final String MODIFY_ATTRIBUTE_OPERATION = "operation";

    public static final String MODIFY_ATTRIBUTE = "ModifyAttribute";

    public static final String MODIFY_ATTRIBUTE_OPERATOR_ADD = "add";

    public static final String MODIFY_ATTRIBUTE_OPERATOR_REMOVE = "remove";

    public static final String ADD_REMOTE_REFERENCE = "AddRemoteReference";

    public static final String MODIFY_REMOTE_REFERENCE = "ModifyRemoteReference";

    public static final String REMOVE_REMOTE_REFERENCE = "RemoveRemoteReference";

    public static final String MODIFY_REFERENCE_MODIFY_NAME = "ModifyName";

    public static final String MODIFY_REFERENCE_MODIFY_CLASS = "ModifyClass";

    public static final String MODIFY_REFERENCE_MODIFY_INIT = "ModifyInit";

    public static final String MODIFY_REFERENCE_PARAMS = "ReferenceParams";

    public static final String MODIFY_REFERENCE_PARAMS_VAL = "Param";

    public static final String MODIFY_REFERENCE_NAME = "Name";

    public static final String MODIFY_REFERENCE_VALUE = "Value";

    public static final String MODIFY_REFERENCE_CLASS = "Class";

    public static final String MODIFY_REFERENCE_INIT = "init";

    public Document parseExtendedXml(Document originalForm, Document extendDocument) throws Exception {
        return this.parse(originalForm, extendDocument);
    }

    /**
     * Executes all operations in extendDocument to modify the xml code in originalDocument
     * @param originalForm - The xml of the original file
     * @param extendDocument - The xml containing the operations to apply in originalDocument
     * @return
     */
    protected Document parse(Document originalForm, Document extendDocument) throws Exception {

        ArrayList operationNodes = this.extractOperationNodes(extendDocument);
        for (int i = 0; i < operationNodes.size(); i++) {
            Node operationNode = (Node) operationNodes.get(i);
            if (operationNode != null) {
                try {
                    ExtendedXmlOperation operation = this.getNodeOperation(operationNode);
                    this.executeOperation(originalForm, operation);
                } catch (Exception e) {
                    ExtendedXmlParser.logger.error(null, e);
                    throw e;
                }
            }
        }
        return originalForm;
    }

    /**
     * This method processes a node that contains an operation and returns an instance of class
     * XmlExtendsOperation with necessary information to apply this operation in original xml document
     * @param node The node of operation
     * @return An instance of class XmlExtendsOperation
     * @throws Exception
     */
    protected ExtendedXmlOperation getNodeOperation(Node node) throws Exception {

        NamedNodeMap mapAtributes = node.getAttributes();
        Node attrNode = mapAtributes.getNamedItem(ExtendedXmlParser.ATTR_ATRIBUTE);
        Node containerNode = mapAtributes.getNamedItem(ExtendedXmlParser.CONTAINER_ATRIBUTE);
        Node levelsNode = mapAtributes.getNamedItem(ExtendedXmlParser.LEVELS_ATRIBUTE);
        String containerValue = "";
        int levelsValue = 0;
        String attrValue = "";

        if ((attrNode == null) && !this.checkNodeWithoutAttributes(node)) {
            throw new Exception("Missing " + ExtendedXmlParser.ATTR_ATRIBUTE + " in " + node.getNodeName());
        }

        if ((levelsNode != null) && !levelsNode.getNodeValue().equals("0")) {
            if (containerNode == null) {
                throw new Exception("Missing " + ExtendedXmlParser.CONTAINER_ATRIBUTE + " in " + node.getNodeName());
            }
            containerValue = containerNode.getNodeValue();
            levelsValue = Integer.parseInt(levelsNode.getNodeValue());
        }

        if (!this.checkNodeWithoutAttributes(node)) {
            attrValue = attrNode.getNodeValue();
        }

        NodeList containedCode = node.getChildNodes();
        if (containedCode.getLength() < 0) {
            throw new Exception("Empty xml code in " + node.getNodeName());
        }

        ExtendedXmlOperation operation = new ExtendedXmlOperation(node.getNodeName(), attrValue, containerValue,
                levelsValue, containedCode);

        return operation;
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
                if (this.isExtendedNode(node)) {
                    // logger.debug(node.getNodeName());
                    extendNodes.add(node);
                }
            }
        }

        return extendNodes;
    }

    /**
     * Returns true or false if the node doesn't need attributes
     * @param node the node to check
     * @return true or false
     */

    protected boolean checkNodeWithoutAttributes(Node node) {
        String nodeName = node.getNodeName();

        if (nodeName.equalsIgnoreCase(ExtendedXmlParser.MODIFY_FORM)
                || nodeName.equalsIgnoreCase(ExtendedXmlParser.ADD_REMOTE_REFERENCE)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true or false if the node parameter is a operation node
     * @param node the node to check
     * @return true or false
     */
    protected boolean isExtendedNode(Node node) {

        String nodeName = node.getNodeName();

        if (nodeName.equalsIgnoreCase(ExtendedXmlParser.INSERT_AFTER_OP)
                || nodeName.equalsIgnoreCase(ExtendedXmlParser.INSERT_BEFORE_OP)
                || nodeName.equalsIgnoreCase(ExtendedXmlParser.REMOVE_OP)
                || nodeName.equalsIgnoreCase(ExtendedXmlParser.REPLACE_OP)
                || nodeName.equalsIgnoreCase(ExtendedXmlParser.MODIFY_FORM)
                || nodeName.equalsIgnoreCase(ExtendedXmlParser.MODIFY_ATTRIBUTE)
                || nodeName.equalsIgnoreCase(ExtendedXmlParser.ADD_REMOTE_REFERENCE)
                || nodeName.equalsIgnoreCase(ExtendedXmlParser.REMOVE_REMOTE_REFERENCE)
                || nodeName.equalsIgnoreCase(ExtendedXmlParser.MODIFY_REMOTE_REFERENCE)) {
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
            return document;
        } catch (Exception e) {
            ExtendedXmlParser.logger.error(this.getClass().toString() + ": " + e.getClass().toString() + " in "
                    + this.getClass().toString() + ": " + e.getMessage(), e);
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
            ExtendedXmlParser.logger.error(null, e);
        }

        return null;
    }

    public static String getExtendedFile(String fileURI) {
        int index = fileURI.lastIndexOf(".");
        if (index >= 0) {
            String uri = fileURI.substring(0, index) + "_extends" + fileURI.substring(index);
            if (ExtendedXmlParser.DEBUG) {
                ExtendedXmlParser.logger.debug("Extended file: " + uri);
            }
            return uri;
        }
        return null;
    }

    public static InputStream getExtendedInput(String fileURI) {
        int pointIndex = fileURI.lastIndexOf(".");
        if (pointIndex > 0) {
            String resource = fileURI.substring(0, pointIndex) + "_extends" + fileURI.substring(pointIndex);
            if (ExtendedXmlParser.DEBUG) {
                ExtendedXmlParser.logger.debug("Extended file: " + resource);
            }
            try {
                return ExtendedXmlParser.class.getClassLoader().getResourceAsStream(resource);
            } catch (Exception e) {
                if (ExtendedXmlParser.DEBUG) {
                    ExtendedXmlParser.logger.error(null, e);
                }
            }
        }
        return null;
    }

    public static Enumeration<URL> getExtendedFile(String fileURI, String baseCP) {
        String nameFile = null;
        if (baseCP != null) {
            int separatorIndex = fileURI.indexOf(baseCP);
            if (separatorIndex >= 0) {
                nameFile = fileURI.substring(separatorIndex + baseCP.length());
            } else {
                int lastSlash = fileURI.lastIndexOf("/");
                nameFile = baseCP + fileURI.substring(lastSlash + 1);
            }
        } else {
            baseCP = "";
            nameFile = fileURI;
        }

        int pointIndex = nameFile.lastIndexOf(".");
        if (pointIndex > 0) {
            String resource = baseCP + nameFile.substring(0, pointIndex) + "_extends" + nameFile.substring(pointIndex);
            ExtendedXmlParser.logger.debug("Extended file: " + resource);
            try {
                return ExtendedXmlParser.class.getClassLoader().getResources(resource);
            } catch (Exception e) {
                ExtendedXmlParser.logger.debug("ExtendedXmlParser error {}", e);
            }
        }
        return null;
    }

    public Document getExtendedDocument(Document doc, Enumeration<URL> input) {
        if (!input.hasMoreElements()) {
            return doc;
        }

        List<OrderDocument> extendedDocumentList = new ArrayList<OrderDocument>();
        Document extendedDocument = null;

        while (input.hasMoreElements()) {
            try {
                extendedDocument = XMLUtil.getExtendedDocument(input.nextElement().openStream());
                Node s = extendedDocument.getChildNodes().item(0).getAttributes().getNamedItem(ExtendedXmlParser.ORDER);
                int index = s != null ? Integer.parseInt(s.getNodeValue()) : -1;
                extendedDocumentList.add(new OrderDocument(index, extendedDocument));
            } catch (Exception e) {
                ExtendedFormXmlParser.logger.error("{}", e);
            }
        }

        Collections.sort(extendedDocumentList);

        for (OrderDocument oDocument : extendedDocumentList) {
            try {
                doc = this.parseExtendedXml(doc, oDocument.getDocument());
                ExtendedFormXmlParser.logger.debug("{} Form extend, Load order -> {}",
                        ((Node) oDocument.getDocument()).getFirstChild().getNodeName(), oDocument.getIndex());
            } catch (Exception e) {
                ExtendedFormXmlParser.logger.error("Extending form", e);
            }
        }

        return doc;
    }

    public Document getExtendedDocumentForm(Document doc, String fileURI, String baseClasspath) {

        Enumeration<URL> input = ExtendedXmlParser.getExtendedFile(fileURI, baseClasspath);
        return this.getExtendedDocument(doc, input);
    }

}
