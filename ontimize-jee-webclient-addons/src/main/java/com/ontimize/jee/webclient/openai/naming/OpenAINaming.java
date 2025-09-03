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
            "Analiza la imagen adjunta. %s\n\nDevuelve la información en el siguiente formato JSON:\n%s";
    public static final String RETRY_PROMPT_FORMAT =
            "La respuesta anterior no cumple con el formato esperado. El error fue:\n%s\n\nVuelve a intentarlo. La estructura esperada es:\n%s";
    public static final String OPENAI_API_ERROR = "OpenAI API error: ";
}
