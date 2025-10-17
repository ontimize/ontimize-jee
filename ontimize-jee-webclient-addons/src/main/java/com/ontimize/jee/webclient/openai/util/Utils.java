package com.ontimize.jee.webclient.openai.util;

import com.ontimize.jee.webclient.openai.model.ProcessResult;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ontimize.jee.webclient.openai.naming.OpenAINaming.INITIAL_PROMPT_FORMAT;
import static com.ontimize.jee.webclient.openai.naming.OpenAINaming.RETRY_PROMPT_FORMAT;

@Component
public class Utils {

    public static String buildPrompt(String userPrompt, String jsonSchema, ProcessResult<?> processResult,
            List<String> errors) {
        return processResult == null
                ? String.format(INITIAL_PROMPT_FORMAT, userPrompt, jsonSchema)
                : String.format(RETRY_PROMPT_FORMAT, userPrompt, jsonSchema, processResult, errors);
    }
}
