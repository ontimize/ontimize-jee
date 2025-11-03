package com.ontimize.jee.webclient.openai.naming;

import com.ontimize.jee.webclient.openai.exception.OpenAIClientException;

public final class OpenAINaming {

    private OpenAINaming() {
        throw new OpenAIClientException("");
    }

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
            "Your task is to process the following image and return the structured information "
                    + "in the JSON format described below.\n\n"
                    + "=== CONTEXT INSTRUCTIONS ===\n"
                    + "%s\n\n"
                    + "=== EXPECTED STRUCTURE ===\n"
                    + "Return only a JSON that follows this structure:\n%s\n\n"
                    + "IMPORTANT:\n"
                    + "- Do not include explanations or comments.\n"
                    + "- Use null if you cannot identify a value.\n"
                    + "- Respect the specified data type: if a number or date is expected, "
                    + "return it correctly formatted.\n"
                    + "- Make sure the JSON is valid and parseable.";

    public static final String RETRY_PROMPT_FORMAT =
            "The following JSON does not meet the expected structure or validation rules. "
                    + "Please correct the errors and regenerate only the corrected JSON.\n\n"
                    + "=== CONTEXT INSTRUCTIONS ===\n"
                    + "%s\n\n"
                    + "=== EXPECTED STRUCTURE ===\n"
                    + "%s\n\n"
                    + "=== PREVIOUS INVALID RESPONSE ===\n"
                    + "%s\n\n"
                    + "=== DETECTED ERRORS ===\n"
                    + "%s\n\n"
                    + "Please generate a new version of the JSON that is valid, well-formed, "
                    + "and strictly follows the defined structure.\n\n"
                    + "REMEMBER:\n"
                    + "- Do not include explanations or comments.\n"
                    + "- Use null if you cannot identify a value.\n"
                    + "- Respect the specified data type: if a number or date is expected, "
                    + "return it correctly formatted.\n"
                    + "- Make sure the JSON is valid and parseable.";

    public static final String OPENAI_API_ERROR = "OpenAI API error: ";
    public static final String OPENAI_API_NO_JSON_ERROR = "No JSON found in the input string";
    public static final String OPENAI_API_SCHEMA_GENERATION_ERROR = "Error generating schema: ";
    public static final String OPENAI_API_SCHEMA_SERIALIZATION_ERROR = "Error serializing schema: ";

    public static final String PROPERTIES = "properties";
}

