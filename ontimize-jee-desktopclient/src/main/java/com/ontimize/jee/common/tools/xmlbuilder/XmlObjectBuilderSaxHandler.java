package com.ontimize.jee.common.tools.xmlbuilder;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ontimize.gui.field.AbstractFormComponent;
import com.ontimize.jee.common.tools.ReflectionTools;

/**
 * Handler que recibe los eventos de inicio/fin de an�lisis de elementos xml. En esta clase se
 * realiza la construccion de los objetos declarados en el xml.
 *
 */
public class XmlObjectBuilderSaxHandler extends DefaultHandler {

    private static final Logger log = LoggerFactory.getLogger(XmlObjectBuilderSaxHandler.class);

    /** The stack. */
    protected Deque<Object> stack;

    /** The root. */
    protected Object root;

    /** The equivalences. */
    protected Map<String, String> equivalences;

    /** The object parameters. */
    protected Map<Object, Map<String, String>> objectParameters;

    /** The extraRoot parameters. */
    protected Map<String, String> extraRootParameters;

    /**
     * Constructor.
     * @param masterContainer el Composite que hara de padre del Composite raiz definido en el xml.
     * @param equivalences el map de equivalencias.
     */
    public XmlObjectBuilderSaxHandler(final Object root, final Map<String, String> equivalences,
            final Map<String, String> extraRootParameters) {
        super();
        this.extraRootParameters = extraRootParameters;
        this.stack = new LinkedList<>();
        this.equivalences = equivalences;
        this.objectParameters = new HashMap<>();
        this.stack.push(root);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {
        Map<String, String> parameters = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            parameters.put(attributes.getQName(i), attributes.getValue(i));
        }
        Object parent = this.stack.peek();
        try {
            if ((this.root == null) && (this.extraRootParameters != null)) {
                parameters.putAll(this.extraRootParameters);
            }
            Object ob = this.buildObject(qName, parameters, parent);
            if (this.root == null) {
                this.root = ob;
            }
            this.stack.push(ob);
        } catch (Exception e) {
            throw new SAXException(null, e);
        }

    }

    /**
     * Se encarga de realizar la construccion de los objetos definidos en el xml. Se invoca al
     * constructor por defecto y se comprueba que el objeto padre en la jerarqu�a xml tenga un
     * m�todo add(Object ob) para anadirle el objeto construido. Adem�s si el componente es
     * Initalizable se invoca al m�todo init.
     * @param qName el nombre del elemento en el xml
     * @param parameters los atributos del elemento en el xml
     * @param parent el objeto padre
     * @return the object
     * @throws ClassNotFoundException the class not found exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws SecurityException the security exception
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     * @throws NoSuchMethodException the no such method exception {@link IInitializable} se invoca al
     *         metodo {@link Initializable.init(Map<String,?)}
     */
    protected Object buildObject(final String qName, final Map<String, String> parameters, final Object parent)
            throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, ParserConfigurationException, SAXException, IOException {
        String className = qName.replace('-', '$');// para poder declarar clases internas

        Object ob = null;
        if ("Include".equalsIgnoreCase(className)) {
            ob = new XmlObjectBuilder().buildFromXml(parent,
                    XmlObjectBuilder.class.getClassLoader().getResourceAsStream(parameters.get("includefile")),
                    this.equivalences,
                    parameters);
        } else {

            String equivalence = this.equivalences == null ? null : this.equivalences.get(className);
            className = equivalence == null ? className : equivalence;

            try {
                // opcion1 constructor con padre+map
                ob = ReflectionTools.newInstance(className, parent, parameters);
            } catch (Exception ex) {
                log.trace(null, ex);
                try {
                    // opcion2 constructor con parametros
                    ob = ReflectionTools.newInstance(className, parameters);
                } catch (Exception ex2) {
                    log.trace(null, ex2);
                    try {
                        // opcion3 constructor con padre
                        ob = ReflectionTools.newInstance(className, parent);
                    } catch (Exception ex3) {
                        log.trace(null, ex3);
                        // opcion4 constructor por defecto
                        ob = ReflectionTools.newInstance(className);
                    }
                }
            }

            if (ob instanceof IInitializable) {
                ((IInitializable) ob).init(parameters);
            }
            this.addToParent(parent, ob, parameters);
            this.objectParameters.put(ob, parameters);
        }
        return ob;
    }

    /**
     * Anade un objeto un objeto padre buscando un metodo <code>add</code> en el padre.
     * @param parent the parent
     * @param ob the ob
     * @param parameters the parameters
     */
    protected void addToParent(final Object parent, final Object ob, final Map<String, String> parameters) {
        try {
            // Cualquier otro objeto lo a�adimos suponiendo que hay un m�todo
            // add(Object x,Map<String,?>
            // parameters)
            try {
                ReflectionTools.invoke(parent, "add", ob, parameters);
            } catch (Exception ex) {
                log.trace(null, ex);
                if ((parent instanceof Container) && (ob instanceof AbstractFormComponent)) {
                    ((Container) parent).add((Component) ob,
                            ((AbstractFormComponent) ob).getConstraints(((Container) parent).getLayout()));
                } else {
                    ReflectionTools.invoke(parent, "add", ob);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not add " + ob.getClass().getName() + " to "
                    + parent.getClass().getName() + " because no add method found", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        this.stack.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        // Do nothing
    }

    /**
     * Devuelve el Composite raiz del xml.
     * @return the root
     */
    public Object getRoot() {
        return this.root;
    }

    /**
     * Devuelve un map con los parametros de todos los objetos creados.
     * @return the object parameters
     */
    public Map<Object, Map<String, String>> getObjectParameters() {
        return this.objectParameters;
    }

}
