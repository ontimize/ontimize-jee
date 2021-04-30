package com.ontimize.jee.server.dao.common.attributedispatcher;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dao.DeleteOperation;
import com.ontimize.jee.common.dao.InsertOperation;
import com.ontimize.jee.common.dao.UpdateOperation;

/**
 * Interfaz que deben soportar los delegados que implmenten funcionalidad de queryotherentity.
 *
 * @param <T> the generic type
 */
public interface IAttributeDispatcher<T> {

    /** The query suffix. */
    String QUERY_SUFFIX = "Query";

    /** The insert suffix. */
    String INSERT_SUFFIX = "Insert";

    /** The update suffix. */
    String UPDATE_SUFFIX = "Update";

    /** The delete suffix. */
    String DELETE_SUFFIX = "Delete";

    /**
     * Procesa el atributo indicado.
     * @param attribute the attribute
     * @param result the result
     * @param apContext the ap context
     */
    void processQueryAttribute(T attribute, EntityResult result, ApplicationContext apContext);

    /**
     * Process insert attribute.
     * @param attribute the attribute
     * @param valuesToInsert the values to insert
     * @param attributesValuesInsertedInParentEntity
     * @param generatedKeysInParentEntity
     * @param applicationContext the application context
     * @return the entity result with generated keys
     */
    EntityResult processInsertAttribute(T attribute, InsertOperation insertOperation,
            Map<?, ?> generatedKeysInParentEntity, Map<?, ?> attributesValuesInsertedInParentEntity,
            ApplicationContext applicationContext);

    /**
     * Process update attribute.
     * @param attribute the table attribute
     * @param result the result
     * @param applicationContext the application context
     */
    EntityResult processUpdateAttribute(T attribute, UpdateOperation updateOperation,
            Map<?, ?> generatedValuesInParentEntity, Map<?, ?> filterInParentEntity,
            ApplicationContext applicationContext);

    /**
     * Process update attribute.
     * @param attribute the table attribute
     * @param result the result
     * @param applicationContext the application context
     */
    EntityResult processDeleteAttribute(T attribute, DeleteOperation deleteOperation,
            Map<?, ?> generatedValuesInParentEntity, Map<?, ?> filterInParentEntity,
            ApplicationContext applicationContext);

}
