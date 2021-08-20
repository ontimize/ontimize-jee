package com.ontimize.jee.common.security;

import com.ontimize.jee.common.builder.CustomNode;
import com.ontimize.jee.common.util.calendar.TimePeriod;
import com.ontimize.jee.common.util.calendar.TimePeriodOperationParser;
import com.ontimize.jee.common.util.calendar.TimePeriodParser;
import com.ontimize.jee.common.util.calendar.TimePeriodParserManager;
import com.ontimize.jee.common.xml.DocumentTreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Class that implements all logic in xml client permissions. All client parts are defined: menu,
 * form, form manager, tree,...
 * <p>
 * Most common tags for XML permission definitions are:<br>
 * <br>
 * <TABLE BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX>
 * <tr>
 * <td>Component</td>
 * <td>Permission Tag</td>
 * </tr>
 * <tr>
 * <td>Menu</td>
 * <td><i>MENU</td>
 * </tr>
 * <tr>
 * <td>Form</td>
 * <td><i>FORM</td>
 * </tr>
 * <tr>
 * <td>FormManager</td>
 * <td><i>FM</td>
 * </tr>
 * <tr>
 * <td>Tree</td>
 * <td><i>TREE</td>
 * </tr>
 * <tr>
 * <td>Application</td>
 * <td><i>APPLICATION</td>
 * </tr>
 * </TABLE>
 *
 * @author Imatia Innovation
 */
public abstract class XMLClientUtilities {

    private static final Logger logger = LoggerFactory.getLogger(XMLClientUtilities.class);

    public static boolean ignoreNullOnClientPermissionCombination = false;

    public static String MENU_ID = "MENU";

    public static String FORM_ID = "FORM";

    public static String FM_ID = "FM";

    public static String TREE_ID = "TREE";

    public static String APPLICATION_ID = "APPLICATION";

    public static boolean DEBUG = false;

    public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>";

    public static final String BASE_DOCUMENT = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><Security></Security>";

    public static Map buildClientPermissions(StringBuffer xmlPermissionDescription) throws Exception {
        return XMLClientUtilities.buildClientPermissions(xmlPermissionDescription, null, null);
    }

    public static Map buildClientPermissions(StringBuffer xmlPermissionDescription, Locale l,
            String businessCalendarFile) throws Exception {
        Map permissions = new HashMap();
        StringBufferInputStream in = null;
        try {
            // Builds model from xml tree definition.
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = fact.newDocumentBuilder();
            in = new StringBufferInputStream(xmlPermissionDescription.toString());
            Document doc = db.parse(in);
            // Well. Now, we have to build permission info from xml definition.
            DocumentTreeModel m = new DocumentTreeModel(doc.getDocumentElement());
            CustomNode cnRoot = (CustomNode) m.getRoot();
            // Tree is looked around to create the model.
            if (!cnRoot.getNodeInfo().equalsIgnoreCase("Security")) {
                throw new Exception("XMLClientUtilities. Root Node must be <Security>");
            }
            XMLClientUtilities.buildPermissionInfo(cnRoot, permissions, l, businessCalendarFile);
            in.close();
            XMLClientUtilities.logger.debug("Permissions Loaded Succesfully: {}", permissions.toString());
            return permissions;
        } catch (Exception e) {
            XMLClientUtilities.logger.error(xmlPermissionDescription.toString(), e);
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                XMLClientUtilities.logger.trace(null, e);
            }
        }
    }

    private static void buildPermissionInfo(CustomNode root, Map permissions, Locale l,
            String businessCalendarFile) throws Exception {
        // Builds model from xml tree definition.
        for (int i = 0; i < root.getChildrenNumber(); i++) {
            CustomNode cnNode = root.child(i);
            if (cnNode.isTag()) {
                if (cnNode.getNodeInfo().equalsIgnoreCase(XMLClientUtilities.MENU_ID)) {
                    XMLClientUtilities.buildMenuPermissionInfo(cnNode, permissions, l, businessCalendarFile);
                } else if (cnNode.getNodeInfo().equalsIgnoreCase(XMLClientUtilities.FORM_ID)) {
                    XMLClientUtilities.buildFormPermissionInfo(cnNode, permissions, l, businessCalendarFile);
                } else if (cnNode.getNodeInfo().equalsIgnoreCase(XMLClientUtilities.FM_ID)) {
                    XMLClientUtilities.buildFMPermissionInfo(cnNode, permissions, l, businessCalendarFile);
                } else if (cnNode.getNodeInfo().equalsIgnoreCase(XMLClientUtilities.TREE_ID)) {
                    XMLClientUtilities.buildTreePermissionInfo(cnNode, permissions, l, businessCalendarFile);
                } else if (cnNode.getNodeInfo().equalsIgnoreCase(XMLClientUtilities.APPLICATION_ID)) {
                    XMLClientUtilities.buildApplicationPermissionInfo(cnNode, permissions, l, businessCalendarFile);
                }
            }
        }
    }

    private static void buildApplicationPermissionInfo(CustomNode node, Map permissions, Locale l,
            String businessCalendarFile) throws Exception {
        // Builds model for application
        if (node.isTag()) {
            if (node.getNodeInfo().equalsIgnoreCase(XMLClientUtilities.APPLICATION_ID)) {
                List vApplicationPermissions = (List) permissions.get(XMLClientUtilities.APPLICATION_ID);
                if (vApplicationPermissions == null) {
                    vApplicationPermissions = new ArrayList();
                }
                for (int i = 0; i < node.getChildrenNumber(); i++) {
                    CustomNode n = node.child(i);
                    if (n.isTag()) {
                        boolean bRestricted = false;
                        String info = n.getNodeInfo();
                        Map hPermissionsE1 = n.hashtableAttribute();
                        Object oRestricted = hPermissionsE1.get("restricted");
                        if (oRestricted == null) {
                            bRestricted = false;
                        } else {
                            if (oRestricted.toString().equalsIgnoreCase("yes")) {
                                bRestricted = true;
                            } else {
                                bRestricted = false;
                            }
                        }
                        Object period = hPermissionsE1.get("period");
                        TimePeriod tPeriod = null;
                        try {
                            if (period != null) {
                                tPeriod = TimePeriodParserManager.getTimePeriodParser()
                                    .parse((String) period, l, businessCalendarFile);
                            }
                        } catch (Exception e) {
                            XMLClientUtilities.logger.info(null, e);
                        }
                        // Now we create the permission
                        ApplicationPermission appPermission = new ApplicationPermission(info, bRestricted);
                        if (!vApplicationPermissions.contains(appPermission)) {
                            vApplicationPermissions.add(appPermission);
                        } else {
                            XMLClientUtilities.logger.info(
                                    "{} permission not added.The permission is already in the permission list",
                                    appPermission);
                        }
                    }
                }

                if (!permissions.containsKey(XMLClientUtilities.APPLICATION_ID)) {
                    permissions.put(XMLClientUtilities.APPLICATION_ID, vApplicationPermissions);
                }
            } else {
                throw new Exception("XMLClientUtilities: This is not an APPLICATION node");
            }
        }

    }

    private static void buildMenuPermissionInfo(CustomNode node, Map permissions, Locale l,
            String businessCalendarFile) throws Exception {
        // Builds model for menu.
        if (node.isTag()) {
            if (node.getNodeInfo().equalsIgnoreCase(XMLClientUtilities.MENU_ID)) {
                List vMenuPermissions = (List) permissions.get(XMLClientUtilities.MENU_ID);
                if (vMenuPermissions == null) {
                    vMenuPermissions = new ArrayList();
                }
                for (int i = 0; i < node.getChildrenNumber(); i++) {
                    CustomNode n = node.child(i);
                    if (n.isTag()) {
                        if (n.getNodeInfo().equalsIgnoreCase("ELEMENT")) {
                            // Takes permissions for each element
                            Map hDataElement = n.hashtableAttribute();
                            // Gets attribute
                            Object attr = hDataElement.get("attr");
                            if (attr == null) {
                                continue;
                            }
                            // Builds a permission for each of children.
                            for (int j = 0; j < n.getChildrenNumber(); j++) {
                                CustomNode nH = n.child(j);
                                if (nH.isTag()) {
                                    // Tag is the permission and 'restricted'
                                    // the
                                    // condition.
                                    String sPermissionName = nH.getNodeInfo();
                                    boolean bRestricted = false;
                                    Map hPermissionsE1 = nH.hashtableAttribute();
                                    Object restricted = hPermissionsE1.get("restricted");
                                    if (restricted == null) {
                                        bRestricted = false;
                                    } else {
                                        if (restricted.toString().equalsIgnoreCase("yes")) {
                                            bRestricted = true;
                                        } else {
                                            bRestricted = false;
                                        }
                                    }
                                    Object period = hPermissionsE1.get("period");
                                    TimePeriod tPeriod = null;
                                    try {
                                        if (period != null) {
                                            tPeriod = TimePeriodParserManager
                                                .getTimePeriodParser()
                                                .parse((String) period, l, businessCalendarFile);
                                        }
                                    } catch (Exception e) {
                                        XMLClientUtilities.logger.info(sPermissionName, e);
                                    }
                                    // Now we create the permission
                                    MenuPermission menuPermission = new MenuPermission(sPermissionName, attr.toString(),
                                            bRestricted, tPeriod);
                                    if (!vMenuPermissions.contains(menuPermission)) {
                                        vMenuPermissions.add(menuPermission);
                                    } else {
                                        XMLClientUtilities.logger.info(
                                                "{} permission not added. The permission is already in the permissions list",
                                                menuPermission);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!permissions.containsKey(XMLClientUtilities.MENU_ID)) {
                    permissions.put(XMLClientUtilities.MENU_ID, vMenuPermissions);
                }
            } else {
                throw new Exception("This is not a MENU node");
            }
        } else {
            throw new Exception("This is not a MENU node");
        }
    }

    private static void buildFormPermissionInfo(CustomNode node, Map permissions, Locale l,
            String businessCalendarFile) throws Exception {
        // Builds model for menu
        if (node.isTag()) {
            if (node.getNodeInfo().equalsIgnoreCase(XMLClientUtilities.FORM_ID)) {
                Map p = node.hashtableAttribute();
                Object oFormArchiveName = p.get("archive");
                if (oFormArchiveName == null) {
                    XMLClientUtilities.logger.warn("Needded 'archive' in Tag FORM");
                    return;
                }
                Map hFormPermissions = null;
                if (permissions.containsKey(XMLClientUtilities.FORM_ID)) {
                    hFormPermissions = (Map) permissions.get(XMLClientUtilities.FORM_ID);
                } else {
                    hFormPermissions = new HashMap();
                }
                List vFormPermissions = (List) hFormPermissions.get(oFormArchiveName);
                if (vFormPermissions == null) {
                    vFormPermissions = new ArrayList();
                }
                for (int i = 0; i < node.getChildrenNumber(); i++) {
                    CustomNode n = node.child(i);
                    if (n.isTag()) {
                        if (n.getNodeInfo().equalsIgnoreCase("ELEMENT")) {
                            // Takes permissions for each element
                            Map hDataElement = n.hashtableAttribute();
                            // Gets attribute
                            Object attr = hDataElement.get("attr");
                            if (attr == null) {
                                continue;
                            }
                            // Builds a permission for each of children.
                            for (int j = 0; j < n.getChildrenNumber(); j++) {
                                CustomNode nH = n.child(j);
                                if (nH.isTag()) {
                                    // Tag is the permission and 'restricted'
                                    // the
                                    // condition.
                                    String sPermissionName = nH.getNodeInfo();
                                    boolean bRestricted = false;
                                    Map hPermissionsE1 = nH.hashtableAttribute();
                                    Object restricted = hPermissionsE1.get("restricted");
                                    if (restricted == null) {
                                        bRestricted = false;
                                    } else {
                                        if (restricted.toString().equalsIgnoreCase("yes")) {
                                            bRestricted = true;
                                        } else {
                                            bRestricted = false;
                                        }
                                    }

                                    Object type = hPermissionsE1.get("type");
                                    Object columnName = hPermissionsE1.get("attr");

                                    Object period = hPermissionsE1.get("period");
                                    TimePeriod tPeriod = null;
                                    try {
                                        if (period != null) {
                                            tPeriod = TimePeriodParserManager
                                                .getTimePeriodParser()
                                                .parse((String) period, l, businessCalendarFile);
                                        }
                                    } catch (Exception e) {
                                        XMLClientUtilities.logger.info(sPermissionName, e);
                                    }
                                    // Now we create permission
                                    FormPermission formPermission = null;
                                    if (columnName != null) {
                                        formPermission = new TableFormPermission(oFormArchiveName.toString(),
                                                sPermissionName, attr.toString(), bRestricted, tPeriod, (String) type,
                                                (String) columnName);
                                    } else if (type != null) {
                                        formPermission = new TableFormPermission(oFormArchiveName.toString(),
                                                sPermissionName, attr.toString(), bRestricted, tPeriod,
                                                (String) type);
                                    } else {
                                        formPermission = new FormPermission(oFormArchiveName.toString(),
                                                sPermissionName, attr.toString(), bRestricted, tPeriod);
                                    }

                                    if (!vFormPermissions.contains(formPermission)) {
                                        vFormPermissions.add(formPermission);
                                    } else {
                                        XMLClientUtilities.logger.info(
                                                "{} permission not added. The permission is already in the permissions list",
                                                formPermission);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!hFormPermissions.containsKey(oFormArchiveName)) {
                    hFormPermissions.put(oFormArchiveName, vFormPermissions);
                }
                if (!permissions.containsKey(XMLClientUtilities.FORM_ID)) {
                    permissions.put(XMLClientUtilities.FORM_ID, hFormPermissions);
                }
            } else {
                throw new Exception("This is not a FORM node");
            }
        } else {
            throw new Exception("This is not a FORM node");
        }
    }

    private static void buildFMPermissionInfo(CustomNode node, Map permissions, Locale l,
            String businessCalendarFile) throws Exception {
        // Here, ELEMENT tags specify the name of form
        if (node.isTag()) {
            if (node.getNodeInfo().equalsIgnoreCase(XMLClientUtilities.FM_ID)) {
                Map p = node.hashtableAttribute();
                Object oFMName = p.get("id");
                if (oFMName == null) {
                    XMLClientUtilities.logger.warn("Required 'id' in {} tag", XMLClientUtilities.FM_ID);
                    return;
                }
                Map hHashPermissions = null;
                if (permissions.containsKey(XMLClientUtilities.FM_ID)) {
                    hHashPermissions = (Map) permissions.get(XMLClientUtilities.FM_ID);
                } else {
                    hHashPermissions = new HashMap();
                }
                List vFMPermissions = (List) hHashPermissions.get(oFMName);
                if (vFMPermissions == null) {
                    vFMPermissions = new ArrayList();
                }
                for (int i = 0; i < node.getChildrenNumber(); i++) {
                    CustomNode n = node.child(i);
                    if (n.isTag()) {
                        if (n.getNodeInfo().equalsIgnoreCase("ELEMENT")) {
                            // Takes permissions by element
                            Map datosElemento = n.hashtableAttribute();
                            // Gets attribute
                            Object attr = datosElemento.get("attr");
                            if (attr == null) {
                                continue;
                            }
                            // Builds permissions for each children
                            for (int j = 0; j < n.getChildrenNumber(); j++) {
                                CustomNode nH = n.child(j);
                                if (nH.isTag()) {
                                    // Tag is the permission and 'restricted'
                                    // the
                                    // condition.
                                    String sPermissionName = nH.getNodeInfo();
                                    boolean bRestricted = false;
                                    Map hPermissionsE1 = nH.hashtableAttribute();
                                    Object restricted = hPermissionsE1.get("restricted");
                                    if (restricted == null) {
                                        bRestricted = false;
                                    } else {
                                        if (restricted.toString().equalsIgnoreCase("yes")) {
                                            bRestricted = true;
                                        } else {
                                            bRestricted = false;
                                        }
                                    }
                                    Object period = hPermissionsE1.get("period");
                                    TimePeriod tPeriod = null;
                                    try {
                                        if (period != null) {
                                            tPeriod = TimePeriodParserManager
                                                .getTimePeriodParser()
                                                .parse((String) period, l, businessCalendarFile);
                                        }
                                    } catch (Exception e) {
                                        XMLClientUtilities.logger.info(sPermissionName, e);
                                    }
                                    // Now we create the permission
                                    FMPermission fmPermission = new FMPermission(oFMName.toString(), sPermissionName,
                                            attr.toString(), bRestricted, tPeriod);
                                    if (!vFMPermissions.contains(fmPermission)) {
                                        vFMPermissions.add(fmPermission);
                                    } else {
                                        XMLClientUtilities.logger.info(
                                                "{} permission not added. The permission is already in the permissions list",
                                                fmPermission);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!hHashPermissions.containsKey(oFMName)) {
                    hHashPermissions.put(oFMName, vFMPermissions);
                }
                if (!permissions.containsKey(XMLClientUtilities.FM_ID)) {
                    permissions.put(XMLClientUtilities.FM_ID, hHashPermissions);
                }
            } else {
                throw new Exception("This is not a FormManager node");
            }
        } else {
            throw new Exception("This is not a FormManager node");
        }
    }

    private static void buildTreePermissionInfo(CustomNode node, Map permissions, Locale l,
            String businessCalendarFile) throws Exception {
        if (node.isTag()) {
            if (node.getNodeInfo().equalsIgnoreCase(XMLClientUtilities.TREE_ID)) {
                Map p = node.hashtableAttribute();
                Object oTreeFileName = p.get("archive");
                if (oTreeFileName == null) {
                    XMLClientUtilities.logger.warn("Required 'archive' in TREE tag");
                    return;
                }
                Map hHashTreePermissions = null;
                if (permissions.containsKey(XMLClientUtilities.TREE_ID)) {
                    hHashTreePermissions = (Map) permissions.get(XMLClientUtilities.TREE_ID);
                } else {
                    hHashTreePermissions = new HashMap();
                }
                List vTreePermissions = (List) hHashTreePermissions.get(oTreeFileName);
                if (vTreePermissions == null) {
                    vTreePermissions = new ArrayList();
                }
                for (int i = 0; i < node.getChildrenNumber(); i++) {
                    CustomNode n = node.child(i);
                    if (n.isTag()) {
                        if (n.getNodeInfo().equalsIgnoreCase("ELEMENT")) {
                            // Takes permissions by name
                            Map hDataElement = n.hashtableAttribute();
                            // Gets attribute
                            Object attr = hDataElement.get("id");
                            if (attr == null) {
                                continue;
                            }
                            // For each child we build a permission
                            for (int j = 0; j < n.getChildrenNumber(); j++) {
                                CustomNode nH = n.child(j);
                                if (nH.isTag()) {
                                    // Tag is the permission and 'restricted'
                                    // the
                                    // condition.
                                    String sPermissionName = nH.getNodeInfo();
                                    boolean bRestricted = false;
                                    Map hPermissionsE1 = nH.hashtableAttribute();
                                    Object restricted = hPermissionsE1.get("restricted");
                                    if (restricted == null) {
                                        bRestricted = false;
                                    } else {
                                        if (restricted.toString().equalsIgnoreCase("yes")) {
                                            bRestricted = true;
                                        } else {
                                            bRestricted = false;
                                        }
                                    }
                                    Object period = hPermissionsE1.get("period");
                                    TimePeriod tPeriod = null;
                                    try {
                                        if (period != null) {
                                            tPeriod = TimePeriodParserManager
                                                .getTimePeriodParser()
                                                .parse((String) period, l, businessCalendarFile);
                                        }
                                    } catch (Exception e) {
                                        XMLClientUtilities.logger.info(sPermissionName, e);
                                    }
                                    // Now we create the permission
                                    TreePermission treePermission = new TreePermission(oTreeFileName.toString(),
                                            sPermissionName, attr.toString(), bRestricted, tPeriod);
                                    if (!vTreePermissions.contains(treePermission)) {
                                        vTreePermissions.add(treePermission);
                                    } else {
                                        XMLClientUtilities.logger.info(
                                                "{} permission not added. The permission is already in the permissions list",
                                                treePermission);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!hHashTreePermissions.containsKey(oTreeFileName)) {
                    hHashTreePermissions.put(oTreeFileName, vTreePermissions);
                }
                if (!permissions.containsKey(XMLClientUtilities.TREE_ID)) {
                    permissions.put(XMLClientUtilities.TREE_ID, hHashTreePermissions);
                }
            } else {
                throw new Exception("This is not a TREE node");
            }
        } else {
            throw new Exception("This is not a TREE node");
        }
    }

    public static StringBuffer mergeXMLPermissions(StringBuffer first, StringBuffer second) throws Exception {
        // We create two DOM trees. Then, we insert children of second at the
        // end
        // of first one.
        XMLClientUtilities.logger.info("XMLClientUtilities: mergeXMLPermissions->Start");
        if ((second == null) || (second.length() == 0)) {
            return first;
        }

        if ((first == null) || (first.length() == 0)) {
            return second;
        }
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = fact.newDocumentBuilder();
        ByteArrayInputStream bIn = null;
        ByteArrayInputStream bIn2 = null;
        try {
            bIn = new ByteArrayInputStream(first.toString().getBytes());
            Document doc = db.parse(bIn);

            bIn2 = new ByteArrayInputStream(second.toString().getBytes());
            Document doc2 = db.parse(bIn2);

            Element e = doc.getDocumentElement();

            // Adds doc2 children

            Element e2 = doc2.getDocumentElement();
            NodeList nodeList = e2.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                switch (node.getNodeType()) {
                    case Node.ELEMENT_NODE:
                        Element eNewElement = XMLClientUtilities.cloneElements((Element) node, doc);
                        e.appendChild(eNewElement);
                        break;
                }
            }

            StringWriter w = null;
            try {
                w = new StringWriter();
                // We use a Transformer for output
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();

                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(w);
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "ISO-8859-1");
                transformer.transform(source, result);
                w.flush();
                StringBuffer sb = w.getBuffer();
                XMLClientUtilities.logger.debug("sb");
                return sb;
            } catch (Exception ex) {
                XMLClientUtilities.logger.error(null, ex);
                return null;
            } finally {
                try {
                    w.close();
                } catch (IOException ex) {
                    XMLClientUtilities.logger.trace(null, ex);
                }
            }

        } catch (Exception e) {
            XMLClientUtilities.logger.error(null, e);
            throw e;
        }

    }

    private static Element cloneElements(Element e, Document destinyDocument) {
        Document doc = destinyDocument;
        Element eNewElement = doc.createElement(e.getTagName());

        NamedNodeMap map = e.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Node node = map.item(i);
            eNewElement.setAttribute(node.getNodeName(), node.getNodeValue());
        }

        XMLClientUtilities.cloneElements(e, destinyDocument, eNewElement);

        return eNewElement;

    }

    private static void cloneElements(Element e, Document destinyDocument, Element eNewElement) {
        NodeList nodeList = e.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element eNewChildNode = destinyDocument.createElement(((Element) n).getTagName());

                NamedNodeMap map = ((Element) n).getAttributes();
                for (int j = 0; j < map.getLength(); j++) {
                    Node node = map.item(j);
                    eNewChildNode.setAttribute(node.getNodeName(), node.getNodeValue());
                }
                eNewElement.appendChild(eNewChildNode);
                XMLClientUtilities.cloneElements((Element) n, destinyDocument, eNewChildNode);
            }

        }
    }

    private static StringBuffer dom2String(Node node) {
        return new StringBuffer(
                XMLClientUtilities.XML_DECLARATION + XMLClientUtilities.dom2StringInternal(node).toString());
    }

    private static StringBuffer dom2StringInternal(Node node) {
        CustomNode nod = new CustomNode(node);
        // Transforms DOM in xml code.
        String sValue = nod.getNodeInfo();
        StringBuffer sbAttributes = new StringBuffer();
        NamedNodeMap namedNodeAttributes = node.getAttributes();
        if (namedNodeAttributes != null) {
            for (int i = 0; i < namedNodeAttributes.getLength(); i++) {
                Node n = namedNodeAttributes.item(i);
                sbAttributes.append(n.getNodeName() + "=\"" + n.getNodeValue() + "\" ");
            }
        }
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            StringBuffer tag = new StringBuffer("<");
            tag.append(sValue);
            tag.append(" ");
            tag.append(sbAttributes.toString());
            tag.append(">");
            tag.append(System.getProperty("line.separator"));
            StringBuffer sbClosedTag = new StringBuffer("</");
            sbClosedTag.append(sValue);
            sbClosedTag.append(">");
            NodeList nodeListChildren = node.getChildNodes();
            for (int i = 0; i < nodeListChildren.getLength(); i++) {
                StringBuffer sb = XMLClientUtilities.dom2StringInternal(nodeListChildren.item(i));
                if (sb != null) {
                    tag.append(sb.toString());
                    tag.append(System.getProperty("line.separator"));
                }
            }
            return new StringBuffer(System.getProperty("line.separator") + tag.append(sbClosedTag.toString()));
        } else {
            return null;
        }
    }

    private static Document createInitializedXMLDocument() {
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        StringBufferInputStream in = null;
        try {
            DocumentBuilder b = fact.newDocumentBuilder();
            in = new StringBufferInputStream(XMLClientUtilities.BASE_DOCUMENT);
            // We read a permission file empty.
            Document doc = b.parse(in);
            return doc;

        } catch (Exception e) {
            XMLClientUtilities.logger.trace(null, e);
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                XMLClientUtilities.logger.trace(null, e);
            }
        }
    }

    protected static List getCommonKeys(List values) {
        // Values is a list with Map objects
        List result = new ArrayList();
        if ((values != null) && (values.size() > 0)) {
            Enumeration keys = Collections.enumeration(((HashMap) values.get(0)).keySet());
            if (!keys.hasMoreElements()) {
                logger.error("XMLClientUtilitiesgetCommonKeys has values but enumeration is empty.");
            }
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                boolean exist = true;
                for (int i = 1; i < values.size(); i++) {
                    if (!((Map) values.get(i)).containsKey(key)) {
                        exist = false;
                        break;
                    }
                }
                if (exist) {
                    result.add(key);
                }
            }
        }
        return result;
    }

    public static Map joinClientPermissions(List permissions) throws Exception {

        boolean nullValues = XMLClientUtilities.checkNullValues(permissions,
                XMLClientUtilities.ignoreNullOnClientPermissionCombination);
        if (nullValues && !XMLClientUtilities.ignoreNullOnClientPermissionCombination) {
            throw new Exception(
                    XMLClientUtilities.class.getName() + ": Error retrieving client permissions. NULL value found");
        }

        // permissions is a List with Map objects
        List commonKeys = XMLClientUtilities.getCommonKeys(permissions);
        Map result = new HashMap(commonKeys.size());

        if (commonKeys.isEmpty()) {
            return result;
        }

        for (int k = 0; k < commonKeys.size(); k++) {
            Object key = commonKeys.get(k);

            for (int i = 0; i < permissions.size(); i++) {
                Map temp = (Map) permissions.get(i);

                // For all the elements in common we join these permission
                Object oPermissions = temp.get(key);
                Object previousValue = result.get(key);
                if (previousValue == null) {
                    // result is empty yet (only with i == 0)
                    result.put(key, oPermissions);
                } else if (previousValue instanceof Map) {
                    Map permissionResult = XMLClientUtilities.getClientPermissionUnion((Map) previousValue,
                            (Map) oPermissions);
                    result.put(key, permissionResult);
                } else if (previousValue instanceof List) {
                    List permissionResult = XMLClientUtilities.joinClientPermissionActions((List) previousValue,
                            (List) oPermissions);
                    result.put(key, permissionResult);
                }
            }
        }
        return result;
    }

    public static Map getClientPermissionUnion(Map p1, Map p2) throws Exception {
        Map result = new HashMap();

        Enumeration keys = Collections.enumeration(p1.keySet());
        // For all the elements we join these permission
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (p2.containsKey(key)) {
                List secondList = (List) p2.get(key);
                // If this key already exist then we have to check all the
                // permission
                List firstList = (List) p1.get(key);
                List resultList = XMLClientUtilities.joinClientPermissionActions(firstList, secondList);
                result.put(key, resultList);
            }
        }
        return result;
    }

    public static List joinClientPermissionActions(List value1, List value2) throws Exception {
        // Both lists contain ClientPermission objects
        // The returned list contains ClientPermission objects too
        List result = new ArrayList();

        // We only have to add in the result list the elements that exist in
        // both
        // lists
        for (int i = 0; i < value1.size(); i++) {

            AbstractClientPermission clientPermission1 = (AbstractClientPermission) value1.get(i);
            int actionIndex = XMLClientUtilities.getClientPermissionIndex(clientPermission1.getPermissionName(),
                    clientPermission1.getAttribute(), value2);
            AbstractClientPermission clientPermission2 = null;
            if (actionIndex >= 0) {
                clientPermission2 = (AbstractClientPermission) value2.get(actionIndex);
            }
            if (clientPermission2 != null) {
                // If both actions are not null then compare them, else continue
                // because in the client side the empty permission is the less
                // restrictive

                analyzeRestrictiveClientPermission(result, clientPermission1, clientPermission2);
                // If any action is allowed and none of them has a period then
                // both
                // of them are restrictive, then we do nothing
            }
        }
        return result;
    }

    protected static void analyzeRestrictiveClientPermission(List result, AbstractClientPermission clientPermission1,
            AbstractClientPermission clientPermission2) throws Exception {
        if ((clientPermission1.getPeriod() == null) && !clientPermission1.isRestricted()) {
            // The first action is the most permissive then add it
            result.add(clientPermission1);
        } else if ((clientPermission2.getPeriod() == null) && !clientPermission2.isRestricted()) {
            // The second action is the most permissive the set this
            // action
            // in
            // the union
            result.add(clientPermission2);
        } else if ((clientPermission1.getPeriod() == null) && clientPermission1.isRestricted()) {
            // The first action is the most restrictive, then add the
            // second
            // to the result
            result.add(clientPermission2);
        } else if ((clientPermission2.getPeriod() == null) && clientPermission2.isRestricted()) {
            // The second action is the most restrictive then use the
            // first
            // one
            result.add(clientPermission1);
        } else if ((clientPermission1.getPeriod() != null) && !clientPermission1.isRestricted()
                && (clientPermission2.getPeriod() != null) && !clientPermission2
                    .isRestricted()) {
            // Both actions are allowed in a different period then the
            // result
            // is a new action allowed in both periods (PeriodA U
            // PeriodB)
            analyzeUnionPeriod(result, clientPermission1, clientPermission2);
        } else if ((clientPermission1.getPeriod() != null) && clientPermission1.isRestricted()
                && (clientPermission2.getPeriod() != null) && clientPermission2
                    .isRestricted()) {
            // Both actions are restricted, each of them in a different
            // period
            // Then the result is an action restricted only in the
            // intersection of both periods
            analyzeCommonPeriod(result, clientPermission1, clientPermission2);
        } else if (((clientPermission1.getPeriod() != null) && !clientPermission1.isRestricted()
                && (clientPermission2.getPeriod() != null) && clientPermission2
                    .isRestricted())
                || ((clientPermission1.getPeriod() != null) && clientPermission1
                    .isRestricted() && (clientPermission2.getPeriod() != null) && !clientPermission2.isRestricted())) {
            // One action is allowed in a period and the other is
            // restricted in other period.
            // TODO
            throw new Exception(XMLClientUtilities.class.getName()
                    + ": Permissions with periods one restricted and the other not is not supported");
        }
    }

    protected static void analyzeCommonPeriod(List result, AbstractClientPermission clientPermission1,
            AbstractClientPermission clientPermission2) {
        TimePeriodParser timePeriodParser = TimePeriodParserManager.getTimePeriodParser();
        if (timePeriodParser instanceof TimePeriodOperationParser) {
            try {
                TimePeriod commonPeriod = ((TimePeriodOperationParser) timePeriodParser)
                    .getCommonPeriod(clientPermission1.getPeriod(), clientPermission2.getPeriod());
                clientPermission1.setPeriod(commonPeriod);
                clientPermission1.setRestricted(commonPeriod != null);
                result.add(clientPermission1);
            } catch (Exception e) {
                XMLClientUtilities.logger.error(null, e);
            }
        }
    }

    protected static void analyzeUnionPeriod(List result, AbstractClientPermission clientPermission1,
            AbstractClientPermission clientPermission2) {
        TimePeriodParser timePeriodParser = TimePeriodParserManager.getTimePeriodParser();
        if (timePeriodParser instanceof TimePeriodOperationParser) {
            try {
                TimePeriod unionPeriod = ((TimePeriodOperationParser) timePeriodParser)
                    .getUnionPeriod(clientPermission1.getPeriod(), clientPermission2.getPeriod());
                clientPermission1.setPeriod(unionPeriod);
                result.add(clientPermission1);
            } catch (Exception e) {
                XMLClientUtilities.logger.error(null, e);
            }
        }
    }

    protected static boolean checkNullValues(List values, boolean delete) {
        boolean nullExist = false;
        for (int i = values.size() - 1; i >= 0; i--) {
            if (values.get(i) == null) {
                nullExist = true;
                if (delete) {
                    values.remove(i);
                }
            }
        }
        return nullExist;
    }

    private static int getClientPermissionIndex(String name, String attribute, List permissions) {
        // permissions is a list with ClientPermission objects
        for (int i = 0; i < permissions.size(); i++) {
            ClientPermission cp = (ClientPermission) permissions.get(i);
            // in previous versions
            // <Enabled restricted="yes" was different from <enabled
            // restricted="yes"
            // and permission component generates the first version (capital
            // letter)
            // since 5.3.5
            if (cp.getPermissionName().equalsIgnoreCase(name)) {
                if (((cp.getAttribute() == null) && (attribute == null)) || cp.getAttribute().equals(attribute)) {
                    return i;
                }
            }
        }

        return -1;
    }

}
