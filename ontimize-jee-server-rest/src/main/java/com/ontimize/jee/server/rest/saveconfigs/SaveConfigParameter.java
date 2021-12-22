package com.ontimize.jee.server.rest.saveconfigs;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SaveConfigParameter implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement
	protected String user;

	@XmlElement
	protected Map<Object, Object> components;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Map<Object, Object> getComponents() {
		return components;
	}

	public void setComponents(Map<Object, Object> components) {
		this.components = components;
	}
}
