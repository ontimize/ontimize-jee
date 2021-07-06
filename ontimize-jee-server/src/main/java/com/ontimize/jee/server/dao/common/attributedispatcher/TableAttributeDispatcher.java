package com.ontimize.jee.server.dao.common.attributedispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.gui.table.TableAttribute;
import com.ontimize.jee.common.dao.DeleteOperation;
import com.ontimize.jee.common.dao.InsertOperation;
import com.ontimize.jee.common.dao.UpdateOperation;

/**
 * The Class TableAttributeDispatcher.
 */
public class TableAttributeDispatcher extends AbstractAttributeDispatcher<TableAttribute> {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(TableAttributeDispatcher.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.server.services.core.IAttributeDispatcher#processAttribute(java.lang.Object,
     * com.ontimize.jee.common.db.EntityResult, org.springframework.context.ApplicationContext)
     */
    @Override
    public void processQueryAttribute(TableAttribute tableAttribute, EntityResult result,
            ApplicationContext applicationContext) {
        String entityName = tableAttribute.getEntity();
        if ((entityName == null) || (tableAttribute.getRecordNumberToInitiallyDownload() == 0)) {
            TableAttributeDispatcher.logger.warn(
                    " INVALID TABLEATTRIBUTE - WITHOUT \"ENTITY\" NOR FOR \"RECORDNUMBERTOINITIALLYDOWNLOAD\" LENGTH ZERO. CHECK CONFIGURATION.");
            return;
        }

        // Query for each main record
        List<Object> vOtherEntityValues = new ArrayList<>();
        int numberRecords = result.calculateRecordNumber();
        for (int k = 0; k < numberRecords; k++) {
            EntityResult res = this.processQueryAttribute(tableAttribute, result, applicationContext, k);
            vOtherEntityValues.add(k, res);
        }
        // Add the entity result to the final result
        result.put(tableAttribute, vOtherEntityValues);

    }

    protected EntityResult processQueryAttribute(TableAttribute tableAttribute, EntityResult result,
            ApplicationContext appContext, int k) {
        String entityName = tableAttribute.getEntity();

        // TODO tener en cuenta que se puede pedir el numero de registros
        int requestedRecordNumber = tableAttribute.getRecordNumberToInitiallyDownload();

        // For each record compose filters
        Map<Object, Object> finalKeysValues = this.collectKeysValues(tableAttribute, result, k);

        // Do Query
        EntityResult res = null;
        if (requestedRecordNumber < 0) {
            res = this.invokeQuery(appContext, entityName, finalKeysValues, (List<?>) tableAttribute.get(entityName));
        } else {
            TableAttributeDispatcher.logger
                .error(" UNIMPLEMENTED TABLEATTRIBUTE WITH \"RECORDNUMBERTOINITIALLYDOWNLOAD\" LENGTH.");
            // TODO implementar esta query
            // res = ((AdvancedEntity)entity).query(hOtherKeysValues, (List)((Map)
            // oAttribute).get(sKey), otherEntitySessionId, requestedRecordNumber, 0, ((TableAttribute)
            // oAttribute).getOrderBy())
        }
        return res;
    }

    private Map<Object, Object> collectKeysValues(TableAttribute tableAttribute, EntityResult result, int k) {
        Map<Object, Object> finalKeysValues = new HashMap<>();

        // Consider parentkeys --------------------------------
        List<?> parentkeys = tableAttribute.getParentKeys();
        if (parentkeys.isEmpty()) {
            TableAttributeDispatcher.logger.warn(tableAttribute + " - TableAttribute doesn't have parentkeys defined ");
        }
        for (int j = 0; j < parentkeys.size(); j++) {
            String parentkeyColumn = parentkeys.get(j).toString();
            List<?> vKeyValues = (List<?>) result.get(parentkeyColumn);
            if (vKeyValues == null) {
                TableAttributeDispatcher.logger.warn(
                        " RESULT List NOT FOUND IN QUERY RESULT NEITHER FOR PARENTKEY: '{}' NOR FOR: '{}'. CHECK CONFIGURATION.",
                        parentkeys.get(j),
                        parentkeyColumn);
            }
            Object oOtherKeyValue = vKeyValues == null ? null : vKeyValues.get(k);
            TableAttributeDispatcher.logger.trace(
                    "The entity \"{}\" receives the value \"{}\" for the column with alias \"{}\"",
                    tableAttribute.getEntity(), oOtherKeyValue,
                    parentkeyColumn);

            if (oOtherKeyValue != null) {
                finalKeysValues.put(tableAttribute.getParentkeyEquivalence(parentkeys.get(j).toString()),
                        oOtherKeyValue);
            } else {
                // en todo caso seria un searchvalue y aun asi no se deberia incluir en la busqueda
                // finalKeysValues.put(tableAttribute.getParentkeyEquivalence(parentkeys.get(j).toString()), new
                // NullValue(result.getColumnSQLType(parentkeys.get(j).toString())));
            }
        }

        // Consider QueryFilter --------------------------------
        if (tableAttribute.getQueryFilter() != null) {
            TableAttributeDispatcher.logger.trace("The entity \"{}\" receives moreover this queryFilters \"{}\".",
                    tableAttribute.getEntity(), tableAttribute.getQueryFilter());
            finalKeysValues.putAll(tableAttribute.getQueryFilter());
        }
        return finalKeysValues;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.core.IAttributeDispatcher#processInsertAttribute(java.lang.
     * Object, com.ontimize.jee.common.services.cascadeoperation.InsertOperation, java.util.Map,
     * java.util.Map, org.springframework.context.ApplicationContext)
     */
    @Override
    public EntityResult processInsertAttribute(TableAttribute attribute, InsertOperation insertOperation,
            Map<?, ?> generatedKeysInParentEntity,
            Map<?, ?> attributesValuesInsertedInParentEntity, ApplicationContext applicationContext) {
        String entityName = attribute.getEntity();
        if (entityName != null) {
            Map<?, ?> valuesToInsert = insertOperation.getValuesToInsert();
            for (Object parentKey : attribute.getParentKeys()) {
                String parentkeyEquivalence = attribute.getParentkeyEquivalence((String) parentKey);
                Object ob = generatedKeysInParentEntity.get(parentKey);
                if (ob == null) {
                    ob = attributesValuesInsertedInParentEntity.get(parentKey);
                }
                if (ob != null) {
                    ((Map<Object, Object>) valuesToInsert).put(parentkeyEquivalence, ob);
                }
            }
            EntityResult res = this.invokeInsert(applicationContext, entityName, valuesToInsert);
            return res;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.core.IAttributeDispatcher#processUpdateAttribute(java.lang.
     * Object, com.ontimize.jee.common.services.cascadeoperation.UpdateOperation, java.util.Map,
     * java.util.Map, org.springframework.context.ApplicationContext)
     */
    @Override
    public EntityResult processUpdateAttribute(TableAttribute attribute, UpdateOperation updateOperation,
            Map<?, ?> generatedValuesInParentEntity, Map<?, ?> valuesInParentEntity,
            ApplicationContext applicationContext) {
        String entityName = attribute.getEntity();
        if (entityName != null) {
            Map<?, ?> valuesToUpdate = updateOperation.getValuesToUpdate();
            Map<?, ?> filter = updateOperation.getFilter();
            for (Object key : attribute.getParentKeys()) {
                Object keyValue = valuesInParentEntity.get(attribute.getParentkeyEquivalence((String) key));
                if (keyValue == null) {
                    keyValue = generatedValuesInParentEntity.get(attribute.getParentkeyEquivalence((String) key));
                }
                if (keyValue != null) {
                    ((Map<Object, Object>) filter).put(key, keyValue);
                }
            }
            EntityResult res = this.invokeUpdate(applicationContext, entityName, valuesToUpdate, filter);
            return res;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.core.IAttributeDispatcher#processDeleteAttribute(java.lang.
     * Object, com.ontimize.jee.common.services.cascadeoperation.DeleteOperation, java.util.Map,
     * java.util.Map, org.springframework.context.ApplicationContext)
     */
    @Override
    public EntityResult processDeleteAttribute(TableAttribute attribute, DeleteOperation deleteOperation,
            Map<?, ?> generatedValuesInParentEntity, Map<?, ?> filterInParentEntity,
            ApplicationContext applicationContext) {
        String entityName = attribute.getEntity();
        if (entityName != null) {
            Map<?, ?> filter = deleteOperation.getFilter();
            EntityResult res = this.invokeDelete(applicationContext, entityName, filter);
            return res;
        }
        return null;
    }

}
