package com.ontimize.jee.common.jackson;

import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.ontimize.dto.EntityResult;
import com.ontimize.dto.EntityResultMapImpl;
import com.ontimize.jee.common.tools.ParseUtilsExtended;

public class EntityResultDeserializer extends StdDeserializer<EntityResult> {

    public static final String CODE_KEY = "code";

    public static final String MESSAGE_KEY = "message";

    public static final String DATA_KEY = "data";

    public static final String SQL_TYPES_KEY = "sqlTypes";

    protected EntityResultDeserializer() {
        super(EntityResult.class);
    }

    @Override
    public EntityResult deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw JsonMappingException.from(jp,
                    "Current token not START_OBJECT (needed to deserialize), but encountered " + jp.getCurrentToken());
        }

        JsonNode node = jp.getCodec().readTree(jp);

        int code = (Integer) node.get(EntityResultDeserializer.CODE_KEY).numberValue();
        String message = node.get(EntityResultDeserializer.MESSAGE_KEY).asText();

        Map<?, ?> sqlTypes = new HashMap<String, Object>();
        JsonNode sqlTypesNode = node.get(EntityResultDeserializer.SQL_TYPES_KEY);
        if (!sqlTypesNode.isNull()) {
            sqlTypes = this.deserializeSqlTypes((ObjectNode) node.get(EntityResultDeserializer.SQL_TYPES_KEY));
        }

        JsonNode dataNode = node.get(EntityResultDeserializer.DATA_KEY);
        Set<String> columns = new LinkedHashSet<>();
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> rawData = new HashMap<>();
        if (dataNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node.withArray(EntityResultDeserializer.DATA_KEY);
            for (int i = 0; i < arrayNode.size(); i++) {
                Map<String, Object> record = this.deserializeObject((ObjectNode) arrayNode.get(i), sqlTypes);
                columns.addAll(record.keySet());
                records.add(record);
            }
        } else if (dataNode.isObject()) {
            rawData = this.deserializeObject((ObjectNode) dataNode, sqlTypes);
        }

        EntityResult er = new EntityResultMapImpl(Arrays.asList(columns.toArray()));// todo review on new
                                                                                    // implementations
        for (Map<String, Object> record : records) {
            er.addRecord(record);
        }
        er.putAll(rawData);
        er.setCode(code);
        er.setMessage(message);
        er.setColumnSQLTypes((Map) sqlTypes);
        return er;
    }

    public Map<?, ?> deserializeSqlTypes(ObjectNode node) {
        Map<String, Object> result = new HashMap<>();
        Iterator<String> ite = node.fieldNames();
        while (ite.hasNext()) {
            String key = ite.next();
            int value = node.get(key).asInt();
            result.put(key, value);
        }
        return result;
    }

    public Map<String, Object> deserializeObject(ObjectNode node, Map<?, ?> sqlTypes) {
        Map<String, Object> result = new HashMap<>();
        Iterator<String> ite = node.fieldNames();
        while (ite.hasNext()) {
            String key = ite.next();
            JsonNode valueNode = node.get(key);
            int sqlType = ((sqlTypes != null) && sqlTypes.containsKey(key)) ? (Integer) sqlTypes.get(key) : Types.OTHER;
            Object value = this.deserializeValue(valueNode, sqlType);
            result.put(key, value);
        }
        return result;
    }

    public Object deserializeValue(JsonNode node, int sqlType) {
        Object value;
        switch (node.getNodeType()) {
            case STRING:
                value = node.asText();
                break;
            case NUMBER:
                value = node.numberValue();
                break;
            case BOOLEAN:
                value = node.asBoolean();
            default:
                value = node.toString();
                break;
        }
        return ParseUtilsExtended.getValueForSQLType(value, sqlType);
    }

}
