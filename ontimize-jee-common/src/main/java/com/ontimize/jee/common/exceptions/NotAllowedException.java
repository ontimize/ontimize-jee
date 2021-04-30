package com.ontimize.jee.common.exceptions;

/**
 * The Class NotAllowedException.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class NotAllowedException extends OntimizeJEERuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -7357600679952019914L;

    /**
     * Instantiates a new not allowed exception.
     */
    public NotAllowedException() {
        super();
    }

    /**
     * Instantiates a new not allowed exception.
     * @param msg the msg
     * @param cause the cause
     */
    public NotAllowedException(final String msg, final Throwable cause) {
        super(msg, null, cause);
    }

    /**
     * Instantiates a new not allowed exception.
     * @param msg the msg
     */
    public NotAllowedException(final String msg) {
        super(msg);
    }

    /**
     * Instantiates a new not allowed exception.
     * @param cause the cause
     */
    public NotAllowedException(final Throwable cause) {
        super(cause);
    }

}
