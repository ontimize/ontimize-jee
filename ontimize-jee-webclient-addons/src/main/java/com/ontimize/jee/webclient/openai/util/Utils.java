package com.ontimize.jee.webclient.openai.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import com.ontimize.jee.webclient.openai.exception.OpenAIClientException;
import com.sun.istack.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.List;

import static com.ontimize.jee.webclient.openai.naming.OpenAINaming.INITIAL_PROMPT_FORMAT;
import static com.ontimize.jee.webclient.openai.naming.OpenAINaming.RETRY_PROMPT_FORMAT;

@Component
public class Utils {

    private Utils() {
        throw new OpenAIClientException("");
    }

    public static String buildPrompt(String userPrompt, String jsonSchema, String processResult,
            List<String> errors) {
        return !ObjectUtils.isNotEmpty(errors)
                ? String.format(INITIAL_PROMPT_FORMAT, userPrompt, jsonSchema)
                : String.format(RETRY_PROMPT_FORMAT, userPrompt, jsonSchema, processResult, errors);
    }

    public static String generateFullSchemaJson(Class<?> dtoClass) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7,
                OptionPreset.PLAIN_JSON);

        configBuilder.with(Option.DEFINITIONS_FOR_ALL_OBJECTS);
        configBuilder.with(Option.FLATTENED_ENUMS);

        configBuilder.forFields().withNullableCheck(fieldScope -> {
            Member rawMember = fieldScope.getRawMember();
            if (rawMember instanceof Field) {
                Field field = (Field) rawMember;
                if (field.isAnnotationPresent(NotNull.class)) {
                    return false;
                }
            }
            return true;
        });

        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(dtoClass);

        return jsonSchema.toPrettyString();
    }
}
