package com.ontimize.jee.common.util.calendar;

import com.ontimize.jee.common.util.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.Locale;


public class BasicTimePeriodParser implements TimePeriodParser, TimePeriodOperationParser {

    private static final Logger logger = LoggerFactory.getLogger(BasicTimePeriodParser.class);

    public static boolean DEBUG = false;

    public static final String INTERVAL = "-";

    public static final String WORKING_DAY = "H";

    public static final String ALL = "*";

    public static final String DAY_MONTH_SEPARATOR = "/";

    public static final String START_REFERENCE = "@";

    public static final int ALL_INT = -1;

    // Shared instance
    private static TimePeriodParser parser = new BasicTimePeriodParser();

    protected Map alias = null;

    protected BasicTimePeriodParser() {
    }

    public static TimePeriodParser getInstance() {
        return BasicTimePeriodParser.parser;
    }

    @Override
    public void setPeriodAlias(Map alias) {
        this.alias = alias;
    }

    protected String getEquivalentPeriod(String periodDef) throws Exception {
        if (this.alias == null) {
            BasicTimePeriodParser.logger
                .debug("It has tried to parse an alias file, but alias file reference is null.");
            throw new Exception("It has tried to parse an alias file, but alias file reference is null.");
        }
        Object equiv = this.alias.get(periodDef.substring(1));
        if (equiv == null) {
            BasicTimePeriodParser.logger.debug("Error parsing alias, equivalence missing.");
            throw new Exception("Error parsing alias, equivalence missing.");
        }
        // Now replace
        if (equiv.toString().startsWith("@")) {
            BasicTimePeriodParser.logger.debug(
                    "It has tried to parse an alias file. Equivalence begins to @ character (cyclic call). This could provoke a stack overflow error");
            throw new Exception(
                    "It has tried to parse an alias file. Equivalence begins to @ character (cyclic call). This could provoke a stack overflow error");
        }
        return equiv.toString();
    }

    @Override
    public TimePeriod parse(String periodDef, Locale locale, String businessCalendarProperties) throws Exception {
        String periodDefinition = periodDef;

        if (BasicTimePeriodParser.DEBUG) {
            BasicTimePeriodParser.logger.debug("TimePeriodParser: Parsing: " + periodDef);
        }
        if ((periodDef != null) && periodDef.startsWith(BasicTimePeriodParser.START_REFERENCE)) {
            // It is an equivalence
            // We need to implement it
            periodDefinition = this.getEquivalentPeriod(periodDefinition);
        }

        TimePeriod timePeriod = this.parseAdvancedPeriod(periodDefinition, locale, businessCalendarProperties);

        // This was the previous method to create the Time Period object
        // timePeriod = parseSimplePeriod(periodDefinition, locale,
        // businessCalendarProperties);
        return timePeriod;

    }

    protected TimePeriod parseAdvancedPeriod(String periodDefinition, Locale locale, String businessCalendarProperties)
            throws Exception {
        if (periodDefinition != null) {
            List tokensAt = ParseTools.getTokensAt(periodDefinition, ";");
            if (tokensAt.size() == 1) {
                AdvancedTimePeriod atp = new AdvancedTimePeriod(periodDefinition, locale, businessCalendarProperties);
                return atp;
            } else {
                AdvancedMultipleTimePeriod amtp = new AdvancedMultipleTimePeriod();
                for (int i = 0; i < tokensAt.size(); i++) {
                    AdvancedTimePeriod period = new AdvancedTimePeriod((String) tokensAt.get(i), locale,
                            businessCalendarProperties);
                    amtp.addPeriod(period);
                }
                return amtp;
            }
        }
        return null;
    }

    protected TimePeriod parseSimplePeriod(String periodDefinition, Locale locale, String businessCalendarProperties)
            throws Exception {
        int periodSeparatorIndex = periodDefinition.indexOf(BasicTimePeriodParser.INTERVAL);

        if (periodSeparatorIndex < 0) {
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger.debug("TimePeriodParser: Found: " + BasicTimePeriodParser.INTERVAL);
            }
            // It must be a month day
            if (periodDefinition.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR) < 0) {
                throw new IllegalArgumentException("Incorrect format: character / not found");
            }
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger
                    .debug("TimePeriodParser: Found: " + BasicTimePeriodParser.DAY_MONTH_SEPARATOR);
            }
            String sDay = periodDefinition.substring(0,
                    periodDefinition.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR));
            String sMonth = periodDefinition
                .substring(periodDefinition.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR) + 1);
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger.debug("TimePeriodParser: Day: " + sDay);
            }
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger.debug("TimePeriodParser: Month: " + sMonth);
            }
            if (sDay.indexOf(BasicTimePeriodParser.WORKING_DAY) >= 0) {
                if (BasicTimePeriodParser.DEBUG) {
                    BasicTimePeriodParser.logger.debug("TimePeriodParser: Found: " + BasicTimePeriodParser.WORKING_DAY);
                }
                // Working day
                int month = this.getMonth(sMonth);
                if (month == BasicTimePeriodParser.ALL_INT) {
                    if (BasicTimePeriodParser.DEBUG) {
                        BasicTimePeriodParser.logger.debug("TimePeriodParser: Found: " + BasicTimePeriodParser.ALL);
                    }
                    int day = this.getDay(sDay.substring(0, sDay.indexOf(BasicTimePeriodParser.WORKING_DAY)));
                    return new WorkingDayOfMonth(day, locale, businessCalendarProperties);
                } else {
                    int day = this.getDay(sDay.substring(0, sDay.indexOf(BasicTimePeriodParser.WORKING_DAY)));
                    return new WorkingDayInMonth(day, month, locale, businessCalendarProperties);
                }
            } else {
                // Normal day
                int month = this.getMonth(sMonth);
                if (month == BasicTimePeriodParser.ALL_INT) {
                    if (BasicTimePeriodParser.DEBUG) {
                        BasicTimePeriodParser.logger.debug("TimePeriodParser: Found: " + BasicTimePeriodParser.ALL);
                    }
                    int day = this.getDay(sDay);
                    return new DayOfMonth(day, locale);
                } else {
                    int day = this.getDay(sDay);
                    return new DayInMonth(day, month, locale);
                }
            }
        } else {
            // Is a period
            if (periodDefinition.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR) < 0) {
                BasicTimePeriodParser.logger.debug(periodDefinition);
                throw new IllegalArgumentException("Format error: character '/' not found");
            }
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger
                    .debug("TimePeriodParser: Found: " + BasicTimePeriodParser.DAY_MONTH_SEPARATOR);
            }
            if (periodDefinition.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR) < periodSeparatorIndex) {
                BasicTimePeriodParser.logger.debug(periodDefinition);
                throw new IllegalArgumentException("Format error: character '-' must go before '/'");
            }
            String sDay = periodDefinition.substring(0,
                    periodDefinition.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR));
            String sMonth = periodDefinition
                .substring(periodDefinition.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR) + 1);
            String strFistDay = sDay.substring(0, sDay.indexOf(BasicTimePeriodParser.INTERVAL));
            String strSecondDay = sDay.substring(sDay.indexOf(BasicTimePeriodParser.INTERVAL) + 1);
            // Parse the month
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger.debug("TimePeriodParser: Day: " + sDay);
            }
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger.debug("TimePeriodParser: Month: " + sMonth);
            }
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger.debug("TimePeriodParser: Fist day: " + strFistDay);
            }
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger.debug("TimePeriodParser: Second day: " + strSecondDay);
            }
            int month = this.getMonth(sMonth);

            // Now the days
            // It must be a month day
            int fistDay = 0;
            int secondDay = 0;

            if (strFistDay.indexOf(BasicTimePeriodParser.WORKING_DAY) >= 0) {
                if (BasicTimePeriodParser.DEBUG) {
                    BasicTimePeriodParser.logger
                        .debug("TimePeriodParser: Found in start period: " + BasicTimePeriodParser.WORKING_DAY);
                }
                // Working day
                fistDay = this.getDay(strFistDay.substring(0, strFistDay.indexOf(BasicTimePeriodParser.WORKING_DAY)));
                if (strSecondDay.indexOf(BasicTimePeriodParser.WORKING_DAY) < 0) {
                    throw new IllegalArgumentException(
                            "Format error: - both days must be the same, working days or holidays");
                }
                if (BasicTimePeriodParser.DEBUG) {
                    BasicTimePeriodParser.logger
                        .debug("TimePeriodParser: Found in end period: " + BasicTimePeriodParser.WORKING_DAY);
                }
                secondDay = this
                    .getDay(strSecondDay.substring(0, strSecondDay.indexOf(BasicTimePeriodParser.WORKING_DAY)));
                if (month == BasicTimePeriodParser.ALL_INT) {
                    if (BasicTimePeriodParser.DEBUG) {
                        BasicTimePeriodParser.logger.debug("TimePeriodParser: Found: " + BasicTimePeriodParser.ALL);
                    }
                    return new WorkingDayIntervalOfMonth(fistDay, secondDay, locale, businessCalendarProperties);
                } else {
                    BasicTimePeriodParser.logger.debug(periodDefinition);
                    throw new IllegalArgumentException("Format error: - Interval in a month are not allowed by now");
                }
            } else {
                // Normal day
                fistDay = this.getDay(strFistDay);
                if (strSecondDay.indexOf(BasicTimePeriodParser.WORKING_DAY) >= 0) {
                    throw new IllegalArgumentException(
                            "Format error: - both days must be the same, working days or holidays");
                }
                secondDay = this.getDay(strSecondDay);
                if (month == BasicTimePeriodParser.ALL_INT) {
                    if (BasicTimePeriodParser.DEBUG) {
                        BasicTimePeriodParser.logger.debug("TimePeriodParser: Found: " + BasicTimePeriodParser.ALL);
                    }
                    return new DayIntervalOfMonth(fistDay, secondDay, locale);
                } else {
                    BasicTimePeriodParser.logger.debug(periodDefinition);
                    throw new IllegalArgumentException("Format error: - Interval in a month are not allowed by now");
                }
            }
        }
    }

    protected int getDay(String s) throws Exception {
        if (BasicTimePeriodParser.DEBUG) {
            BasicTimePeriodParser.logger.debug("TimePeriodParser: Getting Day: " + s);
        }
        if (s.indexOf(BasicTimePeriodParser.INTERVAL) >= 0) {
            throw new IllegalArgumentException("Method getDay() can not be apply to an interval");
        }
        if (BasicTimePeriodParser.DEBUG) {
            BasicTimePeriodParser.logger
                .debug("TimePeriodParser: Getting Day: Correct: Not Found : " + BasicTimePeriodParser.INTERVAL);
        }
        if (s.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR) < 0) {
            return Integer.parseInt(s);
        } else {
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger
                    .debug("TimePeriodParser: Getting Day: Found : " + BasicTimePeriodParser.DAY_MONTH_SEPARATOR);
            }
            String sDay = s.substring(0, s.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR));
            return Integer.parseInt(sDay);
        }
    }

    public int getMonth(String s) throws Exception {
        if (BasicTimePeriodParser.DEBUG) {
            BasicTimePeriodParser.logger.debug("TimePeriodParser: Getting Month: " + s);
        }
        if (s.indexOf(BasicTimePeriodParser.INTERVAL) >= 0) {
            throw new IllegalArgumentException("Method getDay() can not be apply to an interval");
        }
        if (BasicTimePeriodParser.DEBUG) {
            BasicTimePeriodParser.logger
                .debug("TimePeriodParser: Correct: Not Found : " + BasicTimePeriodParser.INTERVAL);
        }
        if (s.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR) < 0) {
            if (s.equals(BasicTimePeriodParser.ALL)) {
                if (BasicTimePeriodParser.DEBUG) {
                    BasicTimePeriodParser.logger
                        .debug("TimePeriodParser: Getting Month: Found : " + BasicTimePeriodParser.ALL);
                }
                return BasicTimePeriodParser.ALL_INT;
            }
            return Integer.parseInt(s) - 1;
        } else {
            if (BasicTimePeriodParser.DEBUG) {
                BasicTimePeriodParser.logger
                    .debug("TimePeriodParser: Getting Month: Found : " + BasicTimePeriodParser.DAY_MONTH_SEPARATOR);
            }
            String sMonth = s.substring(s.indexOf(BasicTimePeriodParser.DAY_MONTH_SEPARATOR) + 1);
            if (sMonth.equals(BasicTimePeriodParser.ALL)) {
                return BasicTimePeriodParser.ALL_INT;
            }
            return Integer.parseInt(sMonth) - 1;
        }
    }

    protected AdvancedMultipleTimePeriod createMultiTimePeriod(TimePeriod p1) throws Exception {
        if (p1 instanceof AdvancedMultipleTimePeriod) {
            return (AdvancedMultipleTimePeriod) p1;
        } else {
            AdvancedMultipleTimePeriod amtp = new AdvancedMultipleTimePeriod();
            if (p1 instanceof AdvancedTimePeriod) {
                amtp.addPeriod(p1);
                return amtp;
            } else {
                throw new Exception(this.getClass().getName() + ": Unknow period class " + p1.getClass().getName());
            }
        }
    }

    @Override
    public String getPeriodString(TimePeriod period) {
        if (period != null) {
            if (period instanceof AdvancedTimePeriod) {
                return ((AdvancedTimePeriod) period).getPeriodString();
            } else if (period instanceof AdvancedMultipleTimePeriod) {
                return ((AdvancedMultipleTimePeriod) period).getPeriodString();
            }
        }
        return null;
    }

    @Override
    public TimePeriod getUnionPeriod(TimePeriod p1, TimePeriod p2) throws Exception {
        AdvancedMultipleTimePeriod multiTimePeriod = this.createMultiTimePeriod(p1);
        multiTimePeriod.addPeriod(p2);
        return multiTimePeriod;
    }

    @Override
    public TimePeriod getCommonPeriod(TimePeriod p1, TimePeriod p2) throws Exception {
        AdvancedMultipleTimePeriod amtp1 = this.createMultiTimePeriod(p1);
        AdvancedMultipleTimePeriod amtp2 = this.createMultiTimePeriod(p2);

        // The common period between two periods are the union of the common
        // period in
        // each sub period
        // This is if Period1 = PA U PB and Period2 = PC U PD then
        // Result = (PA JOIN PC) U (PA JOIN PD) U (PB JOIN PC) U (PB JOIN PD)

        List firstList = amtp1.getTimePeriods();
        List secondList = amtp2.getTimePeriods();
        AdvancedMultipleTimePeriod result = new AdvancedMultipleTimePeriod();
        for (int i = 0; i < firstList.size(); i++) {
            TimePeriod o1 = (TimePeriod) firstList.get(i);
            if (!(o1 instanceof AdvancedTimePeriod)) {
                throw new Exception(this.getClass().getName() + ": only can join objects from "
                        + AdvancedTimePeriod.class.getName());
            }
            for (int k = 0; k < secondList.size(); k++) {
                TimePeriod o2 = (TimePeriod) secondList.get(k);
                if (!(o2 instanceof AdvancedTimePeriod)) {
                    throw new Exception(this.getClass().getName() + ": only can join objects from "
                            + AdvancedTimePeriod.class.getName());
                }
                AdvancedTimePeriod commonAdvancedPeriod = this.getCommonAdvancedPeriod((AdvancedTimePeriod) o1,
                        (AdvancedTimePeriod) o2);
                if (commonAdvancedPeriod != null) {
                    result.addPeriod(commonAdvancedPeriod);
                }
            }
        }
        if ((result.getTimePeriods() != null) && (result.getTimePeriods().size() > 0)) {
            return result;
        }
        return null;

    }

    protected AdvancedTimePeriod getCommonAdvancedPeriod(AdvancedTimePeriod p1, AdvancedTimePeriod p2) {
        int[] months = this.getCommonInterval(p1.getStartMonth(), p1.getEndMonth(), p2.getStartMonth(),
                p2.getEndMonth());
        if (months != null) {
            // Among the days we have to take in account if they are normal or
            // working days
            int[] days = null;
            boolean isWorkingPeriod = false;
            if (p1.isWorkingPeriod() == p2.isWorkingPeriod) {
                days = this.getCommonInterval(p1.getStartDay(), p1.getEndDay(), p2.getStartDay(), p2.getEndDay());
                isWorkingPeriod = p1.isWorkingPeriod();
            } else {
                // One of the periods are normal days and the other working days
                // then the result is working days too
                int startDay1 = p1.getStartDay();
                int endDay1 = p1.getEndDay();
                int startDay2 = p2.getStartDay();
                int endDay2 = p2.getEndDay();

                // TODO maybe this can fail if the period is defined in some
                // months
                // with different working days
                if (!p1.isWorkingPeriod()) {
                    // We have to change all days into working days and not this
                    BusinessCalendar businessCalendar1 = p1.getBusinessCalendar();
                    startDay1 = businessCalendar1.dayOfMonthToWorkingDayOfMonth(startDay1);
                    endDay1 = businessCalendar1.dayOfMonthToWorkingDayOfMonth(endDay1);
                } else if (!p2.isWorkingPeriod()) {
                    // We have to change all days into working days and not this
                    BusinessCalendar businessCalendar2 = p2.getBusinessCalendar();
                    startDay2 = businessCalendar2.dayOfMonthToWorkingDayOfMonth(startDay2);
                    endDay2 = businessCalendar2.dayOfMonthToWorkingDayOfMonth(endDay2);
                }

                days = this.getCommonInterval(startDay1, endDay1, startDay2, endDay2);
                isWorkingPeriod = true;

                days = null;
            }
            if (days != null) {
                AdvancedTimePeriod timePeriod = new AdvancedTimePeriod(days[0], days[1], months[0], months[1],
                        isWorkingPeriod, p1.getLocale(), p1.getBusinessCalendar());
                return timePeriod;
            }
        }
        return null;

    }

    protected int[] getCommonInterval(int start1, int end1, int start2, int end2) {
        int startResult = Math.max(start1, start2);

        int endResult = -1;
        if ((end1 != -1) && (end2 != -1)) {
            endResult = Math.min(end1, end2);
        } else if (end1 == -1) {
            endResult = end2;
        } else {
            endResult = end1;
        }

        if (endResult >= startResult) {
            return new int[] { startResult, endResult };
        } else {
            return null;
        }
    }

}
