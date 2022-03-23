package com.ontimize.jee.common.util.base64;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base64UtilsDecoder implements IBase64Decoder {

	private static final Logger logger = LoggerFactory.getLogger(Base64UtilsDecoder.class);

	private Object decoder;

	private Object sunDeconder;

	public Base64UtilsDecoder() {
		try {
			if (Class.forName("java.util.Base64") != null) {
				Class<?> java64v8 = Class.forName("java.util.Base64");
				Method m = java64v8.getMethod("getDecoder");
				this.decoder = m.invoke(null);
			}
		} catch (Exception e) {
			if (Base64UtilsDecoder.logger.isDebugEnabled()) {
				Base64UtilsDecoder.logger.error("Not java.util.Base64 package detected", e);
			} else {
				Base64UtilsDecoder.logger.info("Not java.util.Base64 package detected");
			}
			try {
				Class<?> sunClass = Class.forName("sun.misc.BASE64Decoder");
				Constructor<?> constructor = sunClass.getConstructor();
				this.sunDeconder = constructor.newInstance();
			} catch (Exception e1) {
				Base64UtilsDecoder.logger.error(null, e1);
			}
		}
	}

	@Override
	public byte[] decodeStringToByteArray(String s) throws Exception {
		if (this.decoder != null) {
			Method m = this.decoder.getClass().getMethod("decode", String.class);
			return (byte[]) m.invoke(this.decoder, s);
		}
		Method m = this.sunDeconder.getClass().getMethod("decodeBuffer", String.class);
		return (byte[]) m.invoke(this.sunDeconder, s);

	}

}
