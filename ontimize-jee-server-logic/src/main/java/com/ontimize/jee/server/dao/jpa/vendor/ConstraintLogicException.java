package com.ontimize.jee.server.dao.jpa.vendor;

import org.springframework.dao.DataAccessException;

public class ConstraintLogicException extends DataAccessException {

	public ConstraintLogicException(String msg) {
		super(msg,null);
	}

}
