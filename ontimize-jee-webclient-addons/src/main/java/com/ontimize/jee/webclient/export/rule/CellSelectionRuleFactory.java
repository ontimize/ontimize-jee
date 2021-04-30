package com.ontimize.jee.webclient.export.rule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */
public class CellSelectionRuleFactory {

    public static CellSelectionRule create(String text) throws Exception {

        // Cell coordinates
        Pattern pattern = Pattern.compile("([0-9]*)\\s*,\\s*([0-9]*)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            String group1 = matcher.group(1);
            int row;
            int col;

            if (group1.isEmpty()) {
                row = -1;
            } else {
                row = Integer.valueOf(group1);
            }

            String group2 = matcher.group(2);

            if (group2.isEmpty()) {
                col = -1;
            } else {
                col = Integer.valueOf(group2);
            }
            return new CellCoordinatesRule(row, col);
        }

        // CellRange
        pattern = Pattern.compile("([0-9]*)\\s*,\\s*([0-9]*),\\s*([0-9]*),\\s*([0-9]*)");
        matcher = pattern.matcher(text);
        if (matcher.matches()) {
            String group1 = matcher.group(1);
            int fromRow;
            int fromCol;
            int toRow;
            int toCol;

            if (group1.isEmpty()) {
                fromRow = -1;
            } else {
                fromRow = Integer.valueOf(group1);
            }
            group1 = matcher.group(2);
            if (group1.isEmpty()) {
                fromCol = -1;
            } else {
                fromCol = Integer.valueOf(group1);
            }
            group1 = matcher.group(3);
            if (group1.isEmpty()) {
                toRow = -1;
            } else {
                toRow = Integer.valueOf(group1);
            }
            group1 = matcher.group(4);
            if (group1.isEmpty()) {
                toCol = -1;
            } else {
                toCol = Integer.valueOf(group1);
            }
            return new CellRangeRule(fromRow, fromCol, toRow, toCol);
        }

        // Error
        throw new Exception("no RowRule!");
    }

}
