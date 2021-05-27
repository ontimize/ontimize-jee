package com.ontimize.jee.common.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class XMLUtil {

    private static final Logger logger = LoggerFactory.getLogger(XMLUtil.class);

    protected static DocumentBuilderFactory factory = null;

    protected static DocumentBuilder builder = null;

    public static Document getDOMDocument(String value) throws Exception {
        if (value == null) {
            return null;
        }

        if (XMLUtil.factory == null) {
            XMLUtil.factory = DocumentBuilderFactory.newInstance();
        }

        if (XMLUtil.builder == null) {
            try {
                XMLUtil.builder = XMLUtil.factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                XMLUtil.logger.error(null, e);
            }
        }
        ByteArrayInputStream input = new ByteArrayInputStream(value.getBytes());
        Document document = null;
        try {
            document = XMLUtil.builder.parse(input);
        } catch (Exception ex) {
            XMLUtil.builder = null;
            throw ex;
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return document;
    }

    public static Document getXMLDocument() {
        if (XMLUtil.factory == null) {
            XMLUtil.factory = DocumentBuilderFactory.newInstance();
        }

        if (XMLUtil.builder == null) {
            try {
                XMLUtil.builder = XMLUtil.factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                XMLUtil.logger.error(null, e);
            }
        }

        try {
            Document document = XMLUtil.builder.newDocument();
            return document;
        } catch (Exception e) {
            XMLUtil.logger.error(null, e);
            return null;
        }
    }

    public static Document getDocumentModel(String fileURI) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // First of all the labels file
        long initialTime = System.currentTimeMillis();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(fileURI);
            long finalTime = System.currentTimeMillis();
            double passTime = (finalTime - initialTime) / 1000.0;
            XMLUtil.logger.trace("Time parsing xml {} seconds", new Double(passTime).toString());
            return document;
        } catch (Exception e) {
            XMLUtil.logger.error("{}", e.getMessage(), e);
            return null;
        }
    }

    public static Document getExtendedDocument(InputStream input) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);
            return document;
        } catch (Exception e) {
            XMLUtil.logger.error("{}", e.getMessage(), e);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    XMLUtil.logger.error(null, e);
                }
            }
        }
    }

    public static String dom2String(Document doc, String encoding) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer xformer = factory.newTransformer();

            xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            xformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            xformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            xformer.setOutputProperty(OutputKeys.METHOD, "xml");

            Source source = new DOMSource(doc);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Result result = new StreamResult(stream);
            xformer.transform(source, result);
            return new String(stream.toByteArray());
        } catch (Exception e) {
            XMLUtil.logger.error(null, e);
        }
        return "";
    }

    public static String dom2String(Document doc) {
        return XMLUtil.dom2String(doc, "UTF-8");
    }

    public static String dom2String(String sFileURI) {
        Document doc = XMLUtil.getDocumentModel(sFileURI);
        return XMLUtil.dom2String(doc);
    }

    public static File dom2File(Document doc, String filename) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            // Prepare the output file
            File file = new File(filename);
            Result result = new StreamResult(file);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            xformer.setOutputProperty(OutputKeys.METHOD, "xml");
            xformer.transform(source, result);

            return file;
        } catch (TransformerConfigurationException e) {
            XMLUtil.logger.error(null, e);
        } catch (TransformerException e) {
            XMLUtil.logger.error(null, e);
        }
        return null;
    }

    public static String getTextValue(Node son) {
        NodeList nodeList = son.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (n instanceof CharacterData) {
                // !CDATA
                CharacterData cd = (CharacterData) n;
                return cd.getData();
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                return n.getNodeValue();
            }
        }
        return "";
    }

    public static Map parseAttributes(NamedNodeMap nodeAttributes) {
        Map attributes = new HashMap();
        for (int i = 0; i < nodeAttributes.getLength(); i++) {
            attributes.put(nodeAttributes.item(i).getNodeName(), nodeAttributes.item(i).getNodeValue());
        }
        return attributes;
    }

}
