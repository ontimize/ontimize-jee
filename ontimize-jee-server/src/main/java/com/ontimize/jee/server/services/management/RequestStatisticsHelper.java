package com.ontimize.jee.server.services.management;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.List;

import com.ontimize.jee.common.dto.EntityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.jee.common.tools.DateTools;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.common.tools.MapTools;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import com.ontimize.jee.server.services.management.dao.IRequestStatisticsDao;

/**
 * The Class RequestStatisticsHelper.
 */
@Component
@Lazy(value = true)
public class RequestStatisticsHelper {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(RequestStatisticsHelper.class);

    @Autowired
    DefaultOntimizeDaoHelper daoHelper;

    /** The request statistics dao. */
    @Autowired(required = false)
    protected IRequestStatisticsDao requestStatisticsDao;

    public void insertRequestStatistics(String serviceName, String methodName, Object params, String user, Date date,
            long timeExecution, String exception) {
        try {
            // new ScheduledThreadPoolExecutor(corePoolSize)
            Map<Object, Object> attributesValues = new HashMap<>();
            MapTools.safePut(attributesValues, "SERVICE_NAME", serviceName);
            MapTools.safePut(attributesValues, "METHOD_NAME", methodName);
            MapTools.safePut(attributesValues, "USER_NAME", user);
            MapTools.safePut(attributesValues, "EXECUTION_DATE", date);
            MapTools.safePut(attributesValues, "EXECUTION_TIME", timeExecution);
            MapTools.safePut(attributesValues, "SERVICE_EXCEPTION", exception);

            // FIXME: process params
            StringBuilder sbParams = new StringBuilder();
            if (params instanceof Object[]) {
                Object[] values = (Object[]) params;
                if (values.length != 0) {
                    for (Object value : values) {
                        // if (value instanceof List<?>) {
                        // List<?> List = (List<?>) value;
                        // sbParams.append(value.getClass());
                        // for (Object vValue : List) {
                        // sbParams.append(vValue.toString() + (List.indexOf(vValue) != (List.size() - 1) ? "," :
                        // ""));
                        // }
                        // } else {
                        if (value != null) {
                            sbParams.append(value.getClass() + "=" + value.toString() + "\n");
                        }
                        // }
                    }
                }
            }
            MapTools.safePut(attributesValues, "METHOD_PARAMS", sbParams.toString());

            if ((this.daoHelper != null) && (this.requestStatisticsDao != null)) {
                this.daoHelper.insert(this.requestStatisticsDao, attributesValues);
            } else {// It is saved locally
                RequestStatisticsContext context = RequestStatisticsContext.getInstance();
                context.addRequest(attributesValues);
            }
        } catch (Exception ex) {
            RequestStatisticsHelper.logger.error(null, ex);
        }

    }

    public EntityResult queryRequestStatistics() {
        Map<String, Object> keysValues = new HashMap<>();
        List<String> attributes = new ArrayList<>();
        attributes.add("SERVICE_NAME");
        attributes.add("METHOD_NAME");
        attributes.add("MEASURES");
        attributes.add("MIN_TIME");
        attributes.add("MAX_TIME");
        attributes.add("MEAN_TIME");
        EntityResult query;
        if ((this.daoHelper != null) && (this.requestStatisticsDao != null)) {
            query = this.daoHelper.query(this.requestStatisticsDao, keysValues, attributes, "statistics");
        } else {
            RequestStatisticsContext context = RequestStatisticsContext.getInstance();
            query = context.getRequest(keysValues, attributes, true);
        }
        return query;
    }

    public EntityResult queryRequestStatistics(String serviceName, String methodName, Date dateBefore, Date dateAfter) {
        Map<String, Object> keysValues = new HashMap<>();
        keysValues.put("SERVICE_NAME", serviceName);
        keysValues.put("METHOD_NAME", methodName);
        List<String> attributes = new ArrayList<>();
        attributes.add("USER_NAME");
        attributes.add("EXECUTION_DATE");
        attributes.add("EXECUTION_TIME");
        attributes.add("METHOD_PARAMS");
        attributes.add("SERVICE_EXCEPTION");

        Calendar calBefore = Calendar.getInstance();
        if (dateBefore != null) {
            calBefore.setTime(dateBefore);
            calBefore.set(Calendar.HOUR_OF_DAY, 0);
            calBefore.set(Calendar.MINUTE, 0);
            calBefore.set(Calendar.SECOND, 0);
            calBefore.set(Calendar.MILLISECOND, 0);
            dateBefore = calBefore.getTime();
        }
        Calendar calAfter = Calendar.getInstance();
        if (dateAfter != null) {
            calAfter.setTime(dateAfter);
            calAfter.set(Calendar.HOUR_OF_DAY, 23);
            calAfter.set(Calendar.MINUTE, 59);
            calAfter.set(Calendar.SECOND, 59);
            calAfter.set(Calendar.MILLISECOND, 9999);
            dateAfter = calAfter.getTime();
        }
        if ((dateAfter != null) || (dateBefore != null)) {
            keysValues.put("EXECUTION_DATE", DateTools.betweenDatesExpression(dateBefore, dateAfter));
        }

        EntityResult query = null;
        if ((this.daoHelper != null) && (this.requestStatisticsDao != null)) {
            query = this.daoHelper.query(this.requestStatisticsDao, keysValues, attributes);
        } else {
            RequestStatisticsContext context = RequestStatisticsContext.getInstance();
            query = context.getRequest(keysValues, attributes, false);
        }

        return query;
    }

    public EntityResult deleteRequestStatistics(int days) {
        // Build filter
        Map<Object, Object> keysValues = new HashMap<>();// Filter by date
        Calendar calendar = Calendar.getInstance(); // this would default to now
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        keysValues.put("EXECUTION_DATE", new SearchValue(SearchValue.LESS_EQUAL, calendar.getTime()));

        // delete from BD
        if ((this.daoHelper != null) && (this.requestStatisticsDao != null)) {
            List<String> attributes = new ArrayList<>();
            attributes.add("ID_REQUEST_STATISTICS");
            List<Number> keysToDelete = (List<Number>) this.daoHelper
                .query(this.requestStatisticsDao, keysValues, attributes)
                .get("ID_REQUEST_STATISTICS");

            if (keysToDelete != null) {
                for (Number key : keysToDelete) {
                    keysValues.put("ID_REQUEST_STATISTICS", key);
                    this.daoHelper.delete(this.requestStatisticsDao, keysValues);
                }
            }
        }
        // delete form Context(Locally)
        keysValues.clear();
        keysValues.put("EXECUTION_DATE", new SearchValue(SearchValue.MORE_EQUAL, calendar.getTime()));// only we want to
                                                                                                      // save valid
                                                                                                      // values
        RequestStatisticsContext context = RequestStatisticsContext.getInstance();
        EntityResult dofilter = EntityResultTools.dofilter(context.getContext(), keysValues);
        context.setContext(dofilter);
        return dofilter;
    }

}
