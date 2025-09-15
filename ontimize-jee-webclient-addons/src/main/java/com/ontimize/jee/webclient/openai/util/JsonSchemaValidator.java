package com.ontimize.jee.webclient.openai.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class JsonSchemaValidator {
    private static final ObjectMapper LENIENT = new ObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true).configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true).configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true).configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true).configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
    private final ObjectMapper mapper = new ObjectMapper();

    public static String extractRawJson(String content) {
        if (content == null) return null;
        String s = content.replace("\uFEFF", "").replace('\u201C', '"').replace('\u201D', '"').replace('\u2018', '\'').replace('\u2019', '\'').trim();

        if (s.startsWith("```")) {
            int first = s.indexOf('\n');
            int last = s.lastIndexOf("```");
            if (first >= 0 && last > first) {
                s = s.substring(first + 1, last).trim();
            } else {
                s = s.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
            }
        }

        int open = s.indexOf('{');
        int openArr = s.indexOf('[');
        int start = (open == -1) ? openArr : (openArr == -1 ? open : Math.min(open, openArr));
        int end = Math.max(s.lastIndexOf('}'), s.lastIndexOf(']'));
        if (start >= 0 && end > start) {
            String candidate = s.substring(start, end + 1).trim();
            if (candidate.startsWith("{") || candidate.startsWith("[")) {
                return candidate;
            }
        }
        return null;
    }

    public void validate(Object dataObject, String schemaJson) {
        try {
            String raw = (dataObject instanceof String) ? (String) dataObject : mapper.writeValueAsString(dataObject);

            String extracted = extractRawJson(raw);
            if (extracted == null) throw new IllegalArgumentException(OPENAI_API_NO_JSON_ERROR);
            JsonNode node = LENIENT.readTree(extracted);
            String normalized = mapper.writeValueAsString(node);
            JSONObject schemaObj = new JSONObject(schemaJson);
            JSONObject rawSchema = schemaObj.getJSONObject(PROPERTIES);
            JSONObject jsonSchema = new JSONObject(mapper.writeValueAsString(rawSchema));
            Schema schema = SchemaLoader.load(jsonSchema);
            if (normalized.trim().startsWith("{")) {
                schema.validate(new JSONObject(normalized));
            } else {
                schema.validate(new JSONArray(normalized));
            }

        } catch (ValidationException ve) {
            String message = "Error de validación JSON: " + String.join("; ", ve.getAllMessages());
            throw new RuntimeException(message, ve);
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado durante la validación JSON: " + e.getMessage(), e);
        }
    }
}
