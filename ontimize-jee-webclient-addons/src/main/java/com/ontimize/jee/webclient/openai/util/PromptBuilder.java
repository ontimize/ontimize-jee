package com.ontimize.jee.webclient.openai.util;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildPrompt(String userPrompt, String jsonSchema, String error) {
        if (error == null) {
            return String.format(
                    "Analiza la imagen adjunta. %s\n\nDevuelve la información en el siguiente formato JSON:\n%s",
                    userPrompt,
                    jsonSchema
            );
        } else {
            return String.format(
                    "La respuesta anterior no cumple con el formato esperado. El error fue:\n%s\n\nVuelve a intentarlo. La estructura esperada es:\n%s",
                    error,
                    jsonSchema
            );
        }
    }
}
