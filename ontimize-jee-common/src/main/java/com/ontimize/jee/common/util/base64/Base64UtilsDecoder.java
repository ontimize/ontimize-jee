package com.ontimize.jee.common.util.base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Base64UtilsDecoder implements IBase64Decoder {

    private static final Logger logger = LoggerFactory.getLogger(Base64UtilsDecoder.class);

    protected static Object decoder;

    protected static Object sunDeconder;


    public Base64UtilsDecoder() {
        try {
            if (Class.forName("java.util.Base64") != null) {
                Class java64v8 = Class.forName("java.util.Base64");
                Method m = java64v8.getMethod("getDecoder", new Class[] {});
                Base64UtilsDecoder.decoder = m.invoke(null, new Object[] {});
            }
        } catch (Exception e) {
            if (Base64UtilsDecoder.logger.isDebugEnabled()) {
                Base64UtilsDecoder.logger.error("Not java.util.Base64 package detected", e);
            } else {
                Base64UtilsDecoder.logger.info("Not java.util.Base64 package detected");
            }
            try {
                Class sunClass = Class.forName("sun.misc.BASE64Decoder");
                Constructor constructor = sunClass.getConstructor(new Class[] {});
                Base64UtilsDecoder.sunDeconder = constructor.newInstance(new Object[] {});
            } catch (Exception e1) {
                Base64UtilsDecoder.logger.error(null, e1);
            }
        }
    }

    @Override
    public byte[] decodeStringToByteArray(String s) throws Exception {
        if (Base64UtilsDecoder.decoder != null) {
            Method m = Base64UtilsDecoder.decoder.getClass().getMethod("decode", new Class[] { String.class });
            return (byte[]) m.invoke(Base64UtilsDecoder.decoder, new Object[] { s });
        } else {
            Method m = Base64UtilsDecoder.sunDeconder.getClass()
                .getMethod("decodeBuffer", new Class[] { String.class });
            return (byte[]) m.invoke(Base64UtilsDecoder.sunDeconder, new Object[] { s });
        }

    }

}
