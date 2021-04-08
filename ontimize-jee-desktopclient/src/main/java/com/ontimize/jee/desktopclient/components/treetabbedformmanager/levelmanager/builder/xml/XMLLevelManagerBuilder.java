package com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.builder.xml;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ontimize.builder.TreeBuilder;
import com.ontimize.builder.xml.CustomNode;
import com.ontimize.builder.xml.XMLTreeBuilder;
import com.ontimize.gui.Form;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.DefaultLevel;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.DefaultLevelManager;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.Level;
import com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.LevelManager;
import com.ontimize.xml.XMLInterpreter;

/**
 * Implementation of a {@link TreeBuilder} from a XML file
 */
public class XMLLevelManagerBuilder extends XMLInterpreter implements
        com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager.builder.LevelManagerBuilder {

    static final Logger logger = LoggerFactory.getLogger(XMLTreeBuilder.class);

    public static boolean INCLUDE_DEFAULT_LABELS = true;

    protected String defaultPackage = "com.ontimize.gui.";

    protected Hashtable equivalenceLabelList = new Hashtable();

    /**
     * @param uriLabelsFile URI to the labels file. Example 'http://.../xml/labels.xml'.<br>
     * @throws Exception
     */
    public XMLLevelManagerBuilder(String uriLabelsFile) throws Exception {
        if (XMLTreeBuilder.INCLUDE_DEFAULT_LABELS) {
            this.equivalenceLabelList = this.getDefaultLabelList();
        }

        try {
            this.processLabelFile(uriLabelsFile, this.equivalenceLabelList, new ArrayList());
        } catch (Exception e) {
            XMLLevelManagerBuilder.logger.error("Processing label file", e);
        }
        this.equivalenceLabelList.put("Node", DefaultLevel.class.getCanonicalName());
    }

    /**
     * @param uriLabelsFile URI to the labels file. Example 'http://.../xml/labels.xml'.<br>
     * @param guiClassesPackage Default package where the gui classes are stored
     * @throws Exception
     */
    public XMLLevelManagerBuilder(String uriLabelsFile, String guiClassesPackage) throws Exception {
        this(uriLabelsFile);
        if (this.defaultPackage != null) {
            this.defaultPackage = guiClassesPackage;
        }
    }

    public XMLLevelManagerBuilder(Hashtable labelEquivalences) throws Exception {
        this.equivalenceLabelList = (Hashtable) labelEquivalences.clone();
        this.equivalenceLabelList.put("Node", DefaultLevel.class.getCanonicalName());
    }

    public XMLLevelManagerBuilder(Hashtable labelEquivalences, String guiClassesPackage) throws Exception {
        this(labelEquivalences);
        if (this.defaultPackage != null) {
            this.defaultPackage = guiClassesPackage;
        }
    }

    protected LevelManager buildTree(CustomNode aux, Form parentForm, ResourceBundle resourceBundle) {
        String tag = aux.getNodeInfo();
        String className = (String) this.equivalenceLabelList.get(tag);
        if (className == null) {
            XMLLevelManagerBuilder.logger.debug("Label not found in equivalence list: {}", tag);
            // Trying with the tag
            className = tag;
        }
        // Convert the tag to the correct format (package + class name)
        className = this.defaultPackage + className;
        // Get the attribute list
        NamedNodeMap attributeList = aux.attributeList();
        Hashtable attributeTable = new Hashtable();
        LevelManager guiRootNode = null;
        for (int i = 0; i < attributeList.getLength(); i++) {
            Node auxNode = attributeList.item(i);
            attributeTable.put(auxNode.getNodeName(), auxNode.getNodeValue());
        }
        Class classObject = null;
        try {
            classObject = Class.forName(className);
            // TODO change DefaultLevelManager by the new ILevelManager Interface
            if (LevelManager.class.isAssignableFrom(classObject)) {
                try {
                    Constructor[] constructors = classObject.getConstructors();
                    attributeTable.put("parentform", parentForm);
                    Object[] parameters = { attributeTable };
                    guiRootNode = (LevelManager) constructors[0].newInstance(parameters);
                    guiRootNode.setResourceBundle(resourceBundle);
                    List<CustomNode> childNodes = new ArrayList<>();
                    for (int i = 0; i < aux.getChildrenNumber(); i++) {
                        childNodes.add(aux.child(i));
                    }
                    this.processChildren(childNodes, guiRootNode, resourceBundle);
                } catch (Exception e2) {
                    XMLLevelManagerBuilder.logger.error("Error creating object. ", e2);
                }
            } else if (Level.class.isAssignableFrom(classObject)) {
                Hashtable<String, Object> parameters = new Hashtable<>();
                parameters.put("opaque", "no");
                parameters.put("attr", "tree");
                parameters.put("displaypathhtml", "yes");
                parameters.put("displaypathseparator", "->");
                parameters.put("parentform", parentForm);
                guiRootNode = new DefaultLevelManager(parameters);
                guiRootNode.setResourceBundle(resourceBundle);
                this.processChildren(Arrays.asList(aux), guiRootNode, resourceBundle);
            } else {
                XMLLevelManagerBuilder.logger.error("Tree node is not a root node nor a level node");
            }

        } catch (Exception e) {
            XMLLevelManagerBuilder.logger.error("Error loading class", e);
        }
        return guiRootNode;
    }

    @Override
    public LevelManager buildLevelManager(String fileURI, Form parentForm, ResourceBundle resourceBundle) {
        // This function allows to create a object tree for tree building
        CustomNode aux = new CustomNode(this.getDocumentModel(fileURI).getDocumentElement());
        return this.buildTree(aux, parentForm, resourceBundle);
    }

    @Override
    public LevelManager buildLevelManager(StringBuffer content, Form parentForm, ResourceBundle resourceBundle) {
        try {
            CustomNode aux = new CustomNode(this.getDocumentModel(content).getDocumentElement());
            return this.buildTree(aux, parentForm, resourceBundle);
        } catch (Exception e) {
            XMLLevelManagerBuilder.logger.error(e.getMessage(), e);
        }
        return null;
    }

    private void preprocessChildren(List<CustomNode> unprocessedChildNodes, List<CustomNodeWrapper> preProcessedNodes,
            CustomNodeWrapper parentPreprocessedNode,
            LevelManager levelManager) {
        for (int i = 0; i < unprocessedChildNodes.size(); i++) {
            if (unprocessedChildNodes.get(i).isTag()) {
                Map<String, Object> attributes = new HashMap<>();
                NamedNodeMap attributeList = unprocessedChildNodes.get(i).attributeList();
                // buscamos su id y su nextlevel
                for (int j = 0; j < attributeList.getLength(); j++) {
                    Node auxNode = attributeList.item(j);
                    attributes.put(auxNode.getNodeName(), auxNode.getNodeValue());
                }
                CustomNodeWrapper cnw = new CustomNodeWrapper(unprocessedChildNodes.get(i));
                if (attributes.get(Level.ID) == null) {
                    // si no tiene atributo id, es el caso del node estandar de un tree
                    attributes.put(Level.ID, "level" + preProcessedNodes.size());
                }
                if ((parentPreprocessedNode != null) && !attributes.containsKey(Level.PREVIOUS_LEVEL)) {
                    attributes.put(Level.PREVIOUS_LEVEL, parentPreprocessedNode.getAttributes().get(Level.ID));
                }
                attributes.put(Level.LEVEL_MANAGER, levelManager);
                cnw.setAttributes(attributes);
                // si no tiene nextLevel, caso estandar de un tree, su hijo es el encargado de establecerselo
                // establecemos el nextlevel del padre si es necesario
                if ((parentPreprocessedNode != null)
                        && (parentPreprocessedNode.getAttributes().get(Level.NEXT_LEVEL) == null)) {
                    parentPreprocessedNode.getAttributes().put(Level.NEXT_LEVEL, attributes.get(Level.ID));
                } else if ((parentPreprocessedNode != null)
                        && (parentPreprocessedNode.getAttributes().get(Level.NEXT_LEVEL) != null)
                        && !parentPreprocessedNode.getAttributes()
                            .get(Level.NEXT_LEVEL)
                            .equals(attributes.get(Level.ID))) {
                    // en este caso este nodo esta anidado dentro de otro, que tiene un next level referenciado que no
                    // es este nodo
                    throw new IllegalArgumentException("Un mismo nodo tiene varios nodos hijos");
                }
                preProcessedNodes.add(cnw);

                // ahora procesamos los hijos de este nodo
                List<CustomNode> childNodes = new ArrayList<>();
                for (int j = 0; j < unprocessedChildNodes.get(i).getChildrenNumber(); j++) {
                    childNodes.add(unprocessedChildNodes.get(i).child(j));
                }
                if (childNodes.size() > 0) {
                    this.preprocessChildren(childNodes, preProcessedNodes, cnw, levelManager);
                }
            }
        }
    }

    protected void processChildren(List<CustomNode> childNodes, LevelManager guiNode, ResourceBundle resourceBundle) {
        List<CustomNodeWrapper> preProcessedNodes = new ArrayList<>();
        this.preprocessChildren(childNodes, preProcessedNodes, null, guiNode);

        // TODO cambiar esto por la interfaz Level
        List<Level> linkedTables = new ArrayList<>();
        for (CustomNodeWrapper cnw : preProcessedNodes) {
            CustomNode customNode = cnw.getCustomNode();
            if (customNode.isTag()) {
                String className = (String) this.equivalenceLabelList.get(customNode.getNodeInfo());

                if (className == null) {
                    String tag = customNode.getNodeInfo();
                    XMLLevelManagerBuilder.logger.debug("Label not found in equivalence list: {}", tag);
                    // Trying with the tag
                    className = tag;
                }

                // Convert the tag to the correct format (package + class name)
                className = this.defaultPackage + className;
                // Get the attribute list
                // TODO cambiar por la interfaz Level
                DefaultLevel guiChildNode = null;
                NamedNodeMap attributeList = customNode.attributeList();
                Hashtable attributeTable = new Hashtable(cnw.getAttributes());
                this.fixAttributesIfNecessary(attributeTable);

                try {
                    Class classObject = Class.forName(className);
                    try {
                        Constructor[] constructors = classObject.getConstructors();
                        Object[] parameters = { attributeTable };
                        guiChildNode = (DefaultLevel) constructors[0].newInstance(parameters);
                        guiChildNode.setResourceBundle(resourceBundle);
                        linkedTables.add(guiChildNode);
                    } catch (Exception e2) {
                        XMLLevelManagerBuilder.logger.error("Error creating object", e2);
                    }
                } catch (Exception e) {
                    XMLLevelManagerBuilder.logger.error("Error loading class ", e);
                }
            }

        }

        for (Level lt : linkedTables) {
            guiNode.add(lt);
        }

    }

    private void fixAttributesIfNecessary(Hashtable attributeTable) {
        if (!attributeTable.containsKey("cols") && attributeTable.containsKey("attr")) {
            attributeTable.put("cols", attributeTable.get("attr"));
        }
        if (!attributeTable.containsKey("visiblecols")) {
            if (attributeTable.containsKey("attr")) {
                if (attributeTable.containsKey("hideattr")) {
                    List<String> attr = Arrays.asList(StringUtils.split((String) attributeTable.get("attr"), ';'));
                    List<String> hideAttr = Arrays
                        .asList(StringUtils.split((String) attributeTable.get("hideattr"), ';'));
                    List<String> visible = new ArrayList<>();
                    for (String at : attr) {
                        if (!hideAttr.contains(at)) {
                            visible.add(at);
                        }
                    }
                    attributeTable.put("visiblecols", StringUtils.join(visible, ';'));

                } else {
                    attributeTable.put("visiblecols", attributeTable.get("attr"));
                }
            } else if (attributeTable.containsKey("cols")) {
                attributeTable.put("visiblecols", attributeTable.get("cols"));
            }
        }
        if (!attributeTable.containsKey("displaytextformat")) {
            if (attributeTable.containsKey("text")) {
                attributeTable.put("displaytextformat", attributeTable.get("text"));
            }
        }
        if (!attributeTable.containsKey("detailformat")) {
            if (attributeTable.containsKey("attr") && attributeTable.containsKey("visiblecols")) {
                String visibleCols = (String) attributeTable.get("visiblecols");
                int separatorCount = StringUtils.countMatches(visibleCols, ";");
                StringBuilder sb = new StringBuilder();

                if (separatorCount > 0) {
                    for (int i = 0; i < separatorCount; i++) {
                        sb.append("{" + i + "}-");
                    }
                    sb.append("{" + separatorCount + "};");
                    sb.append(visibleCols);
                } else if ((separatorCount == 0) && (visibleCols.length() > 0)) {
                    sb.append("{0};");
                    sb.append(visibleCols);
                }
                if (sb.length() > 0) {
                    attributeTable.put("detailformat", sb.toString());
                }
            }
        }

    }

    private static class CustomNodeWrapper {

        private final CustomNode customNode;

        private Map<String, Object> attributes = new HashMap<>();

        public CustomNodeWrapper(CustomNode customNode) {
            this.customNode = customNode;
        }

        public CustomNode getCustomNode() {
            return this.customNode;
        }

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public Map<String, Object> getAttributes() {
            return this.attributes;
        }

    }

}
