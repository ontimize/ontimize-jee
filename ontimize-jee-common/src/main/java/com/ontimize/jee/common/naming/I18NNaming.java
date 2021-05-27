/**
 * I18NNaming.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.common.naming;

/**
 * The Class I18NNaming.
 *
 * @author <a href="user@email.com">Author</a>
 */
public final class I18NNaming {

    /**
     * Instantiates a new i18 n naming.
     */
    private I18NNaming() {
        super();
    }

    /** The Constant E_AUTH_PASSWORD_NOT_MATCH. */
    public static final String E_AUTH_PASSWORD_NOT_MATCH = "E_AUTH_PASSWORD_NOT_MATCH";

    /** No records changed message key. */
    public static final String M_IT_HAS_NOT_CHANGED_ANY_RECORD = "entity.no_registers_have_been_updated";

    /** The Constant M_IT_HAS_NOT_DELETED_ANY_RECORD. */
    public static final String M_IT_HAS_NOT_DELETED_ANY_RECORD = "entity.no_registers_have_been_deleted";

    /** The Constant M_IT_HAS_NOT_CHANGED_N_RECORDS. */
    public static final String M_IT_HAS_CHANGED_N_RECORDS = "entity.registers_updated";

    /** The Constant M_IT_HAS_DELETED_N_RECORDS. */
    public static final String M_IT_HAS_DELETED_N_RECORDS = "entity.registers_deleted";

    /** The Constant M_ERROR_LOADING_CONFIGURATION_FILE. */
    public static final String M_ERROR_LOADING_CONFIGURATION_FILE = "entity.error_loading_configuration_file";

    /** The Constant M_ERROR_LOADING_JPA_ENTITY. */
    public static final String M_ERROR_LOADING_JPA_ENTITY = "entity.error_loading_jpa_entity";

    /** The Constant E_NO_ONTIMIZE_AUTHENTICATORS_DEFINED. */
    public static final String E_NO_ONTIMIZE_AUTHENTICATORS_DEFINED = "No ontimize security authenticators defined";

    /** The Constant E_NO_ONTIMIZE_SECURITY_CONFIGURATION_DEFINED. */
    public static final String E_NO_ONTIMIZE_SECURITY_CONFIGURATION_DEFINED = "No ontimize security configuration defined";

    /** The Constant E_NO_ONTIMIZE_CONFIGURATION_DEFINED. */
    public static final String E_NO_ONTIMIZE_CONFIGURATION_DEFINED = "Ontimize configuration not found in spring context";

    /** The Constant E_NO_ONTIMIZE_SECURITY_AUTHORIZATOR_DEFINED. */
    public static final String E_NO_ONTIMIZE_SECURITY_AUTHORIZATOR_DEFINED = "No ontimize security authorizator defined";

    /** The Constant MC_ERROR_QUERY_TYPE_NOT_KNOWN. */
    public static final String MC_ERROR_QUERY_TYPE_NOT_KNOWN = "Error query type not known: {0}";

}
