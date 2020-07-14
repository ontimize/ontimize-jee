package com.ontimize.jee.common.tools.ertools;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.tools.ertools.AvgAggregateFunction.AvgPartialAggregateValue;

public class AvgAggregateFunction extends AbstractAggregateFunction<AvgPartialAggregateValue> {

    public static class AvgPartialAggregateValue implements IPartialAggregateValue {

        BigDecimal value = new BigDecimal("0");

        int count = 0;

    }

    public AvgAggregateFunction(String toSumColumnName, String aggregateColumnName) {
        super(toSumColumnName, aggregateColumnName == null ? (toSumColumnName + "_AVG") : aggregateColumnName);
    }

    @Override
    public Map<String, Object> computeAggregatedGroupValue(AvgPartialAggregateValue partialValue) {
        Map<String, Object> res = new HashMap<>();
        res.put(this.getResultColumn(), partialValue == null ? null
                : partialValue.value.divide(BigDecimal.valueOf(partialValue.count), 5, BigDecimal.ROUND_HALF_DOWN));
        return res;
    }

    @Override
    public AvgPartialAggregateValue onNewGroupRecord(AvgPartialAggregateValue partialValue, EntityResult res, int idx) {
        if (partialValue == null) {
            partialValue = new AvgPartialAggregateValue();
        }
        List<?> val = (List<?>) res.get(this.getOpColumn());
        if (val != null) {
            Number nb = (Number) val.get(idx);
            if (nb != null) {
                partialValue.value = partialValue.value.add(new BigDecimal(nb.toString()));
                partialValue.count++;
            }
        }
        return partialValue;
    }

}
