package com.ontimize.jee.common.util.calendar;

public interface TimePeriodOperationParser extends java.io.Serializable {

    public TimePeriod getUnionPeriod(TimePeriod p1, TimePeriod p2) throws Exception;

    public TimePeriod getCommonPeriod(TimePeriod p1, TimePeriod p2) throws Exception;

    public String getPeriodString(TimePeriod unionPeriod);

}
