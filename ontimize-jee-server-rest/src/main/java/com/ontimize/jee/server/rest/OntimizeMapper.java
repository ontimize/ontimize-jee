package com.ontimize.jee.server.rest;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ontimize.db.EntityResult;
import com.ontimize.jee.server.rest.jackson.serialize.EntityResultSerializer;

public class OntimizeMapper extends ObjectMapper {

	public OntimizeMapper() {
		  SimpleModule module = new SimpleModule("OntimizeModule", new Version(2, 0, 0, null, null, null));
		  module.addSerializer(EntityResult.class, new EntityResultSerializer());
		  registerModule(module);
	}
}
