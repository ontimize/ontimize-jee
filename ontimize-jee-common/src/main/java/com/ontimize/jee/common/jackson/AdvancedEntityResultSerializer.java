package com.ontimize.jee.common.jackson;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.ontimize.jee.common.db.AdvancedEntityResult;

public class AdvancedEntityResultSerializer extends StdSerializer<AdvancedEntityResult> {

    public static final String CODE_KEY = "code";

    public static final String MESSAGE_KEY = "message";

    public static final String DATA_KEY = "data";

    public static final String SQL_TYPES_KEY = "sqlTypes";

    public static final String TOTAL_RECORD_NUMBER_KEY = "totalQueryRecordsNumber";

    public static final String START_RECORD_INDEX_KEY = "startRecordIndex";

    public AdvancedEntityResultSerializer() {
        super(AdvancedEntityResult.class);
    }

    @Override
    public void serialize(AdvancedEntityResult value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException {
        jgen.writeStartObject();
        jgen.writeNumberField(AdvancedEntityResultSerializer.CODE_KEY, value.getCode());
        jgen.writeStringField(AdvancedEntityResultSerializer.MESSAGE_KEY,
                value.getMessage() != null ? value.getMessage() : "");
        jgen.writeNumberField(AdvancedEntityResultSerializer.TOTAL_RECORD_NUMBER_KEY, value.getTotalRecordCount());
        jgen.writeNumberField(AdvancedEntityResultSerializer.START_RECORD_INDEX_KEY, value.getStartRecordIndex());

        jgen.writeFieldName(AdvancedEntityResultSerializer.DATA_KEY);

        int number = value.calculateRecordNumber();

        jgen.writeStartArray();
        if (number != 0) {
            // EntityResult has values
            for (int i = 0; i < number; i++) {
                Map record = value.getRecordValues(i);
                jgen.writeObject(record);
            }
        }
        jgen.writeEndArray();

        jgen.writeFieldName(AdvancedEntityResultSerializer.SQL_TYPES_KEY);
        Map sqlTypes = value.getColumnSQLTypes();
        jgen.writeObject(sqlTypes);
        jgen.writeEndObject();
    }

}
