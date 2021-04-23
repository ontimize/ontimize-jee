package com.ontimize.jee.core.common.util.calendar;

import java.util.Map;
import java.util.Locale;

public interface TimePeriodParser extends java.io.Serializable {

    public TimePeriod parse(String s, Locale l, String businessCalendarProperties) throws Exception;

    public void setPeriodAlias(Map alias);

}
