package com.ontimize.jee.core.common.util.calendar;

import java.util.ArrayList;
import java.util.List;

public class AdvancedMultipleTimePeriod implements TimePeriod {

    protected List timePeriods = new ArrayList();

    public AdvancedMultipleTimePeriod() {
    }

    public AdvancedMultipleTimePeriod(List periods) {
        this.timePeriods = periods;
    }

    public void addPeriod(TimePeriod period) {
        if (this.timePeriods == null) {
            this.timePeriods = new ArrayList();
        }
        if (period instanceof AdvancedMultipleTimePeriod) {
            this.timePeriods.addAll(((AdvancedMultipleTimePeriod) period).getTimePeriods());
        } else {
            this.timePeriods.add(period);
        }
    }

    public List getTimePeriods() {
        return this.timePeriods;
    }

    @Override
    public boolean timeIsInPeriod(long time) {
        for (int i = 0; i < this.timePeriods.size(); i++) {
            if (((TimePeriod) this.timePeriods.get(i)).timeIsInPeriod(time)) {
                return true;
            }
        }
        return false;
    }

    public String getPeriodString() {
        if ((this.timePeriods != null) && (this.timePeriods.size() > 0)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < this.timePeriods.size(); i++) {
                if (this.timePeriods.get(i) instanceof AdvancedTimePeriod) {
                    String periodString = ((AdvancedTimePeriod) this.timePeriods.get(i)).getPeriodString();
                    if (sb.length() > 0) {
                        sb.append(";");
                    }
                    if (periodString != null) {
                        sb.append(periodString);
                    }
                }
            }
            return sb.toString();
        }
        return null;

    }

}
