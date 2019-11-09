package com.ontimize.jee.common.callback;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringEscapeUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.jackson.OntimizeMapper;
import com.ontimize.util.Base64Utils;

public class CallbackWrapperMessage {

	private Integer	type;
	private String	subtype;
	private String	message;

	public CallbackWrapperMessage() {
		super();
	}

	public CallbackWrapperMessage(Integer type, String subtype, Object toSerialize) {
		super();
		this.type = type;
		this.subtype = subtype;
		this.setMessage(toSerialize);
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMessage(Object toSerialize) {
		try {
			this.message = new OntimizeMapper().writeValueAsString(toSerialize);
		} catch (JsonProcessingException error) {
			throw new OntimizeJEERuntimeException(error);
		}
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getType() {
		return this.type;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getSubtype() {
		return this.subtype;
	}

	public String getMessage() {
		return this.message;
	}

	public <T> T getMessage(Class<T> cl) {
		// Deberia hacerse en base al tipo
		try {
			String unescapeJson = StringEscapeUtils.unescapeJson(this.message);
			if (unescapeJson.startsWith("\"") && unescapeJson.endsWith("\"")) {
				unescapeJson = unescapeJson.substring(1, unescapeJson.length() - 1);
			}
			return new OntimizeMapper().readValue(unescapeJson.replaceAll("\n", ""), cl);
		} catch (Exception error) {
			throw new OntimizeJEERuntimeException(error);
		}
	}

	public String serialize() {
		try {
			return new String(Base64Utils.encode(new OntimizeMapper().writeValueAsString(this).getBytes(StandardCharsets.ISO_8859_1)));
		} catch (JsonProcessingException error) {
			throw new OntimizeJEERuntimeException(error);
		}
	}

	public static CallbackWrapperMessage deserialize(String message) {
		try {
			String newMessage = Base64Utils.decode(new String(message.getBytes(StandardCharsets.ISO_8859_1)));
			return new OntimizeMapper().readValue(newMessage, CallbackWrapperMessage.class);
		} catch (Exception error) {
			throw new OntimizeJEERuntimeException(error);
		}
	}
}
