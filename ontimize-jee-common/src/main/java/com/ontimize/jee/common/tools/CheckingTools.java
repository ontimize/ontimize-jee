package com.ontimize.jee.common.tools;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

public final class CheckingTools {

	/**
	 * Instantiates a new checking tools.
	 */
	private CheckingTools() {
		super();
	}

	/**
	 * Si el objeto a chequear es nulo lanza una {@link OntimizeJEERuntimeException} con el texto indicado.
	 *
	 * @param toCheck
	 *            the to check
	 * @param exceptionText
	 *            the exception text
	 */
	public static void failIfNull(Object toCheck, String messageFormat, Object... messageParameters) {
		if (toCheck == null) {
			throw new OntimizeJEERuntimeException(((messageParameters == null) || (messageParameters.length == 0)) ? messageFormat : String.format(
					messageFormat, messageParameters));
		}
	}

	/**
	 * Si el objeto a chequear es true lanza una {@link OntimizeJEERuntimeException}
	 * con el texto indicado
	 *
	 * @param toCheck
	 * @param exceptionText
	 */
	public static void failIf(boolean toCheck, String messageFormat, Object... messageParameters) {
		if (toCheck) {
			throw new OntimizeJEERuntimeException(((messageParameters == null) || (messageParameters.length == 0)) ? messageFormat : String.format(
					messageFormat, messageParameters));
		}
	}

	/**
	 * Si el objeto a chequear es true lanza una excepcion de la clase definida. con el texto indicado. El constructor debe tener un parametro string
	 *
	 * @param toCheck
	 * @param exceptionText
	 */
	public static <T extends Throwable> void failIf(boolean toCheck, Class<T> exceptionClass, String messageFormat, Object... messageParameters)
			throws T {
		if (toCheck) {
			String message = ((messageParameters == null) || (messageParameters.length == 0)) ? messageFormat : String.format(messageFormat,
					messageParameters);
			throw ReflectionTools.newInstance(exceptionClass, message);
		}
	}

	/**
	 * Fail if empty string.
	 *
	 * @param toCheck
	 *            the to check
	 * @param messageFormat
	 *            the message format
	 * @param messageParameters
	 *            the message parameters
	 */
	public static void failIfEmptyString(String toCheck, String messageFormat, Object... messageParameters) {
		CheckingTools.failIf(CheckingTools.isStringEmpty(toCheck), messageFormat, messageParameters);
	}

	/**
	 * Checks if is string empty.
	 *
	 * @param str
	 *            the str
	 * @return true, if is string empty
	 */
	public static boolean isStringEmpty(String str) {
		return (str == null) || "".equals(str.trim());
	}

	/**
	 * Método para chequear si un {@link EntityResult} es incorrecto. Permite encapsular la excepción con un mensaje concreto.
	 *
	 * @param res
	 * @throws Exception
	 *             Cuando o es reaultado NULL o no es correcto.
	 */
	public static void checkValidEntityResult(EntityResult rs, String messageFormat) throws OntimizeJEEException {
		try {
			if (rs == null) {
				throw new OntimizeJEEException("EMPTY_RESULT");
			} else if (rs.getCode() == EntityResult.OPERATION_WRONG) {
				throw new OntimizeJEEException(rs.getMessage());
			}
		} catch (OntimizeJEEException ex) {
			if (messageFormat != null) {
				throw new OntimizeJEEException(messageFormat, ex);
			}
			throw ex;
		}
	}

	/**
	 * Método para chequear si un {@link EntityResult} es incorrecto.
	 *
	 * @param res
	 * @throws Exception
	 *             Cuando o es reaultado NULL o no es correcto.
	 */
	public static void checkValidEntityResult(EntityResult res) throws Exception {
		CheckingTools.checkValidEntityResult(res, null);
	}

	/**
	 * Método para chequear si un {@link EntityResult} es incorrecto. Permite encapsular la excepción con un mensaje concreto.
	 *
	 * @param res
	 * @throws Exception
	 *             Cuando o es reaultado NULL o no es correcto.
	 */
	public static void checkValidEntityResult(EntityResult res, String messageFormat, boolean checkSomeRecordRequired, boolean checkOnlyOneRecord) throws Exception {
		CheckingTools.checkValidEntityResult(res, messageFormat);
		try {
			int num = res.calculateRecordNumber();
			if (checkSomeRecordRequired && (num <= 0)) {
				throw new OntimizeJEEException("NOT_AVAILABLE_DATA");
			}
			if (checkOnlyOneRecord && (num != 1)) {
				throw new OntimizeJEEException("NOT_ONLY_ONE_RECORD");
			}

		} catch (Exception ex) {
			if (messageFormat != null) {
				throw new OntimizeJEEException(messageFormat, ex);
			}
			throw ex;
		}
	}
}
