package com.ontimize.jee.server.dao.common.attributedispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.springframework.context.ApplicationContext;

import com.ontimize.db.EntityResult;
import com.ontimize.gui.field.ReferenceFieldAttribute;
import com.ontimize.jee.common.dao.DeleteOperation;
import com.ontimize.jee.common.dao.InsertOperation;
import com.ontimize.jee.common.dao.UpdateOperation;

/**
 * The Class ReferenceFieldAttributeDispatcher.
 */
public class ReferenceFieldAttributeDispatcher extends AbstractAttributeDispatcher<ReferenceFieldAttribute> {

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.server.services.core.IAttributeDispatcher#processAttribute(java.lang.Object,
     * com.ontimize.db.EntityResult, org.springframework.context.ApplicationContext)
     */
    @Override
    public void processQueryAttribute(ReferenceFieldAttribute oAttribute, EntityResult result,
            ApplicationContext applicationContext) {
        // Fist of all reference data fields
        String cod = oAttribute.getCod();
        Vector<?> cols = oAttribute.getCols();
        String entityName = oAttribute.getEntity();
        String attr = oAttribute.getAttr();
        Vector<?> attrs = (Vector<?>) result.get(attr);

        Vector<EntityResult> vResults = new Vector<>();
        if ((attrs != null) && (attrs.size() > 0)) {
            for (int j = 0; j < attrs.size(); j++) {
                Object oAttrValue = attrs.get(j);
                if (oAttrValue != null) {
                    Map<Object, Object> otherKeysValues = new HashMap<>();
                    otherKeysValues.put(cod, oAttrValue);
                    EntityResult res = this.invokeQuery(applicationContext, entityName, otherKeysValues, cols);
                    vResults.add(j, res);
                } else {
                    vResults.add(j, null);
                }
            }
        }
        result.put(oAttribute, vResults);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.core.IAttributeDispatcher#processInsertAttribute(java.lang.
     * Object, com.ontimize.jee.common.services.cascadeoperation.InsertOperation, java.util.Map,
     * java.util.Map, org.springframework.context.ApplicationContext)
     */
    @Override
    public EntityResult processInsertAttribute(ReferenceFieldAttribute attribute, InsertOperation insertOperation,
            Map<?, ?> generatedKeysInParentEntity,
            Map<?, ?> attributesValuesInsertedInParentEntity, ApplicationContext applicationContext) {
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
    public EntityResult processUpdateAttribute(ReferenceFieldAttribute attribute, UpdateOperation updateOperation,
            Map<?, ?> generatedValuesInParentEntity,
            Map<?, ?> filterInParentEntity, ApplicationContext applicationContext) {
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
    public EntityResult processDeleteAttribute(ReferenceFieldAttribute attribute, DeleteOperation deleteOperation,
            Map<?, ?> generatedValuesInParentEntity,
            Map<?, ?> filterInParentEntity, ApplicationContext applicationContext) {
        return null;
    }

}
