package com.ontimize.jee.common.tools.ertools;

import java.util.Arrays;
import java.util.List;

public  abstract class AbstractAggregateFunction<T extends IPartialAggregateValue> implements IAggregateFunction<T> {

	private final String	opColumn;
	private final String	resultColumn;

	public AbstractAggregateFunction(String opColumnName, String resultColumnName) {
		super();
		this.opColumn = opColumnName;
		this.resultColumn = resultColumnName;
	}

	public String getOpColumn() {
		return this.opColumn;
	}

	public String getResultColumn() {
		return this.resultColumn;
	}

	@Override
	public List<String> getAggregatedColumnNames() {
		return Arrays.asList(new String[] { this.resultColumn });
	}

}