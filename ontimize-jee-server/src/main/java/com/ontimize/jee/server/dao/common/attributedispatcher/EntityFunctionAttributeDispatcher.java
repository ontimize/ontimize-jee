package com.ontimize.jee.server.dao.common.attributedispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;

import org.springframework.context.ApplicationContext;

import com.ontimize.dto.EntityResult;
import com.ontimize.db.NullValue;
import com.ontimize.db.util.DBFunctionName;
import com.ontimize.gui.field.EntityFunctionAttribute;
import com.ontimize.jee.common.dao.DeleteOperation;
import com.ontimize.jee.common.dao.InsertOperation;
import com.ontimize.jee.common.dao.UpdateOperation;

public class EntityFunctionAttributeDispatcher extends AbstractAttributeDispatcher<EntityFunctionAttribute> {

    @Override
    public void processQueryAttribute(EntityFunctionAttribute attributeAdv, EntityResult result,
            ApplicationContext applicationContext) {
        String entityName = attributeAdv.getEntityName();
        if (entityName != null) {

            // The key-value are the entity primary keys and the attribute
            // parentkeys
            List<Object> filterAttributes = new ArrayList<>();
            if (attributeAdv.getParentkeys() != null) {
                for (int k = 0; k < attributeAdv.getParentkeys().size(); k++) {
                    if (!filterAttributes.contains(attributeAdv.getParentkeys().get(k))) {
                        filterAttributes.add(attributeAdv.getParentkeys().get(k));
                    }
                }
            }

            int recordCount = result.calculateRecordNumber();
            List<Object> queryResults = new ArrayList<>(recordCount);

            Map<Object, Object> queryFilterValues = new HashMap<>();
            for (int k = 0; k < recordCount; k++) {
                queryFilterValues.clear();
                for (int m = 0; m < filterAttributes.size(); m++) {
                    Object keyValues = result.get(filterAttributes.get(m));
                    if ((keyValues != null) && (keyValues instanceof List)
                            && (((List<?>) keyValues).get(k) != null)) {
                        queryFilterValues.put(filterAttributes.get(m), ((List<?>) keyValues).get(k));
                    } else {
                        if (attributeAdv.isUseNullValueToParentkeys()) {
                            if (result.containsKey(filterAttributes.get(m))) {
                                queryFilterValues.put(filterAttributes.get(m),
                                        new NullValue(result.getColumnSQLType(filterAttributes.get(m).toString())));
                            }
                        }
                    }
                }

                List<Object> vAttributes = new ArrayList<>();
                vAttributes.add(new DBFunctionName(attributeAdv.getFunctionStringQuery(), true));
                if (attributeAdv.getQueryColumns() != null) {
                    vAttributes.addAll(attributeAdv.getQueryColumns());
                }
                EntityResult res = this.invokeQuery(applicationContext, entityName, queryFilterValues, vAttributes);

                Object resultObject = res.get(attributeAdv.getAttr());
                if (resultObject != null) {
                    if (resultObject instanceof List) {
                        queryResults.add(k, ((List<?>) resultObject).get(0));
                    } else {
                        queryResults.add(k, resultObject);
                    }
                }
            }
            // Add the entity result to the final result
            result.put(attributeAdv, queryResults);
        }
    }

    @Override
    public EntityResult processInsertAttribute(EntityFunctionAttribute attribute, InsertOperation insertOperation,
            Map<?, ?> generatedKeysInParentEntity,
            Map<?, ?> attributesValuesInsertedInParentEntity, ApplicationContext applicationContext) {
        return null;
    }

    @Override
    public EntityResult processUpdateAttribute(EntityFunctionAttribute attribute, UpdateOperation updateOperation,
            Map<?, ?> generatedValuesInParentEntity,
            Map<?, ?> filterInParentEntity, ApplicationContext applicationContext) {
        return null;
    }

    @Override
    public EntityResult processDeleteAttribute(EntityFunctionAttribute attribute, DeleteOperation deleteOperation,
            Map<?, ?> generatedValuesInParentEntity,
            Map<?, ?> filterInParentEntity, ApplicationContext applicationContext) {
        return null;
    }

}
