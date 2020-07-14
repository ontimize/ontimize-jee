package com.ontimize.jee.server.dao;

import java.util.List;
import java.util.Map;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.One2OneDaoHelper.One2OneType;
import com.ontimize.jee.server.dao.One2OneDaoHelper.OneToOneSubDao;

public interface IMassiveModificationHelper {

    public static final String MASSIVE_MODIFICATION_UNIQUE_IDENTIFIER = "MASSIVE_MODIFICATION_UNIQUE_IDENTIFIER";

    /**
     * @param daoHelper DAOHelper used to QUERY
     * @param dao DAO of element to query
     * @param pkColumn Primary key column identifier
     * @param keysValues Keys that identify element under received DAO
     * @return
     */
    EntityResult query(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn,
            Map<?, ?> keysValues, List<?> attributes);

    EntityResult query(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn,
            Map<?, ?> keysValues, List<?> attributes, String queryId);

    /**
     * @param daoHelper DAOHelper used to QUERY
     * @param dao DAO of element to update
     * @param pkColumn Primary key column identifier
     * @param attributesValues the data for updating the records to.
     * @param keysValues Keys that identify element under received DAO
     * @return
     */
    EntityResult update(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn,
            Map<?, ?> attributesValues, Map<?, ?> keysValues)
            throws OntimizeJEERuntimeException;

    EntityResult update(DefaultOntimizeDaoHelper daoHelper, One2OneDaoHelper one2oneHelper, IOntimizeDaoSupport mainDao,
            String pkColumn, List<OneToOneSubDao> secondaryDaos,
            Map<?, ?> attributesValues, Map<?, ?> keysValues, One2OneType type) throws OntimizeJEERuntimeException;

    EntityResult insert(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String vKeysColumn,
            String pkColumn, Map<?, ?> attributesValues);

    EntityResult insert(DefaultOntimizeDaoHelper daoHelper, One2OneDaoHelper one2oneHelper, IOntimizeDaoSupport mainDao,
            String pkColumn, List<OneToOneSubDao> secondaryDaos,
            Map<?, ?> attributesValues, One2OneType type) throws OntimizeJEERuntimeException;

    boolean isMassiveModification(Object key, Map<?, ?> keysValues);

}
