package com.ontimize.jee.server.services.saveconfigs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;

@Service("SaveConfigurationService")
@Lazy
public class SaveConfigurationService implements ISaveConfiguration, InitializingBean {

	@Autowired
	@Qualifier("ConfigsDao")
	private IOntimizeDaoSupport dao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	@Override
	public void afterPropertiesSet() throws Exception {
		CheckingTools.failIfNull(this.dao, "Dao not found");
	}

	@Override
	public EntityResult getConfigurations(String user, String configType) throws OntimizeJEERuntimeException {

		Map<String, Object> keysValues = new HashMap<>();
		keysValues.put(SaveConfigNameConvention.USER, user);
		keysValues.put(SaveConfigNameConvention.TYPE, configType);
		List<String> attributes = new ArrayList<>();
		attributes.add(SaveConfigNameConvention.ID);
		attributes.add(SaveConfigNameConvention.COMPONENTS);

		return this.daoHelper.query(dao, keysValues, attributes);
	}

	@Override
	public void setConfigurations(String user, String configType, Map<String, Object> components)
			throws OntimizeJEERuntimeException {

		EntityResult ePrefs = this.getConfigurations(user, configType);
		
		if (ePrefs.get(SaveConfigNameConvention.COMPONENTS) == null) {
			Map<String, Object> attrValues = new HashMap<>();
			attrValues.put(SaveConfigNameConvention.USER, user);
			attrValues.put(SaveConfigNameConvention.TYPE, configType);
			attrValues.put(SaveConfigNameConvention.COMPONENTS, String.valueOf(components));
			this.daoHelper.insert(dao, attrValues);
		} else {
			Map<String, Object> keysValues = new HashMap<>();
			keysValues.put(SaveConfigNameConvention.ID, ((List)ePrefs.get(SaveConfigNameConvention.ID)).get(0));
			Map<String, Object> attrValues = new HashMap<>();
			attrValues.put(SaveConfigNameConvention.COMPONENTS, String.valueOf(components));
			this.daoHelper.update(dao, attrValues, keysValues);
		}
	}
}
