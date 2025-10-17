package com.ontimize.jee.webclient.openai.naming;

public class OpenAINaming {
    public static final String MODEL = "model";
    public static final String MESSAGES = "messages";
    public static final String ROLE = "role";
    public static final String USER = "user";
    public static final String CONTENT = "content";
    public static final String TYPE = "type";
    public static final String TEXT = "text";
    public static final String IMAGE_URL = "image_url";
    public static final String IMAGE_TYPE = "data:image/jpeg;base64,";
    public static final String URL = "url";
    public static final String DETAIL = "detail";
    public static final String HIGH = "high";
    public static final String MAX_TOKENS = "max_tokens";
    public static final String TEMPERATURE = "temperature";
    public static final String COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    public static final String CHOICES = "choices";
    public static final String MESSAGE = "message";
    public static final String INITIAL_PROMPT_FORMAT =
            "Tu tarea es procesar la siguiente imagen y devolver la información estructurada en el formato JSON que " +
                    "se indica más abajo.\n\n" +
                    "=== INSTRUCCIONES DE CONTEXTO ===\n" +
                    "%s\n\n" +
                    "=== ESTRUCTURA ESPERADA ===\n" +
                    "Devuelve únicamente un JSON que siga esta estructura:\n%s\n\n" +
                    "IMPORTANTE:\n" +
                    "No incluyas explicaciones ni comentarios.\n" +
                    "Usa null si no puedes identificar un valor.\n" +
                    "Respeta el tipo de dato especificado: si se espera un número o una fecha, devuélvelo " +
                    "correctamente formateado.\n" +
                    "Asegúrate de que el JSON es válido y parseable.";
    public static final String RETRY_PROMPT_FORMAT =
            "El siguiente JSON no cumple con la estructura esperada ni con las validaciones. Corrige los errores y " +
                    "vuelve a generar solo el JSON corregido.\n\n" +
                    "=== INSTRUCCIONES DE CONTEXTO ===\n" +
                    "%s\n\n" +
                    "=== ESTRUCTURA ESPERADA ===\n" +
                    "%s\n\n" +
                    "=== RESPUESTA ANTERIOR CON ERRORES ===\n" +
                    "%s\n\n" +
                    "=== ERRORES DETECTADOS ===\n" +
                    "%s\n\n" +
                    "Por favor, genera una nueva versión del JSON que sea válida, esté bien formada y siga " +
                    "exactamente la estructura definida.\n\n" +
                    "RECUERDA:\n" +
                    "No incluyas explicaciones ni comentarios.\n" +
                    "Usa null si no puedes identificar un valor.\n" +
                    "Respeta el tipo de dato especificado: si se espera un número o una fecha, devuélvelo " +
                    "correctamente formateado.\n" +
                    "Asegúrate de que el JSON es válido y parseable.";
    public static final String OPENAI_API_ERROR = "OpenAI API error: ";
    public static final String OPENAI_API_NO_JSON_ERROR = "No se encontró JSON en la cadena de entrada";
    public static final String OPENAI_API_SCHEMA_GENERATION_ERROR = "Error generando schema: ";
    public static final String OPENAI_API_SCHEMA_SERIALIZATION_ERROR = "Error serializando schema: ";

    public static final String PROPERTIES = "properties";
}
