package com.ontimize.jee.server.services.management;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.common.tools.EntityResultTools.GroupType;
import com.ontimize.jee.common.tools.EntityResultTools.GroupTypeOperation;

public class RequestStatisticsContext {

	/** The logger. */
	private final static Logger logger = LoggerFactory.getLogger(RequestStatisticsContext.class);

	private static RequestStatisticsContext instance;

	private EntityResult rs = new EntityResult(
			Arrays.asList("SERVICE_NAME", "METHOD_NAME", "USER_NAME", "METHOD_PARAMS", "EXECUTION_DATE", "EXECUTION_TIME", "SERVICE_EXCEPTION"));

	public EntityResult getContext() {
		return this.rs;
	}

	public void setContext(EntityResult rs) {
		this.rs = rs;
	}

	private RequestStatisticsContext() {}

	/**
	 * Returns the service request.
	 */
	public static RequestStatisticsContext getInstance() {
		if (RequestStatisticsContext.instance == null) {
			RequestStatisticsContext.instance = new RequestStatisticsContext();
		}

		return RequestStatisticsContext.instance;
	}

	public void addRequest(Hashtable<Object, Object> attributesValues) {
		this.rs.addRecord(attributesValues);
	}

	public EntityResult getRequest(Hashtable<String, Object> keysValues, List<String> attributes, boolean statistics) {
		EntityResult dofilter = EntityResultTools.dofilter(this.rs, keysValues);
		if (statistics) {
			try {
				GroupTypeOperation count = new GroupTypeOperation("SERVICE_NAME", "MEASURES", GroupType.COUNT);
				GroupTypeOperation min = new GroupTypeOperation("EXECUTION_TIME", "MIN_TIME", GroupType.MIN);
				GroupTypeOperation max = new GroupTypeOperation("EXECUTION_TIME", "MAX_TIME", GroupType.MAX);
				GroupTypeOperation avg = new GroupTypeOperation("EXECUTION_TIME", "MEAN_TIME", GroupType.AVG);
				dofilter = EntityResultTools.doGroup(dofilter, new String[] { "SERVICE_NAME", "METHOD_NAME" }, count, min, max, avg);
			} catch (Exception ex) {
				RequestStatisticsContext.logger.error(null, ex);
			}
		}
		return dofilter;
	}

}