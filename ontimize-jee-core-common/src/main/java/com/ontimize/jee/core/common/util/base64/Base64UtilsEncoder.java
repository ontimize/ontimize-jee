package com.ontimize.jee.core.common.util.base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Base64UtilsEncoder implements IBase64Encoder {

    private static final Logger logger = LoggerFactory.getLogger(Base64UtilsEncoder.class);

    protected static Object encoder;

    protected static Object sunEncoder;

    public Base64UtilsEncoder() {
        try {
            if (Class.forName("java.util.Base64") != null) {
                Class java64v8 = Class.forName("java.util.Base64");
                Method m = java64v8.getMethod("getEncoder", new Class[] {});
                Base64UtilsEncoder.encoder = m.invoke(null, new Object[] {});
            }

        } catch (Exception e) {
            if (Base64UtilsEncoder.logger.isDebugEnabled()) {
                Base64UtilsEncoder.logger.error("Not java.util.Base64 package detected", e);
            } else {
                Base64UtilsEncoder.logger.info("Not java.util.Base64 package detected");
            }
            try {
                Class sunClass = Class.forName("sun.misc.BASE64Encoder");
                Constructor constructor = sunClass.getConstructor(new Class[] {});
                Base64UtilsEncoder.sunEncoder = constructor.newInstance(new Object[] {});
            } catch (Exception e1) {
                Base64UtilsEncoder.logger.error(null, e1);
            }
        }
    }

    @Override
    public String encodeByteArrayToString(byte[] src) throws Exception {
        if (Base64UtilsEncoder.encoder != null) {
            Method m = Base64UtilsEncoder.encoder.getClass().getMethod("encodeToString", new Class[] { byte[].class });
            return (String) m.invoke(Base64UtilsEncoder.encoder, new Object[] { src });
        } else {
            Method m = Base64UtilsEncoder.sunEncoder.getClass().getMethod("encode", new Class[] { byte[].class });
            return (String) m.invoke(Base64UtilsEncoder.sunEncoder, new Object[] { src });
            // return new sun.misc.BASE64Encoder().encode(src);
        }
    }

}
