package com.ontimize.jee.common.jackson;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.ontimize.db.EntityResult;

public class EntityResultSerializer extends StdSerializer<EntityResult> {

	public static final String	CODE_KEY		= "code";
	public static final String	MESSAGE_KEY		= "message";
	public static final String	DATA_KEY		= "data";
	public static final String	SQL_TYPES_KEY	= "sqlTypes";

	public EntityResultSerializer() {
		super(EntityResult.class);
	}

	@Override
	public void serialize(EntityResult value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
		jgen.writeStartObject();
		jgen.writeNumberField(EntityResultSerializer.CODE_KEY, value.getCode());
		jgen.writeStringField(EntityResultSerializer.MESSAGE_KEY, value.getMessage() != null ? value.getMessage() : "");
		jgen.writeFieldName(EntityResultSerializer.DATA_KEY);

		int number = value.calculateRecordNumber();

		if (number != 0) {

			jgen.writeStartArray();

			// EntityResult has values
			for (int i = 0; i < number; i++) {

				Hashtable record = value.getRecordValues(i);
				jgen.writeObject(record);
			}
			jgen.writeEndArray();
		} else {
			// Check if EntityResult is the result of one insertion
			Hashtable<String, Object> data = new Hashtable<>();
			Enumeration<Object> keys = value.keys();

			while (keys.hasMoreElements()) {

				String itemKey = (String) keys.nextElement();

				data.put(itemKey, value.get(itemKey));
			}

			jgen.writeObject(data);
		}

		jgen.writeFieldName(EntityResultSerializer.SQL_TYPES_KEY);
		Hashtable sqlTypes = value.getColumnSQLTypes();
		jgen.writeObject(sqlTypes);
		jgen.writeEndObject();
	}

}
