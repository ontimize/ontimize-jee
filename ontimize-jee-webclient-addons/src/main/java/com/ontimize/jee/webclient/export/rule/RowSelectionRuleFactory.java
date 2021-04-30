package com.ontimize.jee.webclient.export.rule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public class RowSelectionRuleFactory {

    public static RowSelectionRule create(String text) throws Exception {
        // RowModule
        Pattern pattern = Pattern.compile("%([0-9]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            return new RowModuleRule(Integer.valueOf(matcher.group(1)));
        }

        // RowRange
        pattern = Pattern.compile("([0-9]*)\\s*,\\s*([0-9]*)");
        matcher = pattern.matcher(text);
        if (matcher.matches()) {
            String group1 = matcher.group(1);
            int fromRow;
            int toRow;

            if (group1.isEmpty()) {
                fromRow = -1;
            } else {
                fromRow = Integer.valueOf(group1);
            }

            String group2 = matcher.group(2);

            if (group2.isEmpty()) {
                toRow = -1;
            } else {
                toRow = Integer.valueOf(group2);
            }
            return new RowRangeRule(fromRow, toRow);
        }

        // RowNumber
        pattern = Pattern.compile("([0-9]+)");
        matcher = pattern.matcher(text);
        if (matcher.matches()) {
            String group1 = matcher.group(1);
            int row;

            if (group1.isEmpty()) {
                row = -1;
            } else {
                row = Integer.valueOf(group1);
            }
            return new RowNumberRule(row);
        }

        // Error
        throw new Exception("no RowRule!");
    }

}
