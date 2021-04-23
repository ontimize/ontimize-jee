package com.ontimize.jee.core.common.util.serializer;

import com.ontimize.jee.core.common.util.serializer.xml.XmlSerializerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializerManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(SerializerManagerFactory.class);

    private static ISerializerManager serializerManager;

    private static int serializerType = 1;

    public static void setSerializerType(int type) {
        SerializerManagerFactory.serializerType = type;
    }

    public static ISerializerManager getSerializerManager() {
        if (SerializerManagerFactory.serializerManager == null) {
            try {
                if (SerializerManagerFactory.serializerType == ISerializerManager.SERIALIZER_XML) {
                    SerializerManagerFactory.serializerManager = new XmlSerializerManager();
                }
            } catch (Exception e) {
                SerializerManagerFactory.logger
                    .debug("Specified serializer is not implemented, using default serializer", e);
            }
            if (SerializerManagerFactory.serializerManager == null) {
                SerializerManagerFactory.serializerManager = new DefaultSerializerManager();
            }
        }
        return SerializerManagerFactory.serializerManager;
    }

    public static ISerializerManager getDefaultSerializerManager() {
        return new DefaultSerializerManager();
    }

}
