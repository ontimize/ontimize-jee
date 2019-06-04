package com.ontimize.jee.server.soa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ServiceTemplate.
 *
 * @param <T>
 *            the generic type
 */
public abstract class ServiceTemplate<T> {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(ServiceTemplate.class);

	/**
	 * Instantiates a new service template.
	 */
	public ServiceTemplate() {
		super();
	}

	/**
	 * Execute.
	 *
	 * @return the t
	 * @throws ServiceException
	 *             the service exception
	 */
	public T execute() throws ServiceException {
		try {
			return this.doTask();
		} catch (Exception error) {
			ServiceTemplate.logger.error(null, error);
			ServiceExceptionDetails serviceExceptionDetailsArray[] = new ServiceExceptionDetails[1];
			ServiceExceptionDetails serviceExceptionDetails = new ServiceExceptionDetails();
			serviceExceptionDetails.setFaultCode("-1");
			serviceExceptionDetails.setFaultMessage(error.getMessage());
			serviceExceptionDetailsArray[0] = serviceExceptionDetails;
			throw new ServiceException(error.getMessage(), serviceExceptionDetailsArray);
		}
	}

	/**
	 * Do task.
	 *
	 * @return the t
	 * @throws Throwable
	 *             the throwable
	 */
	protected abstract T doTask() throws Exception;

}
