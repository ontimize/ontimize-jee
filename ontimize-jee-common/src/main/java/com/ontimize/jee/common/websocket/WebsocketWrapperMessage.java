package com.ontimize.jee.common.websocket;

import org.apache.commons.lang3.StringEscapeUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.util.Base64Utils;

public class WebsocketWrapperMessage {

	private Integer	type;
	private String	subtype;
	private String	message;

	public WebsocketWrapperMessage() {
		super();
	}

	public WebsocketWrapperMessage(Integer type, String subtype, Object toSerialize) {
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
			this.message = new ObjectMapper().writeValueAsString(toSerialize);
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
			return new ObjectMapper().readValue(unescapeJson, cl);
		} catch (Exception error) {
			throw new OntimizeJEERuntimeException(error);
		}
	}

	public String serialize() {
		try {
			return Base64Utils.encode(new ObjectMapper().writeValueAsString(this));
		} catch (JsonProcessingException error) {
			throw new OntimizeJEERuntimeException(error);
		}
	}

	public static WebsocketWrapperMessage deserialize(String message) {
		try {
			String newMessage = Base64Utils.decode(message);
			return new ObjectMapper().readValue(newMessage, WebsocketWrapperMessage.class);
		} catch (Exception error) {
			throw new OntimizeJEERuntimeException(error);
		}
	}
}
