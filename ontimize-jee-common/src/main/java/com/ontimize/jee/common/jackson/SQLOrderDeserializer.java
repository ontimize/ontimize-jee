package com.ontimize.jee.common.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.ontimize.jee.common.db.SQLStatementBuilder.SQLOrder;

public class SQLOrderDeserializer extends StdDeserializer<SQLOrder> {

    public static final String COLUMN_KEY = "columnName";

    public static final String ASCENDENT_KEY = "ascendent";

    public SQLOrderDeserializer() {
        super(SQLOrder.class);
    }

    @Override
    public SQLOrder deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw JsonMappingException.from(jp,
                    "Current token not START_OBJECT (needed to deserialize), but encountered " + jp.getCurrentToken());
        }

        String column = null;
        boolean ascendent = false;

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String key = jp.getCurrentName();
            jp.nextToken();
            if (SQLOrderDeserializer.COLUMN_KEY.equals(key)) {
                column = jp.getValueAsString();
            } else if (SQLOrderDeserializer.ASCENDENT_KEY.equals(key)) {
                ascendent = jp.getBooleanValue();
            }
        }

        SQLOrder sqlOrder = new SQLOrder(column, ascendent);
        return sqlOrder;
    }

}
