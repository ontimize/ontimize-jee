package com.ontimize.jee.common.util.base64;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base64UtilsEncoder implements IBase64Encoder {

	private static final Logger logger = LoggerFactory.getLogger(Base64UtilsEncoder.class);

	private Object encoder;

	private Object sunEncoder;

	public Base64UtilsEncoder() {
		try {
			if (Class.forName("java.util.Base64") != null) {
				Class<?> java64v8 = Class.forName("java.util.Base64");
				Method m = java64v8.getMethod("getEncoder");
				this.encoder = m.invoke(null);
			}

		} catch (Exception e) {
			if (Base64UtilsEncoder.logger.isDebugEnabled()) {
				Base64UtilsEncoder.logger.error("Not java.util.Base64 package detected", e);
			} else {
				Base64UtilsEncoder.logger.info("Not java.util.Base64 package detected");
			}
			try {
				Class<?> sunClass = Class.forName("sun.misc.BASE64Encoder");
				Constructor<?> constructor = sunClass.getConstructor();
				this.sunEncoder = constructor.newInstance();
			} catch (Exception e1) {
				Base64UtilsEncoder.logger.error(null, e1);
			}
		}
	}

	@Override
	public String encodeByteArrayToString(byte[] src) throws Exception {
		if (this.encoder != null) {
			Method m = this.encoder.getClass().getMethod("encodeToString", byte[].class);
			return (String) m.invoke(this.encoder, src);
		}
		Method m = this.sunEncoder.getClass().getMethod("encode", byte[].class);
		return (String) m.invoke(this.sunEncoder, src);
	}

}
