package com.ontimize.jee.common.exceptions;



/**
 * The Class PermissionValidationException.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class PermissionValidationException extends OntimizeJEERuntimeException {

    private static final long serialVersionUID = 398376578824219383L;

	public PermissionValidationException() {
		super();
	}

	public PermissionValidationException(String msg, Throwable cause) {
		super(msg, null, cause);
	}

	public PermissionValidationException(String msg) {
		super(msg);
	}

	public PermissionValidationException(Throwable cause) {
		super(cause);
	}



}
