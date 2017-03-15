package com.ontimize.jee.server.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ontimize.db.EntityResult;
import com.ontimize.db.NullValue;
import com.ontimize.gui.SearchValue;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.One2OneDaoHelper.One2OneType;
import com.ontimize.jee.server.dao.One2OneDaoHelper.OneToOneSubDao;

@Component
@Lazy(true)
public class MassiveModificationHelper implements ApplicationContextAware, IMassiveModificationHelper {

	public static final String		MASSIVE_MODIFICATION_UNIQUE_IDENTIFIER	= "MASSIVE_MODIFICATION_UNIQUE_IDENTIFIER";

	/** The application context. */
	protected ApplicationContext	applicationContext;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext (org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * Gets the application context.
	 *
	 * @return the application context
	 */
	public ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

	@Override
	public EntityResult query(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn, Map<?, ?> keysValues, List<?> attributes) {
		return this.query(daoHelper, dao, pkColumn, keysValues, attributes, "default");
	}

	@Override
	public EntityResult query(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn, Map<?, ?> keysValues, List<?> attributes, String queryId) {
		if (keysValues.containsKey(pkColumn) && (keysValues.get(pkColumn) instanceof int[])) {
			EntityResult query = this.getQuery(daoHelper, dao, pkColumn, keysValues, attributes, queryId);
			if (query.calculateRecordNumber() > 0) {
				return this.compareData(pkColumn, keysValues, attributes, query);
			}
		}
		return daoHelper.query(dao, keysValues, attributes, queryId);
	}

	public EntityResult compareData(String pkColumn, Map<?, ?> keysValues, List<?> attributes, EntityResult query) {
		EntityResult resFinal = new EntityResult(query.getRecordValues(0));
		attributes.remove(pkColumn);
		attributes.remove(MassiveModificationHelper.MASSIVE_MODIFICATION_UNIQUE_IDENTIFIER);
		for (Object attribute : attributes) {
			Vector<Object> vValues = (Vector<Object>) query.get(attribute);
			boolean areEquals = true;
			if (vValues != null) {
				Object lastValue = vValues.get(0);
				for (Object value : vValues) {
					if (!this.safeIsEquals(value, lastValue)) {
						areEquals = false;
						break;
					} else {
						lastValue = value;
					}
				}
				this.safePut(resFinal, attribute, areEquals ? lastValue : new NullValue());
				// FIXME debug mode
				this.safePut(resFinal, attribute + "_INFO", vValues.toString());
			}
		}
		resFinal.put(pkColumn, keysValues.get(pkColumn));
		resFinal.put(MassiveModificationHelper.MASSIVE_MODIFICATION_UNIQUE_IDENTIFIER, resFinal.clone());
		return resFinal;
	}

	@Override
	public EntityResult update(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn, Map<?, ?> keysValues, Map<?, ?> attributesValues)
	        throws OntimizeJEERuntimeException {
		if (keysValues.containsKey(pkColumn) && (keysValues.get(pkColumn) instanceof int[])) {
			int[] ids = (int[]) keysValues.get(pkColumn);
			for (int id : ids) {
				HashMap<String, Object> keys = new HashMap<>();
				keys.put(pkColumn, id);
				daoHelper.update(dao, attributesValues, keys);
			}
			return new EntityResult();
		}
		return daoHelper.update(dao, attributesValues, keysValues);
	}

	@Override
	public EntityResult update(DefaultOntimizeDaoHelper daoHelper, One2OneDaoHelper one2oneHelper, IOntimizeDaoSupport mainDao, String pkColumn, List<OneToOneSubDao> secondaryDaos,
	        Map<?, ?> attributesValues, Map<?, ?> keysValues, One2OneType type) throws OntimizeJEERuntimeException {
		if (keysValues.containsKey(pkColumn) && (keysValues.get(pkColumn) instanceof int[])) {
			int[] ids = (int[]) keysValues.get(pkColumn);
			for (int id : ids) {
				HashMap<Object, Object> keys = new HashMap<>();
				keys.put(pkColumn, id);
				one2oneHelper.update(daoHelper, mainDao, secondaryDaos, attributesValues, keys, type);
			}
			return new EntityResult();
		}
		return one2oneHelper.update(daoHelper, mainDao, secondaryDaos, attributesValues, keysValues, type);
	}

	public EntityResult getQuery(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn, Map<?, ?> keysValues, List<?> attributes) {
		return this.getQuery(daoHelper, dao, pkColumn, keysValues, attributes, "default");
	}

	public EntityResult getQuery(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String pkColumn, Map<?, ?> keysValues, List<?> attributes, String queryId) {
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

	protected <T, Q> boolean safePut(Map<T, Q> map, T key, Q value) {
		if ((map != null) && (key != null) && (value != null)) {
			map.put(key, value);
			return true;
		}
		return false;
	}

	protected boolean safeIsEquals(final Object oba, final Object obb) {
		boolean equals = true;
		if (oba == null) {
			equals = (obb == null);
		} else {
			equals = oba.equals(obb);
		}
		return equals;
	}

	@Override
	public EntityResult insert(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String vKeysColumn, String pkColumn, Map<?, ?> attributesValues) {
		if (attributesValues.containsKey(vKeysColumn) && (attributesValues.get(vKeysColumn) instanceof int[])) {
			int[] ids = (int[]) attributesValues.get(vKeysColumn);
			Vector pks = new Vector<>();
			for (int id : ids) {
				HashMap<Object, Object> keys = new HashMap<>();
				keys.putAll(attributesValues);
				keys.put(vKeysColumn, id);
				EntityResult update = daoHelper.insert(dao, keys);
				pks.add(update.get(pkColumn));
			}
			Hashtable hash = new Hashtable<>();
			hash.put(pkColumn, pks);
			return new EntityResult(hash);
		}
		return daoHelper.insert(dao, attributesValues);
	}

	public boolean isMassiveModification(Object key, Map<?, ?> keysValues) {
		return keysValues.containsKey(key) && (keysValues.get(key) instanceof int[]);
	}

}
