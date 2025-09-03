package com.ontimize.jee.webclient.openai.model;

public enum OpenAiModel {
    GPT_3_5_TURBO("gpt-3.5-turbo"),
    GPT_3_5_TURBO_16K_0613("gpt-3.5-turbo-16k-0613"),
    GPT_4("gpt-4"),
    GPT_4_TURBO("gpt-4-turbo"),
    GPT_4_TURBO_PREVIEW("gpt-4-turbo-preview"),
    GPT_4_1("gpt-4.1"),
    GPT_4_1_MINI("gpt-4.1-mini"),
    GPT_4_1_NANO("gpt-4.1-nano"),
    GPT_4_5_PREVIEW("gpt-4.5-preview"),
    GPT_4O("gpt-4o"),
    GPT_4O_MINI("gpt-4o-mini"),
    GPT_4O_SEARCH_PREVIEW("gpt-4o-search-preview"),
    GPT_IMAGE_1("gpt-image-1"),
    DALL_E_2("dall-e-2"),
    DALL_E_3("dall-e-3"),
    O1("o1"),
    O1_MINI("o1-mini"),
    O1_PREVIEW("o1-preview"),
    O1_PRO("o1-pro"),
    O3("o3"),
    O3_MINI("o3-mini"),
    O3_PRO("o3-pro"),
    O3_DEEP_RESEARCH("o3-deep-research"),
    O4_MINI("o4-mini"),
    O4_MINI_DEEP_RESEARCH("o4-mini-deep-research"),
    GPT_OSS_120B("gpt-oss-120b"),
    GPT_OSS_20B("gpt-oss-20b"),
    TEXT_EMBEDDING_ADA_002("text-embedding-ada-002"),
    TEXT_EMBEDDING_3_SMALL("text-embedding-3-small"),
    TEXT_EMBEDDING_3_LARGE("text-embedding-3-large"),
    TEXT_MODERATION_STABLE("text-moderation-stable"),
    TEXT_MODERATION_LATEST("text-moderation-latest"),
    WHISPER_1("whisper-1");

    private final String value;

    OpenAiModel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
