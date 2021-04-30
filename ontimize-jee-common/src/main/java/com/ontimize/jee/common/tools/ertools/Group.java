package com.ontimize.jee.common.tools.ertools;

import com.ontimize.jee.common.dto.EntityResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * The Class Group.
 */
public class Group {

    /** The keys. */
    protected Map<String, Object> keys;

    /** The values. */
    protected Map<IAggregateFunction, IPartialAggregateValue> aggregateTmpValues;

    private final IAggregateFunction[] aggregateFuncitons;

    /**
     * Instantiates a new group.
     * @param keys the keys
     * @param values the values
     */
    public Group(Map<String, Object> keys, IAggregateFunction[] aggregateFuncitons) {
        this.aggregateFuncitons = aggregateFuncitons;
        this.keys = keys;
        this.aggregateTmpValues = new HashMap<>();
    }

    public void onNewGroupRecord(EntityResult er, int i) {
        for (IAggregateFunction aggregateFunction : this.aggregateFuncitons) {
            this.aggregateTmpValues.put(aggregateFunction,
                    aggregateFunction.onNewGroupRecord(this.aggregateTmpValues.get(aggregateFunction), er, i));
        }
    }

    /**
     * Gets the keys.
     * @return the keys
     */
    public Map<String, Object> getKeys() {
        return this.keys;
    }

    public Map<String, Object> getAggregateValues() {
        Map<String, Object> res = new HashMap<>();
        for (IAggregateFunction aggregateFunction : this.aggregateFuncitons) {
            res.putAll(aggregateFunction.computeAggregatedGroupValue(this.aggregateTmpValues.get(aggregateFunction)));
        }
        return res;
    }

    public Collection<String> getAggregatedColumnNames() {
        Collection<String> res = new ArrayList<>();
        for (IAggregateFunction aggregateFunction : this.aggregateFuncitons) {
            res.addAll(aggregateFunction.getAggregatedColumnNames());
        }
        return res;
    }

}
