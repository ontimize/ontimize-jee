package com.ontimize.jee.common.tools.ertools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ertools.MaxAggregateFunction.MaxPartialAggregateValue;

public class MaxAggregateFunction extends AbstractAggregateFunction<MaxPartialAggregateValue> {

    public static class MaxPartialAggregateValue implements IPartialAggregateValue {

        Number value = null;

    }

    public MaxAggregateFunction(String toSumColumnName, String aggregateColumnName) {
        super(toSumColumnName, aggregateColumnName == null ? (toSumColumnName + "_MAX") : aggregateColumnName);
    }

    @Override
    public Map<String, Object> computeAggregatedGroupValue(MaxPartialAggregateValue partialValue) {
        Map<String, Object> res = new HashMap<>();
        res.put(this.getResultColumn(), partialValue == null ? null : partialValue.value);
        return res;
    }

    @Override
    public MaxPartialAggregateValue onNewGroupRecord(MaxPartialAggregateValue partialValue, EntityResult res, int idx) {
        if (partialValue == null) {
            partialValue = new MaxPartialAggregateValue();
        }
        List<?> val = (List<?>) res.get(this.getOpColumn());
        if (val != null) {
            Number nb = (Number) val.get(idx);
            if (nb != null) {
                if (partialValue.value == null) {
                    partialValue.value = nb;
                } else {
                    if (partialValue.value.doubleValue() < nb.doubleValue()) {
                        partialValue.value = nb;
                    }
                }
            }
        }
        return partialValue;
    }

}
