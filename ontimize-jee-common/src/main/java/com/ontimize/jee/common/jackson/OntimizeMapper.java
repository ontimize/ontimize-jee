package com.ontimize.jee.common.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ontimize.db.EntityResult;

public class OntimizeMapper extends ObjectMapper {

	public OntimizeMapper() {
		SimpleModule module = new SimpleModule("OntimizeModule", new Version(2, 0, 0, null, null, null));
		module.addSerializer(EntityResult.class, new EntityResultSerializer());
		this.registerModule(module);
	}
}
