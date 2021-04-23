package com.ontimize.jee.core.common.util.calendar;

public abstract class TimePeriodParserManager {

    private static TimePeriodParser parser = BasicTimePeriodParser.getInstance();

    public static final void setTimePeriodParser(TimePeriodParser p) {
        TimePeriodParserManager.parser = p;
    }

    public static final TimePeriodParser getTimePeriodParser() {
        return TimePeriodParserManager.parser;
    }

}
