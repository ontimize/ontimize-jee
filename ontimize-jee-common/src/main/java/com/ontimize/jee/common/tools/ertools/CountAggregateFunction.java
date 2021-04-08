package com.ontimize.jee.common.tools.ertools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.ontimize.dto.EntityResult;
import com.ontimize.jee.common.tools.ertools.CountAggregateFunction.CountPartialAggregateValue;

public class CountAggregateFunction extends AbstractAggregateFunction<CountPartialAggregateValue> {

    public static class CountPartialAggregateValue implements IPartialAggregateValue {

        int count = 0;

    }

    public CountAggregateFunction(String toSumColumnName, String aggregateColumnName) {
        super(toSumColumnName, aggregateColumnName == null ? (toSumColumnName + "_COUNT") : aggregateColumnName);
    }

    @Override
    public Map<String, Object> computeAggregatedGroupValue(CountPartialAggregateValue partialValue) {
        Map<String, Object> res = new HashMap<>();
        res.put(this.getResultColumn(), partialValue == null ? null : partialValue.count);
        return res;
    }

    @Override
    public CountPartialAggregateValue onNewGroupRecord(CountPartialAggregateValue partialValue, EntityResult res,
            int idx) {
        if (partialValue == null) {
            partialValue = new CountPartialAggregateValue();
        }
        List<?> val = (List<?>) res.get(this.getOpColumn());
        if (val != null) {
            Object nb = val.get(idx);
            if (nb != null) {
                partialValue.count++;
            }
        }
        return partialValue;
    }

}
