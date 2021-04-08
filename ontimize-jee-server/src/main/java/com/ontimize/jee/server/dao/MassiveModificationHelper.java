package com.ontimize.jee.server.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.List;

import com.ontimize.dto.EntityResultMapImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ontimize.dto.EntityResult;
import com.ontimize.db.NullValue;
import com.ontimize.gui.SearchValue;
import com.ontimize.gui.field.EntityFunctionAttribute;
import com.ontimize.gui.field.MultipleReferenceDataFieldAttribute;
import com.ontimize.gui.field.MultipleTableAttribute;
import com.ontimize.gui.field.ReferenceFieldAttribute;
import com.ontimize.gui.table.TableAttribute;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.common.tools.MapTools;
import com.ontimize.jee.common.tools.ObjectTools;
import com.ontimize.jee.server.dao.One2OneDaoHelper.One2OneType;
import com.ontimize.jee.server.dao.One2OneDaoHelper.OneToOneSubDao;

@Component
@Lazy(true)
public class MassiveModificationHelper implements ApplicationContextAware, IMassiveModificationHelper {

    /** The application context. */
    protected ApplicationContext applicationContext;

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext
     * (org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Gets the application context.
     * @return the application context
     */
    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    @Override
    public EntityResult query(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn,
            Map<?, ?> keysValues, List<?> attributes) {
        return this.query(daoHelper, dao, pkColumn, keysValues, attributes, "default");
    }

    @Override
    public EntityResult query(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn,
            Map<?, ?> keysValues, List<?> attributes, String queryId) {
        if (keysValues.containsKey(pkColumn) && (keysValues.get(pkColumn) instanceof int[])) {

            // TODO first approximation we obviate TableAttribute
            List<Object> finalAttributes = new ArrayList<>();
            for (Iterator<Object> iter = (Iterator<Object>) attributes.listIterator(); iter.hasNext();) {
                Object attribute = iter.next();
                if (!(attribute instanceof TableAttribute)) {
                    finalAttributes.add(attribute);
                }
            }

            EntityResult query = this.getQuery(daoHelper, dao, pkColumn, keysValues, finalAttributes, queryId);
            if (query.calculateRecordNumber() > 0) {
                return this.compareData(pkColumn, keysValues, finalAttributes, query);
            }
        }
        return daoHelper.query(dao, keysValues, attributes, queryId);
    }

    public EntityResult compareData(String pkColumn, Map<?, ?> keysValues, List<?> attributes, EntityResult query) {
        EntityResult resFinal = new EntityResultMapImpl((HashMap) query.getRecordValues(0));
        EntityResult resMassiveMod = (EntityResultMapImpl) resFinal.clone();
        attributes.remove(pkColumn);
        attributes.remove(IMassiveModificationHelper.MASSIVE_MODIFICATION_UNIQUE_IDENTIFIER);

        List<TableAttribute> tableAttrs = new ArrayList<>();
        for (Object attribute : attributes) {
            if (attribute instanceof TableAttribute) {
                tableAttrs.add((TableAttribute) attribute);
            } else {
                List<Object> values = (List<Object>) query.get(attribute);
                boolean isEquals = true;
                if ((values != null) && !values.isEmpty()) {
                    Object lastValue = values.get(0);
                    for (Object value : values) {
                        try {
                            ObjectTools.isEquals(value, lastValue);
                            lastValue = value;
                        } catch (Exception e) {
                            isEquals = false;
                            break;
                        }
                    }
                    List<? extends Object> value = isEquals ? new ArrayList<>(Arrays.asList(lastValue))
                            : new ArrayList<>(Arrays.asList(new NullValue()));
                    MapTools.safePut((EntityResultMapImpl) resFinal, attribute, value);
                    MapTools.safePut((EntityResultMapImpl) resMassiveMod, attribute, values);// debug mode
                }
            }
        }
        for (TableAttribute tableAttr : tableAttrs) {
            List<EntityResult> vValues = (List<EntityResult>) query.get(tableAttr);
            EntityResult doUnionAll = EntityResultTools.doUnionAll(vValues.toArray(new EntityResult[vValues.size()]));
            EntityResult doRemoveDuplicates = EntityResultTools.doRemoveDuplicates(doUnionAll);
            MapTools.safePut((EntityResultMapImpl) resFinal, tableAttr, doRemoveDuplicates);
        }

        MapTools.safePut((EntityResultMapImpl) resFinal, pkColumn, keysValues.get(pkColumn));
        MapTools.safePut((EntityResultMapImpl) resMassiveMod, pkColumn, keysValues.get(pkColumn));
        MapTools.safePut((EntityResultMapImpl) resFinal,
                IMassiveModificationHelper.MASSIVE_MODIFICATION_UNIQUE_IDENTIFIER, resMassiveMod);
        return resFinal;
    }

    @Override
    public EntityResult update(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn,
            Map<?, ?> attributesValues, Map<?, ?> keysValues)
            throws OntimizeJEERuntimeException {
        if (keysValues.containsKey(pkColumn) && (keysValues.get(pkColumn) instanceof int[])) {
            int[] ids = (int[]) keysValues.get(pkColumn);
            for (int id : ids) {
                HashMap<String, Object> keys = new HashMap<>();
                keys.put(pkColumn, id);
                daoHelper.update(dao, attributesValues, keys);
            }
            return new EntityResultMapImpl();
        }
        return daoHelper.update(dao, attributesValues, keysValues);
    }

    @Override
    public EntityResult update(DefaultOntimizeDaoHelper daoHelper, One2OneDaoHelper one2oneHelper,
            IOntimizeDaoSupport mainDao, String pkColumn, List<OneToOneSubDao> secondaryDaos,
            Map<?, ?> attributesValues, Map<?, ?> keysValues, One2OneType type) throws OntimizeJEERuntimeException {
        if (keysValues.containsKey(pkColumn) && (keysValues.get(pkColumn) instanceof int[])) {
            int[] ids = (int[]) keysValues.get(pkColumn);
            for (int id : ids) {
                HashMap<Object, Object> keys = new HashMap<>();
                keys.put(pkColumn, id);
                one2oneHelper.update(daoHelper, mainDao, secondaryDaos, attributesValues, keys, type);
            }
            return new EntityResultMapImpl();
        }
        return one2oneHelper.update(daoHelper, mainDao, secondaryDaos, attributesValues, keysValues, type);
    }

    public EntityResult getQuery(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn,
            Map<?, ?> keysValues, List<?> attributes) {
        return this.getQuery(daoHelper, dao, pkColumn, keysValues, attributes, "default");
    }

    public EntityResult getQuery(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn,
            Map<?, ?> keysValues, List<?> attributes, String queryId) {
        int[] ids = (int[]) keysValues.get(pkColumn);
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i <= (ids.length - 1); i++) {
            list.add(ids[i]);
        }
        SearchValue searchValue = new SearchValue(SearchValue.IN, list);
        HashMap<String, Object> keys = new HashMap<>();
        keys.put(pkColumn, searchValue);

        return daoHelper.query(dao, keys, attributes, queryId);
    }

    @Override
    public EntityResult insert(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String vKeysColumn,
            String pkColumn, Map<?, ?> attributesValues) {
        if (attributesValues.containsKey(vKeysColumn) && (attributesValues.get(vKeysColumn) instanceof int[])) {
            int[] ids = (int[]) attributesValues.get(vKeysColumn);
            List pks = new ArrayList<>();
            for (int id : ids) {
                HashMap<Object, Object> keys = new HashMap<>();
                keys.putAll(attributesValues);
                keys.put(vKeysColumn, id);
                EntityResult update = daoHelper.insert(dao, keys);
                pks.add(update.get(pkColumn));
            }
            Map hash = new HashMap<>();
            hash.put(pkColumn, pks);
            return new EntityResultMapImpl((HashMap) hash);
        }
        return daoHelper.insert(dao, attributesValues);
    }

    @Override
    public EntityResult insert(DefaultOntimizeDaoHelper daoHelper, One2OneDaoHelper one2oneHelper,
            IOntimizeDaoSupport mainDao, String pkColumn, List<OneToOneSubDao> secondaryDaos,
            Map<?, ?> attributesValues, One2OneType type) throws OntimizeJEERuntimeException {
        if (attributesValues.containsKey(pkColumn) && (attributesValues.get(pkColumn) instanceof int[])) {
            int[] ids = (int[]) attributesValues.get(pkColumn);
            attributesValues.remove(pkColumn);
            for (int id : ids) {
                MapTools.safePut((Map<Object, Object>) attributesValues, pkColumn, id);
                one2oneHelper.insert(daoHelper, mainDao, secondaryDaos, attributesValues, type);
            }
            return new EntityResultMapImpl();
        }
        return one2oneHelper.insert(daoHelper, mainDao, secondaryDaos, attributesValues, type);
    }

    @Override
    public boolean isMassiveModification(Object key, Map<?, ?> keysValues) {
        return (keysValues != null) && keysValues.containsKey(key) && (keysValues.get(key) instanceof int[]);
    }

    public static String getStringAttr(Object attribute) {
        if (attribute instanceof String) {
            return (String) attribute;
        } else if (attribute instanceof ReferenceFieldAttribute) {
            return ((ReferenceFieldAttribute) attribute).getAttr();
        } else if (attribute instanceof TableAttribute) {
            return ((TableAttribute) attribute).getEntity();
        } else if (attribute instanceof MultipleTableAttribute) {
            return ((MultipleTableAttribute) attribute).getAttribute().toString();
        } else if (attribute instanceof MultipleReferenceDataFieldAttribute) {
            return ((MultipleReferenceDataFieldAttribute) attribute).getAttr();
        } else if (attribute instanceof EntityFunctionAttribute) {
            return ((EntityFunctionAttribute) attribute).getAttr();
        }
        return attribute.toString();
    }

}
