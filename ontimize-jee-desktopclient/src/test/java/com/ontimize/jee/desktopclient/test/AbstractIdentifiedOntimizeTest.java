package com.ontimize.jee.desktopclient.test;

import com.ontimize.jee.common.test.AbstractOntimizeTest;

public abstract class AbstractIdentifiedOntimizeTest extends AbstractOntimizeTest {

	@Override
	protected String getServiceBaseUrl() {
		return "http://127.0.0.1:9999/rexunta/services/hessian";
		// return "http://10.7.0.70:8080/gasim/services/hessian";
	}

	@Override
	protected String getUser() {
		return "a";
	}

	@Override
	protected String getPass() {
		return "a";
	}

}
