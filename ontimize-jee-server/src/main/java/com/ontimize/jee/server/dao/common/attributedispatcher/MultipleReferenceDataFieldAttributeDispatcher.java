package com.ontimize.jee.server.dao.common.attributedispatcher;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.springframework.context.ApplicationContext;

import com.ontimize.db.EntityResult;
import com.ontimize.gui.MultipleValue;
import com.ontimize.gui.field.MultipleReferenceDataFieldAttribute;
import com.ontimize.jee.common.dao.DeleteOperation;
import com.ontimize.jee.common.dao.InsertOperation;
import com.ontimize.jee.common.dao.UpdateOperation;

public class MultipleReferenceDataFieldAttributeDispatcher
        extends AbstractAttributeDispatcher<MultipleReferenceDataFieldAttribute> {

    @Override
    public void processQueryAttribute(MultipleReferenceDataFieldAttribute multipleAttr, EntityResult result,
            ApplicationContext applicationContext) {
        List<?> lCd = multipleAttr.getCods();
        List<?> lK = multipleAttr.getKeys();
        List<?> lPK = multipleAttr.getParentKeys();
        List<?> lPCds = multipleAttr.getParentCods();
        EntityResult res = null;

        String entityName = multipleAttr.getEntity();

        Vector<MultipleValue> vResults = new Vector<>();
        for (int j = 0; j < result.calculateRecordNumber(); j++) {
            Hashtable<?, ?> hPrevious = result.getRecordValues(j);
            // Keys and request columns must be selected

            Hashtable<Object, Object> hKeysValues = new Hashtable<>();
            List<Object> v = new ArrayList<>(multipleAttr.getCols());
            v.addAll(lK);

            boolean needQuery = false;
            // Since 5.2066EN-0.4
            for (int k = 0; k < lPK.size(); k++) {
                Object oKey = lPK.get(k);
                if (hPrevious.containsKey(oKey)) {
                    hKeysValues.put(lPCds.get(k), hPrevious.get(oKey));
                    needQuery = true;
                }
            }
            //
            if (needQuery) {
                res = this.invokeQuery(applicationContext, entityName, hKeysValues, v);
            } else {
                res = null;
            }

            if ((res != null) && (res.calculateRecordNumber() > 0)) {
                Hashtable<Object, Object> h = res.getRecordValues(0);
                for (int k = 0; k < lK.size(); k++) {
                    h.put(lCd.get(k), h.get(lK.get(k)));
                }
                MultipleValue vCR = new MultipleValue(h);
                vResults.add(vCR);
            } else {
                vResults.add(null);
            }
        }
        result.put(multipleAttr, vResults);
    }

    @Override
    public EntityResult processInsertAttribute(MultipleReferenceDataFieldAttribute attribute,
            InsertOperation insertOperation, Map<?, ?> generatedKeysInParentEntity,
            Map<?, ?> attributesValuesInsertedInParentEntity, ApplicationContext applicationContext) {
        return null;
    }

    @Override
    public EntityResult processUpdateAttribute(MultipleReferenceDataFieldAttribute attribute,
            UpdateOperation updateOperation, Map<?, ?> generatedValuesInParentEntity,
            Map<?, ?> filterInParentEntity, ApplicationContext applicationContext) {
        return null;
    }

    @Override
    public EntityResult processDeleteAttribute(MultipleReferenceDataFieldAttribute attribute,
            DeleteOperation deleteOperation, Map<?, ?> generatedValuesInParentEntity,
            Map<?, ?> filterInParentEntity, ApplicationContext applicationContext) {
        return null;
    }

}
