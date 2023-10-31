package com.ontimize.jee.common.db.handler;

import com.ontimize.jee.common.db.LocalePair;
import com.ontimize.jee.common.db.NullValue;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.util.DBFunctionName;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.gui.LongString;
import com.ontimize.jee.common.tools.ParseUtilsExtended;
import com.ontimize.jee.common.util.remote.BytesBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultSQLStatementHandler implements SQLStatementHandler {

    static final Logger logger = LoggerFactory.getLogger(SQLStatementBuilder.class);

    protected SQLStatementBuilder.SQLConditionValuesProcessor queryConditionsProcessor = null;

    protected SQLStatementBuilder.SQLNameEval sqlNameEval = null;

    protected char[] CONFLICT_CHARS = { ' ', '/', '-', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '%' };

    public boolean useAsInSubqueries = true;

    protected static boolean returnByteArray = false;

    /**
     * Sets which method will be used in JDBC prepared statements for setting parameters of type string
     * and length > 255. If this variable is false (default) <code>setCharacterStream()</code> will be
     * used instead of <code>setString()</code>
     */
    public static boolean USE_SETSTRING_LONG_STRINGS = false;

    /**
     * Calendar object used internally.
     */
    protected static Calendar calendar = null;

    public DefaultSQLStatementHandler() {
        // this.setSQLConditionValuesProcessor(new
        // DefaultSQLConditionValuesProcessor(false));//
        // ;ExtendedSQLConditionValuesProcessor();)
        this.setSQLConditionValuesProcessor(new SQLStatementBuilder.ExtendedSQLConditionValuesProcessor(false));// ;ExtendedSQLConditionValuesProcessor();)
    }

    @Override
    public boolean isUseAsInSubqueries() {
        return this.useAsInSubqueries;
    }

    @Override
    public void setUseAsInSubqueries(boolean useAsInSubqueries) {
        this.useAsInSubqueries = useAsInSubqueries;
    }

    /**
     * Sets the <code>SQLConditionValuesProcessor<code> to be used in <code>SQLStatementBuilder</code>
     * @param processor the SQLConditionValuesProcessor instance that be used
     */
    @Override
    public void setSQLConditionValuesProcessor(SQLStatementBuilder.SQLConditionValuesProcessor processor) {
        this.queryConditionsProcessor = processor;
        this.queryConditionsProcessor.setSQLStatementHandler(this);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a query
     * that obtains the number of query records
     * @param table name of the table the query is executed against
     * @param conditions condition list used in the query
     * @param wildcards column list that can use wildcards
     * @return a <code>SQLStatement</code> class
     */

    @Override
    public SQLStatementBuilder.SQLStatement createCountQuery(String table, Map conditions, List wildcards,
            List countColumns) {
        StringBuilder sql = new StringBuilder();
        List vValues = new ArrayList();
        String sCountQuery = "";
        if ((countColumns != null) && !countColumns.isEmpty()) {
            sCountQuery = this.createCountQuery(table, countColumns);
        } else {
            sCountQuery = this.createCountQuery(table);
        }
        sql.append(sCountQuery);
        String cond = this.createQueryConditions(conditions, wildcards, vValues);
        if (cond != null) {
            sql.append(cond);
        }

        DefaultSQLStatementHandler.logger.debug(sql.toString());
        return new SQLStatementBuilder.SQLStatement(sql.toString(), vValues);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @see SQLStatementBuilder.SQLStatement
     * @param table name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions condition list used to created the query
     * @param wildcards
     * @return a <code>SQLStatement</code> class
     */
    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards) {
        return this.createSelectQuery(table, requestedColumns, conditions, wildcards, null);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @see SQLStatementBuilder.SQLStatement
     * @param table name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions condition list used to created the query
     * @param wildcards column list that can use wildcards
     * @param columnSorting column list where query sorting is established
     * @param recordCount number of records requested in the query
     * @return a <code>SQLStatement</code> class
     */

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount) {
        return this.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount,
                false);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @see SQLStatementBuilder.SQLStatement
     * @param table name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions condition list used to created the query
     * @param wildcards column list that can use wildcards
     * @param columnSorting column list where query sorting is established
     * @param recordCount number of records requested in the query
     * @param descending true if sorting should be descending
     * @return a <code>SQLStatement</code> class
     */

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount,
            boolean descending) {
        return this.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount,
                descending, false);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @see SQLStatementBuilder.SQLStatement
     * @param table name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions condition list used to created the query
     * @param wildcards column list that can use wildcards
     * @param columnSorting column list where query sorting is established
     * @param recordCount number of records requested in the query
     * @param forceDistinct true if query result cannot have duplicated records
     * @param descending true if sorting should be descending
     * @return a <code>SQLStatement</code> class
     */

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, boolean descending,
            boolean forceDistinct) {
        StringBuilder sql = new StringBuilder();
        List vValues = new ArrayList();
        if ((columnSorting != null) && !requestedColumns.isEmpty()) {
            for (int i = 0; i < columnSorting.size(); i++) {
                if (!requestedColumns.contains(columnSorting.get(i).toString())) {
                    requestedColumns.add(columnSorting.get(i).toString());
                }
            }
        }
        sql.append(this.createSelectQuery(table, requestedColumns, forceDistinct));
        String cond = this.createQueryConditions(conditions, wildcards, vValues);
        if (cond != null) {
            sql.append(cond);
        }
        if ((columnSorting != null) && (!columnSorting.isEmpty())) {
            String sort = this.createSortStatement(columnSorting, descending);
            sql.append(sort);
        }

        DefaultSQLStatementHandler.logger.debug(sql.toString());
        return new SQLStatementBuilder.SQLStatement(sql.toString(), vValues);
    }

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, int offset) {
        return this.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount,
                offset, false);
    }

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, int offset,
            boolean descending) {
        return this.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount,
                offset, descending, false);
    }

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, int offset,
            boolean descending, boolean forceDistinct) {
        return this.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting,
                recordCount + offset, descending, forceDistinct);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @see SQLStatementBuilder.SQLStatement
     * @param table name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions condition list used to created the query
     * @param wildcards column list that can use wildcards
     * @param columnSorting column list where query sorting is established
     * @return a <code>SQLStatement</code> class
     */

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting) {
        return this.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, false);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @see SQLStatementBuilder.SQLStatement
     * @param table name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions condition list used to created the query
     * @param wildcards column list that can use wildcards
     * @param columnSorting column list where query sorting is established
     * @param descending true if sorting should be descending
     * @return a <code>SQLStatement</code> class
     */

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, boolean descending) {
        return this.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending, false);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @see SQLStatementBuilder.SQLStatement
     * @param table name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions condition list used to created the query
     * @param wildcards column list that can use wildcards
     * @param columnSorting column list where query sorting is established
     * @param forceDistinct true if query result cannot have duplicated records
     * @param descending true if sorting should be descending
     * @return a <code>SQLStatement</code> class
     */

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, boolean descending,
            boolean forceDistinct) {
        StringBuilder sql = new StringBuilder();
        List vValues = new ArrayList();

        if ((columnSorting != null) && (!requestedColumns.isEmpty()) && forceDistinct) {
            for (int i = 0; i < columnSorting.size(); i++) {
                if (!requestedColumns.contains(columnSorting.get(i).toString())) {
                    requestedColumns.add(columnSorting.get(i).toString());
                }
            }
        }

        // TODO REVIEW
        DBFunctionName function = this.hasIsolationFunction(requestedColumns);
        if (function == null) {
            sql.append(this.createSelectQuery(table, requestedColumns, forceDistinct));
        } else {
            List temp = new ArrayList();
            temp.add(function);
            sql.append(this.createSelectQuery("", temp, false));
            sql.append("(");
            sql.append(this.createSelectQuery(table, requestedColumns, forceDistinct));

        }

        String cond = this.createQueryConditions(conditions, wildcards, vValues);
        if (cond != null) {
            sql.append(cond);
        }
        if ((columnSorting != null) && (!columnSorting.isEmpty())) {
            String sort = this.createSortStatement(columnSorting, descending);
            sql.append(sort);
        }
        if (function != null) {
            sql.append(")");
            if (this.isUseAsInSubqueries()) {
                sql.append(" as vFunctionDB");
            } else {
                sql.append(" vFunctionDB");
            }
        }
        DefaultSQLStatementHandler.logger.debug(sql.toString());
        return new SQLStatementBuilder.SQLStatement(sql.toString(), vValues);
    }

    protected DBFunctionName hasIsolationFunction(List requestedColumns) {
        if (requestedColumns == null) {
            return null;
        }
        for (int i = 0; i < requestedColumns.size(); i++) {
            if ((requestedColumns.get(i) instanceof DBFunctionName)
                    && ((DBFunctionName) requestedColumns.get(i)).isIsolated()) {
                DBFunctionName function = (DBFunctionName) requestedColumns.remove(i);
                return function;
            }
        }
        return null;
    }

    @Override
    public String createSortStatement(List sortColumns, boolean descending) {
        if ((sortColumns == null) || sortColumns.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(SQLStatementBuilder.ORDER_BY);

        for (int i = 0; i < sortColumns.size(); i++) {
            if (i < (sortColumns.size() - 1)) {
                String col = sortColumns.get(i).toString();

                if (this.checkColumnName(col)) {
                    sb.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sb.append(col);
                    sb.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);

                    if (sortColumns.get(i) instanceof SQLStatementBuilder.SQLOrder) {
                        if (!((SQLStatementBuilder.SQLOrder) sortColumns.get(i)).isAscendent()) {
                            sb.append(SQLStatementBuilder.DESC);
                        }
                    } else {
                        if (descending) {
                            sb.append(SQLStatementBuilder.DESC);
                        }
                    }
                    sb.append(",");
                } else {
                    sb.append(col);
                    if (sortColumns.get(i) instanceof SQLStatementBuilder.SQLOrder) {
                        if (!((SQLStatementBuilder.SQLOrder) sortColumns.get(i)).isAscendent()) {
                            sb.append(SQLStatementBuilder.DESC);
                        }
                    } else {
                        if (descending) {
                            sb.append(SQLStatementBuilder.DESC);
                        }
                    }

                    sb.append(",");
                }
            } else {
                String col = sortColumns.get(i).toString();
                if (this.checkColumnName(col)) {
                    sb.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sb.append(col);
                    sb.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                } else {
                    sb.append(col);
                }

                if (sortColumns.get(i) instanceof SQLStatementBuilder.SQLOrder) {
                    if (!((SQLStatementBuilder.SQLOrder) sortColumns.get(i)).isAscendent()) {
                        sb.append(SQLStatementBuilder.DESC);
                    }
                } else {
                    if (descending) {
                        sb.append(SQLStatementBuilder.DESC);
                    }
                }
            }

        }
        return sb.toString();
    }

    @Override
    public String createSortStatement(List sortColumns) {
        return this.createSortStatement(sortColumns, false);
    }

    protected String createCountQuery(String table) {
        return this.createCountQuery(table, null);
    }

    /**
     * Builds a count query using a column name.
     * @param table Database table
     * @param countColumns Column to perform the count operation. If this parameter is null will be used
     *        <code>count(*)</code>.
     * @return the query string
     * @since 5.2080EN
     */
    protected String createCountQuery(String table, List countColumns) {
        StringBuilder sbStringQuery = new StringBuilder(SQLStatementBuilder.SELECT);
        // since 5.2080EN -> count(countColumns) is supported
        if ((countColumns != null) && !countColumns.isEmpty()) {
            sbStringQuery.append(SQLStatementBuilder.COUNT_COLUMN);
            for (int i = 0; i < countColumns.size(); i++) {
                String countColumn = (String) countColumns.get(i);
                sbStringQuery.append(countColumn);
                if (i != (countColumns.size() - 1)) {
                    sbStringQuery.append(SQLStatementBuilder.PIPES);
                }
            }
            sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
            sbStringQuery.append(SQLStatementBuilder.AS);
            sbStringQuery.append("\"");
            sbStringQuery.append(SQLStatementBuilder.COUNT_COLUMN_NAME);
            sbStringQuery.append("\" ");
        } else {
            // old behaviour -> count(*)
            sbStringQuery.append(SQLStatementBuilder.COUNT);
        }
        sbStringQuery.append(SQLStatementBuilder.FROM);
        String tableaux = table.toLowerCase();
        if ((tableaux.indexOf("select") < 0) && this.checkColumnName(tableaux)) {
            sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
            sbStringQuery.append(table);
            sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
        } else {
            if ((tableaux.indexOf("select") != -1) || this.checkColumnName(tableaux)) {
                sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                sbStringQuery.append(table);
                sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                sbStringQuery.append(" COUNT_NUMBER");
            } else {
                sbStringQuery.append(table);
            }
        }
        return sbStringQuery.toString();
    }

    protected String createSelectClause(boolean forceDistinct) {
        StringBuilder selectClause = new StringBuilder(SQLStatementBuilder.SELECT);
        if (forceDistinct) {
            selectClause.append(SQLStatementBuilder.DISTINCT);
        }
        return selectClause.toString();
    }

    protected String createSelectQuery(String table, List askedColumns, boolean forceDistinct) {
        StringBuilder sStringQuery = new StringBuilder(this.createSelectClause(forceDistinct));
        String tableaux = table.toLowerCase();
        if ((askedColumns == null) || (askedColumns.isEmpty())) {
            sStringQuery.append(SQLStatementBuilder.ASTERISK);
            sStringQuery.append(SQLStatementBuilder.FROM);
            if ((tableaux.indexOf("select") < 0) && this.checkColumnName(table)) {
                sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                sStringQuery.append(table);
                sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
            } else {
                sStringQuery.append(table);
            }
        } else {
            // If attributes is empty, then create the query with all requested
            // columns.
            // Requested columns number
            int attributesNumber = askedColumns.size();
            for (int i = 0; i < attributesNumber; i++) {
                Object oColumn = askedColumns.get(i);
                if (oColumn == null) {
                    continue;
                }

                // In the last attribute the comma is not added
                String sColumn = oColumn.toString();
                // If some conflicted character is contained then use brackets
                boolean bBrackets = this.checkColumnName(sColumn);
                if (i < (attributesNumber - 1)) {
                    if (bBrackets) {
                        // since 5.2071EN-0.2
                        String[] tables = table.split(",");
                        String tablek = table;
                        for (int k = 0; k < tables.length; k++) {
                            if (sColumn.indexOf(tables[k] + ".") == 0) {
                                tablek = tables[k];
                                continue;
                            }
                        }
                        if (!tablek.equals(table)) {
                            StringBuilder sbsColumn = new StringBuilder(sColumn);
                            // column begins with table name so brackets must be
                            // placed where name of column begins not including
                            // all (e.g.) table_name.[column]
                            int indexOpenBracket = (tablek + ".").length();
                            int indexCloseBracket = sColumn.length();
                            int indexOpenBracketInAs = -1;
                            int indexCloseBracketInAs = -1;
                            if (sColumn.toUpperCase().indexOf(" AS ") >= 0) {
                                indexCloseBracket = sColumn.toUpperCase().indexOf(" AS ");
                                indexOpenBracketInAs = indexCloseBracket + 5;
                                indexCloseBracketInAs = sColumn.length();
                            }
                            // need brackets without blank spaces, cannot use
                            // SQLStatementBuilder.OPEN_SQUARE_BRACKET nor
                            // SQLStatementBuilder.CLOSE_SQUARE_BRACKET
                            if (indexCloseBracketInAs != -1) {
                                sbsColumn.insert(indexCloseBracketInAs, "]");
                            }
                            sbsColumn.insert(indexCloseBracket, "]");
                            if (indexOpenBracketInAs != -1) {
                                sbsColumn.insert(indexOpenBracketInAs, "[");
                            }
                            sbsColumn.insert(indexOpenBracket, "[");
                            sStringQuery.append(sbsColumn.toString());
                            sStringQuery.append(SQLStatementBuilder.COMMA);
                        }
                        //
                        else {
                            sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                            sStringQuery.append(sColumn);
                            sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                            sStringQuery.append(SQLStatementBuilder.COMMA);
                        }
                        // }

                    } else {
                        sStringQuery.append(sColumn);
                        sStringQuery.append(SQLStatementBuilder.COMMA);
                    }
                } else {
                    if (bBrackets) {
                        // since 5.2076EN-0.2
                        String[] tables = table.split(",");
                        String tablek = table;
                        for (int k = 0; k < tables.length; k++) {
                            if (sColumn.indexOf(tables[k] + ".") == 0) {
                                tablek = tables[k];
                                continue;
                            }
                        }
                        if (!tablek.equals(table)) {
                            StringBuilder sbsColumn = new StringBuilder(sColumn);
                            // column begins with table name so brackets must be
                            // placed where name of column begins not including
                            // all (e.g.) table_name.[column]
                            int indexOpenBracket = (tablek + ".").length();
                            int indexCloseBracket = sColumn.length();
                            int indexOpenBracketInAs = -1;
                            int indexCloseBracketInAs = -1;
                            if (sColumn.toUpperCase().indexOf(" AS ") >= 0) {
                                indexCloseBracket = sColumn.toUpperCase().indexOf(" AS ");
                                indexOpenBracketInAs = indexCloseBracket + 5;
                                indexCloseBracketInAs = sColumn.length();
                            }
                            // need brackets without blank spaces, cannot use
                            // SQLStatementBuilder.OPEN_SQUARE_BRACKET nor
                            // SQLStatementBuilder.CLOSE_SQUARE_BRACKET
                            if (indexCloseBracketInAs != -1) {
                                sbsColumn.insert(indexCloseBracketInAs, "]");
                            }
                            sbsColumn.insert(indexCloseBracket, "]");
                            if (indexOpenBracketInAs != -1) {
                                sbsColumn.insert(indexOpenBracketInAs, "[");
                            }
                            sbsColumn.insert(indexOpenBracket, "[");
                            sStringQuery.append(sbsColumn.toString());
                        }
                        //
                        else {
                            sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                            sStringQuery.append(sColumn);
                            sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                        }
                        sStringQuery.append(SQLStatementBuilder.FROM);
                        if ((tableaux.indexOf("select") < 0) && this.checkColumnName(table)) {
                            sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                            sStringQuery.append(table);
                            sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                        } else {
                            sStringQuery.append(table);
                        }
                    } else {
                        // The last attribute does not include the COMMA
                        sStringQuery.append(sColumn);
                        sStringQuery.append(SQLStatementBuilder.FROM);
                        if ((tableaux.indexOf("select") < 0) && this.checkColumnName(table)
                                && (tableaux.indexOf("join") < 0)) {
                            sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                            sStringQuery.append(table);
                            sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                        } else {
                            sStringQuery.append(table);
                        }
                    }
                }
            }
        }
        return sStringQuery.toString();
    }

    protected String createSelectQuery(String table, List askedColumns) {
        return this.createSelectQuery(table, askedColumns, false);
    }

    protected String createQueryConditions(Map conditions, List wildcardColumns, List values,
            boolean withWhere) {
        StringBuilder sbStringQuery = new StringBuilder(" ");
        // Create the conditions string.
        if ((conditions == null) || conditions.isEmpty()) {
            // If there are no conditions query is finished
            return null;
        } else {
            if (!conditions.isEmpty() && withWhere) {
                sbStringQuery.append(SQLStatementBuilder.WHERE);
            }
        }
        sbStringQuery.append(this.queryConditionsProcessor.createQueryConditions(conditions, wildcardColumns, values));
        return sbStringQuery.toString();
    }

    /**
     * Returns the condition string for a SQL Statement.
     * @param conditions condition list that be using for create the string
     * @param wildcard column list that can use wildcards
     * @param values List where the value of each processed conditions is stored
     * @return a condition String
     */
    protected String createQueryConditions(Map conditions, List wildcard, List values) {
        return this.createQueryConditions(conditions, wildcard, values, true);
    }

    /**
     * Returns the condition string for a SQL Statement without WHERE clause in SQL.
     * @param conditions condition list that be using for create the string
     * @param wildcard column list that can use wildcards
     * @param values List where the value of each processed conditions is stored
     * @return a condition String
     */
    @Override
    public String createQueryConditionsWithoutWhere(Map conditions, List wildcard, List values) {
        return this.createQueryConditions(conditions, wildcard, values, false);
    }

    @Override
    public void setSQLNameEval(SQLStatementBuilder.SQLNameEval eval) {
        this.sqlNameEval = eval;
    }

    @Override
    public SQLStatementBuilder.SQLNameEval getSQLNameEval() {
        return this.sqlNameEval;
    }

    /**
     * Returns true if square brackets are necessary insert in this column name.
     * <p>
     * If the column name has a character of the special character list, it's necessary insert column
     * name in square brackets.
     * @return true if square brackets are necessary insert in this column name.
     */

    @Override
    public boolean checkColumnName(String columnName) {
        if (this.sqlNameEval != null) {
            return this.sqlNameEval.needCorch(columnName);
        }
        boolean bBrackets = false;
        if (columnName.toUpperCase().indexOf(" AS ") >= 0) {
            // since 5.2071EN-0.2
            String columnNameNoAs = columnName.toUpperCase().replace(" AS ", "");
            if (columnNameNoAs.indexOf(' ') >= 0) {
                return true;
            }
            //
            return false;
        }
        if ((columnName.indexOf(',') >= 0) || (columnName.indexOf('[') >= 0)) {
            return false;
        }
        for (int i = 0; i < this.CONFLICT_CHARS.length; i++) {
            if (columnName.indexOf(this.CONFLICT_CHARS[i]) >= 0) {
                bBrackets = true;
                break;
            }
        }
        return bBrackets;
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a insert
     * query.
     * @param table name of the table the query is executed against
     * @param attributes attributes a Map specifying pairs of key-value corresponding to the attribute
     *        (or column of a table in a database) and the value that must be stored.
     * @return
     */

    @Override
    public SQLStatementBuilder.SQLStatement createInsertQuery(String table, Map attributes) {
        if (attributes.isEmpty()) {
            return null;
        }
        StringBuilder sbSqlString = new StringBuilder(SQLStatementBuilder.INSERT_INTO);
        if (this.checkColumnName(table)) {
            sbSqlString.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
            sbSqlString.append(table);
            sbSqlString.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
        } else {
            sbSqlString.append(table);
        }
        sbSqlString.append(SQLStatementBuilder.OPEN_PARENTHESIS);
        // Now the attributes. Using =?,
        Enumeration enumAttributes = Collections.enumeration(attributes.keySet());
        StringBuilder aux = new StringBuilder("(");
        List vValues = new ArrayList();
        int i = 0;
        int discard = 0;
        while (enumAttributes.hasMoreElements()) {
            Object oAttribute = enumAttributes.nextElement();
            String sColumn = oAttribute.toString();
            if (this.checkColumnName(sColumn)) {
                sbSqlString.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                sbSqlString.append(sColumn);
                sbSqlString.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
            } else {
                sbSqlString.append(sColumn);
            }
            vValues.add(attributes.get(oAttribute));
            if (i < (attributes.size() - discard - 1)) {
                sbSqlString.append(SQLStatementBuilder.COMMA);
            }
            i++;
        }
        sbSqlString.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
        sbSqlString.append(SQLStatementBuilder.VALUES);
        List vResValues = new ArrayList();
        for (int p = 0; p < vValues.size(); p++) {
            Object oValues = vValues.get(p);
            if (p < (vValues.size() - 1)) {
                if (oValues instanceof DBFunctionName) {
                    aux.append(((DBFunctionName) oValues).getName());
                    aux.append(SQLStatementBuilder.COMMA);
                } else {
                    vResValues.add(vValues.get(p));
                    aux.append(SQLStatementBuilder.QUESTIONMARK);
                    aux.append(SQLStatementBuilder.COMMA);
                }
            } else {
                if (oValues instanceof DBFunctionName) {
                    aux.append(((DBFunctionName) oValues).getName());
                    aux.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                } else {
                    vResValues.add(vValues.get(p));
                    aux.append(SQLStatementBuilder.QUESTIONMARK);
                    aux.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                }
            }
        }
        sbSqlString.append(aux.toString());

        DefaultSQLStatementHandler.logger.debug(sbSqlString.toString());
        DefaultSQLStatementHandler.logger.debug(vResValues.toString());

        return new SQLStatementBuilder.SQLStatement(sbSqlString.toString(), vResValues);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a update
     * query.
     *
     * @see SQLStatementBuilder.SQLStatement
     * @param table name of the table the update is executed against
     * @param attributesValues attributesValues the data for updating the records to. The keys specify
     *        the attributes (or columns) and the values, the values for these columns.
     * @param keysValues keysValues the conditions that the records to be updated must fulfill. The keys
     *        specify the attributes (or columns) and the values, the values for these columns.
     * @return
     */

    @Override
    public SQLStatementBuilder.SQLStatement createUpdateQuery(String table, Map attributesValues, Map keysValues) {
        // If attributesValues is empty --> no update
        if (attributesValues.isEmpty()) {
            return null;
        }
        // Now creates SQL sentence
        StringBuilder sbSqlString = new StringBuilder(SQLStatementBuilder.UPDATE);
        if (this.checkColumnName(table)) {
            sbSqlString.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
            sbSqlString.append(table);
            sbSqlString.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
        } else {
            sbSqlString.append(table);
        }
        sbSqlString.append(SQLStatementBuilder.SET);
        Enumeration enumAttributes = Collections.enumeration(attributesValues.keySet());
        List vValues = new ArrayList();
        int i = 0;
        int discard = 0;
        while (enumAttributes.hasMoreElements()) {
            Object oAttribute = enumAttributes.nextElement();
            String sColumn = oAttribute.toString();
            boolean bBrackets = this.checkColumnName(sColumn);
            if (bBrackets) {
                sbSqlString.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                sbSqlString.append(sColumn);
                sbSqlString.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
            } else {
                sbSqlString.append(sColumn);
            }
            if (attributesValues.get(oAttribute) instanceof DBFunctionName) {
                sbSqlString.append(SQLStatementBuilder.EQUAL);
                sbSqlString.append(((DBFunctionName) attributesValues.get(oAttribute)).getName());
            } else {
                sbSqlString.append(SQLStatementBuilder.EQUAL_XQUESTIONMARK);
                vValues.add(vValues.size(), attributesValues.get(oAttribute));
            }
            // WARNING: decrement discard
            if (i < (attributesValues.size() - discard - 1)) {
                sbSqlString.append(SQLStatementBuilder.COMMA);
            }
            i++;
        }

        List vConditionValues = new ArrayList();

        String cond = this.createQueryConditions(keysValues, new ArrayList(), vConditionValues);
        if (cond != null) {
            sbSqlString.append(cond);
        }
        for (int j = 0; j < vConditionValues.size(); j++) {
            vValues.add(vValues.size(), vConditionValues.get(j));
        }

        DefaultSQLStatementHandler.logger.debug(sbSqlString.toString());
        DefaultSQLStatementHandler.logger.debug(vValues.toString());

        return new SQLStatementBuilder.SQLStatement(sbSqlString.toString(), vValues);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a delete
     * query.
     * @param table name of the table the query is executed against
     * @param keysValues the conditions that the records to be deleted must fulfill. The keys specify
     *        the attributes (or columns) and the values, the values for these columns.
     * @return
     */

    @Override
    public SQLStatementBuilder.SQLStatement createDeleteQuery(String table, Map keysValues) {
        StringBuilder sbSqlString = new StringBuilder(SQLStatementBuilder.DELETE_FROM);
        if (this.checkColumnName(table)) {
            sbSqlString.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
            sbSqlString.append(table);
            sbSqlString.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
        } else {
            sbSqlString.append(table);
        }

        List vValues = new ArrayList();
        String cond = this.createQueryConditions(keysValues, new ArrayList(), vValues);
        if (cond != null) {
            sbSqlString.append(cond);
        }

        DefaultSQLStatementHandler.logger.debug(sbSqlString.toString());
        DefaultSQLStatementHandler.logger.debug(vValues.toString());

        return new SQLStatementBuilder.SQLStatement(sbSqlString.toString(), vValues);
    }

    /**
     * Adds characters to the list of special characters.
     * <p>
     * When the query is be created if a column name contains a character of the list this column name
     * is inserted between parenthesis in the query.
     * @param c a array with the new characters
     */

    @Override
    public void addSpecialCharacters(char[] c) {
        char[] cNew = new char[this.CONFLICT_CHARS.length + c.length];
        System.arraycopy(this.CONFLICT_CHARS, 0, cNew, 0, this.CONFLICT_CHARS.length);
        System.arraycopy(c, 0, cNew, this.CONFLICT_CHARS.length, c.length);
        this.CONFLICT_CHARS = cNew;
        DefaultSQLStatementHandler.logger.info("Special characters have been established: {}", cNew);
    }

    /**
     * This class provides a implementation of the <code>SQLConditionValuesProcessor</code> interface.
     * <p>
     * This class extends
     * <code>DefaultSQLConditionValuesProcessor<code> and permits process complex conditions
     *   where condition key doesn't have to be the column of a table in a database.
     *   <code>EXPRESSION_KEY</code> is used as condition key to indicate which is a complex condition
     * <p>
     * Using classes that implements <code>Expression, Field, Operator </code> interfaces is necessary
     * to define a complex condition. Each class that implements the <code>Expression</code> interface
     * has three important parts. The left operand that can only be another expression or a class that
     * implements <code>Field</code> interface The right operand that can be another expression or a
     * value The operator that has to be a class that implement <code>Operator<code> interface
     * <p>
     * In the next examples the basic implementations have been used:
     *
     * <pre>
     *  Example for a simple expression:
     *
     *  	Operator equalOperator=BasicOperator.EQUAL_OP;
     *  	Field field = new BasicField("columnName1");
     *
     * </pre>
     *
     * <pre>
     *  Example for a complex expression:
     *  	Operator equalOperator=BasicOperator.EQUAL_OP;
     *  	Field field1 = new BasicField("columnName1");
     *  	Expression expression1 = new BasicExpression(field1,equalOperator,"filterValue");
     *
     *  	Field field2 = new BasicField("columnName2");
     *  	Expression expression2 = new BasicExpression(field2,BasicOperator.LESS,new Integer(10));
     *
     *  	Expression totalExpression = new Expression(expression1,BasicOperator.AND,expression2);
     *
     *  	Map conditions=new HashMap();
     *   	conditions.put(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,totalExpression);
     * </pre>
     *
     * @see SQLStatementBuilder.Expression
     * @author Imatia Innovation S.L.
     */

    @Override
    public SQLStatementBuilder.SQLStatement createJoinSelectQuery(String principalTable, String secondaryTable,
            List principalKeys,
            List secondaryKeys, List principalTableRequestedColumns,
            List secondaryTableRequestedColumns, Map principalTableConditions,
            Map secondaryTableConditions, List wildcards, List columnSorting,
            boolean forceDistinct) {

        return this.createJoinSelectQuery(principalTable, secondaryTable, principalKeys, secondaryKeys,
                principalTableRequestedColumns, secondaryTableRequestedColumns,
                principalTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, false);
    }

    /**
     * Returns a <code>String</code> with the qualified name.
     * <p>
     * This static methods creates a qualified name from the column name and the table name. The used
     * pattern is table_name.column_name. If the table name or the column name has special characters,
     * square brackets are used [table_name].[column_name]
     * @param col a String with the column name
     * @param table a String with the table name
     * @return a String with the qualified name.
     */

    @Override
    public String qualify(String col, String table) {
        if (col.equals(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY)) {
            return col;
        }

        if (col.equals(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.FILTER_KEY)) {
            return col;
        }
        if (col.startsWith(table + ".") || col.startsWith(
                SQLStatementBuilder.OPEN_SQUARE_BRACKET + table + SQLStatementBuilder.CLOSE_SQUARE_BRACKET + ".")
                || col
                    .startsWith(SQLStatementBuilder.OPEN_SQUARE_BRACKET.trim() + table
                            + SQLStatementBuilder.CLOSE_SQUARE_BRACKET.trim() + ".")) {
            return col;
        } else return qualifyStartsNoParticularized(col, table);
    }

    /**
     * Method used to reduce cognitive complexity of {@link #qualify(String, String)}
     * @param col
     * @param table
     * @return
     */
    private String qualifyStartsNoParticularized(String col, String table) {
        if (this.checkColumnName(col)) {
            if (this.checkColumnName(table)) {
                return SQLStatementBuilder.OPEN_SQUARE_BRACKET.trim() + table
                        + SQLStatementBuilder.CLOSE_SQUARE_BRACKET.trim() + "."
                        + SQLStatementBuilder.OPEN_SQUARE_BRACKET.trim() + col
                        + SQLStatementBuilder.CLOSE_SQUARE_BRACKET.trim();
            }
            return table + "." + SQLStatementBuilder.OPEN_SQUARE_BRACKET.trim() + col
                    + SQLStatementBuilder.CLOSE_SQUARE_BRACKET.trim();
        } else {
            if (this.checkColumnName(table)) {
                table = table.substring(0, table.indexOf(" "));
            }
            if (col.indexOf(".") > 0) {
                return col;
            } else {
                return table + "." + col;
            }

        }
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query against two table used a join.
     * @param mainTable name of the principal table the query is executed against
     * @param secondaryTable name of the secondary table the query is executed against
     * @param mainKeys a List specifying the column names of the principal table that be used to combine
     *        the two tables
     * @param secondaryKeys a List specifying the column names of the secondary table that be used to
     *        combine the two tables
     * @param mainTableRequestedColumns column list that be requested in the query from principal table
     * @param secondaryTableRequestedColumns column list that be requested in the query from secondary
     *        table
     * @param mainTableConditions a Map specifying conditions that must comply the set of records
     *        returned from principal table
     * @param secondaryTableConditions a List Mapxx conditions that must comply the set of records
     *        returned from secondary table
     * @param wildcards column list which wildcards can be used in
     * @param columnSorting column list where query sorting is established
     * @param forceDistinct true if query result cannot have duplicated records
     * @param descending true if sorting should be descending
     * @return a <code>SQLStatement</code> class which stores the SQL Statement and the required values
     */

    @Override
    public SQLStatementBuilder.SQLStatement createJoinSelectQuery(String mainTable, String secondaryTable,
            List mainKeys,
            List secondaryKeys, List mainTableRequestedColumns,
            List secondaryTableRequestedColumns, Map mainTableConditions, Map secondaryTableConditions,
            List wildcards, List columnSorting, boolean forceDistinct,
            boolean descending) {

        if (mainKeys.size() != secondaryKeys.size()) {
            throw new IllegalArgumentException("The number of keys of principal and secondary table have to be equals");
        }

        List askedColumns = new ArrayList();
        for (int i = 0; i < mainTableRequestedColumns.size(); i++) {
            askedColumns.add(this.qualify((String) mainTableRequestedColumns.get(i), mainTable));
        }
        for (int i = 0; i < secondaryTableRequestedColumns.size(); i++) {
            askedColumns.add(this.qualify((String) secondaryTableRequestedColumns.get(i), secondaryTable));
        }
        Map conditions = new HashMap();
        Enumeration enumKeys = Collections.enumeration(mainTableConditions.keySet());
        while (enumKeys.hasMoreElements()) {
            Object oKey = enumKeys.nextElement();
            Object oValue = mainTableConditions.get(oKey);
            conditions.put(this.qualify(((String) oKey).replaceAll(mainTable + "\\.", ""), mainTable), oValue);
        }
        enumKeys = Collections.enumeration(secondaryTableConditions.keySet());
        while (enumKeys.hasMoreElements()) {
            Object oKey = enumKeys.nextElement();
            Object oValues = secondaryTableConditions.get(oKey);
            conditions.put(this.qualify(((String) oKey).replaceAll(secondaryTable + "\\.", ""), secondaryTable),
                    oValues);
        }

        StringBuilder sql = new StringBuilder();
        List vValues = new ArrayList();

        List vMainKeys = new ArrayList();
        for (int i = 0; i < mainKeys.size(); i++) {
            vMainKeys.add(this.qualify((String) mainKeys.get(i), mainTable));
        }
        List vSecondaryKeys = new ArrayList();
        for (int i = 0; i < secondaryKeys.size(); i++) {
            vSecondaryKeys.add(this.qualify((String) secondaryKeys.get(i), secondaryTable));
        }

        if (this.checkColumnName(mainTable)) {
            mainTable = SQLStatementBuilder.OPEN_SQUARE_BRACKET + mainTable + SQLStatementBuilder.CLOSE_SQUARE_BRACKET;
        }
        if (this.checkColumnName(secondaryTable)) {
            secondaryTable = SQLStatementBuilder.OPEN_SQUARE_BRACKET + mainTable + secondaryTable;
        }
        sql.append(this.createSelectQuery(mainTable + "," + secondaryTable, askedColumns, forceDistinct));
        sql.append(SQLStatementBuilder.WHERE + " ");
        for (int i = 0; i < vMainKeys.size(); i++) {
            String sMainKey = (String) vMainKeys.get(i);
            String sSecondaryKey = (String) vSecondaryKeys.get(i);
            sql.append(sMainKey + "=" + sSecondaryKey);
            if (i < (vMainKeys.size() - 1)) {
                sql.append(" AND ");
            }
        }
        String cond = this.createQueryConditionsWithoutWhere(conditions, wildcards, vValues);
        if ((cond != null) && (cond.length() > 0)) {
            sql.append(" AND " + cond);
        }

        if ((columnSorting != null) && (!columnSorting.isEmpty())) {
            String sort = this.createSortStatement(columnSorting, descending);
            sql.append(sort);
        }

        DefaultSQLStatementHandler.logger.debug(sql.toString());
        return new SQLStatementBuilder.SQLStatement(sql.toString(), vValues);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query against two subselects used a join.
     * @param primaryAlias name of the principal query
     * @param secondaryAlias name of the secondary query
     * @param primaryQuery principal query is executed against
     * @param secondaryQuery secondary query is executed against
     * @param primaryKeys a List specifying the column names of the principal table that be used to
     *        combine the two tables
     * @param secondaryKeys a List specifying the column names of the secondary table that be used to
     *        combine the two tables
     * @param primaryTableRequestedColumns column list that be requested in the query from principal
     *        table
     * @param secondaryTableRequestedColumns column list that be requested in the query from secondary
     *        table
     * @param primaryTableConditions a Map specifying conditions that must comply the set of records
     *        returned from principal table
     * @param secondaryTableConditions a Map specifying conditions that must comply the set of records
     *        returned from secondary table
     * @param wildcards column list which wildcards can be used in
     * @param columnSorting column list where query sorting is established
     * @param forceDistinct true if query result cannot have duplicated records
     * @param descending true if sorting should be descending
     * @return a <code>SQLStatement</code> class which stores the SQL Statement and the required values
     */
    @Override
    public SQLStatementBuilder.SQLStatement createJoinFromSubselectsQuery(String primaryAlias, String secondaryAlias,
            String primaryQuery,
            String secondaryQuery, List primaryKeys,
            List secondaryKeys, List primaryTableRequestedColumns, List secondaryTableRequestedColumns,
            Map primaryTableConditions, Map secondaryTableConditions,
            List wildcards, List columnSorting, boolean forceDistinct, boolean descending) {

        if (primaryKeys.size() != secondaryKeys.size()) {
            throw new IllegalArgumentException("The number of keys of principal and secondary table have to be equals");
        }

        List askedColumns = new ArrayList();
        for (int i = 0; i < primaryTableRequestedColumns.size(); i++) {
            askedColumns.add(this.qualify((String) primaryTableRequestedColumns.get(i), primaryAlias));
        }

        for (int i = 0; i < secondaryTableRequestedColumns.size(); i++) {
            askedColumns.add(this.qualify((String) secondaryTableRequestedColumns.get(i), secondaryAlias));
        }

        Map conditions = new HashMap();
        Enumeration enumKeys = Collections.enumeration(primaryTableConditions.keySet());
        while (enumKeys.hasMoreElements()) {
            Object oKey = enumKeys.nextElement();
            Object oValue = primaryTableConditions.get(oKey);
            conditions.put(this.qualify(((String) oKey).replaceAll(primaryAlias + "\\.", ""), primaryAlias), oValue);
        }

        enumKeys = Collections.enumeration(secondaryTableConditions.keySet());
        while (enumKeys.hasMoreElements()) {
            Object oKey = enumKeys.nextElement();
            Object oValues = secondaryTableConditions.get(oKey);
            conditions.put(this.qualify(((String) oKey).replaceAll(secondaryQuery + "\\.", ""), secondaryQuery),
                    oValues);
        }

        StringBuilder sql = new StringBuilder();
        List vValues = new ArrayList();

        List vMainKeys = new ArrayList();
        for (int i = 0; i < primaryKeys.size(); i++) {
            vMainKeys.add(this.qualify((String) primaryKeys.get(i), primaryAlias));
        }
        List vSecondaryKeys = new ArrayList();
        for (int i = 0; i < secondaryKeys.size(); i++) {
            vSecondaryKeys.add(this.qualify((String) secondaryKeys.get(i), secondaryAlias));
        }

        if (this.checkColumnName(primaryAlias)) {
            primaryQuery = SQLStatementBuilder.OPEN_SQUARE_BRACKET + primaryAlias
                    + SQLStatementBuilder.CLOSE_SQUARE_BRACKET;
        }
        if (this.checkColumnName(secondaryAlias)) {
            secondaryQuery = SQLStatementBuilder.OPEN_SQUARE_BRACKET + primaryAlias + secondaryAlias;
        }

        primaryQuery = "(" + primaryQuery + ") " + primaryAlias;
        secondaryQuery = "(" + secondaryQuery + ") " + secondaryAlias;

        sql.append(this.createSelectQuery(primaryQuery + SQLStatementBuilder.INNER_JOIN + secondaryQuery, askedColumns,
                forceDistinct));
        sql.append(SQLStatementBuilder.ON + " ");
        for (int i = 0; i < vMainKeys.size(); i++) {
            String sMainKey = (String) vMainKeys.get(i);
            String sSecondaryKey = (String) vSecondaryKeys.get(i);
            sql.append(sMainKey + "=" + sSecondaryKey);
            if (i < (vMainKeys.size() - 1)) {
                sql.append(" AND ");
            }
        }
        String cond = this.createQueryConditionsWithoutWhere(conditions, wildcards, vValues);
        if ((cond != null) && (cond.length() > 0)) {
            sql.append(" AND " + cond);
        }

        if ((columnSorting != null) && (!columnSorting.isEmpty())) {
            String sort = this.createSortStatement(columnSorting, descending);
            sql.append(sort);
        }

        DefaultSQLStatementHandler.logger.debug(sql.toString());
        return new SQLStatementBuilder.SQLStatement(sql.toString(), vValues);

    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query against two subselects used a join.
     * @param primaryAlias name of the principal query
     * @param secondaryAlias name of the secondary query
     * @param primaryTable principal query is executed against
     * @param secondaryTable secondary query is executed against
     * @param primaryKeys a List specifying the column names of the principal table that be used to
     *        combine the two tables
     * @param secondaryKeys a List specifying the column names of the secondary table that be used to
     *        combine the two tables
     * @param primaryTableRequestedColumns column list that be requested in the query from principal
     *        table
     * @param secondaryTableRequestedColumns column list that be requested in the query from secondary
     *        table
     * @param primaryTableConditions a Map specifying conditions that must comply the set of records
     *        returned from principal table
     * @param secondaryTableConditions a Map specifying conditions that must comply the set of records
     *        returned from secondary table
     * @param wildcards column list which wildcards can be used in
     * @param columnSorting column list where query sorting is established
     * @param forceDistinct true if query result cannot have duplicated records
     * @param descending true if sorting should be descending
     * @param recordNumber number of rows to return
     * @param startIndex number of initial row
     * @return a <code>SQLStatement</code> class which stores the SQL Statement and the required values
     */
    @Override
    public SQLStatementBuilder.SQLStatement createLeftJoinSelectQueryPageable(String mainTable, String subquery,
            String secondaryTable,
            List mainKeys, List secondaryKeys,
            List mainTableRequestedColumns, List secondaryTableRequestedColumns, Map mainTableConditions,
            Map secondaryTableConditions, List wildcards,
            List columnSorting, boolean forceDistinct, boolean descending, int recordNumber, int startIndex) {
        return this.createLeftJoinSelectQueryPageable(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys,
                mainTableRequestedColumns, secondaryTableRequestedColumns,
                mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending,
                recordNumber + startIndex);
    }

    protected SQLStatementBuilder.SQLStatement createLeftJoinSelectQueryPageable(String mainTable, String subquery,
            String secondaryTable,
            List mainKeys, List secondaryKeys,
            List mainTableRequestedColumns, List secondaryTableRequestedColumns, Map mainTableConditions,
            Map secondaryTableConditions, List wildcards,
            List columnSorting, boolean forceDistinct, boolean descending, int i) {
        return this.createLeftJoinSelectQuery(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys,
                mainTableRequestedColumns, secondaryTableRequestedColumns,
                mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query against two table used a left join.
     * @param mainTable name of the principal table the query is executed against
     * @param subQuery the subquery. If this parameter is not null, the principal table acts as alias
     *        for the subquery, like "FROM (SUBQUERY) MAINTABLE". If null, subquery is not executed.
     * @param subQueryValues conditions for the subquery. Null if subquery has not conditions
     * @param secondaryTable name of the secondary table the query is executed against
     * @param mainKeys a List specifying the column names of the principal table that be used to combine
     *        the two tables
     * @param secondaryKeys a List specifying the column names of the secondary table that be used to
     *        combine the two tables
     * @param mainTableRequestedColumns column list that be requested in the query from principal table
     * @param secondaryTableRequestedColumns column list that be requested in the query from secondary
     *        table
     * @param mainTableConditions a Map specifying conditions that must comply the set of records
     *        returned from principal table
     * @param secondaryTableConditions a Map specifying conditions that must comply the set of records
     *        returned from secondary table
     * @param wildcards column list which wildcards can be used in
     * @param columnSorting column list where query sorting is established
     * @param forceDistinct true if query result cannot have duplicated records
     * @param descending true if sorting should be descending
     * @return a <code>SQLStatement</code> class which stores the SQL Statement and the required values
     */
    @Override
    public SQLStatementBuilder.SQLStatement createLeftJoinSelectQuery(String mainTable, String subquery,
            String secondaryTable,
            List mainKeys, List secondaryKeys, List mainTableRequestedColumns,
            List secondaryTableRequestedColumns, Map mainTableConditions, Map secondaryTableConditions,
            List wildcards, List columnSorting, boolean forceDistinct,
            boolean descending) {

        if (mainKeys.size() != secondaryKeys.size()) {
            throw new IllegalArgumentException("The number of keys of principal and secondary table have to be equals");
        }

        List askedColumns = new ArrayList();
        for (int i = 0; i < mainTableRequestedColumns.size(); i++) {
            askedColumns.add(this.qualify((String) mainTableRequestedColumns.get(i), mainTable));
        }
        for (int i = 0; i < secondaryTableRequestedColumns.size(); i++) {
            askedColumns.add(this.qualify((String) secondaryTableRequestedColumns.get(i), secondaryTable));
        }
        Map conditions = new HashMap();
        Map filterkeys = new HashMap();
        Enumeration enumKeys = Collections.enumeration(mainTableConditions.keySet());
        while (enumKeys.hasMoreElements()) {
            Object oKey = enumKeys.nextElement();
            Object oValue = mainTableConditions.get(oKey);
            if (!oKey.toString().equalsIgnoreCase(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.FILTER_KEY)) {
                conditions.put(this.qualify(((String) oKey).replaceAll(mainTable + "\\.", ""), mainTable), oValue);
            } else {
                filterkeys.put(this.qualify(((String) oKey).replaceAll(mainTable + "\\.", ""), mainTable), oValue);
            }
        }
        enumKeys = Collections.enumeration(secondaryTableConditions.keySet());
        while (enumKeys.hasMoreElements()) {
            Object oKey = enumKeys.nextElement();
            Object oValues = secondaryTableConditions.get(oKey);
            conditions.put(this.qualify(((String) oKey).replaceAll(secondaryTable + "\\.", ""), secondaryTable),
                    oValues);
        }

        StringBuilder sql = new StringBuilder();
        List vValues = new ArrayList();

        List vMainKeys = new ArrayList();
        for (int i = 0; i < mainKeys.size(); i++) {
            vMainKeys.add(this.qualify((String) mainKeys.get(i), mainTable));
        }
        List vSecondaryKeys = new ArrayList();
        for (int i = 0; i < secondaryKeys.size(); i++) {
            vSecondaryKeys.add(this.qualify((String) secondaryKeys.get(i), secondaryTable));
        }
        String mainTableMultilanguage = mainTable.toLowerCase();
        if (this.checkColumnName(mainTable) && !(mainTableMultilanguage.indexOf("join") > 0)) {
            mainTable = SQLStatementBuilder.OPEN_SQUARE_BRACKET + mainTable + SQLStatementBuilder.CLOSE_SQUARE_BRACKET;
        }
        if (this.checkColumnName(secondaryTable)) {
            secondaryTable = SQLStatementBuilder.OPEN_SQUARE_BRACKET + mainTable + secondaryTable;
        }

        if (subquery != null) {
            mainTable = "(" + subquery + ")" + mainTable;
        }

        sql.append(this.createSelectQuery(mainTable + SQLStatementBuilder.LEFT_JOIN + secondaryTable, askedColumns,
                forceDistinct));
        sql.append(SQLStatementBuilder.ON + " ");
        for (int i = 0; i < vMainKeys.size(); i++) {
            String sMainKey = (String) vMainKeys.get(i);
            String sSecondaryKey = (String) vSecondaryKeys.get(i);
            sql.append(sMainKey + "=" + sSecondaryKey);
            if (i < (vMainKeys.size() - 1)) {
                sql.append(" AND ");
            }
        }

        String cond = this.createQueryConditionsWithoutWhere(conditions, wildcards, vValues);
        if ((cond != null) && (cond.length() > 0)) {
            sql.append(" AND " + cond);
        }

        String filter = this.createQueryConditions(filterkeys, wildcards, new ArrayList(), true);
        if ((filter != null) && (filter.length() > 0)) {
            sql.append(filter);
        }

        if ((columnSorting != null) && (!columnSorting.isEmpty())) {
            String sort = this.createSortStatement(columnSorting, descending);
            sql.append(sort);
        }

        DefaultSQLStatementHandler.logger.debug(sql.toString());

        return new SQLStatementBuilder.SQLStatement(sql.toString(), vValues);
    }

    /**
     * Returns a instance of <code>SQLConditionValuesProcessor</code> that be used in this class to
     * create the condition string
     * @return a <code>SQLConditionValuesProcessor</code> class
     */
    @Override
    public SQLStatementBuilder.SQLConditionValuesProcessor getQueryConditionsProcessor() {
        return this.queryConditionsProcessor;
    }

    @Override
    public boolean isPageable() {
        return false;
    }

    @Override
    public boolean isDelimited() {
        return false;
    }

    @Override
    public void resultSetToEntityResult(ResultSet resultSet, EntityResult entityResult, List columnNames)
            throws Exception {
        this.resultSetToEntityResult(resultSet, entityResult, -1, 0, true, columnNames);
    }

    /**
     * Transforms a java.sql.ResultSet object into an Ontimize {@link EntityResult}. The columns in the
     * ResultSet are the keys in the EntityResult, and the values for the columns are stored in List
     * objects corresponding to the keys in the EntityResult.
     * <p>
     * The following getxxxxx ResulSet methods are used for getting column data:
     * <ul>
     * <li>getBlob for BLOB SQLType</li>
     * <li>getClob for CLOB SQLType</li>
     * <li>getBinaryStream for VARBINARY or LOGVARBINARY SQLType</li>
     * <li>getAsciiStream for LONGVARCHAR SQLType</li>
     * <li>getObject for rest of SQLTypes</li>
     * </ul>
     * @param resultSet the source ResultSet
     * @param entityResult the destination EntityResult. It has a List of Lists structure.
     * @param recordNumber Number of records to query
     * @param offset number of the row where start
     * @param delimited If delimited is true then all the resultSet must be queried into the
     *        EntityResult, because the original query that generated the ResultSet only returns the
     *        number of records specified in <code>recordNumber</code>. If delimited is false then this
     *        method only read the specified <code>recordNumber</code> from the ResultSet into the
     *        EntityResult
     * @param columnNames Names of the columns to return in the EntityResult. If this parameter is null
     *        or empty then return exactly the same names in the ResultSet
     * @throws Exception if any error (database, etc.) occurs
     */
    @Override
    public void resultSetToEntityResult(ResultSet resultSet, EntityResult entityResult, int recordNumber, int offset,
            boolean delimited, List columnNames) throws Exception {
        if (offset > 0) {
            resultSet.absolute(offset);
        }

        try {
            ResultSetMetaData rsMetaData = resultSet.getMetaData();
            // Optimization: Array access, instead of request the name in each
            // loop
            String[] sColumnLabels = this.getColumnNames(rsMetaData);

            // Optimization: use column types.
            int[] columnTypes = this.getColumnTypes(rsMetaData);

            Map hColumnTypesAux = new HashMap();
            if (hColumnTypesAux != null) {
                for (int i = 0; i < columnTypes.length; i++) {
                    hColumnTypesAux.put(sColumnLabels[i], new Integer(columnTypes[i]));
                }
            }
            entityResult.setColumnSQLTypes(hColumnTypesAux);

            int recordCount = 0;
            while (resultSet.next()) {
                if (!delimited && (recordNumber == recordCount)) {
                    // If we only want to query an specific number of records
                    // instead
                    // of all data and we have already read them
                    break;
                }
                for (int i = 0; i < sColumnLabels.length; i++) {
                    String columnName = sColumnLabels[i];
                    Object oValue = this.getResultSetValue(resultSet, columnName, columnTypes[i]);
                    List vPreviousData = (List) entityResult.get(columnName);
                    if (vPreviousData != null) {
                        vPreviousData.add(oValue);
                    } else {
                        List vData = new ArrayList();
                        vData.add(oValue);
                        entityResult.put(columnName, vData);
                    }
                }
                recordCount++;
            }
            this.changeColumnNames(entityResult, columnNames);
        } catch (Exception e) {
            DefaultSQLStatementHandler.logger.error(null, e);
            throw e;
        }
    }

	protected Object getResultSetValue(ResultSet resultSet, String columnLabel, int columnType) throws Exception {
        // +1 is because array index starts in 0 and metadata index
        // starts in 1
        if (columnType == Types.BLOB) {
            Blob blob = resultSet.getBlob(columnLabel);
            if (blob == null) {
                return null;
            } else {
                InputStream flujoEntr = blob.getBinaryStream();
                return this.readBinaryStream(flujoEntr);
            }
        } else if (columnType == Types.CLOB) {
            Clob clob = resultSet.getClob(columnLabel);
            if (clob == null) {
                return null;
            } else {
                return this.readCharacterStream(clob.getCharacterStream());
            }
        } else if ((columnType == Types.LONGVARBINARY) || (columnType == Types.BINARY)
                || (columnType == Types.VARBINARY)) {
            InputStream flujoEntr = resultSet.getBinaryStream(columnLabel);
            return this.readBinaryStream(flujoEntr);
        } else if (columnType == Types.LONGVARCHAR) {
            Reader r = resultSet.getCharacterStream(columnLabel);
            return this.readCharacterStream(r);
        } else {
            return resultSet.getObject(columnLabel);
        }
    }

    @Override
    public void generatedKeysToEntityResult(ResultSet resultSet, EntityResult entityResult, List generatedKeys)
            throws Exception {
        try {
            ResultSetMetaData rsMetaData = resultSet.getMetaData();
            // Optimization: Array access, instead of request the name in each
            // loop
            String[] sColumnNames = this.getColumnNames(rsMetaData);

            // Optimization: use column types.
            int[] columnTypes = this.getColumnTypes(rsMetaData);

            Map hColumnTypesAux = new HashMap();
            if (hColumnTypesAux != null) {
                for (int i = 0; i < columnTypes.length; i++) {
                    hColumnTypesAux.put(sColumnNames[i], new Integer(columnTypes[i]));
                }
            }
            entityResult.setColumnSQLTypes(hColumnTypesAux);

            while (resultSet.next()) {
                for (int i = 0; i < sColumnNames.length; i++) {
                    String columnName = sColumnNames[i];
                    Object oValue = resultSet.getObject(columnName);
                    if (oValue != null) {
                        entityResult.put(columnName, oValue);
                    }
                }
            }
            this.changeGenerateKeyNames(entityResult, generatedKeys);
        } catch (Exception e) {
            DefaultSQLStatementHandler.logger.error(null, e);
            throw e;
        }
    }

    protected void changeGenerateKeyNames(EntityResult result, List columnNames) {
        this.changeColumnNames(result, columnNames);
    }

    protected void changeColumnNames(EntityResult result, List columnNames) {
        if (columnNames != null) {
            for (int i = 0; i < columnNames.size(); i++) {
                Object columnName = columnNames.get(i);
                if (columnName != null) {
                    if (!result.containsKey(columnName)) {
                        // Search the same columnName but to uppercase or to
                        // lowercase
                        if (result.containsKey(columnName.toString().toUpperCase())) {
                            this.changeColumnName(result, columnName.toString().toUpperCase(), columnName.toString());
                        } else if (result.containsKey(columnName.toString().toLowerCase())) {
                            this.changeColumnName(result, columnName.toString().toLowerCase(), columnName.toString());
                        }
                    }
                }
            }
        }
    }

    protected void changeColumnName(EntityResult result, String nameColumn, String replaceByColumn) {
        if (result.containsKey(nameColumn)) {
            result.put(replaceByColumn, result.remove(nameColumn));
            Map sqlTypes = result.getColumnSQLTypes();
            if ((sqlTypes != null) && sqlTypes.containsKey(nameColumn)) {
                sqlTypes.put(replaceByColumn, sqlTypes.get(nameColumn));
            }

            List order = result.getOrderColumns();
            if ((order != null) && order.contains(nameColumn)) {
                int index = order.indexOf(nameColumn);
                order.remove(index);
                order.add(index, replaceByColumn);
                result.setColumnOrder(order);
            }
        }
    }

    /**
     * @since 5.2071EN-0.1
     * @param rsMetaData Database metadata. See custom implementation at
     *        {@link MySQLSQLStatementHandler}.
     * @return Column names.
     */
    protected String[] getColumnNames(ResultSetMetaData rsMetaData) {
        String[] sColumnLabels = null;
        try {
            sColumnLabels = new String[rsMetaData.getColumnCount()];
            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                sColumnLabels[i - 1] = rsMetaData.getColumnLabel(i);
            }
        } catch (SQLException e) {
            DefaultSQLStatementHandler.logger.error(null, e);
        }
        return sColumnLabels;
    }

    protected int[] getColumnTypes(ResultSetMetaData rsMetaData) {
        int[] columnTypes = null;
        try {
            columnTypes = new int[rsMetaData.getColumnCount()];
            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                columnTypes[i - 1] = rsMetaData.getColumnType(i);
                if (columnTypes[i - 1] == java.sql.Types.OTHER
                        && "java.util.UUID".equals(rsMetaData.getColumnClassName(i))) {
                    columnTypes[i - 1] = ParseUtilsExtended.UUID;
                }
            }
        } catch (SQLException e) {
            DefaultSQLStatementHandler.logger.error(null, e);
        }
        return columnTypes;
    }

    /**
     * Returns a String containing the characters read from the stream <code>in</code>.
     * <p>
     * No character conversion is performed.
     * @param in
     * @return the characters contained in the stream or null if stream is empty
     */
    protected Object readCharacterStream(InputStream in) {
        if (in == null) {
            return null;
        } else {

            try (BufferedInputStream bIn = new BufferedInputStream(in)){
                int b = -1;
                StringBuilder buff = new StringBuilder();
                while ((b = bIn.read()) != -1) {
                    buff.append((char) b);
                }
                if (buff.length() > 0) {
                    return buff.toString().trim();
                } else {
                    return null;
                }
            } catch (Exception e) {
                DefaultSQLStatementHandler.logger.error(null, e);
                return null;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e) {
                    DefaultSQLStatementHandler.logger.trace(null, e);
                }
            }
        }
    }

    /**
     * Returns a String containing the characters read from the reader <code>in</code>.
     * <p>
     * No character conversion is performed.
     * @param in
     * @return the characters contained in the reader or null if stream is empty
     */
    protected Object readCharacterStream(Reader in) {
        if (in == null) {
            return null;
        } else {
            BufferedReader bIn = null;
            try {
                bIn = new BufferedReader(in);
                int b = -1;
                StringBuilder buff = new StringBuilder();
                while ((b = bIn.read()) != -1) {
                    buff.append((char) b);
                }
                if (buff.length() > 0) {
                    return buff.toString().trim();
                } else {
                    return null;
                }
            } catch (Exception e) {
                DefaultSQLStatementHandler.logger.error(null, e);
                return null;
            } finally {
                try {
                    if (bIn != null) {
                        bIn.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e) {
                    DefaultSQLStatementHandler.logger.trace(null, e);
                }
            }
        }
    }

    /**
     * Reads an array of bytes from the stream <code>in</code>.
     * @param in
     * @return a BytesBlock object containing the byte array or null if the stream is empty
     */
    protected Object readBinaryStream(InputStream in) {
        ByteArrayOutputStream bOut = null;

        if (in == null) {
            return null;
        } else {
            BufferedInputStream bInputStream = null;
            try {
                bOut = new ByteArrayOutputStream(256);
                bInputStream = new BufferedInputStream(in);
                byte[] byteAux = new byte[1];
                while (bInputStream.read(byteAux) > 0) {
                    bOut.write(byteAux);
                }
                bInputStream.close();
                // Now creates a byte array.
                byte[] arrayBytes = bOut.toByteArray();
                if (arrayBytes.length > 0) {
                    if (this.isReturnByteArray()) {
                        return arrayBytes;
                    } else {
                        return new BytesBlock(arrayBytes, BytesBlock.NO_COMPRESSION);
                    }
                } else {
                    return null;
                }
            } catch (Exception e) {
                DefaultSQLStatementHandler.logger.error(null, e);
                return null;
            } finally {
                try {
                    if (bOut != null) {
                        bOut.close();
                    }
                    if (bInputStream != null) {
                        bInputStream.close();
                    }
                } catch (Exception e) {
                    DefaultSQLStatementHandler.logger.trace(null, e);
                }
            }
        }
    }

    /**
     * Sets a parameter value in a prepared statement, at index <code>index</code>
     * <p>
     * The methods used are:
     * <ul>
     * <li>setBlob for Blob objects</li>
     * <li>setClob for Clob objects</li>
     * <li>setBinaryStream for BytesBlock objects</li>
     * <li>setCharacterStream for com.ontimize.jee.common.gui.LongString</li>
     * <li>setNull for com.ontimize.jee.common.db.NullValue</li>
     * <li>setObject for all other object</li>
     * </ul>
     * <p>
     * <br>
     * @param index
     * @param value
     * @param preparedStatement
     * @param truncDates
     * @throws SQLException
     */

    @Override
    public void setObject(int index, Object value, PreparedStatement preparedStatement, boolean truncDates)
            throws SQLException {
        if (value == null) {
            DefaultSQLStatementHandler.logger.debug(" setObject {} with NULL", index);
            preparedStatement.setObject(index, value);
        } else if (value instanceof Blob) {
            preparedStatement.setBlob(index, (Blob) value);
        } else if (value instanceof Clob) {
            preparedStatement.setClob(index, (Clob) value);
        } else if (value instanceof BytesBlock) {
            byte[] bytes = ((BytesBlock) value).getBytes();
            preparedStatement.setBinaryStream(index, new ByteArrayInputStream(bytes), bytes.length);
        } else if (value instanceof LongString) {
            String sValue = ((LongString) value).getString();
            preparedStatement.setCharacterStream(index, new StringReader(sValue), sValue.length());
        } else if ((value instanceof String) && (((String) value).length() > 255)) {
            DefaultSQLStatementHandler.logger.debug(" setObject {}  with string length: {}", index,
                    ((String) value).length());
            if (USE_SETSTRING_LONG_STRINGS) {
                preparedStatement.setString(index, (String) value);
            } else {
                preparedStatement.setCharacterStream(index, new StringReader((String) value),
                        ((String) value).length());
            }
        } else if (value instanceof NullValue) {
            DefaultSQLStatementHandler.logger.debug("setObject {} with NULL", index);
            try {
                preparedStatement.setNull(index, ((NullValue) value).getSQLDataType());
            } catch (java.sql.SQLException e) {
                DefaultSQLStatementHandler.logger.trace(null, e);
                preparedStatement.setBytes(index, new byte[0]);
            }
        } else if (value instanceof Timestamp) {
            this.setTimestampObject((Timestamp) value, index, preparedStatement, truncDates);
        } else if (value instanceof Time) {
            DefaultSQLStatementHandler.logger.debug(" setTime {}  with Time ", index, value);
            Time oNewDate = (Time) value;
            preparedStatement.setTime(index, oNewDate);
        } else if (value instanceof java.sql.Date) {
            DefaultSQLStatementHandler.logger.debug(" setDate {}  with java.sql.Date ", index, value);
            Date oNewDate = (Date) value;
            preparedStatement.setDate(index, oNewDate);
        } else if (value instanceof java.util.Date) {
            DefaultSQLStatementHandler.logger.debug(" setObject {}  with java.util.Date ", index, value);
            Object oNewDate = value;
            oNewDate = new Timestamp(((java.util.Date) value).getTime());
            if (!truncDates) {
                preparedStatement.setObject(index, oNewDate);
            } else {
                preparedStatement.setObject(index, truncateDataToHour_00_00_00((java.util.Date) oNewDate));
            }
        } else {
            DefaultSQLStatementHandler.logger.debug(" setObject {} with value {}", index, value);
            preparedStatement.setObject(index, value);
        }
    }

    protected void setTimestampObject(Timestamp value, int index, PreparedStatement preparedStatement,
            boolean truncDates) throws SQLException {
        DefaultSQLStatementHandler.logger.debug(" setTimestamp {}  with Timestamp ", index, value);
        Timestamp oNewDate = value;
        if (!truncDates) {
            preparedStatement.setTimestamp(index, oNewDate);
        } else {
            preparedStatement.setTimestamp(index, truncateDataToHour_00_00_00(oNewDate));
        }
    }

    @Override
    public String addMultilanguageLeftJoinTables(String table, List tables, LinkedHashMap keys, LocalePair localeId)
            throws SQLException {
        // TODO Auto-generated method stub
        StringBuilder buffer = new StringBuilder();
        buffer.append(table);
        Set keysKV = keys.keySet();
        if (keysKV.isEmpty()) {
            DefaultSQLStatementHandler.logger.error("LocaleTablesKeys from {} table is empty", table);
            throw new SQLException();
        }

        Iterator itr = keysKV.iterator();
        for (int i = 0; i < tables.size(); i++) {
            Object keyKVActual = itr.next();
            Object valueKVActual = keys.get(keyKVActual);
            buffer.append(SQLStatementBuilder.LEFT_JOIN);
            buffer.append(tables.get(i));
            buffer.append(SQLStatementBuilder.ON);
            buffer.append(tables.get(i) + "." + keyKVActual);
            buffer.append(" = ");
            buffer.append(table + "." + valueKVActual);
            buffer.append(SQLStatementBuilder.AND);
            buffer.append(tables.get(i) + "." + localeId.getKey());
            buffer.append(" = ? ");
        }

        return buffer.toString();
    }

    @Override
    public String addInnerMultilanguageColumns(String subSqlQuery, List attributes, Map hLocaleTablesAV) {
        Enumeration av = Collections.enumeration(hLocaleTablesAV.keySet());
        StringBuilder buffer = new StringBuilder();
        buffer.append(subSqlQuery);
        while (av.hasMoreElements()) {
            Object avActualKey = av.nextElement();
            if (!attributes.contains(avActualKey)) {
                Object avActualValue = hLocaleTablesAV.get(avActualKey);
                int index = subSqlQuery.toLowerCase().indexOf(" from");
                buffer.insert(index, ", " + avActualKey + SQLStatementBuilder.AS + avActualValue);
            }
        }
        return buffer.toString();
    }

    @Override
    public String addOuterMultilanguageColumns(String sqlQuery, String table, Map hLocaleTablesAV) {
        Enumeration av = Collections.enumeration(hLocaleTablesAV.keySet());
        StringBuilder buffer = new StringBuilder();
        buffer.append(sqlQuery);
        while (av.hasMoreElements()) {
            Object avActualKey = av.nextElement();
            Object avActualValue = hLocaleTablesAV.get(avActualKey);
            int index = sqlQuery.toLowerCase().indexOf(" from");
            buffer.insert(index, ", " + table + "." + avActualValue);
        }
        return buffer.toString();
    }

    @Override
    public String addOuterMultilanguageColumnsPageable(String sqlQuery, String table, Map hLocaleTablesAV) {
        return this.addOuterMultilanguageColumns(sqlQuery, table, hLocaleTablesAV);
    }

    public boolean isReturnByteArray() {
        return DefaultSQLStatementHandler.returnByteArray;
    }

    public void setReturnByteArray(boolean returnBytes) {
        DefaultSQLStatementHandler.returnByteArray = returnBytes;
    }

    @Override
    public String convertPaginationStatement(String sqlTemplate, int startIndex, int recordNumber) {
        return sqlTemplate;
    }

    @Override
    public String addCastStatement(final String expression, final int fromSqlType, final int toSqlType) {
        return expression;
    }

    /**
     * Sets the following fields of the object <code>date</code> to zero:
     * <p>
     * Calendar.MILLISECOND,Calendar.SECOND
     * <p>
     * The default calendar (for the default locale and timezone) is used.
     * @param date
     * @return a new Timestamp object with the same time as <code>date</code> except the fields set to
     *         zero.
     */
    public static synchronized Timestamp truncateDateToMinutes(java.util.Date date) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        return new Timestamp(calendar.getTime().getTime());
    }

    /**
     * Sets the following fields of the object <code>date</code> to zero:
     * <p>
     * Calendar.MILLISECOND,Calendar.SECOND,Calendar.MINUTE,Calendar.HOUR_OF_DAY
     * <p>
     * The default calendar (for the default locale and timezone) is used.
     * @param date
     * @return a new Timestamp object with the same time as <code>date</code> except the fields set to
     *         zero.
     */
    public static synchronized Timestamp truncateDataToHour_00_00_00(java.util.Date date) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return new Timestamp(calendar.getTime().getTime());
    }

}
