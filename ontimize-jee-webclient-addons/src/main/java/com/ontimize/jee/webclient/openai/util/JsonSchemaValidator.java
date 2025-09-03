package com.ontimize.jee.webclient.openai.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class JsonSchemaValidator {

    private final ObjectMapper mapper = new ObjectMapper();

    public void validate(Object dataObject, String schemaJson) {
        try {
            JSONObject json = new JSONObject(mapper.writeValueAsString(dataObject));
            JSONObject rawSchema = new JSONObject(schemaJson);

            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(json);

        } catch (ValidationException ve) {
            String message = "Error de validación JSON: " + String.join("; ", ve.getAllMessages());
            throw new RuntimeException(message, ve);
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado durante la validación JSON: " + e.getMessage(), e);
        }
    }
}
