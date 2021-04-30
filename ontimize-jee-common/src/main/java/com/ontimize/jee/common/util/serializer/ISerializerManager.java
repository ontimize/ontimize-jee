package com.ontimize.jee.common.util.serializer;

import java.util.Map;

public interface ISerializerManager {

    public static final int SERIALIZER_DEFAULT = 1;

    public static final int SERIALIZER_XML = 2;

    public String serializeMapToString(Map<String, Object> data) throws Exception;

    public Map<String, Object> deserializeStringToMap(String data) throws Exception;

}
