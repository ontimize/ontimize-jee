package com.ontimize.jee.common.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ontimize.db.AdvancedEntityResult;
import com.ontimize.db.EntityResult;
import com.ontimize.db.SQLStatementBuilder.SQLOrder;

public class OntimizeMapper extends ObjectMapper {

    public OntimizeMapper() {
        SimpleModule module = new SimpleModule("OntimizeModule", new Version(2, 0, 0, null, null, null));
        module.addSerializer(EntityResult.class, new EntityResultSerializer());
        module.addSerializer(AdvancedEntityResult.class, new AdvancedEntityResultSerializer());
        module.addDeserializer(SQLOrder.class, new SQLOrderDeserializer());
        module.addDeserializer(EntityResult.class, new EntityResultDeserializer());
        this.registerModule(module);
    }

}
