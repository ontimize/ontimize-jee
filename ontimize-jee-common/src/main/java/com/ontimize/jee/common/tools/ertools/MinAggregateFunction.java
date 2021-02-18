package com.ontimize.jee.common.tools.ertools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.ontimize.dto.EntityResult;
import com.ontimize.jee.common.tools.ertools.MinAggregateFunction.MinPartialAggregateValue;

public class MinAggregateFunction extends AbstractAggregateFunction<MinPartialAggregateValue> {

    public static class MinPartialAggregateValue implements IPartialAggregateValue {

        Number value = null;

    }

    public MinAggregateFunction(String toSumColumnName, String aggregateColumnName) {
        super(toSumColumnName, aggregateColumnName == null ? (toSumColumnName + "_MIN") : aggregateColumnName);
    }

    @Override
    public Map<String, Object> computeAggregatedGroupValue(MinPartialAggregateValue partialValue) {
        Map<String, Object> res = new HashMap<>();
        res.put(this.getResultColumn(), partialValue == null ? null : partialValue.value);
        return res;
    }

    @Override
    public MinPartialAggregateValue onNewGroupRecord(MinPartialAggregateValue partialValue, EntityResult res, int idx) {
        if (partialValue == null) {
            partialValue = new MinPartialAggregateValue();
        }
        List<?> val = (List<?>) res.get(this.getOpColumn());
        if (val != null) {
            Number nb = (Number) val.get(idx);
            if (nb != null) {
                if (partialValue.value == null) {
                    partialValue.value = nb;
                } else {
                    if (partialValue.value.doubleValue() > nb.doubleValue()) {
                        partialValue.value = nb;
                    }
                }
            }
        }
        return partialValue;
    }

}
