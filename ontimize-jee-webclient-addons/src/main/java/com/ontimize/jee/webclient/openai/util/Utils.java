package com.ontimize.jee.webclient.openai.util;

import org.springframework.stereotype.Component;

import static com.ontimize.jee.webclient.openai.naming.OpenAINaming.INITIAL_PROMPT_FORMAT;
import static com.ontimize.jee.webclient.openai.naming.OpenAINaming.RETRY_PROMPT_FORMAT;

@Component
public class Utils {

    public static String buildPrompt(String userPrompt, String jsonSchema, String error) {
        return error == null
                ? String.format(INITIAL_PROMPT_FORMAT, userPrompt, jsonSchema)
                : String.format(RETRY_PROMPT_FORMAT, error, jsonSchema);
    }
}
