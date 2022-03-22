package com.ontimize.jee.common.util.calendar;

import java.util.ArrayList;
import java.util.List;

public class AdvancedMultipleTimePeriod implements TimePeriod {

	private List<TimePeriod> timePeriods = new ArrayList<>();

	public AdvancedMultipleTimePeriod() {
	}

	public AdvancedMultipleTimePeriod(List<TimePeriod> periods) {
		this.timePeriods = periods;
	}

	public void addPeriod(TimePeriod period) {
		if (this.timePeriods == null) {
			this.timePeriods = new ArrayList<>();
		}
		if (period instanceof AdvancedMultipleTimePeriod) {
			this.timePeriods.addAll(((AdvancedMultipleTimePeriod) period).getTimePeriods());
		} else {
			this.timePeriods.add(period);
		}
	}

	public List<TimePeriod> getTimePeriods() {
		return this.timePeriods;
	}

	@Override
	public boolean timeIsInPeriod(long time) {
		for (TimePeriod period : this.timePeriods) {
			if (period.timeIsInPeriod(time)) {
				return true;
			}
		}
		return false;
	}

	public String getPeriodString() {
		if ((this.timePeriods != null) && (!this.timePeriods.isEmpty())) {
			StringBuilder sb = new StringBuilder();
			for (TimePeriod element : this.timePeriods) {
				if (element instanceof AdvancedTimePeriod) {
					String periodString = ((AdvancedTimePeriod) element).getPeriodString();
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
