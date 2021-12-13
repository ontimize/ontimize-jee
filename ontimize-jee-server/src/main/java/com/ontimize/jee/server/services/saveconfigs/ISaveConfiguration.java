package com.ontimize.jee.server.services.saveconfigs;

import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

public interface ISaveConfiguration {

	//ONTIMIZE PREFERENCES
	public EntityResult getConfigPreferences(String user, String configType) throws OntimizeJEERuntimeException;
	public void setConfigPreferences(String user, String configType, Map<String, Object> components) throws OntimizeJEERuntimeException;
}
