package com.ontimize.jee.server.services.management;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

import com.ontimize.dto.EntityResultMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.dto.EntityResult;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.common.tools.ertools.AvgAggregateFunction;
import com.ontimize.jee.common.tools.ertools.CountAggregateFunction;
import com.ontimize.jee.common.tools.ertools.IAggregateFunction;
import com.ontimize.jee.common.tools.ertools.MaxAggregateFunction;
import com.ontimize.jee.common.tools.ertools.MinAggregateFunction;

public class RequestStatisticsContext {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(RequestStatisticsContext.class);

    private static RequestStatisticsContext instance;

    private EntityResult rs = new EntityResultMapImpl(
            Arrays.asList("SERVICE_NAME", "METHOD_NAME", "USER_NAME", "METHOD_PARAMS", "EXECUTION_DATE",
                    "EXECUTION_TIME", "SERVICE_EXCEPTION"));

    public EntityResult getContext() {
        return this.rs;
    }

    public void setContext(EntityResult rs) {
        this.rs = rs;
    }

    private RequestStatisticsContext() {
    }

    /**
     * Returns the service request.
     */
    public static RequestStatisticsContext getInstance() {
        if (RequestStatisticsContext.instance == null) {
            RequestStatisticsContext.instance = new RequestStatisticsContext();
        }

        return RequestStatisticsContext.instance;
    }

    public void addRequest(Map<Object, Object> attributesValues) {
        this.rs.addRecord(attributesValues);
    }

    public EntityResult getRequest(Map<String, Object> keysValues, List<String> attributes, boolean statistics) {
        EntityResult dofilter = EntityResultTools.dofilter(this.rs, keysValues);
        if (statistics) {
            try {
                IAggregateFunction count = new CountAggregateFunction("SERVICE_NAME", "MEASURES");
                IAggregateFunction min = new MinAggregateFunction("EXECUTION_TIME", "MIN_TIME");
                IAggregateFunction max = new MaxAggregateFunction("EXECUTION_TIME", "MAX_TIME");
                IAggregateFunction avg = new AvgAggregateFunction("EXECUTION_TIME", "MEAN_TIME");
                dofilter = EntityResultTools.doGroup(dofilter, new String[] { "SERVICE_NAME", "METHOD_NAME" }, count,
                        min, max, avg);
            } catch (Exception ex) {
                RequestStatisticsContext.logger.error(null, ex);
            }
        }
        return dofilter;
    }

}
