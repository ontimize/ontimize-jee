/**
 * Initializable.java 18-abr-2013
 */
package com.ontimize.jee.common.tools.xmlbuilder;

import java.util.Map;

/**
 * Interfaz que deben implementar todas las clases que se carguen por xml y quieran recibir las
 * propiedads que se establecieron en el xml.
 *
 * @author <a href="user@email.com">Author</a>
 */
public interface IInitializable {

    /**
     * Recibe las propiedades establecidas en el xml.
     * @param parameters the parameters
     */
    void init(Map<String, ?> parameters);

}
