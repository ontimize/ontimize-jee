package com.ontimize.jee.common.db;

import java.rmi.Remote;
import java.util.Map;

public interface MetadataEntity extends Remote {

    public static final String INSERT_KEYS = "InsertKeys";

    /**
     * Autonumerical property.
     */
    public static final String AUTONUMERICAL = "Autonumerical";

    /**
     * GeneratedKey property
     */
    public static final String GENERATED_KEY = "GeneratedKey";


    public Map getMetadata(int sessionId) throws Exception;

}
