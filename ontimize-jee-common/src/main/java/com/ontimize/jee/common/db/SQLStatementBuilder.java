package com.ontimize.jee.common.db;

import com.ontimize.jee.common.db.handler.DefaultSQLStatementHandler;
import com.ontimize.jee.common.db.handler.HSQLDBSQLStatementHandler;
import com.ontimize.jee.common.db.handler.MySQLSQLStatementHandler;
import com.ontimize.jee.common.db.handler.OracleSQLStatementHandler;
import com.ontimize.jee.common.db.handler.PostgresSQLStatementHandler;
import com.ontimize.jee.common.db.handler.SQLServerSQLStatementHandler;
import com.ontimize.jee.common.db.handler.SQLStatementHandler;
import com.ontimize.jee.common.gui.SearchValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The <code>SQLStatementBuilder</code> class builds SQL statements
 * <p>
 * This class is used by every entity to build the SQL Statements needed to query the database.
 *
 * @author Imatia Innovation S.L.
 */

public abstract class SQLStatementBuilder {

    public static final String DEFAULT_HANDLER = "default";
    public static final String POSTGRES_HANDLER = "Postgres";
    public static final String SQLSERVER_HANDLER = "SQLServer";
    public static final String ORACLE_HANDLER = "Oracle";
    public static final String HSQLDB_HANDLER = "HSQLDB";
    public static final String ACCESS_HANDLER = "Access";
    public static final String MYSQL_HANDLER = "MySQL";
    public static final String SELECT = " SELECT ";
    public static final String DISTINCT = " DISTINCT ";
    public static final String WHERE = " WHERE ";
    public static final String ON = " ON ";
    public static final String FROM = " FROM ";
    public static final String LIKE = " LIKE ";
    public static final String NOT_LIKE = " NOT LIKE ";
    public static final String AND = " AND ";
    public static final String OR = " OR ";
    public static final String LEFT_JOIN = " LEFT JOIN ";
    public static final String INNER_JOIN = " INNER JOIN ";
    public static final String IN = " IN ";
    public static final String NOT_IN = " NOT IN ";
    public static final String COMMA = " , ";

    // * The interface that defines how the conditions must be defined to
    // * pass to SQLConditionValuesProcessor
    // *
    // * The interface that defines the construction of the conditions
    // * to be passed to SQLConditionValuesProcessor
    // *
    // * This interface must be implemented by each class that want defining
    // * a expression to be processed in SQLConditionValuesProcessor
    // *
    // * This interface must be implemented by every class that is going to
    // define
    // * a expression to be processed in SQLConditionValuesProcessor
    public static final String QUESTIONMARK = " ? ";
    public static final String OPEN_PARENTHESIS = " ( ";
    public static final String CLOSE_PARENTHESIS = " ) ";
    public static final String ASTERISK = " * ";
    public static final String OPEN_SQUARE_BRACKET = " [";
    public static final String CLOSE_SQUARE_BRACKET = "] ";
    public static final String EQUAL_XQUESTIONMARK = " = ? ";
    public static final String NOT_EQUAL_XQUESTIONMARK = " <> ? ";
    public static final String EQUAL = " = ";
    public static final String NOT_EQUAL = " <> ";
    public static final String NOT_EQUAL_ID = "<>";
    public static final String IS_NULL = " IS NULL ";
    public static final String NULL = " NULL ";
    public static final String MORE_EQUAL = " >= ";
    public static final String LESS_EQUAL = " <= ";
    public static final String MORE_EQUAL_XQUESTIONMARK = " >= ? ";
    public static final String LESS_EQUAL_XQUESTIONMARK = " <= ? ";
    public static final String LESS_XQUESTIONMARK = " < ? ";

    // // STATIC VALUES FOR QUERIES: These include blanks to avoid problems
    public static final String MORE_XQUESTIONMARK = " > ? ";
    public static final String AS = " AS ";
    public static final String COUNT_COLUMN = " COUNT( ";
    public static final String COUNT_COLUMN_NAME = "TotalRecordNumber";
    public static final String COUNT = " COUNT(*) AS \"" + SQLStatementBuilder.COUNT_COLUMN_NAME + "\" ";
    public static final String TOP = " TOP ";
    public static final String ORDER_BY = " ORDER BY ";
    public static final String DESC = " DESC ";
    public static final String INSERT_INTO = " INSERT INTO ";
    public static final String VALUES = " VALUES ";
    public static final String DELETE_FROM = "DELETE  FROM ";
    public static final String UPDATE = " UPDATE ";
    public static final String SET = " SET ";
    public static final String SPACE = " ";
    public static final String PIPES = "||";
    public static final String GENERATED_KEY_COLUMN_NAME = "GENERATED_KEY";
    public static final char ASTERISK_CHAR = '*';
    public static final char INTERROG = '?';
    public static final char PERCENT = '%';
    public static final char LOW_LINE = '_';
    protected static final Map<String, SQLStatementHandler> registerHandlers = new HashMap<>();
    static final Logger logger = LoggerFactory.getLogger(SQLStatementBuilder.class);
    /**
     * @see DefaultSQLStatementHandler#useAsInSubqueries
     * @deprecated Use:
     */
    @Deprecated(since = "5.3.2")
    public static boolean useAsInSubqueries = true;
    /**
     * Debugging purposes variable. If enabled, debugging information will be sent to standard output.
     */
    public static boolean checkParenthesis = false;

    static {
        SQLStatementBuilder.registerHandlers.put(SQLStatementBuilder.DEFAULT_HANDLER, new DefaultSQLStatementHandler());
        SQLStatementBuilder.registerHandlers.put(SQLStatementBuilder.POSTGRES_HANDLER,
                new PostgresSQLStatementHandler());
        SQLStatementBuilder.registerHandlers.put(SQLStatementBuilder.SQLSERVER_HANDLER,
                new SQLServerSQLStatementHandler());
        SQLStatementBuilder.registerHandlers.put(SQLStatementBuilder.HSQLDB_HANDLER, new HSQLDBSQLStatementHandler());
        SQLStatementBuilder.registerHandlers.put(SQLStatementBuilder.ORACLE_HANDLER, new OracleSQLStatementHandler());
        SQLStatementBuilder.registerHandlers.put(SQLStatementBuilder.MYSQL_HANDLER, new MySQLSQLStatementHandler());
    }

    public static SQLStatementHandler getSQLStatementHandler(String handlerID) {
        if (SQLStatementBuilder.registerHandlers.containsKey(handlerID)) {
            return SQLStatementBuilder.registerHandlers.get(handlerID);
        }
        SQLStatementBuilder.logger.error("SQLStatementHandler {} isn't registered", handlerID);
        return SQLStatementBuilder.registerHandlers.get(SQLStatementBuilder.DEFAULT_HANDLER);
    }

    public static void registerSQLStatementHandler(String handlerID, SQLStatementHandler handler) {
        SQLStatementBuilder.registerHandlers.put(handlerID, handler);
    }

    public static boolean isUseAsInSubqueries() {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER).isUseAsInSubqueries();
    }

    public static void setUseAsInSubqueries(boolean useAsInSubqueries) {
        SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .setUseAsInSubqueries(useAsInSubqueries);
    }

    public static StringBuilder createQueryConditionsSearchValue(Object oValue, List<Object> values, boolean bracket,
                                                                 Object oKey) {

        StringBuilder sbStringQuery = new StringBuilder();
        if (((SearchValue) oValue).getCondition() == SearchValue.OR) {
            if (createQueryConditionSearchValueOR((SearchValue) oValue, values, bracket, oKey, sbStringQuery)) {
                return null;
            }
        } else if ((((SearchValue) oValue).getCondition() == SearchValue.IN)
                || (((SearchValue) oValue).getCondition() == SearchValue.EXISTS) || (((SearchValue) oValue)
                .getCondition() == SearchValue.NOT_IN)) {
            if (createQueryConditionSearchValueExistsNotIn(oValue, values, oKey, sbStringQuery)) {
                return null;
            }
        } else if (((SearchValue) oValue).getCondition() == SearchValue.BETWEEN) {
            sbStringQuery.append(SQLStatementBuilder.createBETWEENQueryConditions(oValue, values, bracket, oKey));
        } else if (((SearchValue) oValue).getCondition() == SearchValue.NOT_BETWEEN) {
            sbStringQuery.append(SQLStatementBuilder.createNOTBETWEENQueryConditions(oValue, values, bracket, oKey));
        } else {
            sbStringQuery.append(SQLStatementBuilder.createOtherQueryConditions(oValue, values, bracket, oKey));
        }
        return sbStringQuery;
    }

    /**
     * Method extracted to reduce cognitive complexity of {@link #createQueryConditionsSearchValue(Object, List, boolean, Object)}
     * @param oValue
     * @param values
     * @param oKey
     * @param sbStringQuery
     * @return
     */
    private static boolean createQueryConditionSearchValueExistsNotIn(Object oValue, List<Object> values, Object oKey, StringBuilder sbStringQuery) {
        // If it is IN, then value is a List with
        // Objects
        Object oSearchValue = ((SearchValue) oValue).getValue();
        if (oSearchValue instanceof List) {
            List<Object> vSearchValue = (List<Object>) oSearchValue;
            if (!vSearchValue.isEmpty()) {
                sbStringQuery.append(SQLStatementBuilder.createINQueryConditionsListInstance(oValue, values, oKey,
                        vSearchValue));
            } else {
                return true;
            }
        } else if (oSearchValue instanceof String) {
            sbStringQuery
                    .append(SQLStatementBuilder.createINQueryConditionsStringInstance(oValue, oKey, oSearchValue));
        } else if (oSearchValue instanceof SQLExpression) {
            sbStringQuery.append(SQLStatementBuilder.createINQueryConditionsSQLExpressionInstance(oValue, values,
                    oKey, oSearchValue));

        } else {
            return true;
        }
        return false;
    }

    /**
     * Method extracted to reduce cognitive complexity of {@link #createQueryConditionsSearchValue(Object, List, boolean, Object)}
     * @param oValue
     * @param values
     * @param bracket
     * @param oKey
     * @param sbStringQuery
     * @return
     */
    private static boolean createQueryConditionSearchValueOR(SearchValue oValue, List<Object> values, boolean bracket, Object oKey, StringBuilder sbStringQuery) {
        // If it is OR, then value is a List with
        // objects
        Object oSearchValue = oValue.getValue();
        if (oSearchValue instanceof List) {
            List<Object> vSearchValues = (List<Object>) oSearchValue;
            if (!vSearchValues.isEmpty()) {
                sbStringQuery
                        .append(SQLStatementBuilder.createORQueryConditions(values, bracket, oKey, vSearchValues));
            } else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * Method to reduce the complexity of
     * {@link #createQueryConditionsSearchValue(Object, List, boolean, Object)}
     *
     * @param values
     * @param bracket
     * @param oKey
     * @param sbStringQuery
     * @param vSearchValues
     */
    protected static String createORQueryConditions(List<Object> values, boolean bracket, Object oKey, List<Object> vSearchValues) {

        StringBuilder sbStringQuery = new StringBuilder();

        sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
        for (int vs = 0; vs < vSearchValues.size(); vs++) {
            if (vSearchValues.get(vs) != null) {
                values.add(vSearchValues.get(vs));
                if (bracket) {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.EQUAL_XQUESTIONMARK);
                } else {
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.EQUAL_XQUESTIONMARK);
                }
            } else {
                if (bracket) {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.IS_NULL);
                } else {
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.IS_NULL);
                }
            }
            if (vs < (vSearchValues.size() - 1)) {
                sbStringQuery.append(SQLStatementBuilder.OR);
                // In the last loop do not
                // increment i
            }
        }
        sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);

        return sbStringQuery.toString();
    }

    /**
     * Method to reduce the complexity of
     * {@link #createQueryConditionsSearchValue(Object, List, boolean, Object)}
     *
     * @param oValue
     * @param values
     * @param oKey
     * @param sbStringQuery
     * @param vSearchValue
     */
    protected static String createINQueryConditionsListInstance(Object oValue, List<Object> values, Object oKey,
                                                                List<Object> vSearchValue) {
        StringBuilder sbStringQuery = new StringBuilder();
        sbStringQuery.append(oKey);
        sbStringQuery.append(" ");
        sbStringQuery.append(SearchValue.conditionIntToStr(((SearchValue) oValue).getCondition()));
        sbStringQuery.append(" ");
        sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
        for (int vs = 0; vs < vSearchValue.size(); vs++) {
            if (vSearchValue.get(vs) != null) {
                values.add(vSearchValue.get(vs));
                sbStringQuery.append(SQLStatementBuilder.QUESTIONMARK);
            } else {
                sbStringQuery.append(SQLStatementBuilder.NULL);
            }
            if (vs < (vSearchValue.size() - 1)) {
                // In the last loop do not
                // increment i
                sbStringQuery.append(SQLStatementBuilder.COMMA);
            }
        }
        sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);

        return sbStringQuery.toString();
    }

    /**
     * Method to reduce the complexity of
     * {@link #createQueryConditionsSearchValue(Object, List, boolean, Object)}
     *
     * @param oValue
     * @param oKey
     * @param sbStringQuery
     * @param oSearchValue
     */
    protected static String createINQueryConditionsStringInstance(Object oValue, Object oKey, Object oSearchValue) {
        StringBuilder sbStringQuery = new StringBuilder();
        String sQuery = (String) oSearchValue;
        putSearchValueBetweenParenthesis((SearchValue) oValue, oKey, sbStringQuery, sQuery);

        return sbStringQuery.toString();
    }

    private static void putSearchValueBetweenParenthesis(SearchValue oValue, Object oKey, StringBuilder sbStringQuery, String sQuery) {
        boolean putParenthesis = true;
        if (SQLStatementBuilder.checkParenthesis && DefaultSQLConditionValuesProcessor.hasParenthesis(sQuery)) {
            putParenthesis = false;
        }
        sbStringQuery.append(oKey);
        sbStringQuery.append(" ");
        sbStringQuery.append(SearchValue.conditionIntToStr(oValue.getCondition()));
        sbStringQuery.append(" ");
        if (putParenthesis) {
            sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
        }
        sbStringQuery.append(sQuery);
        if (putParenthesis) {
            sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
        }
    }

    /**
     * Method to reduce the complexity of
     * {@link #createQueryConditionsSearchValue(Object, List, boolean, Object)}
     *
     * @param oValue
     * @param values
     * @param oKey
     * @param sbStringQuery
     * @param oSearchValue
     */
    protected static String createINQueryConditionsSQLExpressionInstance(Object oValue, List<Object> values, Object oKey,
                                                                         Object oSearchValue) {
        String sQuery = ((SQLExpression) oSearchValue).getQuery();
        StringBuilder sbStringQuery = new StringBuilder();
        boolean putParenthesis = true;
        if (SQLStatementBuilder.checkParenthesis && DefaultSQLConditionValuesProcessor.hasParenthesis(sQuery)) {
            putParenthesis = false;
        }

        sbStringQuery.append(oKey);
        sbStringQuery.append(" ");
        sbStringQuery.append(SearchValue.conditionIntToStr(((SearchValue) oValue).getCondition()));
        sbStringQuery.append(" ");
        if (putParenthesis) {
            sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
        }
        sbStringQuery.append(sQuery);
        if (putParenthesis) {
            sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
        }
        if (((SQLExpression) oSearchValue).getValues() != null) {
            for (int vs = 0; vs < ((SQLExpression) oSearchValue).getValues().size(); vs++) {
                if (((SQLExpression) oSearchValue).getValues().get(vs) != null) {
                    values.add(((SQLExpression) oSearchValue).getValues().get(vs));
                }
                if (vs < (((SQLExpression) oSearchValue).getValues().size() - 1)) {
                    // In the last loop do not
                    // increment i
                }
            }
        }

        return sbStringQuery.toString();
    }

    /**
     * Method to reduce the complexity of
     * {@link #createQueryConditionsSearchValue(Object, List, boolean, Object)}
     *
     * @param oValue
     * @param values
     * @param bracket
     * @param oKey
     * @param sbStringQuery
     */
    protected static String createBETWEENQueryConditions(Object oValue, List<Object> values, boolean bracket, Object oKey) {
        // If it is OR, then value is a List with
        // objects
        StringBuilder sbStringQuery = new StringBuilder();
        Object oSearchValue = ((SearchValue) oValue).getValue();
        if (oSearchValue instanceof List) {
            List<Object> vSearchValues = (List) oSearchValue;
            if ((vSearchValues.size() >= 2) && (vSearchValues.get(0) != null) && (vSearchValues.get(1) != null)) {

                openParenthesisAndBrackets(values, bracket, oKey, sbStringQuery, vSearchValues);
                sbStringQuery.append(SQLStatementBuilder.AND);
                values.add(vSearchValues.get(1));
                if (bracket) {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.LESS_EQUAL_XQUESTIONMARK);
                } else {
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.LESS_EQUAL_XQUESTIONMARK);
                }
                sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
            }
        }

        return sbStringQuery.toString();
    }

    /**
     * This method is used to avoid code duplication
     * @param values
     * @param bracket
     * @param oKey
     * @param sbStringQuery
     * @param vSearchValues
     */
    private static void openParenthesisAndBrackets(List<Object> values, boolean bracket, Object oKey, StringBuilder sbStringQuery, List<Object> vSearchValues) {
        sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);

        values.add(vSearchValues.get(0));
        if (bracket) {
            sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
            sbStringQuery.append(oKey);
            sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
            sbStringQuery.append(SQLStatementBuilder.MORE_EQUAL_XQUESTIONMARK);
        } else {
            sbStringQuery.append(oKey);
            sbStringQuery.append(SQLStatementBuilder.MORE_EQUAL_XQUESTIONMARK);
        }
    }

    /**
     * Method to reduce the complexity of
     * {@link #createQueryConditionsSearchValue(Object, List, boolean, Object)}
     *
     * @param oValue
     * @param values
     * @param bracket
     * @param oKey
     * @param sbStringQuery
     */
    protected static String createNOTBETWEENQueryConditions(Object oValue, List<Object> values, boolean bracket,
                                                            Object oKey) {
        // If it is OR, then value is a List with
        // objects
        StringBuilder sbStringQuery = new StringBuilder();
        Object oSearchValue = ((SearchValue) oValue).getValue();
        if (oSearchValue instanceof List) {
            List<Object> vSearchValues = (List) oSearchValue;
            if ((vSearchValues.size() >= 2) && (vSearchValues.get(0) != null) && (vSearchValues.get(1) != null)) {

                sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);

                values.add(vSearchValues.get(0));
                if (bracket) {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.LESS_XQUESTIONMARK);
                } else {
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.LESS_XQUESTIONMARK);
                }
                sbStringQuery.append(SQLStatementBuilder.OR);

                values.add(vSearchValues.get(1));
                if (bracket) {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.MORE_XQUESTIONMARK);
                } else {
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.MORE_XQUESTIONMARK);
                }
                sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
            }
        }

        return sbStringQuery.toString();
    }

    /**
     * Method to reduce the complexity of
     * {@link #createQueryConditionsSearchValue(Object, List, boolean, Object)}
     *
     * @param oValue
     * @param values
     * @param bracket
     * @param oKey
     * @param sbStringQuery
     */
    protected static String createOtherQueryConditions(Object oValue, List<Object> values, boolean bracket, Object oKey) {
        StringBuilder sbStringQuery = new StringBuilder();
        Object oSearchValue = ((SearchValue) oValue).getValue();
        if (oSearchValue != null) {
            values.add(oSearchValue);
        }

        // This is the last pair key-value
        if (bracket) {
            sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
            sbStringQuery.append(oKey);
            sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
            sbStringQuery.append(((SearchValue) oValue).getStringCondition());
            if (oSearchValue != null) {
                sbStringQuery.append(SQLStatementBuilder.QUESTIONMARK);
            }
        } else {
            sbStringQuery.append(oKey);
            sbStringQuery.append(SQLStatementBuilder.SPACE);
            sbStringQuery.append(((SearchValue) oValue).getStringCondition());
            if (oSearchValue != null) {
                sbStringQuery.append(SQLStatementBuilder.QUESTIONMARK);
            }
        }

        return sbStringQuery.toString();
    }

    /**
     * Sets the <code>SQLConditionValuesProcessor<code> to be used in <code>SQLStatementBuilder</code>
     *
     * @param processor the SQLConditionValuesProcessor instance that be used
     */
    public static void setSQLConditionValuesProcessor(SQLConditionValuesProcessor processor) {
        SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .setSQLConditionValuesProcessor(processor);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a query
     * that obtains the number of query records
     *
     * @param table      name of the table the query is executed against
     * @param conditions condition list used in the query
     * @param wildcards  column list that can use wildcards
     * @return a <code>SQLStatement</code> class
     */

    public static SQLStatement createCountQuery(String table, Map<Object, Object> conditions, List<Object> wildcards,
                                                List<Object> countColumns) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createCountQuery(table, conditions, wildcards, countColumns);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @param table            name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions       condition list used to created the query
     * @param wildcards
     * @return a <code>SQLStatement</code> class
     * @see SQLStatementBuilder.SQLStatement
     */
    public static SQLStatement createSelectQuery(String table, List<Object> requestedColumns, Map<Object, Object> conditions,
                                                 List<Object> wildcards) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createSelectQuery(table, requestedColumns, conditions, wildcards);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @param table            name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions       condition list used to created the query
     * @param wildcards        column list that can use wildcards
     * @param columnSorting    column list where query sorting is established
     * @param recordCount      number of records requested in the query
     * @return a <code>SQLStatement</code> class
     * @see SQLStatementBuilder.SQLStatement
     */

    public static SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
                                                 List wildcards, List columnSorting, int recordCount) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting,
                        recordCount);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @param table            name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions       condition list used to created the query
     * @param wildcards        column list that can use wildcards
     * @param columnSorting    column list where query sorting is established
     * @param recordCount      number of records requested in the query
     * @param descending       true if sorting should be descending
     * @return a <code>SQLStatement</code> class
     * @see SQLStatementBuilder.SQLStatement
     */

    public static SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
                                                 List wildcards, List columnSorting, int recordCount,
                                                 boolean descending) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting,
                        descending);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @param table            name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions       condition list used to created the query
     * @param wildcards        column list that can use wildcards
     * @param columnSorting    column list where query sorting is established
     * @param recordCount      number of records requested in the query
     * @param forceDistinct    true if query result cannot have duplicated records
     * @param descending       true if sorting should be descending
     * @return a <code>SQLStatement</code> class
     * @see SQLStatementBuilder.SQLStatement
     */

    public static SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
                                                 List wildcards, List columnSorting, int recordCount,
                                                 boolean descending, boolean forceDistinct) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting,
                        recordCount, descending, forceDistinct);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @param table            name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions       condition list used to created the query
     * @param wildcards        column list that can use wildcards
     * @param columnSorting    column list where query sorting is established
     * @return a <code>SQLStatement</code> class
     * @see SQLStatementBuilder.SQLStatement
     */

    public static SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
                                                 List wildcards, List columnSorting) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @param table            name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions       condition list used to created the query
     * @param wildcards        column list that can use wildcards
     * @param columnSorting    column list where query sorting is established
     * @param descending       true if sorting should be descending
     * @return a <code>SQLStatement</code> class
     * @see SQLStatementBuilder.SQLStatement
     */

    public static SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
                                                 List wildcards, List columnSorting, boolean descending) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting,
                        descending);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query.
     *
     * @param table            name of the table the query is executed against
     * @param requestedColumns a List specifying the requested column name in the query
     * @param conditions       condition list used to created the query
     * @param wildcards        column list that can use wildcards
     * @param columnSorting    column list where query sorting is established
     * @param forceDistinct    true if query result cannot have duplicated records
     * @param descending       true if sorting should be descending
     * @return a <code>SQLStatement</code> class
     * @see SQLStatementBuilder.SQLStatement
     */

    public static SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
                                                 List wildcards, List columnSorting, boolean descending,
                                                 boolean forceDistinct) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting,
                        descending, forceDistinct);
    }

    public static SQLNameEval getSQLNameEval() {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER).getSQLNameEval();
    }

    public static void setSQLNameEval(SQLNameEval eval) {
        SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER).setSQLNameEval(eval);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a insert
     * query.
     *
     * @param table      name of the table the query is executed against
     * @param attributes attributes a Map specifying pairs of key-value corresponding to the attribute
     *                   (or column of a table in a database) and the value that must be stored.
     * @return
     */

    public static SQLStatement createInsertQuery(String table, Map attributes) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createInsertQuery(table, attributes);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a update
     * query.
     *
     * @param table            name of the table the update is executed against
     * @param attributesValues attributesValues the data for updating the records to. The keys specify
     *                         the attributes (or columns) and the values, the values for these columns.
     * @param keysValues       keysValues the conditions that the records to be updated must fulfill. The keys
     *                         specify the attributes (or columns) and the values, the values for these columns.
     * @return
     * @see SQLStatementBuilder.SQLStatement
     */

    public static SQLStatement createUpdateQuery(String table, Map attributesValues, Map keysValues) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createUpdateQuery(table, attributesValues, keysValues);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a delete
     * query.
     *
     * @param table      name of the table the query is executed against
     * @param keysValues the conditions that the records to be deleted must fulfill. The keys specify
     *                   the attributes (or columns) and the values, the values for these columns.
     * @return
     */

    public static SQLStatement createDeleteQuery(String table, Map keysValues) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createDeleteQuery(table, keysValues);
    }

    /**
     * Adds characters to the list of special characters.
     * <p>
     * When the query is be created if a column name contains a character of the list this column name
     * is inserted between parenthesis in the query.
     *
     * @param c a array with the new characters
     */

    public static void addSpecialCharacters(char[] c) {
        SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER).addSpecialCharacters(c);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query against two table used a join.
     *
     * @param principalTable                 name of the principal table the query is executed against
     * @param secondaryTable                 name of the secondary table the query is executed against
     * @param principalKeys                  a List specifying the column names of the principal table that be used to
     *                                       combine the two tables
     * @param secondaryKeys                  a List specifying the column names of the secondary table that be used to
     *                                       combine the two tables
     * @param principalTableRequestedColumns column list that be requested in the query from principal
     *                                       table
     * @param secondaryTableRequestedColumns column list that be requested in the query from secondary
     *                                       table
     * @param principalTableConditions       a Map specifying conditions that must comply the set of records
     *                                       returned from principal table
     * @param secondaryTableConditions       a Map specifying conditions that must comply the set of records
     *                                       returned from secondary table
     * @param wildcards                      column list which wildcards can be used in
     * @param columnSorting                  column list where query sorting is established
     * @param forceDistinct                  true if query result cannot have duplicated records
     * @return a <code>SQLStatement</code> class which stores the SQL Statement and the required values
     */

    public static SQLStatement createJoinSelectQuery(String principalTable, String secondaryTable, List principalKeys,
                                                     List secondaryKeys,
                                                     List principalTableRequestedColumns, List secondaryTableRequestedColumns,
                                                     Map principalTableConditions, Map secondaryTableConditions, List wildcards,
                                                     List columnSorting, boolean forceDistinct) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createJoinSelectQuery(principalTable, secondaryTable, principalKeys, secondaryKeys,
                        principalTableRequestedColumns, secondaryTableRequestedColumns, principalTableConditions,
                        secondaryTableConditions, wildcards, columnSorting, forceDistinct);
    }

    /**
     * Returns a <code>String</code> with the qualified name.
     * <p>
     * This static methods creates a qualified name from the column name and the table name. The used
     * pattern is table_name.column_name. If the table name or the column name has special characters,
     * square brackets are used [table_name].[column_name]
     *
     * @param col   a String with the column name
     * @param table a String with the table name
     * @return a String with the qualified name.
     */

    public static String qualify(String col, String table) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER).qualify(col, table);
    }

    /**
     * Returns a <code>SQLStatement</code> class that stores the information needed to execute a select
     * query against two table used a join.
     *
     * @param mainTable                      name of the principal table the query is executed against
     * @param secondaryTable                 name of the secondary table the query is executed against
     * @param mainKeys                       a List specifying the column names of the principal table that be used to combine
     *                                       the two tables
     * @param secondaryKeys                  a List specifying the column names of the secondary table that be used to
     *                                       combine the two tables
     * @param mainTableRequestedColumns      column list that be requested in the query from principal table
     * @param secondaryTableRequestedColumns column list that be requested in the query from secondary
     *                                       table
     * @param mainTableConditions            a Map specifying conditions that must comply the set of records
     *                                       returned from principal table
     * @param secondaryTableConditions       a Map specifying conditions that must comply the set of records
     *                                       returned from secondary table
     * @param wildcards                      column list which wildcards can be used in
     * @param columnSorting                  column list where query sorting is established
     * @param forceDistinct                  true if query result cannot have duplicated records
     * @param descending                     true if sorting should be descending
     * @return a <code>SQLStatement</code> class which stores the SQL Statement and the required values
     */

    public static SQLStatement createJoinSelectQuery(String mainTable, String secondaryTable, List mainKeys,
                                                     List secondaryKeys, List mainTableRequestedColumns,
                                                     List secondaryTableRequestedColumns, Map mainTableConditions, Map secondaryTableConditions,
                                                     List wildcards, List columnSorting, boolean forceDistinct,
                                                     boolean descending) {

        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createJoinSelectQuery(mainTable, secondaryTable, mainKeys, secondaryKeys,
                        mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions,
                        secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
    }

    /**
     * Returns a instance of <code>SQLConditionValuesProcessor</code> that be used in this class to
     * create the condition string
     *
     * @return a <code>SQLConditionValuesProcessor</code> class
     */
    public static SQLConditionValuesProcessor getQueryConditionsProcessor() {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .getQueryConditionsProcessor();
    }

    public static String createQueryConditionsWithoutWhere(Map conditions, List wildcard, List values) {
        return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER)
                .createQueryConditionsWithoutWhere(conditions, wildcard, values);
    }

    public static interface SQLNameEval extends java.io.Serializable {

        public boolean needCorch(String s);

    }

    /**
     * This interface defines the basic methods that must be implemented by any class that defines a
     * expression to be processed in SQLConditionValuesProcessor
     *
     * @author Imatia Innovation S.L.
     */
    public static interface Expression extends java.io.Serializable {

        public Object getLeftOperand();

        public void setLeftOperand(Object o);

        public Object getRightOperand();

        public void setRightOperand(Object o);

        public Operator getOperator();

        public void setOperator(Operator o);

        public boolean evaluate(Map m);

    }

    /**
     * This interface defines the basic methods that must be implemented by every class that defines the
     * operation that is used by the <code>Expression</code> when it's processed by
     * <code>SQLConditionValuesProcessor</code>.
     *
     * @author Imatia Innovation S.L
     */

    public static interface Operator extends java.io.Serializable {

        @Override
        public String toString();

    }

    /**
     * This interface defines the basic methods that must be implemented by any class that stores the
     * name of the column that is used by the <code>Expression</code> when it's processed by
     * <code>SQLConditionValuesProcessor</code>.
     *
     * @author Imatia Innovation S.L.
     */
    public static interface Field extends java.io.Serializable {

        @Override
        public String toString();

    }

    /**
     * This interface defines the basic method that must be implemented by any class that creates the
     * condition string for a SQL Statement.
     */

    public static interface SQLConditionValuesProcessor {

        /**
         * Creates the condition string for a SQL Statement.
         *
         * @param conditions condition list that be using for create the string
         * @param wildcards  column list that can use wildcards
         * @param values     List where the value of each processed conditions is stored
         * @return
         */
        public String createQueryConditions(Map conditions, List wildcards, List values);

        public void setSQLStatementHandler(SQLStatementHandler handler);

        public boolean getUpperLike();

        public boolean getUpperStrings();

    }

    public static class SQLOrder implements Serializable {

        public static final String ASC = " ASC ";

        public static final String DESC = " DESC ";

        protected String columnName = null;

        protected boolean ascendent = true;

        public SQLOrder(String columnName) {
            this.columnName = columnName;
        }

        public SQLOrder(String columnName, boolean ascendent) {
            this.columnName = columnName;
            this.ascendent = ascendent;
        }

        public String getColumnName() {
            return this.columnName;
        }

        public boolean isAscendent() {
            return this.ascendent;
        }

        @Override
        public String toString() {
            return this.columnName;
        }

    }

    /**
     * This class is used to store a SQL Statement and the required values to be processed in
     * <code>SQLConditionValuesProcessor</code>. This SQLStatement is inserted in brackets into a
     * condition string after an IN clause
     *
     * @author Imatia Innovation S.L.
     */
    public static class SQLExpression {

        protected String query = null;

        protected List<Object> values = null;

        public SQLExpression(String query, List<Object> values) {
            this.query = query;
            this.values = values;
        }

        public String getQuery() {
            return this.query;
        }

        public List getValues() {
            return this.values;
        }

    }

    /**
     * This class is used for avoid using wildcard when the condition string is created in
     * <code>SQLConditionValuesProcessor</code>
     *
     * @author Imatia Innovation S.L.
     */
    public static class NoWildCard {

        private String v = null;

        public NoWildCard(String v) {
            this.v = v;
        }

        @Override
        public String toString() {
            return this.v;
        }

        public String getValue() {
            return this.v;
        }

    }

    /**
     * Default implementation of <code>Field</code> interfaces.
     *
     * @author Imatia Innovation S.L
     * @see SQLStatementBuilder.ExtendedSQLConditionValuesProcessor
     */
    public static class BasicField implements Field {

        protected String name = null;

        public BasicField(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

    /**
     * Default implementation of <code>Operator</code> interfaces.
     *
     * @author Imatia Innovation S.L
     * @see SQLStatementBuilder.ExtendedSQLConditionValuesProcessor
     */

    public static class BasicOperator implements Operator {

        public static final String LESS = "<";
        public static final String LESS_EQUAL = "<=";
        public static final String EQUAL = "=";
        public static final String MORE_EQUAL = ">=";
        public static final String MORE = ">";
        public static final String NULL = " IS NULL ";
        public static final String NOT_EQUAL = "<>";
        public static final String NOT_NULL = " IS NOT NULL ";
        public static final String LIKE = " LIKE ";
        public static final String NOT_LIKE = " NOT LIKE ";
        public static final String OR = " OR ";

        // public static final String BETWEEN = " BETWEEN ";

        // public static final String IN = " IN ";
        public static final String AND = " AND ";
        public static final String OR_NOT = " OR NOT ";
        public static final String AND_NOT = " AND NOT ";
        public static final Operator OR_OP = new BasicOperator(BasicOperator.OR);

        public static final Operator AND_OP = new BasicOperator(BasicOperator.AND);

        public static final Operator OR_NOT_OP = new BasicOperator(BasicOperator.OR_NOT);

        public static final Operator AND_NOT_OP = new BasicOperator(BasicOperator.AND_NOT);

        public static final Operator LESS_OP = new BasicOperator(BasicOperator.LESS);

        public static final Operator LESS_EQUAL_OP = new BasicOperator(BasicOperator.LESS_EQUAL);

        public static final Operator EQUAL_OP = new BasicOperator(BasicOperator.EQUAL);

        public static final Operator MORE_EQUAL_OP = new BasicOperator(BasicOperator.MORE_EQUAL);

        public static final Operator MORE_OP = new BasicOperator(BasicOperator.MORE);

        public static final Operator NULL_OP = new BasicOperator(BasicOperator.NULL);

        public static final Operator NOT_EQUAL_OP = new BasicOperator(BasicOperator.NOT_EQUAL);

        // public static Operator BETWEEN_OP = new BasicOperator(BETWEEN);
        public static final Operator IN_OP = new BasicOperator(SQLStatementBuilder.IN);

        public static final Operator NOT_IN_OP = new BasicOperator(SQLStatementBuilder.NOT_IN);

        public static final Operator NOT_NULL_OP = new BasicOperator(BasicOperator.NOT_NULL);

        public static final Operator LIKE_OP = new BasicOperator(BasicOperator.LIKE);

        public static final Operator NOT_LIKE_OP = new BasicOperator(BasicOperator.NOT_LIKE);

        protected String operator = null;

        public BasicOperator(String op) {
            this.operator = op;
        }

        public static BasicOperator basicOperatorfromSearchOperator(int searchOperatorCode) {
            BasicOperator toRet = null;
            switch (searchOperatorCode) {
                case 0:
                    toRet = (BasicOperator) BasicOperator.LESS_OP;
                    break;
                case 1:
                    toRet = (BasicOperator) BasicOperator.LESS_EQUAL_OP;
                    break;
                case 2:
                    toRet = (BasicOperator) BasicOperator.EQUAL_OP;
                    break;
                case 3:
                    toRet = (BasicOperator) BasicOperator.MORE_EQUAL_OP;
                    break;
                case 4:
                    toRet = (BasicOperator) BasicOperator.MORE_OP;
                    break;
                case 5:
                    toRet = (BasicOperator) BasicOperator.NULL_OP;
                    break;
                case 6:
                    toRet = (BasicOperator) BasicOperator.NOT_EQUAL_OP;
                    break;
                // case 7:
                // toRet = (BasicOperator) BasicOperator.OR_OP;
                // break;
                // case 8:
                // toRet = new BasicOperator(BasicOperator.BETWEEN);
                // break;
                case 9:
                    toRet = (BasicOperator) BasicOperator.IN_OP;
                    break;
                case 10:
                    toRet = (BasicOperator) BasicOperator.NOT_NULL_OP;
                    break;
                case 11:
                    // toRet = new BasicOperator(BasicOperator.EXISTS);
                    // break;
                case 12:
                    toRet = (BasicOperator) BasicOperator.NOT_IN_OP;
                    break;
                // case 13:
                // toRet = new BasicOperator(BasicOperator.NOT_BETWEEN);
                // break;
                default:
                    break;
            }
            return toRet;
        }

        @Override
        public String toString() {
            return this.operator;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof BasicOperator) {
                return this.operator.equalsIgnoreCase(((BasicOperator) o).toString());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.operator.toLowerCase().hashCode();
        }

    }

    /**
     * Default implementation of <code>Expression</code> interfaces. This class is processed by
     * <code>ExtendedSQLConditionValuesProcessor</code> for created a condition string.
     * <p>
     * This class stored the required information to create a condition to a SQL Statement
     *
     * @author Imatia Innovation S.L
     * @see SQLStatementBuilder.ExtendedSQLConditionValuesProcessor
     */

    public static class BasicExpression implements Expression {

        protected Object leftOperand = null;

        protected Object rightOperand = null;

        protected Operator operator = null;

        protected boolean brackets = false;

        public BasicExpression(Object lo, Operator o, Object ro) {
            this.leftOperand = lo;
            this.operator = o;
            this.rightOperand = ro;
        }

        /**
         * Build a new BasicExpression object from a SearchValue.
         *
         * @param lo      Left operand of the Basic Expression
         * @param search  SearchValue associated with the left operand
         * @param bracket Boolean to add brackets to the left operand name
         */
        public BasicExpression(Object lo, SearchValue search, boolean bracket) {
            this.leftOperand = lo;
            this.operator = BasicOperator.EQUAL_OP;
            this.rightOperand = search;
            if (bracket) {
                this.brackets = bracket;
            }
        }

        /**
         * Method used to reduce the cognitive complexity of {@link #evaluateEquals(Object, Object, Object, String, Object)}
         *
         * @param ro
         * @param oValue
         * @return
         */
        private static boolean evaluateEqualsIfNumberOrSQLStatement(Object ro, Object oValue) {
            if ((oValue instanceof Number) && (ro instanceof Number)) {
                return Double.compare(((Number) oValue).doubleValue(), ((Number) ro).doubleValue()) == 0;
            } else {
                if (SQLStatementBuilder.getQueryConditionsProcessor().getUpperStrings()) {
                    return oValue.toString().equalsIgnoreCase(ro.toString());
                } else {
                    return oValue.toString().equals(ro.toString());
                }
            }
        }

        @Override
        public Object getLeftOperand() {
            return this.leftOperand;
        }

        @Override
        public void setLeftOperand(Object o) {
            this.leftOperand = o;
        }

        @Override
        public Object getRightOperand() {
            return this.rightOperand;
        }

        @Override
        public void setRightOperand(Object o) {
            this.rightOperand = o;
        }

        @Override
        public Operator getOperator() {
            return this.operator;
        }

        @Override
        public void setOperator(Operator o) {
            this.operator = o;
        }

        public boolean getBrackets() {
            return this.brackets;
        }

        @Override
        public boolean evaluate(Map m) {
            // Recursive
            Object lo = this.getLeftOperand();
            Object ro = this.getRightOperand();
            Object o = this.getOperator();

            if (lo instanceof Field) {
                return evaluateLeftOperand(m, lo, ro, o);
            }else if (lo instanceof Expression) {
                return this.evaluateLeftOperandAsExpression(m, lo, ro, o);
            } else {
                throw new IllegalArgumentException("Left operand have to be a Field or Expression: "
                        + (lo != SQLStatementBuilder.NULL ? lo.getClass().toString() : "is NULL"));
            }
        }

        /**
         * Method extracted to reduce cognituve complexity of {@link #evaluate(Map)}
         * @param m
         * @param lo
         * @param ro
         * @param o
         * @return
         */
        private boolean evaluateLeftOperand(Map<Object, Object> m, Object lo, Object ro, Object o) {
            String op = o.toString();
            String sField = lo.toString();
            Object oValue = m.get(sField);

            if (ro instanceof SearchValue) {
                return this.evaluateRightOperatorAsSearchValue(m, lo, ro, op, sField, oValue);
            } else if (op.equals(BasicOperator.EQUAL)) {
                return evaluateOperatorEquals(lo, ro, o, op, sField, oValue);
            } else if (op.equals(BasicOperator.NOT_EQUAL)) {
                return evaluateOperatorNotEquals(lo, ro, o, op, sField, oValue);
            } else if (op.equals(BasicOperator.NULL)) {
                return evaluateOperatorNull(lo, ro, o, sField, oValue);
            } else if (op.equals(BasicOperator.NOT_NULL)) {
                return evaluateOperatorNotNull(lo, ro, o, sField, oValue);
            } else if (op.equals(BasicOperator.LESS) || op.equals(BasicOperator.LESS_EQUAL)
                    || op.equals(BasicOperator.MORE) || op.equals(BasicOperator.MORE_EQUAL)) {
                return evaluateOperatorLessLessEqualsMoreMoreEquals(lo, ro, o, op, sField, oValue);
            } else if (op.equals(SQLStatementBuilder.IN)) {
                return evaluateOperatorIN(lo, ro, o, op, sField, oValue);
            }else if (op.equals(SQLStatementBuilder.NOT_IN)) {
                return evaluateOperatorNotIn(lo, ro, o, op, sField, oValue);
            }else if (op.equals(BasicOperator.LIKE)) {
                return evaluateOperatorLike(lo, ro, o, op, sField, oValue);
            }else if (op.equals(BasicOperator.NOT_LIKE)) {
                return evaluateoperatorNotLike(lo, ro, o, op, sField, oValue);
            }else {
                // FIXME Other BasicOperand
                throw new IllegalArgumentException("Operator " + op + " not implements");
            }
        }

        /**
         * Method extracted to reduce cognituve complexity of {@link #evaluate(Map)}
         * @param lo
         * @param ro
         * @param o
         * @param op
         * @param sField
         * @param oValue
         * @return
         */
        private boolean evaluateoperatorNotLike(Object lo, Object ro, Object o, String op, String sField, Object oValue) {
            SQLStatementBuilder.logger.debug(
                    "Expression with NOT LIKE operator is being evaluated -> \"{}\" {} \"{}\" and value for \"{}\" = \"{}\"",
                    lo, o, ro, sField,
                    oValue);
            boolean eval = !this.evaluateLike(lo, op, ro, sField, oValue);
            if (eval) {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" doesn't match the pattern with operator \"{}\": \"{}\" doesn't match with \"{}\" -> TRUE",
                        lo, o, oValue, ro);
            } else {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" matches the pattern with operator \"{}\": \"{}\" matches with \"{}\" -> FALSE",
                        lo, o,
                        oValue, ro);
            }
            return eval;
        }

        /**
         * Method extracted to reduce cognituve complexity of {@link #evaluate(Map)}
         * @param lo
         * @param ro
         * @param o
         * @param op
         * @param sField
         * @param oValue
         * @return
         */
        private boolean evaluateOperatorLike(Object lo, Object ro, Object o, String op, String sField, Object oValue) {
            SQLStatementBuilder.logger.debug(
                    "Expression with LIKE operator is being evaluated -> \"{}\" {} \"{}\" and value for \"{}\" = \"{}\"",
                    lo, o, ro, sField,
                    oValue);
            boolean eval = this.evaluateLike(lo, op, ro, sField, oValue);
            if (eval) {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" matches the pattern with operator \"{}\": \"{}\" matches with \"{}\" -> TRUE",
                        lo, o,
                        oValue, ro);
            } else {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" doesn't match the pattern with operator \"{}\": \"{}\" don't match with \"{}\" -> FALSE",
                        lo, o, oValue, ro);
            }
            return eval;
        }

        /**
         * Method extracted to reduce cognituve complexity of {@link #evaluate(Map)}
         * @param lo
         * @param ro
         * @param o
         * @param op
         * @param sField
         * @param oValue
         * @return
         */
        private boolean evaluateOperatorNotIn(Object lo, Object ro, Object o, String op, String sField, Object oValue) {
            SQLStatementBuilder.logger.debug(
                    "Expression with NOT IN operator is being evaluated -> \"{}\" {} {} and value for \"{}\" = \"{}\"",
                    lo, o, ro, sField, oValue);
            boolean eval = !this.evaluateIn(lo, op, ro, sField, oValue);
            if (eval) {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" matches with operator \"{}\": \"{}\" isn't in \"{}\" -> TRUE",
                        lo, o, oValue, ro);
            } else {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" doesn't match with operator \"{}\": \"{}\" is in \"{}\" -> FALSE",
                        lo, o, oValue, ro);
            }
            return eval;
        }

        /**
         * Method extracted to reduce cognituve complexity of {@link #evaluate(Map)}
         * @param lo
         * @param ro
         * @param o
         * @param op
         * @param sField
         * @param oValue
         * @return
         */
        private boolean evaluateOperatorIN(Object lo, Object ro, Object o, String op, String sField, Object oValue) {
            SQLStatementBuilder.logger.debug(
                    "Expression with IN operator is being evaluated -> \"{}\" {} {} and value for \"{}\" = \"{}\"",
                    lo, o, ro, sField, oValue);
            boolean eval = this.evaluateIn(lo, op, ro, sField, oValue);
            if (eval) {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" matches with operator \"{}\": \"{}\" is in \"{}\" -> TRUE",
                        lo, o, oValue, ro);
            } else {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" doesn't match with operator \"{}\": \"{}\" isn't in \"{}\" -> FALSE",
                        lo, o, oValue, ro);
            }
            return eval;
        }

        /**
         * Used to reduce cognitive complexity of {@link #evaluate(Map)}
         *
         * @param lo     The left operand
         * @param ro     The right operand
         * @param o      The operator
         * @param sField The field name
         * @param oValue The value of the field
         * @return
         */
        private boolean evaluateOperatorLessLessEqualsMoreMoreEquals(Object lo, Object ro, Object o, String op, String sField, Object oValue) {
            SQLStatementBuilder.logger.debug(
                    "Expression with order operator is being evaluated -> \"{}\" {} \"{}\" and value for \"{}\" = \"{}\"",
                    lo, o, ro, sField,
                    oValue);
            boolean eval = this.evaluateOrder(lo, o, ro, sField, oValue);
            if (eval) {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" matches with the operator \"{}\":  \"{}\" is \"{}\" than \"{}\"-> TRUE",
                        sField, op, oValue,
                        op, ro);
            } else {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" doesn't match with the operator \"{}\":  \"{}\" is not \"{}\" than \"{}\"-> FALSE",
                        sField,
                        op, oValue, op, ro);
            }
            return eval;
        }

        /**
         * Used to reduce cognitive complexity of {@link #evaluate(Map)}
         *
         * @param lo     The left operand
         * @param ro     The right operand
         * @param o      The operator
         * @param sField The field name
         * @param oValue The value of the field
         * @return
         */
        private boolean evaluateOperatorNotNull(Object lo, Object ro, Object o, String sField, Object oValue) {
            SQLStatementBuilder.logger.debug(
                    "Expression with NOT NULL operator is being evaluated -> \"{}\"{}and value for \"{}\" = \"{}\"",
                    lo, o, sField, oValue);
            boolean eval = !this.evaluateNull(lo, o, ro, sField, oValue);
            if (eval) {
                SQLStatementBuilder.logger.debug("Value for \"{}\" is NOT NULL -> TRUE", sField);
            } else {
                SQLStatementBuilder.logger.debug("Value for \"{}\" is NULL -> FALSE", sField);
            }
            return eval;
        }

        /**
         * Used to reduce cognitive complexity of {@link #evaluate(Map)}
         *
         * @param lo     The left operand
         * @param ro     The right operand
         * @param o      The operator
         * @param sField The field name
         * @param oValue The value of the field
         * @return
         */
        private boolean evaluateOperatorNull(Object lo, Object ro, Object o, String sField, Object oValue) {
            SQLStatementBuilder.logger.debug(
                    "Expression with NULL operator is being evaluated -> \"{}\"{}and value for \"{}\" = \"{}\"",
                    lo, o, sField, oValue);
            boolean eval = this.evaluateNull(lo, o, ro, sField, oValue);
            if (eval) {
                SQLStatementBuilder.logger.debug("Value for \"{}\" is NULL -> TRUE", sField);
            } else {
                SQLStatementBuilder.logger.debug("Value for \"{}\" is NOT NULL -> FALSE", sField);
            }
            return eval;
        }

        /**
         * Used to reduce cognitive complexity of {@link #evaluate(Map)}
         *
         * @param lo     The left operand
         * @param ro     The right operand
         * @param o      The operator
         * @param sField The field name
         * @param oValue The value of the field
         * @return
         */
        private boolean evaluateOperatorNotEquals(Object lo, Object ro, Object o, String op, String sField, Object oValue) {
            SQLStatementBuilder.logger.debug(
                    "Expression with NOT EQUAL operator is being evaluated -> \"{}\" {} \"{}\" and value for \"{}\" = \"{}\"",
                    lo, o, ro, sField,
                    oValue);
            boolean eval = !this.evaluateEquals(lo, BasicOperator.EQUAL_OP, ro, sField, oValue);
            if (eval) {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" matches with the operator \"{}\": \"{}\" is different than \"{}\" -> TRUE",
                        sField, op, ro,
                        oValue);
            } else {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" doesn't match with the operator \"{}\": are equals -> FALSE",
                        sField, op, ro, oValue);
            }
            return eval;
        }

        /**
         * Used to reduce cognitive complexity of {@link #evaluate(Map)}
         *
         * @param lo     The left operand
         * @param ro     The right operand
         * @param o      The operator
         * @param sField The field name
         * @param oValue The value of the field
         * @return
         */
        private boolean evaluateOperatorEquals(Object lo, Object ro, Object o, String op, String sField, Object oValue) {
            SQLStatementBuilder.logger.debug(
                    "Expression with EQUAL operator is being evaluated -> \"{}\" {} \"{}\" and value for \"{}\" = \"{}\"",
                    lo, o, ro, sField,
                    oValue);
            boolean eval = this.evaluateEquals(lo, o, ro, sField, oValue);
            if (eval) {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" matches with the operator \"{}\": are equals -> TRUE",
                        sField, op);
            } else {
                SQLStatementBuilder.logger.debug(
                        "Register for the field \"{}\" doesn't match with the operator \"{}\": \"{}\" is different than \"{}\" -> FALSE",
                        sField,
                        op, ro, oValue);
            }
            return eval;
        }

        /**
         * Method used to reduce the complexity of {@link #evaluate(Map)}
         *
         * @param m
         * @param lo
         * @param ro
         * @param o
         * @return
         */
        protected boolean evaluateLeftOperandAsExpression(Map m, Object lo, Object ro, Object o) {
            // Left operand is an expression. If right operand is an
            // expression too, then check AND and OR operators.
            String op = o.toString();
            boolean res = false;
            boolean rLO = ((Expression) lo).evaluate(m);
            if (ro instanceof Expression) {
                boolean rRO = ((Expression) ro).evaluate(m);
                if (op.equals(BasicOperator.AND)) {
                    res = rLO && rRO;
                    SQLStatementBuilder.logger.debug(" AND Operator between \"{}\" and \"{}\" expressions -> {}", rLO,
                            rRO, res);
                } else if (op.equals(BasicOperator.OR)) {
                    res = rLO || rRO;
                    SQLStatementBuilder.logger.debug(" OR Operator between \"{}\" and \"{}\" expressions -> {}", rLO,
                            rRO, res);
                } else if (op.equals(BasicOperator.OR_NOT)) {
                    res = rLO || !rRO;
                    SQLStatementBuilder.logger.debug(" OR NOT Operator between \"{}\" and \"{}\" expressions -> {}",
                            rLO, rRO, res);
                } else if (op.equals(BasicOperator.AND_NOT)) {
                    res = rLO && !rRO;
                    SQLStatementBuilder.logger.debug(" AND NOT Operator between \"{}\" and \"{}\" expressions -> {}",
                            rLO, rRO, res);
                } else {
                    throw new IllegalArgumentException(op + " operator doesn't recognize as expression operator");
                }
            } else {
                res = rLO;
            }
            return res;
        }

        /**
         * Method to reduce the complexity of {@link #evaluate(Map)}
         *
         * @param m
         * @param lo
         * @param ro
         * @param op
         * @param sField
         * @param oValue
         * @return
         */
        protected boolean evaluateRightOperatorAsSearchValue(Map m, Object lo, Object ro, String op, String sField,
                                                             Object oValue) {
            SQLStatementBuilder.logger.debug("Evaluating expression with SearchValue for field \"{}\"", lo);
            BasicOperator bOp = BasicOperator.basicOperatorfromSearchOperator(((SearchValue) ro).getCondition());
            if (bOp != null) {
                BasicExpression eval = new BasicExpression(lo, bOp, ((SearchValue) ro).getValue());
                boolean result = eval.evaluate(m);
                SQLStatementBuilder.logger.debug("Expression with SearchValue evaluated with result -> \"{}\"", result);
                return result;
            } else if (((SearchValue) ro).getCondition() == 7) {
                SQLStatementBuilder.logger.debug(
                        "SearchValue expression with OR operator is being evaluated -> \"{}\" IN \"{}\" and value for \"{}\" = \"{}\"",
                        lo,
                        ((SearchValue) ro).getValue(), sField, oValue);
                BasicExpression eval = new BasicExpression(lo, BasicOperator.IN_OP, ((SearchValue) ro).getValue());
                boolean result = eval.evaluate(m);
                SQLStatementBuilder.logger.debug("Expression with SearchValue evaluated with result -> \"{}\"", result);
                return result;
            } else if (((SearchValue) ro).getCondition() == 8) {
                SQLStatementBuilder.logger.debug(
                        "SearchValue expression with BETWEEN operator is being evaluated -> \"{}\" BETWEEN \"{}\" and value for \"{}\" = \"{}\"",
                        lo, ((SearchValue) ro).getValue(), sField, oValue);
                boolean eval = this.evaluateBetween(lo, ro, sField, oValue);
                if (eval) {
                    SQLStatementBuilder.logger.debug(
                            "Register for the field \"{}\" matches with the operator BETWEEN: \"{}\" is in range between \"{}\" -> TRUE",
                            lo,
                            oValue, ((SearchValue) ro).getValue());
                    SQLStatementBuilder.logger.debug("Expression with SearchValue evaluated with result -> \"true\"");
                } else {
                    SQLStatementBuilder.logger.debug(
                            "Register for the field \"{}\" doesn't match with the operator BETWEEN: \"{}\" is out of range between \"{}\" -> FALSE",
                            lo, oValue,
                            ((SearchValue) ro).getValue());
                    SQLStatementBuilder.logger.debug("Expression with SearchValue evaluated with result -> \"false\"");
                }
                return eval;
            } else if (((SearchValue) ro).getCondition() == 13) {
                SQLStatementBuilder.logger.debug(
                        "SearchValue expression with NOT BETWEEN operator is being evaluated -> \"{}\" NOT BETWEEN \"{}\" and value for \"{}\" = \"{}\"",
                        lo,
                        ((SearchValue) ro).getValue(), sField, oValue);
                boolean eval = !this.evaluateBetween(lo, ro, sField, oValue);
                if (eval) {
                    SQLStatementBuilder.logger.debug(
                            "Register for the field \"{}\" matches with the operator NOT BETWEEN: \"{}\" is out of range between \"{}\" -> TRUE",
                            lo, oValue, ((SearchValue) ro).getValue());
                    SQLStatementBuilder.logger.debug("Expression with SearchValue evaluated with result -> \"true\"");
                } else {
                    SQLStatementBuilder.logger.debug(
                            "Register for the field \"{}\" doesn't match with the operator NOT BETWEEN: \"{}\" is in range between \"{}\" -> FALSE",
                            lo, oValue,
                            ((SearchValue) ro).getValue());
                    SQLStatementBuilder.logger.debug("Expression with SearchValue evaluated with result -> \"false\"");
                }
                return eval;
            } else if (((SearchValue) ro).getCondition() == 11) {
                throw new IllegalArgumentException(
                        "It is not possible to evaluate an operation with an EXIST operator.");
            } else {
                throw new IllegalArgumentException(op + " operator doesn't recognize as a SearchValue operator");
            }
        }

        protected boolean evaluateEquals(Object lo, Object o, Object ro, String sField, Object oValue) {
            if (oValue == null) {
                return (ro == null);
            } else if (ro == null) {
                return false;
            } else {
                if (oValue.equals(ro)) {
                    return true;
                } else return evaluateEqualsIfNumberOrSQLStatement(ro, oValue);
            }
        }

        protected boolean evaluateNull(Object lo, Object o, Object ro, String sField, Object oValue) {
            return (oValue == null);
        }

        protected boolean evaluateOrder(Object lo, Object o, Object ro, String sField, Object oValue) {
            String op = o.toString();
            if (oValue == null) {
                return false;
            } else if (ro == null) {
                return false;
            } else {
                if ((oValue instanceof Number) && (ro instanceof Number)) {
                    return this.evaluateOrderInstanceOfNumber(ro, oValue, op);
                } else if (oValue instanceof Comparable) {
                    return this.evaluateOrderInstanceOfComparable(ro, oValue, op);
                } else return evaluateOrderLessLessEqualMoreMoreEqual(ro, oValue, op);
            }
        }

        /**
         * Methos extracted to reduce cognitive complexity of {@link #evaluateOrder(Object, Object, Object, String, Object)}
         * @param ro
         * @param oValue
         * @param op
         * @return
         */
        private static boolean evaluateOrderLessLessEqualMoreMoreEqual(Object ro, Object oValue, String op) {
            String s = oValue.toString();
            int r = s.compareTo(ro.toString());
            if (r < 0) {
                return (op.equals(BasicOperator.LESS_EQUAL) || op.equals(BasicOperator.LESS));
            } else if (r > 0) {
                return (op.equals(BasicOperator.MORE) || op.equals(BasicOperator.MORE_EQUAL));
            } else {
                return (op.equals(BasicOperator.LESS_EQUAL) || op.equals(BasicOperator.MORE_EQUAL));
            }
        }

        /**
         * Method to reduce the complexity of {@link #evaluateOrder(Object, Object, Object, String, Object)}
         *
         * @param ro
         * @param oValue
         * @param op
         * @return
         */
        protected boolean evaluateOrderInstanceOfNumber(Object ro, Object oValue, String op) {
            double r = ((Number) oValue).doubleValue() - ((Number) ro).doubleValue();
            if (r < 0) {
                return (op.equals(BasicOperator.LESS_EQUAL) || op.equals(BasicOperator.LESS));
            } else if (r > 0) {
                return (op.equals(BasicOperator.MORE) || op.equals(BasicOperator.MORE_EQUAL));
            } else {
                return (op.equals(BasicOperator.LESS_EQUAL) || op.equals(BasicOperator.MORE_EQUAL));
            }
        }

        /**
         * Method to reduce the complexity of {@link #evaluateOrder(Object, Object, Object, String, Object)}
         *
         * @param ro
         * @param oValue
         * @param op
         * @return
         */
        protected boolean evaluateOrderInstanceOfComparable(Object ro, Object oValue, String op) {
            int r = ((Comparable) oValue).compareTo(ro);
            if (r < 0) {
                return (op.equals(BasicOperator.LESS_EQUAL) || op.equals(BasicOperator.LESS));
            } else if (r > 0) {
                return (op.equals(BasicOperator.MORE) || op.equals(BasicOperator.MORE_EQUAL));
            } else {
                return (op.equals(BasicOperator.LESS_EQUAL) || op.equals(BasicOperator.MORE_EQUAL));
            }
        }

        protected boolean evaluateIn(Object lo, Object o, Object ro, String sField, Object oValue) {
            if (ro instanceof List) {
                for (Object actual : (List) ro) {
                    if (this.evaluateEquals(lo, BasicOperator.EQUAL_OP, oValue, sField, actual)) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        }

        protected boolean evaluateLike(Object lo, Object o, Object ro, String sField, Object oValue) {
            if ((oValue instanceof String) && (ro instanceof String)) {
                Pattern pattern;
                Matcher matcher;
                String pattrn = this.replaceLikeToJavaPattern((String) ro);
                String value = oValue.toString();
                if (SQLStatementBuilder.getQueryConditionsProcessor().getUpperLike()) {
                    pattrn = pattrn.toUpperCase();
                    value = value.toUpperCase();
                }
                pattern = Pattern.compile(pattrn);
                matcher = pattern.matcher(value);
                return matcher.matches();
            }
            return false;
        }

        protected boolean evaluateBetween(Object lo, Object ro, String sField, Object oValue) {
            if (oValue == null) {
                return false;
            } else if (ro == null) {
                return false;
            } else {
                if (((SearchValue) ro).getValue() instanceof List) {
                    return evaluateBetweenList(lo, (SearchValue) ro, sField, oValue);
                }else {
                    return false;
                }
            }
        }

        /**
         * Method used to reduce the cognitive complexity of {@link #evaluateBetween(Object, Object, String, Object)}
         * @param lo
         * @param ro
         * @param sField
         * @param oValue
         * @return
         */
        private boolean evaluateBetweenList(Object lo, SearchValue ro, String sField, Object oValue) {
            List<Object> listValues = (List) ro.getValue();
            if ((listValues.size() == 2) && (listValues.get(0) != null) && (listValues.get(1) != null)) {
                Object min;
                Object max;
                if (this.evaluateOrder(lo, BasicOperator.MORE_EQUAL, listValues.get(0), sField,
                        listValues.get(1))) {
                    min = listValues.get(0);
                    max = listValues.get(1);
                } else {
                    min = listValues.get(1);
                    max = listValues.get(0);
                }

                boolean greaterThan = this.evaluateOrder(lo, BasicOperator.MORE_EQUAL, min, sField, oValue);
                boolean lessThan = this.evaluateOrder(lo, BasicOperator.LESS_EQUAL, max, sField, oValue);

                return (greaterThan && lessThan);

            } else {
                return false;
            }
        }

        protected String replaceLikeToJavaPattern(String sqlLike) {
            if (sqlLike != null) {
                sqlLike = sqlLike.replace("%", ".*");
                sqlLike = sqlLike.replace("_", ".{1}");
                sqlLike = sqlLike.replace("[!", "[^");
                StringBuilder builder = new StringBuilder();
                builder.append("^");
                builder.append(sqlLike);
                builder.append("$");
                return builder.toString();
            } else {
                return "";
            }
        }

    }

    /**
     * This class provides default implementations for the <code>SQLConditionValuesProcessor</code>
     * interface.
     *
     * @author Imatia Innovation S.L.
     */

    public static class DefaultSQLConditionValuesProcessor implements SQLConditionValuesProcessor {

        public static final String UPPER_FUNCTION = "UPPER";
        protected boolean upperLike = false;
        protected boolean upperStrings = false;
        protected SQLStatementHandler handler = null;

        public DefaultSQLConditionValuesProcessor() {
        }

        /**
         * Creates a <code>DefaultSQLConditionValuesProcessor</code> where every condition that uses
         * <code>LIKE</code> is case-insensitive.
         * <p>
         * Inserts a upper function in both sides of <code>LIKE</code> conditions (UPPER(Field) LIKE
         * UPPER(Value))
         *
         * @param upperLike true if the LIKE condition should be case-insensitive
         */
        public DefaultSQLConditionValuesProcessor(boolean upperLike) {
            this.upperLike = upperLike;
        }

        /**
         * Creates a <code>DefaultSQLConditionValuesProcessor</code> where every condition that uses
         * <code>LIKE</code> or is a column of String type is case-insensitive.
         * <p>
         * Inserts a upper function in both sides of <code>LIKE</code> conditions (UPPER(Field) LIKE
         * UPPER(Value)) Inserts a upper function in both sides if column type is a String (UPPER(Field)
         * LIKE UPPER(String))
         *
         * @param upperStrings true if the String column type should be case-insensitive
         * @param upperLike    true if the LIKE condition should be case-insensitive
         */
        public DefaultSQLConditionValuesProcessor(boolean upperStrings, boolean upperLike) {
            this.upperLike = upperLike;
            this.upperStrings = upperStrings;
        }

        /**
         * Checks if the string contains a parenthesis
         *
         * @param sqlquery string to check
         * @return true if the string contains a parenthesis
         */
        protected static boolean hasParenthesis(String sqlquery) {
            if ((sqlquery == null) || (sqlquery.length() == 0)) {
                return false;
            }
            String query = sqlquery.trim();
            return (('(' == query.charAt(0)) && (')' == query.charAt(query.length() - 1)));
        }

        protected SQLStatementHandler getSQLStatementHandler() {
            if (this.handler == null) {
                return SQLStatementBuilder.getSQLStatementHandler(SQLStatementBuilder.DEFAULT_HANDLER);
            } else {
                return this.handler;
            }
        }

        @Override
        public void setSQLStatementHandler(SQLStatementHandler handler) {
            this.handler = handler;
        }

        /**
         * Creates the condition string for a SQL Statement.
         *
         * @param conditions a Map specifying pairs of key-value corresponding to the attribute (or column
         *                   of a table in a database) and the value that is formed by the condition.
         * @param wildcards  column list that can use wildcards
         * @param values     List where the value of each processed conditions is stored
         * @return
         */

        @Override
        public String createQueryConditions(Map conditions, List wildcards, List values) {
            StringBuilder sbStringQuery = new StringBuilder();
            // For each key (attribute), set the restriction
            // int i = 0;
            // int wildcardValues = 0;
            // int nullValues = 0;
            // int searchValuesInString = 0;
            Enumeration enumKeys = Collections.enumeration(conditions.keySet());
            while (enumKeys.hasMoreElements()) {
                Object oKey = enumKeys.nextElement();
                Object oValue = conditions.get(oKey);
                if (oKey instanceof String) {
                    boolean bracket = this.getSQLStatementHandler().checkColumnName((String) oKey);
                    // Check wildcards:
                    if ((oValue instanceof String)
                            && ((wildcards == null) || wildcards.isEmpty() || wildcards.contains(oKey))) {
                        createQueryConditionsIfOValuesIsString(values, sbStringQuery, enumKeys, oKey, oValue, bracket);
                    }else {
                        // Add values in order
                        if (oValue instanceof SearchValue) {
                            createQueryConditionsIfOValuesIsSearchValue(values, sbStringQuery, enumKeys, oKey, oValue, bracket);
                        }else if (oValue instanceof String) {
                            createQueryConditionsIfOValuesInstanceOfString(values, sbStringQuery, enumKeys, oKey, oValue, bracket);
                        }else {
                            createQueryConditionsIfNotMeetPreviousRequisites(values, sbStringQuery, enumKeys, (String) oKey, oValue, bracket);
                        }
                    }
                }
            }
            return sbStringQuery.toString();
        }

        private void createQueryConditionsIfNotMeetPreviousRequisites(List<Object> values, StringBuilder sbStringQuery, Enumeration<Object> enumKeys, String oKey, Object oValue, boolean bracket) {
            String sCondition = SQLStatementBuilder.EQUAL_XQUESTIONMARK;
            if ((oValue instanceof String)
                    && (((String) oValue).indexOf(SQLStatementBuilder.NOT_EQUAL_ID) == 0)) {
                sCondition = SQLStatementBuilder.NOT_EQUAL_XQUESTIONMARK;
                oValue = ((String) oValue).substring(SQLStatementBuilder.NOT_EQUAL_ID.length());
            } else if (oValue instanceof NoWildCard) {
                oValue = ((NoWildCard) oValue).getValue();
            }

            // <pre>values.add(i - wildcardValues - nullValues -
            // searchValuesInString, oValue);</pre>
            values.add(oValue);
            if (enumKeys.hasMoreElements()) {
                if (bracket) {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(sCondition);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                } else {
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(sCondition);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                }
            } else {
                // The last pair key-value
                if (bracket) {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(sCondition);
                } else {
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(sCondition);
                }
            }
        }

        /**
         * Method used to reduce cognitive complexity of {@link #createQueryConditions(Map, List, List)}
         * @param values
         * @param sbStringQuery
         * @param enumKeys
         * @param oKey
         * @param oValue
         * @param bracket
         */
        private void createQueryConditionsIfOValuesInstanceOfString(List<Object> values, StringBuilder sbStringQuery, Enumeration<Object> enumKeys, Object oKey, Object oValue, boolean bracket) {
            StringBuilder sbCondition = new StringBuilder();
            if (this.upperStrings) {
                sbCondition.append(SQLStatementBuilder.EQUAL);
                sbCondition.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                sbCondition.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                sbCondition.append(SQLStatementBuilder.QUESTIONMARK);
                sbCondition.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
            } else {
                sbCondition.append(SQLStatementBuilder.EQUAL_XQUESTIONMARK);
            }
            if (((String) oValue).indexOf(SQLStatementBuilder.NOT_EQUAL_ID) == 0) {
                if (this.upperStrings) {
                    sbCondition.append(SQLStatementBuilder.NOT_EQUAL);
                    sbCondition.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbCondition.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbCondition.append(SQLStatementBuilder.QUESTIONMARK);
                    sbCondition.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                } else {
                    sbCondition.append(SQLStatementBuilder.NOT_EQUAL_XQUESTIONMARK);
                }
                oValue = ((String) oValue).substring(SQLStatementBuilder.NOT_EQUAL_ID.length());
            }
            // <pre>values.add(i - wildcardValues - nullValues -
            // searchValuesInString, oValue);</pre>
            values.add(oValue);
            if (enumKeys.hasMoreElements()) {
                sbStringQuery
                        .append(this.addNonLastPairKeyValueInOrder(oKey, bracket, sbCondition.toString()));
            } else {
                sbStringQuery
                        .append(this.addLastPairKeyValueInOrder(oKey, bracket, sbCondition.toString()));
            }
        }

        /**
         * Method extracted to reduce cognitive complexity of {@link #createQueryConditions(Map, List, List)}
         * @param values
         * @param sbStringQuery
         * @param enumKeys
         * @param oKey
         * @param oValue
         * @param bracket
         */
        private void createQueryConditionsIfOValuesIsSearchValue(List<Object> values, StringBuilder sbStringQuery, Enumeration<Object> enumKeys, Object oKey, Object oValue, boolean bracket) {
            StringBuilder sbStringQueryAux = SQLStatementBuilder
                    .createQueryConditionsSearchValue(oValue, values, bracket, oKey);
            if (sbStringQueryAux != null) {
                sbStringQuery.append(sbStringQueryAux);
                if (enumKeys.hasMoreElements()) {
                    sbStringQuery.append(SQLStatementBuilder.AND);
                }
            }
        }

        /**
         * Method extracted to reduce cognitive complexity of {@link #createQueryConditions(Map, List, List)}
         * @param values
         * @param sbStringQuery
         * @param enumKeys
         * @param oKey
         * @param oValue
         * @param bracket
         */
        private void createQueryConditionsIfOValuesIsString(List<Object> values, StringBuilder sbStringQuery, Enumeration<Object> enumKeys, Object oKey, Object oValue, boolean bracket) {
            if ((((String) oValue).indexOf(SQLStatementBuilder.ASTERISK_CHAR) >= 0)
                    || (((String) oValue).indexOf(SQLStatementBuilder.INTERROG) >= 0)) {
                sbStringQuery
                        .append(this.createQueryConditonsWithWildcards(enumKeys, oKey, oValue, bracket));
            } else {
                // There are no Wildcards
                // Add values in order
                StringBuilder sbCondition = new StringBuilder();
                if (this.upperStrings) {
                    sbCondition.append(SQLStatementBuilder.EQUAL);
                    sbCondition.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbCondition.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbCondition.append(SQLStatementBuilder.QUESTIONMARK);
                    sbCondition.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                } else {
                    sbCondition.append(SQLStatementBuilder.EQUAL_XQUESTIONMARK);
                }
                if (((String) oValue).indexOf(SQLStatementBuilder.NOT_EQUAL_ID) == 0) {
                    if (this.upperStrings) {
                        sbCondition.append(SQLStatementBuilder.NOT_EQUAL);
                        sbCondition.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                        sbCondition.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                        sbCondition.append(SQLStatementBuilder.QUESTIONMARK);
                        sbCondition.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    } else {
                        sbCondition.append(SQLStatementBuilder.NOT_EQUAL_XQUESTIONMARK);
                    }
                    oValue = ((String) oValue).substring(SQLStatementBuilder.NOT_EQUAL_ID.length());
                }

                // <pre>values.add(i - wildcardValues - nullValues -
                // searchValuesInString, oValue);</pre>
                values.add(oValue);
                if (enumKeys.hasMoreElements()) {
                    sbStringQuery
                            .append(this.addNonLastPairKeyValue(oKey, bracket, sbCondition.toString()));
                } else {
                    sbStringQuery.append(this.addLastPairKeyValue(oKey, bracket, sbCondition.toString()));

                }
            }
        }

        /**
         * Method to reduce the complexity of {@link #createQueryConditions(Map, List, List)}
         *
         * @param sbStringQuery
         * @param enumKeys
         * @param oKey
         * @param oValue
         * @param bracket
         */
        protected String createQueryConditonsWithWildcards(Enumeration enumKeys, Object oKey, Object oValue,
                                                           boolean bracket) {
            // Add sort values. We use '%' and '_' instead of
            // '*' and
            // '?'
            String newValue = oValue.toString();
            newValue = newValue.replace(SQLStatementBuilder.ASTERISK_CHAR, SQLStatementBuilder.PERCENT);
            newValue = newValue.replace(SQLStatementBuilder.INTERROG, SQLStatementBuilder.LOW_LINE);
            String likeOperator = SQLStatementBuilder.LIKE;
            if (newValue.indexOf(SQLStatementBuilder.NOT_EQUAL_ID) == 0) {
                likeOperator = SQLStatementBuilder.NOT_LIKE;
                newValue = newValue.substring(SQLStatementBuilder.NOT_EQUAL_ID.length());
            }
            // Do not add to the values List, put it directly
            // in the
            // query.

            StringBuilder sbStringQuery = new StringBuilder();
            if (enumKeys.hasMoreElements()) {
                createQueryConditionsWithWildcardsNotLastvalue((String) oKey, bracket, newValue, likeOperator, sbStringQuery);
            }else {
                createqueryConditionsWithWildcardsLastValue((String) oKey, bracket, newValue, likeOperator, sbStringQuery);
            }
            return sbStringQuery.toString();
        }

        /**
         * Method extracted to reduce the cognitive complexity of {@link #createQueryConditonsWithWildcards(Enumeration, Object, Object, boolean)}
         * @param oKey
         * @param bracket
         * @param newValue
         * @param likeOperator
         * @param sbStringQuery
         */
        private void createqueryConditionsWithWildcardsLastValue(String oKey, boolean bracket, String newValue, String likeOperator, StringBuilder sbStringQuery) {
            // The last pair key-value
            if (bracket) {
                if (this.upperLike) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(likeOperator);
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append("'");
                    sbStringQuery.append(newValue);
                    sbStringQuery.append("'");
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                } else {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(likeOperator);
                    sbStringQuery.append("'");
                    sbStringQuery.append(newValue);
                    sbStringQuery.append("'");
                }
            } else {
                if (this.upperLike) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(likeOperator);
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append("'");
                    sbStringQuery.append(newValue);
                    sbStringQuery.append("'");
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                } else {
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(likeOperator);
                    sbStringQuery.append("'");
                    sbStringQuery.append(newValue);
                    sbStringQuery.append("'");
                }
            }
        }

        /**
         * Method extracted to reduce the cognitive complexity of {@link #createQueryConditonsWithWildcards(Enumeration, Object, Object, boolean)}
         * @param oKey
         * @param bracket
         * @param newValue
         * @param likeOperator
         * @param sbStringQuery
         */
        private void createQueryConditionsWithWildcardsNotLastvalue(String oKey, boolean bracket, String newValue, String likeOperator, StringBuilder sbStringQuery) {
            if (bracket) {
                if (this.upperLike) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(likeOperator);
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append("'");
                    sbStringQuery.append(newValue);
                    sbStringQuery.append("'");
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                } else {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(likeOperator);
                    sbStringQuery.append("'");
                    sbStringQuery.append(newValue);
                    sbStringQuery.append("'");
                    sbStringQuery.append(SQLStatementBuilder.AND);
                }
            } else {
                if (this.upperLike) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(likeOperator);
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append("'");
                    sbStringQuery.append(newValue);
                    sbStringQuery.append("'");
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                } else {
                    sbStringQuery.append(oKey);
                    sbStringQuery.append(likeOperator);
                    sbStringQuery.append("'");
                    sbStringQuery.append(newValue);
                    sbStringQuery.append("'");
                    sbStringQuery.append(SQLStatementBuilder.AND);
                }

            }
        }

        /**
         * Mehtod to reduce the complexity of {@link #createQueryConditions(Map, List, List)}
         *
         * @param sbStringQuery
         * @param oKey
         * @param bracket
         * @param sbCondition
         * @return
         */
        protected String addNonLastPairKeyValue(Object oKey, boolean bracket, String sbCondition) {
            StringBuilder sbStringQuery = new StringBuilder();
            if (bracket) {
                if (this.upperStrings) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(sbCondition);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                } else {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(sbCondition);
                    sbStringQuery.append(SQLStatementBuilder.AND);

                }
            } else {
                if (this.upperStrings) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(sbCondition);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                } else {
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(sbCondition);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                }
            }

            return sbStringQuery.toString();
        }

        /**
         * Method to reduce the complexity of {@link #createQueryConditions(Map, List, List)}
         *
         * @param sbStringQuery
         * @param oKey
         * @param bracket
         * @param sbCondition
         * @return
         */
        protected String addLastPairKeyValue(Object oKey, boolean bracket, String sbCondition) {
            StringBuilder sbStringQuery = new StringBuilder();
            if (bracket) {
                if (this.upperStrings) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(sbCondition);
                } else {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(sbCondition);
                }
            } else {
                if (this.upperStrings) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(sbCondition);
                } else {
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(sbCondition);
                }
            }

            return sbStringQuery.toString();
        }

        /**
         * @param sbStringQuery
         * @param oKey
         * @param bracket
         * @param sbCondition
         * @return
         */
        protected String addNonLastPairKeyValueInOrder(Object oKey, boolean bracket, String sbCondition) {
            StringBuilder sbStringQuery = new StringBuilder();
            if (bracket) {
                if (this.upperStrings) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(sbCondition);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                } else {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(sbCondition);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                }
            } else {
                if (this.upperStrings) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(sbCondition);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                } else {
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(sbCondition);
                    sbStringQuery.append(SQLStatementBuilder.AND);
                }
            }

            return sbStringQuery.toString();
        }

        /**
         * Method to reduce the complexity of {@link #createQueryConditions(Map, List, List)}
         *
         * @param sbStringQuery
         * @param oKey
         * @param bracket
         * @param sbCondition
         */
        protected String addLastPairKeyValueInOrder(Object oKey, boolean bracket, String sbCondition) {
            StringBuilder sbStringQuery = new StringBuilder();
            if (bracket) {
                if (this.upperStrings) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(sbCondition);
                } else {
                    sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                    sbStringQuery.append(sbCondition);
                }
            } else {
                if (this.upperStrings) {
                    sbStringQuery.append(" " + DefaultSQLConditionValuesProcessor.UPPER_FUNCTION);
                    sbStringQuery.append(SQLStatementBuilder.OPEN_PARENTHESIS);
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
                    sbStringQuery.append(sbCondition);
                } else {
                    sbStringQuery.append((String) oKey);
                    sbStringQuery.append(sbCondition);
                }
            }

            return sbStringQuery.toString();
        }

        @Override
        public boolean getUpperLike() {
            return this.upperLike;
        }

        @Override
        public boolean getUpperStrings() {
            return this.upperStrings;
        }

    }

    /**
     * This class is used to store the information to create a SQL statement and the required values.
     */

    public static class SQLStatement {

        protected String statement = null;

        protected List values = null;

        public SQLStatement(String statement) {
            this.statement = statement;
        }

        public SQLStatement(String statement, List<Object> values) {
            this.statement = statement;
            this.values = values;
        }

        /**
         * Returns the string that be used to create the SQL Statement
         *
         * @return a String
         */
        public String getSQLStatement() {
            return this.statement;
        }

        /**
         * Returns a List of the required values for SQL Statement
         *
         * @return the required values or null
         */
        public List<Object> getValues() {
            return this.values;
        }

        /**
         * Add a values List at beginning of values List for SQL Statement
         *
         * @param values
         */
        public void addValues(List<Object> values) {
            this.values.addAll(0, values);
        }

    }

    /**
     * This class provides a implementation of the <code>SQLConditionValuesProcessor</code> interface.
     * <p>
     * This class extends <code>DefaultSQLConditionValuesProcessor<code> and
     * permits process complex conditions where condition key doesn't have to be the column of a table in a database. <code>EXPRESSION_KEY</code>
     * is used as condition key to indicate which is a complex condition
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
     *  	Expression expression = new BasicExpression(field,equalOperator,"filterValue");
     *  	Map conditions=new HashMap();
     *  	conditions.put(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,expression);
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
     * @author Imatia Innovation S.L.
     * @see SQLStatementBuilder.Expression
     */

    public static class ExtendedSQLConditionValuesProcessor extends DefaultSQLConditionValuesProcessor {

        /**
         * Identifier used as condition key when has a complex expression.
         */
        public static final String EXPRESSION_KEY = "EXPRESSION_KEY_UNIQUE_IDENTIFIER";

        public static final String FILTER_KEY = "FILTER_KEY_UNIQUE_IDENTIFIER";

        public ExtendedSQLConditionValuesProcessor() {
            super(false);
        }

        /**
         * Creates a <code>ExtendedSQLConditionValuesProcessor</code> where every condition that uses
         * <code>LIKE</code> is case-insensitive.
         * <p>
         * Inserts a upper function in both sides of <code>LIKE</code> conditions (UPPER(Field) LIKE
         * UPPER(Value))
         *
         * @param upperLike true if the LIKE condition should be case-insensitive
         */

        public ExtendedSQLConditionValuesProcessor(boolean upperLike) {
            super(upperLike);
        }

        /**
         * Creates a <code>ExtendedSQLConditionValuesProcessor</code> where every condition that uses
         * <code>LIKE</code> or is a column of String type is case-insensitive.
         * <p>
         * Inserts a upper function in both sides of <code>LIKE</code> conditions (UPPER(Field) LIKE
         * UPPER(Value)) Inserts a upper function in both sides if column type is a String (UPPER(Field)
         * LIKE UPPER(String))
         *
         * @param upperStrings true if the String column type should be case-insensitive
         * @param upperLike    true if the LIKE condition should be case-insensitive
         */

        public ExtendedSQLConditionValuesProcessor(boolean upperStrings, boolean upperLike) {
            super(upperStrings, upperLike);
        }

        /**
         * Creates the condition string from a expression. This condition string be used after WHERE clause
         * in the SQL Statement.
         *
         * @param expression a class that implements <code>Expression<code> interface
         * @return a String where is stored a condition in text format
         */

        public static String createQueryConditionsExpress(Expression expression) {
            if (expression == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            ExtendedSQLConditionValuesProcessor.createQueryConditionsExpress(expression, sb);
            return sb.toString();
        }

        /**
         * Creates the condition string from a expression.
         *
         * @param expression a class that implements <code>Expression<code> interface
         *                   &#64;param sb
         *                   <code>StringBuilder</code> where the condition string is stored.
         */

        public static void createQueryConditionsExpress(Expression expression, StringBuilder sb) {
            // Recursive
            Object lo = expression.getLeftOperand();
            Object ro = expression.getRightOperand();
            Object o = expression.getOperator();
            sb.append("(");
            if (lo instanceof Field) {
                sb.append(lo);
                sb.append(" ");
                sb.append(o);
                if (ro != null) createQueryConditionsExpressRightOperandNotNull(sb, ro, o);
            } else if (lo instanceof Expression) {
                ExtendedSQLConditionValuesProcessor.createQueryConditionsExpress((Expression) lo, sb);
                if (ro instanceof Expression) {
                    sb.append(" ");
                    sb.append(o);
                    sb.append(" ");
                    ExtendedSQLConditionValuesProcessor.createQueryConditionsExpress((Expression) ro, sb);
                }
            }
            sb.append(")");
        }

        /**
         * Method extracted to reduce cognitive complexity of {@link #createQueryConditionsExpress(Expression, StringBuilder)}
         * @param sb
         * @param ro
         * @param o
         */
        private static void createQueryConditionsExpressRightOperandNotNull(StringBuilder sb, Object ro, Object o) {
            if (ro instanceof Expression) {
                ExtendedSQLConditionValuesProcessor.createQueryConditionsExpress((Expression) ro, sb);
            } else if (ro instanceof Field) {
                sb.append(ro);
            } else {
                if (BasicOperator.LIKE_OP.equals(o) || BasicOperator.NOT_LIKE_OP.equals(o)) {
                    sb.append(" '");
                    sb.append(ro);
                    sb.append("' ");
                } else {
                    sb.append(ro);
                }
            }
        }

        /**
         * Returns the column name in square bracket if the name contains a special characters
         *
         * @param name column name to checks
         * @return the column name in square bracket if it's necessary
         */
        protected String getColumnName(String name) {
            boolean corchetes = this.getSQLStatementHandler().checkColumnName(name);
            if (!corchetes) {
                return name;
            } else {
                return SQLStatementBuilder.OPEN_SQUARE_BRACKET + name + SQLStatementBuilder.CLOSE_SQUARE_BRACKET;
            }
        }

        protected String renderQueryConditionsFromExpression(Expression expression) {
            if (expression == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            this.renderQueryConditionsFromExpression(expression, sb);
            return sb.toString();
        }

        protected void renderQueryConditionsFromExpression(Expression expression, StringBuilder sb) {
            // Recursive
            Object lo = expression.getLeftOperand();
            Object ro = expression.getRightOperand();
            Object o = expression.getOperator();
            sb.append("(");
            if (lo instanceof Field) {
                sb.append(lo);
                sb.append(" ");
                sb.append(o);
                if (ro != null) renderQueryConditionsFromExpressionRightOperandNotNull(sb, ro, o);
            } else if (lo instanceof Expression) {
                this.renderQueryConditionsFromExpression((Expression) lo, sb);
                if (ro instanceof Expression) {
                    sb.append(" ");
                    sb.append(o);
                    sb.append(" ");
                    this.renderQueryConditionsFromExpression((Expression) ro, sb);
                }
            }
            sb.append(")");
        }

        /**
         * Method extracted to reduce cognitive complexity of {@link #renderQueryConditionsFromExpression(Expression, StringBuilder)}
         * @param sb
         * @param ro
         * @param o
         */
        private void renderQueryConditionsFromExpressionRightOperandNotNull(StringBuilder sb, Object ro, Object o) {
            if (ro instanceof Expression) {
                this.renderQueryConditionsFromExpression((Expression) ro, sb);
            } else if (ro instanceof Field) {
                sb.append(ro);
            } else {
                if (BasicOperator.LIKE_OP.equals(o) || BasicOperator.NOT_LIKE_OP.equals(o)) {
                    sb.append(" '");
                    sb.append(ro);
                    sb.append("' ");
                } else {
                    sb.append(ro);
                }
            }
        }

        /**
         * Creates the condition string from a expression and stores required values in a List.
         *
         * @param expression a class that implements <code>Expression<code> interface
         *                   &#64;param values
         *                   a list where the required values for the condition are stored
         *                   &#64;return a <code>String</code> where is stored a condition in text format
         */

        protected String createQueryConditionsFromExpression(Expression expression, List values) {
            return this.createQueryConditionsFromExpression(expression, values, this.upperLike);
        }

        /**
         * Creates the condition string from a expression and stores required values in a List.
         *
         * @param expression a class that implements <code>Expression<code> interface
         *                   &#64;param values
         *                   a list where the required values for the condition are stored
         *                   &#64;return a <code>String</code> where is stored a condition in text format
         */
        protected String createQueryConditionsFromExpression(Expression expression, List values, boolean upper) {
            if (expression == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            this.createQueryConditionsFromExpression(expression, values, sb, upper);
            return sb.toString();
        }

        /**
         * Creates the condition string from a expression. The condition string is stored in the
         * <code>StringBuilder</code> param and the required values in a <code>List</code> param
         *
         * @param expression a class that implements <code>Expression<code> interface
         *                   &#64;param values
         *                   a list where the required values for the condition are stored
         *                   &#64;param sb
         *                   a <code>StringBuilder</code> where the condition string is stored
         */

        protected void createQueryConditionsFromExpression(Expression expression, List values, StringBuilder sb) {
            this.createQueryConditionsFromExpression(expression, values, sb, this.upperLike);
        }

        /**
         * Creates the condition string from a expression. The condition string is stored in the
         * <code>StringBuilder</code> param and the required values in a <code>List</code> param
         *
         * @param expression a class that implements <code>Expression<code> interface
         *                   &#64;param values
         *                   a list where the required values for the condition are stored
         *                   &#64;param sb
         *                   a <code>StringBuilder</code> where the condition string is stored
         * @param upper      Boolean that enable the String compare in upper case, i.e., for non case-sensitive.
         */
        protected void createQueryConditionsFromExpression(Expression expression, List values, StringBuilder sb,
                                                           boolean upper) {
            // Recursive
            Object lo = expression.getLeftOperand();
            Object ro = expression.getRightOperand();
            Object expressionOperator = expression.getOperator();

            sb.append("(");

            if (lo instanceof Field) {
                createQueryConditionsFromExpressionWhenLeftOperandIsField((BasicExpression) expression, values, sb, upper, lo, ro, expressionOperator);
            }
            else if (lo instanceof Expression) {
                this.createQueryConditionsFromExpression((Expression) lo, values, sb, upper);
                if (ro instanceof Expression) {
                    sb.append(" ");
                    sb.append(expressionOperator);
                    sb.append(" ");
                    this.createQueryConditionsFromExpression((Expression) ro, values, sb, upper);
                }
            }

            sb.append(")");

        }

        /**
         * Method extracted to reduce cognitive complexity of {@link #createQueryConditionsFromExpression(Expression, List, StringBuilder, boolean)}
         * @param expression
         * @param values
         * @param sb
         * @param upper
         * @param lo
         * @param ro
         * @param expressionOperator
         */
        private void createQueryConditionsFromExpressionWhenLeftOperandIsField(BasicExpression expression, List<Object> values, StringBuilder sb, boolean upper, Object lo, Object ro, Object expressionOperator) {
            lo = this.getColumnName(lo.toString());
            if ((BasicOperator.LIKE_OP.equals(expressionOperator)
                    || (BasicOperator.NOT_LIKE_OP.equals(expressionOperator)) && upper) || ((ro instanceof String) && this.upperStrings)) {
                lo = DefaultSQLConditionValuesProcessor.UPPER_FUNCTION + "(" + lo + ")";
            }

            if (ro instanceof SearchValue) {
                boolean brakets = expression.getBrackets();
                sb.append(SQLStatementBuilder.createQueryConditionsSearchValue(ro, values, brakets, lo));
            } else {
                sb.append(lo);
                sb.append(" ");
                sb.append(expressionOperator);
                if (ro != null) {
                    createQueryConditionsFromExpressionIfRightOperandIsNotNull(values, sb, upper, ro, expressionOperator);
                }
            }
        }

        /**
         * Method extracted to reduce cogniteve complexity of {@link #createQueryConditionsFromExpression(Expression, List, StringBuilder, boolean)}
         * @param values
         * @param sb
         * @param upper
         * @param ro
         * @param expressionOperator
         */
        private void createQueryConditionsFromExpressionIfRightOperandIsNotNull(List<Object> values, StringBuilder sb, boolean upper, Object ro, Object expressionOperator) {
            if (ro instanceof Expression) {
                this.createQueryConditionsFromExpression((Expression) ro, values, sb, upper);
            } else if (ro instanceof Field) {
                sb.append(" ");
                sb.append(ro);
            } else {
                if (BasicOperator.LIKE_OP.equals(expressionOperator)
                        || BasicOperator.NOT_LIKE_OP.equals(expressionOperator)) {
                    if (upper) {
                        sb.append(DefaultSQLConditionValuesProcessor.UPPER_FUNCTION + "(");
                        sb.append(" '");
                        sb.append(ro);
                        sb.append("' ");
                        sb.append(")");
                    } else {
                        sb.append(" '");
                        sb.append(ro);
                        sb.append("' ");
                    }
                } else if ((ro instanceof String) && this.upperStrings) {
                    sb.append(DefaultSQLConditionValuesProcessor.UPPER_FUNCTION + "(");
                    sb.append(" '");
                    sb.append(ro);
                    sb.append("' ");
                    sb.append(")");
                } else if (BasicOperator.IN_OP.equals(expressionOperator)
                        || BasicOperator.NOT_IN_OP.equals(expressionOperator))
                    createQueryConditionsFromExpressionIfRightOperadIsNotNullOperatorInNotIn(values, sb, (List) ro);
                else {
                    sb.append(" ? ");
                    values.add(ro);
                }
            }
        }

        /**
         * Method extracted to reduce cognitive complexity of {@link #createQueryConditionsFromExpressionIfRightOperandIsNotNull(List, StringBuilder, boolean, Object, Object)}
         * @param values
         * @param sb
         * @param ro
         */
        private static void createQueryConditionsFromExpressionIfRightOperadIsNotNullOperatorInNotIn(List<Object> values, StringBuilder sb, List<Object> ro) {
            // ro has to be a List
            List<Object> valueList = ro;
            sb.append("(");
            for (int i = 0; i < valueList.size(); i++) {
                sb.append(" ? ");
                if (i < (valueList.size() - 1)) {
                    sb.append(",");
                }
                values.add(valueList.get(i));
            }
            sb.append(")");
        }

        /**
         * Creates the condition string for a SQL Statement.
         *
         * @param conditions a condition list
         * @param wildcards  column list that can use wildcards
         * @param values     List where the value of each processed conditions is stored
         * @return
         */

        @Override
        public String createQueryConditions(Map conditions, List wildcards, List values) {
            // Separate the expressions
            Expression expression = null;
            if (conditions.containsKey(
                    ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY)
                    && (conditions.get(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY) instanceof Expression)) {
                expression = (Expression) conditions.get(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
                conditions.remove(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
            } else if (conditions.containsKey(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY)) {
                conditions.remove(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
            }

            Expression filterExpression = null;
            if (conditions.containsKey(ExtendedSQLConditionValuesProcessor.FILTER_KEY)
                    && (conditions.get(ExtendedSQLConditionValuesProcessor.FILTER_KEY) instanceof Expression)) {
                filterExpression = (Expression) conditions.get(ExtendedSQLConditionValuesProcessor.FILTER_KEY);
                conditions.remove(ExtendedSQLConditionValuesProcessor.FILTER_KEY);
            } else if (conditions.containsKey(ExtendedSQLConditionValuesProcessor.FILTER_KEY)) {
                conditions.remove(ExtendedSQLConditionValuesProcessor.FILTER_KEY);
            }

            String s = super.createQueryConditions(conditions, wildcards, values);
            // Now add the expressions
            String sExp = this.createQueryConditionsFromExpression(expression, values);
            // Now add the filter_key
            String sFilter = this.createQueryConditionsFromExpression(filterExpression, values, true);

            StringBuilder result = new StringBuilder();
            createQueryConditionsWhenExpressionExists(s, sExp, sFilter, result);

            if ((sExp != null) && (sExp.length() > 0)) {
                if (result.length() > 0) {
                    result.append(SQLStatementBuilder.AND);
                }
                result.append(sExp);
            }

            if ((sFilter != null) && (sFilter.length() > 0)) {
                if (result.length() > 0) {
                    result.append(SQLStatementBuilder.AND);
                }
                result.append(sFilter);
            }

            return result.toString();
        }

        /**
         * Method extracted to reduce cognitive complexity of {@link ExtendedSQLConditionValuesProcessor#createQueryConditions(Map, List, List)}
         * @param s
         * @param sExp
         * @param sFilter
         * @param result
         */
        private  void createQueryConditionsWhenExpressionExists(String s, String sExp, String sFilter, StringBuilder result) {
            if ((s != null) && (s.length() > 0)) {
                if (((sExp != null) && (sExp.length() > 0)) || ((sFilter != null) && (sFilter.length() > 0))) {
                    result.append("( ").append(s).append(" )");
                } else {
                    result.append(s);
                }
            }
        }

    }

}
