package com.ontimize.jee.common.tools.ertools;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.tools.ertools.SumAggregateFunction.SumPartialAggregateValue;

public class SumAggregateFunction extends AbstractAggregateFunction<SumPartialAggregateValue> {

    public static class SumPartialAggregateValue implements IPartialAggregateValue {

        BigDecimal value = new BigDecimal("0");

    }

    public SumAggregateFunction(String toSumColumnName, String aggregateColumnName) {
        super(toSumColumnName, aggregateColumnName == null ? (toSumColumnName + "_SUM") : aggregateColumnName);
    }

    @Override
    public Map<String, Object> computeAggregatedGroupValue(SumPartialAggregateValue partialValue) {
        Map<String, Object> res = new HashMap<>();
        res.put(this.getResultColumn(), partialValue == null ? null : partialValue.value);
        return res;
    }

    @Override
    public SumPartialAggregateValue onNewGroupRecord(SumPartialAggregateValue partialValue, EntityResult res, int idx) {
        if (partialValue == null) {
            partialValue = new SumPartialAggregateValue();
        }
        List<?> val = (List<?>) res.get(this.getOpColumn());
        if (val != null) {
            Number nb = (Number) val.get(idx);
            if (nb != null) {
                partialValue.value = partialValue.value.add(new BigDecimal(nb.toString()));
            }
        }
        return partialValue;
    }

}
