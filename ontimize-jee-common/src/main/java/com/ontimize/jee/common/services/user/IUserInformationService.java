/**
 * UserServerInformation.java 17/07/2013
 *
 *
 *
 */
package com.ontimize.jee.common.services.user;

/**
 * Obtiene la información del usuario con la que está trabajando el servidor.
 *
 * @author <a href=""></a>
 */
public interface IUserInformationService {

    /**
     * Obtiene la información del usuario con la que está trabajando el servidor.
     * @return user server information
     */
    UserInformation getUserInformation();

}
