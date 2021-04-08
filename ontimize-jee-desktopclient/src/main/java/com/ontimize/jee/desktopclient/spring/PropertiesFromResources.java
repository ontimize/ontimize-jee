/**
 * PropertiesFromResources.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.desktopclient.spring;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.Resource;

/**
 * Clase para cargar propiedades de uno o más ficheros de properties e inyectarlas por spring.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class PropertiesFromResources {

    /** The properties. */
    protected Properties properties;

    /**
     * Constructor.
     * @param resources el array de recursos
     * @throws IOException Si hay algún problema accediendo a los ficheros de propiedades.
     */
    public PropertiesFromResources(final Resource[] resources) throws IOException {
        super();
        this.properties = this.joinResources(resources);
    }

    /**
     * Une las propiedades de varios ficheros de propiedades en un único Properties.
     * @param resources el array de ficheros de recursos
     * @return the properties
     * @throws IOException Si hay algún problema accediendo a los ficheros de propiedades.
     */
    protected Properties joinResources(final Resource[] resources) throws IOException {
        final Properties prop = new Properties();
        for (final Resource res : resources) {
            prop.load(res.getInputStream());
        }
        return prop;
    }

    /**
     * Wrapper del containsValue del Properties interno.
     * @param value the value
     * @return true, if successful
     */
    public boolean containsValue(Object value) {
        return this.properties.containsValue(value);
    }

    /**
     * Wrapper del containsKey del Properties interno.
     * @param key the key
     * @return true, if successful
     */
    public boolean containsKey(Object key) {
        return this.properties.containsKey(key);
    }

    /**
     * Wrapper del getProperty del Properties interno.
     * @param key the key
     * @return the property
     */
    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    /**
     * Obtiene el properties interno.
     * @return the properties
     */
    public Properties getProperties() {
        return this.properties;
    }

}
