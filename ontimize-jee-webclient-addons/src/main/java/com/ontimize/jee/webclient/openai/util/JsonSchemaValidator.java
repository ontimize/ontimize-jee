package com.ontimize.jee.webclient.openai.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontimize.jee.webclient.openai.exception.OpenAIClientException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import static com.ontimize.jee.webclient.openai.naming.OpenAINaming.OPENAI_API_NO_JSON_ERROR;

@Component
public class JsonSchemaValidator {
    private static final ObjectMapper LENIENT = new ObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true)
            .configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
    private final ObjectMapper mapper = new ObjectMapper();

    public static String extractRawJson(String content) {
        if (content == null) {
            return null;
        }
        String s = content.replace("\uFEFF", "")
                .replace('\u201C', '"')
                .replace('\u201D', '"')
                .replace('\u2018', '\'')
                .replace('\u2019', '\'')
                .trim();

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
        int start;
        if (open == -1) {
            start = openArr;
        } else if (openArr == -1) {
            start = open;
        } else {
            start = Math.min(open, openArr);
        }

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
            if (extracted == null) {
                throw new IllegalArgumentException(OPENAI_API_NO_JSON_ERROR);
            }
            JsonNode node = LENIENT.readTree(extracted);
            String normalized = mapper.writeValueAsString(node);
            JSONObject rawSchema = new JSONObject(schemaJson);
            Schema schema = SchemaLoader.load(rawSchema);
            if (normalized.trim().startsWith("{")) {
                schema.validate(new JSONObject(normalized));
            } else {
                schema.validate(new JSONArray(normalized));
            }

        } catch (ValidationException ve) {
            String message = "Error de validación JSON: " + String.join("; ", ve.getAllMessages());
            throw new OpenAIClientException(message, ve);
        } catch (Exception e) {
            throw new OpenAIClientException("Error inesperado durante la validación JSON: " + e.getMessage(), e);
        }
    }
}
