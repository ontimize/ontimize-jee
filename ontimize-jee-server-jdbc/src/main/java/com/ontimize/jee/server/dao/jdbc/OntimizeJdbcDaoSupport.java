/**
 *
 */
package com.ontimize.jee.server.dao.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.metadata.TableParameterMetaData;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.Assert;

import com.ontimize.jee.common.db.AdvancedEntityResult;
import com.ontimize.jee.common.db.AdvancedEntityResultMapImpl;
import com.ontimize.jee.common.db.NullValue;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicField;
import com.ontimize.jee.common.db.SQLStatementBuilder.ExtendedSQLConditionValuesProcessor;
import com.ontimize.jee.common.db.SQLStatementBuilder.Operator;
import com.ontimize.jee.common.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.jee.common.db.SQLStatementBuilder.SQLStatement;
import com.ontimize.jee.common.db.handler.SQLStatementHandler;
import com.ontimize.jee.common.db.util.DBFunctionName;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.gui.MultipleValue;
import com.ontimize.jee.common.gui.field.MultipleTableAttribute;
import com.ontimize.jee.common.gui.field.ReferenceFieldAttribute;
import com.ontimize.jee.common.naming.I18NNaming;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.Chronometer;
import com.ontimize.jee.common.tools.ObjectTools;
import com.ontimize.jee.common.tools.Pair;
import com.ontimize.jee.common.tools.StringTools;
import com.ontimize.jee.common.tools.streamfilter.ReplaceTokensFilterReader;
import com.ontimize.jee.server.dao.DaoProperty;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.dao.ISQLQueryAdapter;
import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.common.INameConvention;
import com.ontimize.jee.server.dao.common.INameConverter;
import com.ontimize.jee.server.dao.jdbc.extension.IDaoExtensionHelper;
import com.ontimize.jee.server.dao.jdbc.setup.AmbiguousColumnType;
import com.ontimize.jee.server.dao.jdbc.setup.FunctionColumnType;
import com.ontimize.jee.server.dao.jdbc.setup.JdbcEntitySetupType;
import com.ontimize.jee.server.dao.jdbc.setup.OrderColumnType;
import com.ontimize.jee.server.dao.jdbc.setup.QueryType;

/**
 * The Class OntimizeJdbcDaoSupport.
 */
public class OntimizeJdbcDaoSupport extends JdbcDaoSupport implements ApplicationContextAware, IOntimizeDaoSupport {

	/** The logger. */
	protected static final Logger logger = LoggerFactory.getLogger(OntimizeJdbcDaoSupport.class);

	/** The Constant PLACEHOLDER_ORDER. */
	protected static final String PLACEHOLDER_ORDER = "#ORDER#";

	/** The Constant PLACEHOLDER_ORDER_CONCAT. */
	protected static final String PLACEHOLDER_ORDER_CONCAT = "#ORDER_CONCAT#";

	/** The Constant PLACEHOLDER_WHERE. */
	protected static final String PLACEHOLDER_WHERE = "#WHERE#";

	/** The Constant PLACEHOLDER_WHERE_CONCAT. */
	protected static final String PLACEHOLDER_WHERE_CONCAT = "#WHERE_CONCAT#";

	/** The Constant PLACEHOLDER_COLUMNS. */
	protected static final String PLACEHOLDER_COLUMNS = "#COLUMNS#";

	/** The Constant PLACEHOLDER_SCHEMA. */
	protected static final String PLACEHOLDER_SCHEMA = "#SCHEMA#";

	/** Context used to retrieve and manage database metadata. */
	protected final OntimizeTableMetaDataContext tableMetaDataContext;

	/** List of columns objects to be used in insert statement. */
	protected final List<String> declaredColumns = new ArrayList<>();

	/**
	 * Has this operation been compiled? Compilation means at least checking that a DataSource or
	 * JdbcTemplate has been provided, but subclasses may also implement their own custom validation.
	 */
	private boolean compiled = false;

	private String[] generatedKeyNames = {};

	/** The statement builder. */
	private SQLStatementHandler statementHandler;

	/** The bean property converter. */
	private INameConverter nameConverter;

	/** Mandatory delete keys. */
	private List<String> deleteKeys;

	/** Mandatory update keys. */
	private List<String> updateKeys;

	/** Queries. */
	protected final Map<String, QueryTemplateInformation> sqlQueries = new HashMap<>();

	/** The application context. */
	private ApplicationContext applicationContext;

	/**
	 * Configuration file
	 */
	private String configurationFile = null;

	/**
	 * Configuration file placeholder
	 */
	private String configurationFilePlaceholder = null;

	/**
	 * Name convention
	 *
	 */
	@Autowired
	private INameConvention nameConvention;

	/** Dao Extension helper. */
	@Autowired(required = false)
	private IDaoExtensionHelper daoExtensionHelper;

	/**
	 * Instantiates a new ontimize jdbc dao support.
	 */
	public OntimizeJdbcDaoSupport() {
		super();
		this.tableMetaDataContext = this.createTableMetadataContext();
	}

	public OntimizeJdbcDaoSupport(final String configurationFile, final String configurationFilePlaceholder) {
		this();
		this.configurationFile = configurationFile;
		this.configurationFilePlaceholder = configurationFilePlaceholder;
	}

	@Override
	public EntityResult query(Map<?, ?> keysValues, List<?> attributes, List<?> sort, String queryId) {
		return this.query(keysValues, attributes, sort, queryId, null);
	}

	/**
	 * Query.
	 * @param keysValues the keys values
	 * @param attributes the attributes
	 * @param sort the sort
	 * @param queryId the query id
	 * @return the entity result
	 */
	@Override
	public EntityResult query(final Map<?, ?> keysValues, final List<?> attributes, final List<?> sort,
			final String queryId, ISQLQueryAdapter queryAdapter) {
		this.checkCompiled();
		final QueryTemplateInformation queryTemplateInformation = this.getQueryTemplateInformation(queryId);

		final SQLStatement stSQL = this.composeQuerySql(queryId, attributes, keysValues, sort, null, queryAdapter);

		final String sqlQuery = stSQL.getSQLStatement();
		final List<?> vValues = stSQL.getValues();
		// TODO los atributos que se pasan al entityresultsetextractor tienen que ir "desambiguados" porque
		// cuando el DefaultSQLStatementHandler busca
		// las columnas toUpperCase y toLowerCase no tiene en cuenta el '.'
		Chronometer chrono = new Chronometer().start();
		try {

			JdbcTemplate jdbcTemplate = this.getJdbcTemplate();

			if (jdbcTemplate != null) {

				ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(vValues.toArray());

				return jdbcTemplate.query(sqlQuery, pss,
						new EntityResultResultSetExtractor(this.getStatementHandler(), queryTemplateInformation,
								attributes));
			}

			return new EntityResultMapImpl(EntityResult.OPERATION_WRONG, EntityResult.NODATA_RESULT);

		} finally {
			OntimizeJdbcDaoSupport.logger.trace("Time consumed in query+result= {} ms", chrono.stopMs());
		}
	}

	@Override
	public AdvancedEntityResult paginationQuery(Map<?, ?> keysValues, List<?> attributes, int recordNumber,
			int startIndex, List<?> orderBy, String queryId) {
		return this.paginationQuery(keysValues, attributes, recordNumber, startIndex, orderBy, queryId, null);
	}

	/**
	 * Pageable query.
	 * @param keysValues the keys values
	 * @param attributes the attributes
	 * @param recordNumber number of records to query
	 * @param startIndex number of first row
	 * @param orderBy list of columns to establish the order
	 * @param queryId the query id
	 * @return the entity result
	 */
	@Override
	public AdvancedEntityResult paginationQuery(Map<?, ?> keysValues, List<?> attributes, int recordNumber,
			int startIndex, List<?> orderBy, String queryId,
			ISQLQueryAdapter queryAdapter) {
		this.checkCompiled();
		final QueryTemplateInformation queryTemplateInformation = this.getQueryTemplateInformation(queryId);
		final SQLStatement stSQL = this.composeQuerySql(queryId, attributes, keysValues, orderBy,
				new PageableInfo(recordNumber, startIndex),
				queryAdapter);
		final String sqlQuery = stSQL.getSQLStatement();
		final List<?> vValues = stSQL.getValues();

		ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(vValues.toArray());

		JdbcTemplate jdbcTemplate = this.getJdbcTemplate();

		if (jdbcTemplate != null) {
			AdvancedEntityResult advancedER = jdbcTemplate.query(
					new SimpleScrollablePreparedStatementCreator(sqlQuery), pss, new AdvancedEntityResultResultSetExtractor(
							this.getStatementHandler(), queryTemplateInformation, attributes, recordNumber, startIndex));

			advancedER.setTotalRecordCount(this.getQueryRecordNumber(keysValues, queryId));
			return advancedER;

		}

		return new AdvancedEntityResultMapImpl(EntityResult.OPERATION_WRONG, EntityResult.NODATA_RESULT);

	}

	protected static class SimpleScrollablePreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

		private final String sql;

		public SimpleScrollablePreparedStatementCreator(String sql) {
			Assert.notNull(sql, "SQL must not be null");
			this.sql = sql;
		}

		@Override
		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			return con.prepareStatement(this.sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}

		@Override
		public String getSql() {
			return this.sql;
		}
	}

	protected int getQueryRecordNumber(Map<?, ?> keysValues, final String queryId) {
		final QueryTemplateInformation queryTemplateInformation = this.getQueryTemplateInformation(queryId);
		final Map<?, ?> kvWithoutReferenceAttributes = this.processReferenceDataFieldAttributes(keysValues);
		Map<Object, Object> kvValidKeysValues = new HashMap<>();
		final Map<?, ?> processMultipleValueAttributes = this
				.processMultipleValueAttributes(kvWithoutReferenceAttributes);
		if (processMultipleValueAttributes != null) {
			kvValidKeysValues.putAll(processMultipleValueAttributes);
		}

		SQLStatement stSQL = null;

		if (queryTemplateInformation != null) {
			List<String> validColumns = queryTemplateInformation.getValidColumns();
			kvValidKeysValues = this.getValidQueryingKeysValues(kvValidKeysValues, validColumns);

			kvValidKeysValues = this.applyTransformations(queryTemplateInformation, kvValidKeysValues);

			final StringBuilder sbColumns = new StringBuilder();
			sbColumns.append(SQLStatementBuilder.COUNT);

			String sqlTemplate = queryTemplateInformation.getSqlTemplate()
					.replaceAll(OntimizeJdbcDaoSupport.PLACEHOLDER_COLUMNS, sbColumns.toString());
			// Where
			final List<Object> vValues = new ArrayList<>();
			String cond = this.getStatementHandler()
					.createQueryConditionsWithoutWhere(kvValidKeysValues, new ArrayList<>(), vValues);
			if (cond == null) {
				cond = "";
			}
			cond = cond.trim();

			List<Object> vValuesTemp = new ArrayList<>();
			vValuesTemp.addAll(vValues);

			sqlTemplate = this.applyWherePlaceholders(sqlTemplate, cond, vValues, vValuesTemp);

			sqlTemplate = sqlTemplate.replaceAll(OntimizeJdbcDaoSupport.PLACEHOLDER_SCHEMA, this.getSchemaName());
			sqlTemplate = sqlTemplate.replaceAll(OntimizeJdbcDaoSupport.PLACEHOLDER_ORDER_CONCAT, "");
			sqlTemplate = sqlTemplate.replaceAll(OntimizeJdbcDaoSupport.PLACEHOLDER_ORDER, "");
			stSQL = new SQLStatement(sqlTemplate, vValues);
		} else {
			stSQL = this.getStatementHandler()
					.createCountQuery(this.getSchemaTable(), new HashMap<>(kvValidKeysValues), new ArrayList<>(),
							new ArrayList<>());
		}

		String sqlQuery = stSQL.getSQLStatement();
		List vValues = stSQL.getValues();
		EntityResult erResult = this.getJdbcTemplate()
				.query(sqlQuery,
						new EntityResultResultSetExtractor(this.getStatementHandler(), queryTemplateInformation),
						vValues.toArray());

		if ((erResult == null) || (erResult.getCode() == EntityResult.OPERATION_WRONG)) {
			OntimizeJdbcDaoSupport.logger.error("Error executed record count query:{} : {}", erResult.getMessage(),
					sqlQuery);
			return 0;
		}

		List<?> v = (List<?>) erResult.get(SQLStatementBuilder.COUNT_COLUMN_NAME);
		if ((v == null) || v.isEmpty()) {
			OntimizeJdbcDaoSupport.logger
			.error("Error executed record cound query. The result not contain record number.");
			return 0;
		}
		return ((Number) v.get(0)).intValue();
	}

	protected String performPlaceHolderPagination(String sqlTemplate, PageableInfo pageableInfo) {
		if (pageableInfo == null) {
			return sqlTemplate;
		}
		return this.getStatementHandler()
				.convertPaginationStatement(sqlTemplate, pageableInfo.getStartIndex(), pageableInfo.getRecordNumber());
	}

	/**
	 * Compose sql.
	 * @param queryId the query id
	 * @param attributes the attributes
	 * @param keysValues the keys values
	 * @param sort the sort
	 * @return the SQL statement
	 */
	public SQLStatement composeQuerySql(final String queryId, final List<?> attributes, final Map<?, ?> keysValues,
			final List<?> sort, PageableInfo pageableInfo,
			ISQLQueryAdapter queryAdapter) {
		final QueryTemplateInformation queryTemplateInformation = this.getQueryTemplateInformation(queryId);

		final Map<?, ?> kvWithoutReferenceAttributes = this.processReferenceDataFieldAttributes(keysValues);
		Map<Object, Object> kvValidKeysValues = new HashMap<>();
		final Map<?, ?> processMultipleValueAttributes = this
				.processMultipleValueAttributes(kvWithoutReferenceAttributes);
		if (processMultipleValueAttributes != null) {
			kvValidKeysValues.putAll(processMultipleValueAttributes);
		}

		List<?> vValidAttributes = this.processReferenceDataFieldAttributes(attributes);

		SQLStatement stSQL = null;
		if (queryTemplateInformation == null) {
			vValidAttributes = this.getValidAttributes(vValidAttributes, null);
			CheckingTools.failIf(vValidAttributes.isEmpty(), "NO_ATTRIBUTES_TO_QUERY");
			// use table
			if (pageableInfo != null) {
				stSQL = this.getStatementHandler()
						.createSelectQuery(this.getSchemaTable(), new ArrayList<>(vValidAttributes),
								new HashMap<>(kvValidKeysValues), new ArrayList<>(),
								new ArrayList<>(sort == null ? Collections.emptyList() : sort),
								pageableInfo.getRecordNumber(),
								pageableInfo.getStartIndex());
			} else {
				stSQL = this.getStatementHandler()
						.createSelectQuery(this.getSchemaTable(), new ArrayList<>(vValidAttributes),
								new HashMap<>(kvValidKeysValues), new ArrayList<>(),
								new ArrayList<>(sort == null ? Collections.emptyList() : sort));
			}

		} else {
			List<String> validColumns = queryTemplateInformation.getValidColumns();
			kvValidKeysValues = this.getValidQueryingKeysValues(kvValidKeysValues, validColumns);
			vValidAttributes = this.getValidAttributes(vValidAttributes, validColumns);

			kvValidKeysValues = this.applyTransformations(queryTemplateInformation, kvValidKeysValues);
			vValidAttributes = this.applyTransformations(queryTemplateInformation, vValidAttributes);
			CheckingTools.failIf(vValidAttributes.isEmpty(), "NO_ATTRIBUTES_TO_QUERY");
			final StringBuilder sbColumns = new StringBuilder();
			// columns
			for (final Object ob : vValidAttributes) {
				sbColumns.append(ob.toString());
				sbColumns.append(SQLStatementBuilder.COMMA);
			}
			for (int i = 0; i < SQLStatementBuilder.COMMA.length(); i++) {
				sbColumns.deleteCharAt(sbColumns.length() - 1);
			}
			String sqlTemplate = queryTemplateInformation.getSqlTemplate()
					.replaceAll(OntimizeJdbcDaoSupport.PLACEHOLDER_COLUMNS, sbColumns.toString());

			// Where
			final List<Object> vValues = new ArrayList<>();
			String cond = this.getStatementHandler()
					.createQueryConditionsWithoutWhere(kvValidKeysValues, new ArrayList<>(), vValues);
			if (cond == null) {
				cond = "";
			}
			cond = cond.trim();

			List<Object> vValuesTemp = new ArrayList<>();
			vValuesTemp.addAll(vValues);

			sqlTemplate = this.applyWherePlaceholders(sqlTemplate, cond, vValues, vValuesTemp);

			// Order by
			List<OrderColumnType> orderColumns = queryTemplateInformation.getOrderColumns();
			List<Object> sortColumns = this.applyOrderColumns(sort, orderColumns);
			String order = this.getStatementHandler().createSortStatement(sortColumns, false);
			if (order.length() > 0) {
				order = order.substring(SQLStatementBuilder.ORDER_BY.length());
			}
			order = order.trim();

			sqlTemplate = sqlTemplate.replaceAll(OntimizeJdbcDaoSupport.PLACEHOLDER_ORDER_CONCAT,
					order.length() == 0 ? "" : SQLStatementBuilder.COMMA + " " + order);
			sqlTemplate = sqlTemplate.replaceAll(OntimizeJdbcDaoSupport.PLACEHOLDER_ORDER,
					order.length() == 0 ? "" : SQLStatementBuilder.ORDER_BY + " " + order);
			if (pageableInfo != null) {
				sqlTemplate = this.performPlaceHolderPagination(sqlTemplate, pageableInfo);
			}
			sqlTemplate = sqlTemplate.replaceAll(OntimizeJdbcDaoSupport.PLACEHOLDER_SCHEMA, this.getSchemaName());
			stSQL = new SQLStatement(sqlTemplate, vValues);
		}
		if (queryAdapter != null) {
			stSQL = queryAdapter.adaptQuery(stSQL, this, keysValues, kvValidKeysValues, attributes, vValidAttributes,
					sort, queryId);
		}
		OntimizeJdbcDaoSupport.logger.trace(stSQL.getSQLStatement());
		return stSQL;
	}

	/**
	 * Replaces the WHERE placeholders in the SQL template and updates the values.
	 *
	 * @param sqlTemplate   The original SQL template.
	 * @param cond          The generated condition.
	 * @param vValues       The list of values to be updated.
	 * @param vValuesTemp   Temporary list of condition values.
	 * @return The modified sqlTemplate with the placeholders replaced.
	 */
	public String applyWherePlaceholders(String sqlTemplate, String cond, List<Object> vValues, List<Object> vValuesTemp) {
		Pair<String, Integer> replaceAll = StringTools.replaceAll(sqlTemplate,
				OntimizeJdbcDaoSupport.PLACEHOLDER_WHERE_CONCAT,
				cond.length() == 0 ? "" : SQLStatementBuilder.AND + " " + cond);
		sqlTemplate = replaceAll.getFirst();
		for (int i = 1; i < replaceAll.getSecond(); i++) {
			vValues.addAll(vValuesTemp);
		}

		replaceAll = StringTools.replaceAll(sqlTemplate, OntimizeJdbcDaoSupport.PLACEHOLDER_WHERE,
				cond.length() == 0 ? "" : SQLStatementBuilder.WHERE + " " + cond);
		sqlTemplate = replaceAll.getFirst();
		for (int i = 1; i < replaceAll.getSecond(); i++) {
			vValues.addAll(vValuesTemp);
		}
		return sqlTemplate;
	}

	@Override
	public <T> List<T> query(final Map<?, ?> keysValues, final List<?> sort, final String queryId,
			final Class<T> clazz) {
		return this.query(keysValues, sort, queryId, clazz, null);
	}

	/**
	 * Query.
	 * @param <T> the generic type
	 * @param keysValues the keys values
	 * @param sort the sort
	 * @param queryId the query id
	 * @param clazz the clazz
	 * @return the list
	 */
	@Override
	public <T> List<T> query(final Map<?, ?> keysValues, final List<?> sort, final String queryId, final Class<T> clazz,
			ISQLQueryAdapter queryAdapter) {
		this.checkCompiled();
		BeanPropertyRowMapper<T> rowMapper = this.createRowMapper(clazz);
		final SQLStatement stSQL = this.composeQuerySql(queryId, rowMapper.convertBeanPropertiesToDB(clazz), keysValues,
				sort, null, queryAdapter);
		final String sqlQuery = stSQL.getSQLStatement();
		final List<?> vValues = stSQL.getValues();
		return this.getJdbcTemplate().query(sqlQuery, rowMapper, vValues.toArray());
	}

	/**
	 * Executes a single SQL statement using a prepared statement with the given parameters.
	 *
	 * <p>This method is intended for executing a single SQL command (e.g., CREATE, DROP, UPDATE), and supports parameter
	 * substitution using placeholders.</p>
	 *
	 * <p>Restrictions:
	 * <ul>
	 * <li>Only one SQL statement can be executed per call. Do not pass multiple statements separated by semicolons.</li>
	 * <li>Dynamic WHERE clauses not working with DLL statements.</li>
	 * <li>This method does not return results.</li>
	 * </ul>
	 * </p>
	 *
	 * @param sqlStatement The SQL statement to execute.
	 * @param vValues      The list of values for the prepared statement.
	 */
	public boolean executeComposeSQLStatement(String sqlStatement, List<?> vValues) {
		Chronometer chrono = new Chronometer().start();
		try {
			JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
			if (jdbcTemplate != null) {
				jdbcTemplate.execute((ConnectionCallback<Boolean>) con -> {
					PreparedStatement ps = con.prepareStatement(sqlStatement);
					ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(vValues.toArray());
					pss.setValues(ps);
					return ps.execute();
				});
			}
			return false;
		} finally {
			logger.trace("Time consumed in statement= {} ms", chrono.stopMs());
		}
    }

	/**
	 * Creates the row mapper.
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @return the bean property row mapper
	 */
	protected <T> BeanPropertyRowMapper<T> createRowMapper(final Class<T> clazz) {
		return new BeanPropertyRowMapper<>(this.getNameConverter(), this.getDataSource(), clazz);
	}

	/**
	 * Apply template prefix.
	 * @param templateInformation the template information
	 * @param vValidAttributes the v valid attributes
	 * @return the list
	 */
	protected List<?> applyTransformations(final QueryTemplateInformation templateInformation,
			final List<?> vValidAttributes) {
		final List<AmbiguousColumnType> ambiguousColumns = templateInformation.getAmbiguousColumns();
		final List<FunctionColumnType> functionColumns = templateInformation.getFunctionColumns();

		final List<Object> res = new ArrayList<>(vValidAttributes.size());
		for (final Object ob : vValidAttributes) {
			boolean transformed = false;
			if (ambiguousColumns != null) {
				for (final AmbiguousColumnType ambiguosColumn : ambiguousColumns) {
					if (ob.toString().toUpperCase().equals(ambiguosColumn.getName().toUpperCase())) {
						final String dbName = ambiguosColumn.getDatabaseName() == null ? ambiguosColumn.getName()
								: ambiguosColumn.getDatabaseName();
						final StringBuilder sb = new StringBuilder();
						sb.append(ambiguosColumn.getPrefix());
						sb.append(".");
						sb.append(dbName);
						sb.append(SQLStatementBuilder.AS);
						sb.append(ambiguosColumn.getName());
						res.add(sb.toString());
						transformed = true;
						break;
					}
				}
			}
			if (!transformed && (functionColumns != null)) {
				for (final FunctionColumnType functionColumn : functionColumns) {
					if (ob.toString().toUpperCase().equals(functionColumn.getName().toUpperCase())) {
						final StringBuilder sb = new StringBuilder();
						sb.append(SQLStatementBuilder.OPEN_PARENTHESIS);
						sb.append(functionColumn.getValue());
						sb.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
						sb.append(SQLStatementBuilder.AS);
						sb.append(functionColumn.getName());
						res.add(sb.toString());
						transformed = true;
						break;
					}
				}
			}
			if (!transformed) {
				res.add(ob);
			}
		}
		return res;
	}

	/**
	 * Apply template prefix.
	 * @param templateInformation the template information
	 * @param kvValidKeysValues the kv valid keys values
	 * @return the Map
	 */
	protected Map<Object, Object> applyTransformations(final QueryTemplateInformation templateInformation,
			final Map<Object, Object> kvValidKeysValues) {
		final List<AmbiguousColumnType> ambiguousColumns = templateInformation.getAmbiguousColumns();
		final List<FunctionColumnType> functionColumns = templateInformation.getFunctionColumns();

		final Map<Object, Object> res = new HashMap<>();
		for (final Entry<Object, Object> kvEntry : kvValidKeysValues.entrySet()) {
			if (kvEntry.getKey() instanceof String) {
				String key = (String) kvEntry.getKey();
				boolean transformed = false;
				if ( (ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY.equals(key) || ExtendedSQLConditionValuesProcessor.FILTER_KEY.equals(key))
						&& (kvEntry.getValue() instanceof BasicExpression)) {
					res.put(key, this.applyTransformationsToBasicExpression((BasicExpression) kvEntry.getValue(),
							ambiguousColumns, functionColumns));
					transformed = true;
				} else {
					String resolvedAmbiguousColumn = this.resolveAmbiguousColumn(key, ambiguousColumns);
					if (resolvedAmbiguousColumn != null) {
						res.put(resolvedAmbiguousColumn, kvEntry.getValue());
						transformed = true;
					} else {
						String resolvedFunctionColumn = this.resolveFunctionColumn(key, functionColumns);
						if (resolvedFunctionColumn != null) {
							res.put(resolvedFunctionColumn, kvEntry.getValue());
							transformed = true;
						}
					}
				}
				if (!transformed) {
					res.put(key, kvEntry.getValue());
				}
			} else {
				res.put(kvEntry.getKey(), kvEntry.getValue());
			}
		}
		return res;
	}

	protected List<Object> applyOrderColumns(final List<?> sort, final List<OrderColumnType> orderColumns) {
		List<Object> vResult = new ArrayList<>();
		if ((sort != null) && (sort.size() > 0)) {
			vResult.addAll(sort);
		}

		if ((orderColumns != null) && (orderColumns.size() > 0)) {
			for (OrderColumnType orderColumnType : orderColumns) {
				SQLOrder sqlOrder = new SQLOrder(orderColumnType.getName(), "ASC".equals(orderColumnType.getType()));
				vResult.add(sqlOrder);
			}
		}

		return vResult;
	}

	/**
	 * Resolve function column.
	 * @param key the key
	 * @param functionColumns the function columns
	 * @return the string
	 */
	protected String resolveFunctionColumn(String key, List<FunctionColumnType> functionColumns) {
		if (functionColumns != null) {
			for (final FunctionColumnType functionColumn : functionColumns) {
				if (key.toString().toUpperCase().equals(functionColumn.getName().toUpperCase())) {
					return functionColumn.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Resolve ambiguous column.
	 * @param key the key
	 * @param ambiguousColumns the ambiguous columns
	 * @return the string
	 */
	protected String resolveAmbiguousColumn(String key, List<AmbiguousColumnType> ambiguousColumns) {
		if (ambiguousColumns != null) {
			for (final AmbiguousColumnType ambiguosColumn : ambiguousColumns) {
				if (key.toUpperCase().equals(ambiguosColumn.getName().toUpperCase())) {
					final String dbName = ambiguosColumn.getDatabaseName() == null ? key
							: ambiguosColumn.getDatabaseName();
					return ambiguosColumn.getPrefix() + "." + dbName;
				}
			}
		}
		return null;
	}

	/**
	 * Apply transformations to basic expression.
	 * @param functionColumns
	 * @param ambiguousColumns
	 * @param value the value
	 * @return the object
	 */
	protected BasicExpression applyTransformationsToBasicExpression(final BasicExpression original,
			List<AmbiguousColumnType> ambiguousColumns,
			List<FunctionColumnType> functionColumns) {
		Object originalLeftOperand = original.getLeftOperand();
		Operator originalOperator = original.getOperator();
		Object originalRightOperand = original.getRightOperand();
		Object transformedLeftOperand = null;
		Operator transformedOperator = originalOperator;
		Object transformedRightOperand = null;
		if (originalLeftOperand instanceof BasicField) {
			transformedLeftOperand = this.applyTransformationsToBasicField((BasicField) originalLeftOperand,
					ambiguousColumns, functionColumns);
		} else if (originalLeftOperand instanceof BasicExpression) {
			transformedLeftOperand = this.applyTransformationsToBasicExpression((BasicExpression) originalLeftOperand,
					ambiguousColumns, functionColumns);
		} else {
			transformedLeftOperand = originalLeftOperand;
		}

		if (originalRightOperand instanceof BasicField) {
			transformedRightOperand = this.applyTransformationsToBasicField((BasicField) originalRightOperand,
					ambiguousColumns, functionColumns);
		} else if (originalRightOperand instanceof BasicExpression) {
			transformedRightOperand = this.applyTransformationsToBasicExpression((BasicExpression) originalRightOperand,
					ambiguousColumns, functionColumns);
		} else {
			transformedRightOperand = originalRightOperand;
		}

		return new BasicExpression(transformedLeftOperand, transformedOperator, transformedRightOperand);
	}

	/**
	 * Apply transformations to basic field.
	 * @param originalField the original field
	 * @param ambiguousColumns the ambiguous columns
	 * @param functionColumns the function columns
	 * @return the basic field
	 */
	protected BasicField applyTransformationsToBasicField(BasicField originalField,
			List<AmbiguousColumnType> ambiguousColumns, List<FunctionColumnType> functionColumns) {
		String columnName = originalField.toString();
		Integer columnType = originalField.getSqlType();
		if (columnType == null) columnType = this.getColumnSQLType(columnName);
		String resolvedAmbiguousColumn = this.resolveAmbiguousColumn(columnName, ambiguousColumns);
		if (resolvedAmbiguousColumn != null) {
			return new BasicField(resolvedAmbiguousColumn, columnType);
		}
		String resolvedFunctionColumn = this.resolveFunctionColumn(columnName, functionColumns);
		if (resolvedFunctionColumn != null) {
			return new BasicField(resolvedFunctionColumn, columnType);
		}
		return new BasicField(columnName, columnType);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.server.entity.IOntimizeDaoSupport#insert(java.util.Map)
	 */
	/**
	 * Insert.
	 * @param attributesValues the attributes values
	 * @return the entity result
	 */
	@Override
	public EntityResult insert(final Map<?, ?> attributesValues) {
		this.checkCompiled();
		final EntityResult erResult = new EntityResultMapImpl();

		final Map<?, ?> avWithoutMultipleTableAttributes = this.processMultipleTableAttribute(attributesValues);
		final Map<?, ?> avWithoutReferenceAttributes = this
				.processReferenceDataFieldAttributes(avWithoutMultipleTableAttributes);
		final Map<?, ?> avWithoutMultipleValueAttributes = this
				.processMultipleValueAttributes(avWithoutReferenceAttributes);
		final Map<String, Object> avValidPre = this
				.getValidAttributes(this.processStringKeys(avWithoutMultipleValueAttributes));
		final Map<String, Object> avValid = this.removeNullValues(avValidPre);
		if (avValid.isEmpty()) {
			// TODO se deber�a lanzar excepci�n, pero puede tener colaterales con la one-2-one
			OntimizeJdbcDaoSupport.logger.warn("Insert: Attributes does not contain any pair key-value valid");
			return erResult;
		}

		if (this.getGeneratedKeyNames().length < 1) {
			final int res = this.doExecuteInsert(avValid);
			if (res != 1) {
				throw new SQLWarningException(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD, null);
			}
		} else if (this.getGeneratedKeyNames().length == 1) {
			final Object res = this.doExecuteInsertAndReturnKey(avValid);
			if (res == null) {
				throw new DataRetrievalFailureException(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD);
			}
			erResult.put(this.nameConvention.convertName(this.getGeneratedKeyNames()[0]), res);
		}
		return erResult;
	}

	/**
	 * Removes the null values.
	 * @param inputAttributesValues the input attributes values
	 * @return the map
	 */
	protected Map<String, Object> removeNullValues(Map<String, Object> inputAttributesValues) {
		final Map<String, Object> hValidKeysValues = new HashMap<>();
		for (final Entry<String, ?> entry : inputAttributesValues.entrySet()) {
			final String oKey = entry.getKey();
			final Object oValue = entry.getValue();
			if ((oValue != null) && !(oValue instanceof NullValue)) {
				hValidKeysValues.put(oKey, oValue);
			}
		}
		return hValidKeysValues;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.server.entity.IOntimizeDaoSupport#unsafeUpdate(java.util .Map,
	 * java.util.Map)
	 */
	/**
	 * Unsafe update.
	 * @param attributesValues the attributes values
	 * @param keysValues the keys values
	 * @return the entity result
	 */
	@Override
	public EntityResult unsafeUpdate(final Map<?, ?> attributesValues, final Map<?, ?> keysValues) {
		return this.innerUpdate(attributesValues, keysValues, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.server.entity.IOntimizeDaoSupport#update(java.util.Map, java.util.Map)
	 */
	/**
	 * Update.
	 * @param attributesValues the attributes values
	 * @param keysValues the keys values
	 * @return the entity result
	 */
	@Override
	public EntityResult update(final Map<?, ?> attributesValues, final Map<?, ?> keysValues) {
		return this.innerUpdate(attributesValues, keysValues, true);
	}

	/**
	 * Inner update.
	 * @param attributesValues the attributes values
	 * @param keysValues the keys values
	 * @param safe the safe
	 * @return the entity result
	 */
	protected EntityResult innerUpdate(final Map<?, ?> attributesValues, final Map<?, ?> keysValues,
			final boolean safe) {
		this.checkCompiled();
		final EntityResult erResult = new EntityResultMapImpl();

		// Check the primary keys
		final Map<?, ?> avWithoutMultipleTableAttributes = this.processMultipleTableAttribute(attributesValues);
		final Map<?, ?> avWithoutReferenceAttributes = this
				.processReferenceDataFieldAttributes(avWithoutMultipleTableAttributes);
		final Map<?, ?> avWithoutMultipleValue = this.processMultipleValueAttributes(avWithoutReferenceAttributes);

		final Map<?, ?> kvWithoutMulpleTableAttributes = this.processMultipleTableAttribute(keysValues);
		final Map<?, ?> kvWithoutReferenceAttributessRef = this
				.processReferenceDataFieldAttributes(kvWithoutMulpleTableAttributes);

		final Map<String, ?> hValidAttributesValues = this.getValidAttributes(avWithoutMultipleValue);
		Map<?, ?> hValidKeysValues = null;
		if (safe) {
			hValidKeysValues = this.getValidUpdatingKeysValues(kvWithoutReferenceAttributessRef);
			this.checkUpdateKeys(hValidKeysValues);
		} else {
			hValidKeysValues = kvWithoutReferenceAttributessRef;
		}

		if (hValidAttributesValues.isEmpty() || hValidKeysValues.isEmpty()) {
			OntimizeJdbcDaoSupport.logger.debug("Update:  Attributes or Keys do not contain any pair key-value valid");
			throw new SQLWarningException(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD, null);
		}
		final SQLStatement stSQL = this.getStatementHandler()
				.createUpdateQuery(this.getSchemaTable(), new HashMap<>(hValidAttributesValues),
						new HashMap<>(hValidKeysValues));
		final String sqlQuery = stSQL.getSQLStatement();
		final List<?> vValues = this.processNullValues(stSQL.getValues());
		final int update = this.getJdbcTemplate().update(sqlQuery, vValues.toArray());
		if (update == 0) {
			erResult.setCode(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE);
			erResult.setMessage(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD);
		}
		return erResult;
	}

	/**
	 * Process null values.
	 * @param values the values
	 * @return the list
	 */
	protected List<?> processNullValues(final List<?> values) {
		for (int i = 0; i < values.size(); i++) {
			final Object ob = values.get(i);
			if (ob instanceof NullValue) {
				((List<Object>) values).set(i, new SqlParameterValue(((NullValue) ob).getSQLDataType(), null));
			}
		}
		return values;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.server.entity.IOntimizeDaoSupport#unsafeDelete(java.util .Map)
	 */
	/**
	 * Unsafe delete.
	 * @param keysValues the keys values
	 * @return the entity result
	 */
	@Override
	public EntityResult unsafeDelete(final Map<?, ?> keysValues) {
		return this.innerDelete(keysValues, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.server.entity.IOntimizeDaoSupport#delete(java.util.Map)
	 */
	/**
	 * Delete.
	 * @param keysValues the keys values
	 * @return the entity result
	 */
	@Override
	public EntityResult delete(final Map<?, ?> keysValues) {
		return this.innerDelete(keysValues, true);
	}

	/**
	 * Inner delete.
	 * @param keysValues the keys values
	 * @param safe the safe
	 * @return the entity result
	 */
	public EntityResult innerDelete(final Map<?, ?> keysValues, final boolean safe) {
		this.checkCompiled();
		final EntityResult erResult = new EntityResultMapImpl();
		Map<?, ?> keysValuesChecked = keysValues;
		if (safe) {
			keysValuesChecked = this.checkDeleteKeys(keysValues);
		}

		if (keysValuesChecked.isEmpty()) {
			OntimizeJdbcDaoSupport.logger
			.debug("Delete:  Keys does not contain any pair key-value valid:" + keysValues);
			throw new SQLWarningException(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD, null);
		}

		final SQLStatement stSQL = this.getStatementHandler()
				.createDeleteQuery(this.getSchemaTable(), new HashMap<>(keysValuesChecked));
		final String sqlQuery = stSQL.getSQLStatement();
		final List<?> vValues = stSQL.getValues();
		this.getJdbcTemplate().update(sqlQuery, vValues.toArray());

		return erResult;
	}

	/**
	 * Checks if <code>keysValues</code> contains a value for all columns defined in 'delete_keys'
	 * parameter.
	 * <p>
	 * @param keysValues the keys values
	 */
	protected Map<Object, Object> checkDeleteKeys(final Map<?, ?> keysValues) {
		Map<Object, Object> res = new HashMap<>();
		for (String element : this.deleteKeys) {
			String mapKey = element;
			if (!keysValues.containsKey(mapKey)) {
				throw new SQLWarningException("M_NECESSARY_" + mapKey.toUpperCase(), null);
			}
			res.put(mapKey, keysValues.get(mapKey));
		}
		OntimizeJdbcDaoSupport.logger.debug(" Delete valid keys values: Input: {} -> Result: {}", keysValues, res);
		return res;
	}

	/**
	 * Checks if <code>keysValues</code> contains a value for all columns defined in 'update_keys'
	 * parameter.
	 * <p>
	 * @param keysValues the keys values
	 */
	protected void checkUpdateKeys(final Map<?, ?> keysValues) {
		for (String element : this.updateKeys) {
			if (!keysValues.containsKey(element)) {
				throw new SQLWarningException("M_NECESSARY_" + element.toUpperCase(), new SQLWarning());
			}
		}
	}

	/**
	 * Returns a Map containing a list of valid key-value pairs from those contained in the
	 * <code>keysValues</code> argument.
	 * <p>
	 * A key-value pair is valid if the key is valid.
	 * <p>
	 * Only keys matching (case-sensitive) any of the columns defined by the 'update_keys' parameter are
	 * considered valid.
	 * <p>
	 * @param keysValues the keys values
	 * @return the valid updating keys values
	 */
	public Map<?, ?> getValidUpdatingKeysValues(final Map<?, ?> keysValues) {
		final Map<Object, Object> hValidKeysValues = new HashMap<>();
		for (String element : this.updateKeys) {
			if (keysValues.containsKey(element)) {
				hValidKeysValues.put(element, keysValues.get(element));
			}
		}
		OntimizeJdbcDaoSupport.logger
		.debug(" Update valid keys values: Input: " + keysValues + " -> Result: " + hValidKeysValues);
		return hValidKeysValues;
	}

	/**
	 * Returns a Map containing a list of valid key-value pairs from those contained in the
	 * <code>attributesValues</code> argument.
	 * <p>
	 * A key-value pair is valid if the key is in the table column list.
	 * <p>
	 * @param inputAttributesValues the attributes values
	 * @return the valid attributes
	 */
	public Map<String, Object> getValidAttributes(final Map<?, ?> inputAttributesValues) {
		final Map<String, Object> hValidKeysValues = new HashMap<>();
		List<String> nameConventionTableColumns = this.tableMetaDataContext.getNameConventionTableColumns();
		for (final Entry<?, ?> entry : inputAttributesValues.entrySet()) {
			final Object oKey = entry.getKey();
			final Object oValue = entry.getValue();
			if (nameConventionTableColumns.contains(oKey)) {
				hValidKeysValues.put((String) oKey, oValue);
			}
		}
		OntimizeJdbcDaoSupport.logger.debug(
				" Update valid attributes values: Input: " + inputAttributesValues + " -> Result: " + hValidKeysValues);
		return hValidKeysValues;
	}

	/**
	 * Processes all the MultipleTableAttribute contained as keys ih the Map <code>av</code>. All other
	 * objects are added to the resulting List with no changes. The MultipleTableAttribute objects are
	 * replaced by their attribute.
	 * @param av the av
	 * @return a new HashMap with the processed objects.
	 */
	protected Map<?, ?> processMultipleTableAttribute(final Map<?, ?> av) {
		final Map<Object, Object> res = new HashMap<>();
		for (final Entry<?, ?> entry : av.entrySet()) {
			final Object oKey = entry.getKey();
			final Object oValue = entry.getValue();
			if (oKey instanceof MultipleTableAttribute) {
				res.put(((MultipleTableAttribute) oKey).getAttribute(), oValue);
			} else {
				res.put(oKey, oValue);
			}
		}
		return res;
	}

	/**
	 * Processes the ReferenceFieldAttribute objects contained in <code>keysValues</code>.
	 * <p>
	 * Returns a Map containing all the objects contained in the argument <code>keysValues</code> except
	 * in the case of keys that are ReferenceFieldAttribute objects, which are replaced by
	 * ((ReferenceFieldAttribute)object).getAttr()
	 * <p>
	 * @param keysValues the keysValues to process
	 * @return a Map containing the processed objects
	 */
	public Map<?, ?> processReferenceDataFieldAttributes(final Map<?, ?> keysValues) {
		if (keysValues == null) {
			return null;
		}
		final Map<Object, Object> res = new HashMap<>();
		for (final Entry<?, ?> entry : keysValues.entrySet()) {
			final Object oKey = entry.getKey();
			final Object oValue = entry.getValue();
			if (oKey instanceof ReferenceFieldAttribute) {
				final String attr = ((ReferenceFieldAttribute) oKey).getAttr();
				res.put(attr, oValue);
			} else {
				res.put(oKey, oValue);
			}
		}
		return res;
	}

	/**
	 * Processes the ReferenceFieldAttribute objects contained in <code>list</code>.
	 * <p>
	 * Returns a List containing all the objects in the argument <code>list</code> except in the case of
	 * keys that are ReferenceFieldAttribute objects, which are maintained but also
	 * ((ReferenceFieldAttribute)object).getAttr() is added
	 * <p>
	 * @param list the list to process
	 * @return a List containing the processed objects
	 */
	public List<?> processReferenceDataFieldAttributes(final List<?> list) {
		if (list == null) {
			return null;
		}
		final List<Object> res = new ArrayList<>();
		for (final Object ob : list) {
			// Add the attribute
			if (!res.contains(ob)) {
				res.add(ob);
			}
			// If the attribute is ReferenceFieldAttribute add the string to
			if ((ob instanceof ReferenceFieldAttribute) && !res.contains(((ReferenceFieldAttribute) ob).getAttr())) {
				res.add(((ReferenceFieldAttribute) ob).getAttr());
			}
		}
		return res;
	}

	/**
	 * Returns a list containing the valid attributes of those included in the List
	 * <code>attributes</code>
	 * <p>
	 * If valid column names have been specified for this entity, only attributes matching
	 * (case-sensitive) any of this column names are considered valid.
	 * <p>
	 * If no columns have been defined, all attributes will be considered valid.
	 * @param attributes the attributes
	 * @param validColumns the valid columns
	 * @return a List with the valid attributes
	 */
	public List<?> getValidAttributes(final List<?> attributes, List<String> validColumns) {
		List<String> inputValidColumns = validColumns == null ? (List<String>) Collections.EMPTY_LIST : validColumns;
		final List<Object> validAttributes = new ArrayList<>();
		for (final Object ob : attributes) {
			if ((ob instanceof String) || (ob instanceof DBFunctionName)) {
				boolean isValid = true;
				if (ob instanceof String) {
					if (ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY.equals(ob)) {
						isValid = true;
					} else if (!inputValidColumns.isEmpty() && !inputValidColumns.contains(ob)) {
						isValid = false;
					} else {
						isValid = this.isColumnNameValid((String) ob);
					}
				}
				if (isValid) {
					validAttributes.add(ob);
				}
			}
		}
		return validAttributes;
	}

	/**
	 * Checks if is column name valid.
	 * @param ob the ob
	 * @return true, if is column name valid
	 */
	protected boolean isColumnNameValid(String ob) {
		boolean notValid = ob.contains(" ") || ob.contains("*");
		return !notValid;
	}

	/**
	 * Returns a cleaned map containing the valid pairs of those included in the map
	 * <code>inputKeysValues</code>
	 * <p>
	 * If valid column names have been specified for this entity/query, only attributes matching
	 * (case-sensitive) any of this column names are considered valid.
	 * <p>
	 * If no columns have been defined, all attributes will be considered valid.
	 *
	 * Returns cleaned keys values to do query according valid columns (if defined).
	 * @param inputKeysValues
	 * @param validColumns
	 * @return
	 */
	protected Map<Object, Object> getValidQueryingKeysValues(Map<Object, Object> inputKeysValues,
			List<String> validColumns) {
		if ((validColumns == null) || validColumns.isEmpty()) {
			return inputKeysValues;
		}
		final Map<Object, Object> hValidKeysValues = new HashMap<>();
		for (Entry<Object, Object> entry : inputKeysValues.entrySet()) {
			if (ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY.equals(entry.getKey())
					|| validColumns.contains(entry.getKey())) {
				hValidKeysValues.put(entry.getKey(), entry.getValue());
			}
		}
		OntimizeJdbcDaoSupport.logger
		.debug(" Query valid keys values: Input: " + inputKeysValues + " -> Result: " + hValidKeysValues);
		return hValidKeysValues;
	}

	/**
	 * Processes the MultipleValue objects contained in <code>keysValues</code>. Returns a new HashMap
	 * with the same data as <code>keysValues</code> except that MultipleValue objects are deleted and
	 * the key-value pairs of these objects are added to the new HashMap.
	 * @param keysValues the keys values
	 * @return a new HashMap with MultipleValue objects replaced by their key-value pairs
	 */
	public Map<?, ?> processMultipleValueAttributes(final Map<?, ?> keysValues) {
		if (keysValues == null) {
			return null;
		}
		final Map<Object, Object> res = new HashMap<>();
		for (final Entry<?, ?> entry : keysValues.entrySet()) {
			final Object oKey = entry.getKey();
			final Object oValue = entry.getValue();
			if (oValue instanceof MultipleValue) {
				final Enumeration<?> mvKeys = ((MultipleValue) oValue).keys();
				while (mvKeys.hasMoreElements()) {
					final Object iMvKeyM = mvKeys.nextElement();
					final Object oMvValue = ((MultipleValue) oValue).get(iMvKeyM);
					res.put(iMvKeyM, oMvValue);
				}
			} else {
				res.put(oKey, oValue);
			}
		}
		return res;
	}

	/**
	 * Processes the keys in order to get String as column name.
	 * @param keysValues the keys values
	 * @return a new HashMap with MultipleValue objects replaced by their key-value pairs
	 */
	public Map<String, ?> processStringKeys(final Map<?, ?> keysValues) {
		if (keysValues == null) {
			return null;
		}
		final Map<String, Object> res = new HashMap<>();
		for (final Entry<?, ?> entry : keysValues.entrySet()) {
			final Object oKey = entry.getKey();
			final Object oValue = entry.getValue();
			res.put(oKey.toString(), oValue);
		}
		return res;
	}

	// -------------------------------------------------------------------------
	// Methods dealing with configuration properties
	// -------------------------------------------------------------------------

	/**
	 * Set the name of the table for this insert.
	 * @param tableName the new table name
	 */
	public void setTableName(final String tableName) {
		this.checkIfConfigurationModificationIsAllowed();
		this.tableMetaDataContext.setTableName(tableName);
	}

	/**
	 * Get the name of the table for this insert.
	 * @return the table name
	 */
	public String getTableName() {
		this.checkCompiled();
		return this.tableMetaDataContext.getTableName();
	}

	/**
	 * Set the name of the schema for this insert.
	 * @param schemaName the new schema name
	 */
	public void setSchemaName(final String schemaName) {
		this.checkIfConfigurationModificationIsAllowed();
		this.tableMetaDataContext.setSchemaName(StringTools.isEmpty(schemaName) ? null : schemaName);
	}

	/**
	 * Get the name of the schema for this insert.
	 * @return the schema name
	 */
	public String getSchemaName() {
		this.checkCompiled();
		return this.tableMetaDataContext.getSchemaName();
	}

	/**
	 * Set the name of the catalog for this insert.
	 * @param catalogName the new catalog name
	 */
	public void setCatalogName(final String catalogName) {
		this.checkIfConfigurationModificationIsAllowed();
		this.tableMetaDataContext.setCatalogName(StringTools.isEmpty(catalogName) ? null : catalogName);
	}

	/**
	 * Get the name of the catalog for this insert.
	 * @return the catalog name
	 */
	public String getCatalogName() {
		this.checkCompiled();
		return this.tableMetaDataContext.getCatalogName();
	}

	/**
	 * Set the names of the columns to be used.
	 * @param columnNames the new column names
	 */
	public void setColumnNames(final List<String> columnNames) {
		this.checkIfConfigurationModificationIsAllowed();
		this.declaredColumns.clear();
		this.declaredColumns.addAll(columnNames);
	}

	/**
	 * Get the names of the columns used.
	 * @return the column names
	 */
	public List<String> getColumnNames() {
		this.checkCompiled();
		return Collections.unmodifiableList(this.declaredColumns);
	}

	/**
	 * Get the names of any generated keys.
	 * @return the generated key names
	 */
	public String[] getGeneratedKeyNames() {
		this.checkCompiled();
		return this.generatedKeyNames;
	}

	/**
	 * Set the names of any generated keys.
	 * @param generatedKeyNames the new generated key names
	 */
	public void setGeneratedKeyNames(final String[] generatedKeyNames) {
		this.checkIfConfigurationModificationIsAllowed();
		this.generatedKeyNames = generatedKeyNames;
	}

	/**
	 * Specify the name of a single generated key column.
	 * @param generatedKeyName the new generated key name
	 */
	public void setGeneratedKeyName(final String generatedKeyName) {
		this.checkIfConfigurationModificationIsAllowed();
		if (generatedKeyName == null) {
			this.generatedKeyNames = new String[] {};
		} else {
			this.generatedKeyNames = new String[] { generatedKeyName };
		}
	}

	/**
	 * Specify whether the parameter metadata for the call should be used. The default is true.
	 * @param accessTableColumnMetaData the new access table column meta data
	 */
	public void setAccessTableColumnMetaData(final boolean accessTableColumnMetaData) {
		this.tableMetaDataContext.setAccessTableColumnMetaData(accessTableColumnMetaData);
	}

	/**
	 * Specify whether the default for including synonyms should be changed. The default is false.
	 * @param override the new override include synonyms default
	 */
	public void setOverrideIncludeSynonymsDefault(final boolean override) {
		this.tableMetaDataContext.setOverrideIncludeSynonymsDefault(override);
	}


	// -------------------------------------------------------------------------
	// Methods handling compilation issues
	// -------------------------------------------------------------------------
	/**
	 * Compile this JdbcInsert using provided parameters and meta data plus other settings. This
	 * finalizes the configuration for this object and subsequent attempts to compile are ignored. This
	 * will be implicitly called the first time an un-compiled insert is executed.
	 * @throws InvalidDataAccessApiUsageException if the object hasn't been correctly initialized, for
	 *         example if no DataSource has been provided
	 */
	public synchronized final void compile() throws InvalidDataAccessApiUsageException {
		if (!this.isCompiled()) {
			final ConfigurationFile annotation = this.getClass().getAnnotation(ConfigurationFile.class);
			if (annotation != null) {
				this.configurationFile = annotation.configurationFile();
				this.configurationFilePlaceholder = annotation.configurationFilePlaceholder();
			}
			this.loadConfigurationFile(this.configurationFile, this.configurationFilePlaceholder);

			if (this.getJdbcTemplate() == null) {
				throw new IllegalArgumentException("'dataSource' or 'jdbcTemplate' is required");
			}

			if (this.tableMetaDataContext.getTableName() == null) {
				throw new InvalidDataAccessApiUsageException("Table name is required");
			}
			try {
				this.getJdbcTemplate().afterPropertiesSet();
			} catch (final IllegalArgumentException ex) {
				throw new InvalidDataAccessApiUsageException(ex.getMessage(), ex);
			}
			this.compileInternal();
			this.compiled = true;
			if (OntimizeJdbcDaoSupport.logger.isDebugEnabled()) {
				OntimizeJdbcDaoSupport.logger.debug("JdbcInsert for table [{}] compiled", this.getTableName());
			}
		}
	}

	@Override
	public void reload() {
		OntimizeJdbcDaoSupport.logger.debug("dao {} - {} marked to recompile", this.getClass().getName(),
				this.getTableName());
		this.compiled = false;
		this.setTableName(null);
		this.setSchemaName(null);
		this.setCatalogName(null);
		this.setDeleteKeys(null);
		this.setUpdateKeys(null);
		this.sqlQueries.clear();
		this.setGeneratedKeyName(null);
		this.setStatementHandler(null);
		this.setNameConverter(null);
	}

	/**
	 * Load the configuration file.
	 * @param path the path
	 * @param pathToPlaceHolder the path to place holder
	 * @throws InvalidDataAccessApiUsageException the invalid data access api usage exception
	 */
	protected void loadConfigurationFile(final String path, final String pathToPlaceHolder)
			throws InvalidDataAccessApiUsageException {

		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);) {
			Reader reader = null;
			if (pathToPlaceHolder != null) {
				try (InputStream isPlaceHolder = Thread.currentThread()
						.getContextClassLoader()
						.getResourceAsStream(pathToPlaceHolder);) {
					final Properties prop = new Properties();
					if (isPlaceHolder != null) {
						prop.load(isPlaceHolder);
					}

					Map<String, String> mapProperties = prop.stringPropertyNames()
							.stream()
							.collect(Collectors.toMap(
									Function.identity(),
									prop::getProperty
							));
					reader = new ReplaceTokensFilterReader(new InputStreamReader(is),
							mapProperties);
				}
			} else {
				reader = new InputStreamReader(is);
			}

			JdbcEntitySetupType baseSetup = JAXB.unmarshal(reader, JdbcEntitySetupType.class);

			// Support to Dao extensions
			JdbcEntitySetupType setupConfig = this.checkDaoExtensions(baseSetup, path, pathToPlaceHolder);

			// Process setup information to configure dao
			this.setTableName(setupConfig.getTable());
			this.setSchemaName(setupConfig.getSchema());
			this.setCatalogName(setupConfig.getCatalog());
			this.setDeleteKeys(setupConfig.getDeleteKeys().getColumn());
			this.setUpdateKeys(setupConfig.getUpdateKeys().getColumn());
			if (setupConfig.getQueries() != null) {
				for (final QueryType query : setupConfig.getQueries().getQuery()) {//
					this.addQueryTemplateInformation(query.getId(), query.getSentence().getValue(), //
							query.getAmbiguousColumns() == null ? null
									: query.getAmbiguousColumns().getAmbiguousColumn(), //
									query.getFunctionColumns() == null ? null : query.getFunctionColumns().getFunctionColumn(), //
											query.getValidColumns() != null ? query.getValidColumns().getColumn()
													: new ArrayList<String>(), //
													query.getOrderColumns() == null ? null : query.getOrderColumns().getOrderColumn());
				}
			}
			this.setGeneratedKeyName(setupConfig.getGeneratedKey());
			this.setDataSource((DataSource) this.applicationContext.getBean(setupConfig.getDatasource()));
			this.setStatementHandler(
					(SQLStatementHandler) this.applicationContext.getBean(setupConfig.getSqlhandler()));

			final String nameConverter = setupConfig.getNameconverter();
			if (!CheckingTools.isStringEmpty(nameConverter)) {
				this.setNameConverter((INameConverter) this.applicationContext.getBean(nameConverter));
			}
			this.tableMetaDataContext.setNameConvention(this.nameConvention);
		} catch (final IOException e) {
			throw new InvalidDataAccessApiUsageException(I18NNaming.M_ERROR_LOADING_CONFIGURATION_FILE, e);
		}

	}

	protected JdbcEntitySetupType checkDaoExtensions(JdbcEntitySetupType baseSetup, final String path,
			final String pathToPlaceHolder) {
		if (this.daoExtensionHelper == null) {
			return baseSetup;
		}
		return this.daoExtensionHelper.checkDaoExtensions(baseSetup, path, pathToPlaceHolder);
	}

	/**
	 * Sets the bean property converter.
	 * @param converter the new bean property converter
	 */
	protected void setNameConverter(final INameConverter converter) {
		this.nameConverter = converter;
	}

	/**
	 * Gets the bean property converter.
	 * @return the bean property converter
	 */
	public INameConverter getNameConverter() {
		this.checkCompiled();
		return this.nameConverter;
	}

	/**
	 * Sets the configuration file.
	 * @param configurationFile the new configuration file
	 */
	public synchronized void setConfigurationFile(final String configurationFile) {
		this.configurationFile = configurationFile;
	}

	/**
	 * Gets the configuration file.
	 * @return the configuration file
	 */
	public String getConfigurationFile() {
		return this.configurationFile;
	}

	/**
	 * Sets the configuration file placeholder.
	 * @param configurationFilePlaceholder the new configuration file placeholder
	 */
	public synchronized void setConfigurationFilePlaceholder(final String configurationFilePlaceholder) {
		this.configurationFilePlaceholder = configurationFilePlaceholder;
	}

	/**
	 * Gets the configuration file placeholder.
	 * @return the configuration file placeholder
	 */
	public String getConfigurationFilePlaceholder() {
		return this.configurationFilePlaceholder;
	}

	public INameConvention getNameConvention() {
		return nameConvention;
	}

	public void setNameConvention(INameConvention nameConvention) {
		this.nameConvention = nameConvention;
	}

	/**
	 * Check dao config.
	 */
	@Override
	protected void checkDaoConfig() {
		// no need of jdbctemplate at this point
	}

	/**
	 * Adds a query.
	 * @param id the id
	 * @param value the value
	 * @param ambiguousColumns the ambiguous columns
	 * @param functionColumns the function columns
	 */
	public void addQueryTemplateInformation(final String id, final String value,
			final List<AmbiguousColumnType> ambiguousColumns, final List<FunctionColumnType> functionColumns,
			List<OrderColumnType> orderColumns) {
		this.addQueryTemplateInformation(id, value, ambiguousColumns, functionColumns, new ArrayList<>(),
				orderColumns);
	}

	/**
	 * Adds a query, allowing determine valid columns to query to DB.
	 * @param id
	 * @param value
	 * @param ambiguousColumns
	 * @param functionColumns
	 * @param validColumns
	 */
	public void addQueryTemplateInformation(final String id, final String value,
			final List<AmbiguousColumnType> ambiguousColumns, final List<FunctionColumnType> functionColumns,
			List<String> validColumns, List<OrderColumnType> orderColumns) {
		this.sqlQueries.put(id,
				new QueryTemplateInformation(value, ambiguousColumns, functionColumns, validColumns, orderColumns));
	}

	/**
	 * Gets the template query.
	 * @param id the id
	 * @return the template query
	 */
	public QueryTemplateInformation getQueryTemplateInformation(final String id) {
		this.checkCompiled();
		return this.sqlQueries.get(id);
	}

	/**
	 * Method to perform the actual compilation. Subclasses can override this template method to perform
	 * their own compilation. Invoked after this base class's compilation is complete.
	 */
	protected void compileInternal() {
		this.tableMetaDataContext.processMetaData(this.getJdbcTemplate().getDataSource(), this.declaredColumns,
				this.generatedKeyNames);
		this.onCompileInternal();
	}

	/**
	 * Hook method that subclasses may override to react to compilation. This implementation does
	 * nothing.
	 */
	protected void onCompileInternal() {
		// This implementation does nothing.
	}

	/**
	 * Is this operation "compiled"?.
	 * @return whether this operation is compiled, and ready to use.
	 */
	public boolean isCompiled() {
		return this.compiled;
	}

	/**
	 * Check whether this operation has been compiled already; lazily compile it if not already
	 * compiled.
	 * <p>
	 * Automatically called by {@code validateParameters}.
	 */
	public void checkCompiled() {
		if (!this.isCompiled()) {
			OntimizeJdbcDaoSupport.logger.debug("JdbcInsert not compiled before execution - invoking compile");
			this.compile();
		}
	}

	/**
	 * Method to check whether we are allowd to make any configuration changes at this time. If the
	 * class has been compiled, then no further changes to the configuration are allowed.
	 */
	protected void checkIfConfigurationModificationIsAllowed() {
		if (this.isCompiled()) {
			throw new InvalidDataAccessApiUsageException(
					"Configuration can't be altered once the class has been compiled or used");
		}
	}

	// -------------------------------------------------------------------------
	// Methods handling execution
	// -------------------------------------------------------------------------

	/**
	 * Method that provides execution of the insert using the passed in Map of parameters.
	 * @param args Map with parameter names and values to be used in insert
	 * @return number of rows affected
	 */
	protected int doExecuteInsert(final Map<String, Object> args) {
		this.checkCompiled();
		final InsertMetaInfoHolder holder = this.matchInParameterValuesWithInsertColumns(args);
		return this.executeInsertInternal(holder);
	}

	/**
	 * Method that provides execution of the insert using the passed in.
	 * @param parameterSource parameter names and values to be used in insert
	 * @return number of rows affected {@link SqlParameterSource}
	 */
	protected int doExecuteInsert(final SqlParameterSource parameterSource) {
		this.checkCompiled();
		final InsertMetaInfoHolder holder = this.matchInParameterValuesWithInsertColumns(parameterSource);
		return this.executeInsertInternal(holder);
	}

	/**
	 * Method to execute the insert.
	 * @param values the values
	 * @return the int
	 */
	protected int executeInsertInternal(InsertMetaInfoHolder holder) {
		OntimizeJdbcDaoSupport.logger.debug("The following parameters are used for insert {} with: {}",
				holder.getInsertString(), holder.getValues());
		return this.getJdbcTemplate()
				.update(holder.getInsertString(), holder.getValues().toArray(), holder.getInsertTypes());
	}

	/**
	 * Method that provides execution of the insert using the passed in Map of parameters and returning
	 * a generated key.
	 * @param args Map with parameter names and values to be used in insert
	 * @return the key generated by the insert
	 */
	protected Object doExecuteInsertAndReturnKey(final Map<String, Object> args) {
		this.checkCompiled();
		final InsertMetaInfoHolder holder = this.matchInParameterValuesWithInsertColumns(args);
		return this.executeInsertAndReturnKeyInternal(holder);
	}

	/**
	 * Method that provides execution of the insert using the passed in.
	 * @param parameterSource parameter names and values to be used in insert
	 * @return the key generated by the insert {@link SqlParameterSource} and returning a generated key
	 */
	protected Object doExecuteInsertAndReturnKey(final SqlParameterSource parameterSource) {
		this.checkCompiled();
		final InsertMetaInfoHolder holder = this.matchInParameterValuesWithInsertColumns(parameterSource);
		return this.executeInsertAndReturnKeyInternal(holder);
	}

	/**
	 * Method that provides execution of the insert using the passed in Map of parameters and returning
	 * all generated keys.
	 * @param args Map with parameter names and values to be used in insert
	 * @return the KeyHolder containing keys generated by the insert
	 */
	protected KeyHolder doExecuteInsertAndReturnKeyHolder(final Map<String, Object> args) {
		this.checkCompiled();
		final InsertMetaInfoHolder holder = this.matchInParameterValuesWithInsertColumns(args);
		return this.executeInsertAndReturnKeyHolderInternal(holder);
	}

	/**
	 * Method that provides execution of the insert using the passed in.
	 * @param parameterSource parameter names and values to be used in insert
	 * @return the KeyHolder containing keys generated by the insert {@link SqlParameterSource} and
	 *         returning all generated keys
	 */
	protected KeyHolder doExecuteInsertAndReturnKeyHolder(final SqlParameterSource parameterSource) {
		this.checkCompiled();
		final InsertMetaInfoHolder holder = this.matchInParameterValuesWithInsertColumns(parameterSource);
		return this.executeInsertAndReturnKeyHolderInternal(holder);
	}

	/**
	 * Method to execute the insert generating single key.
	 * @param values the values
	 * @return the number
	 */
	protected Object executeInsertAndReturnKeyInternal(final InsertMetaInfoHolder holder) {
		final KeyHolder kh = this.executeInsertAndReturnKeyHolderInternal(holder);
		if ((kh != null) && (kh.getKeyAs(Object.class) != null)) {
			return kh.getKeyAs(Object.class);
		}
		throw new DataIntegrityViolationException(
				"Unable to retrieve the generated key for the insert: " + holder.getInsertString());
	}

	/**
	 * Method to execute the insert generating any number of keys.
	 * @param values the values
	 * @return the key holder
	 */
	protected KeyHolder executeInsertAndReturnKeyHolderInternal(final InsertMetaInfoHolder holder) {
		OntimizeJdbcDaoSupport.logger.debug("The following parameters are used for call {} with: {}",
				holder.getInsertString(), holder.getValues());
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		if (!this.tableMetaDataContext.isGetGeneratedKeysSupported()) {
			if (!this.tableMetaDataContext.isGetGeneratedKeysSimulated()) {
				throw new InvalidDataAccessResourceUsageException(
						"The getGeneratedKeys feature is not supported by this database");
			}
			if (this.getGeneratedKeyNames().length < 1) {
				throw new InvalidDataAccessApiUsageException(
						"Generated Key Name(s) not specificed. "
								+ "Using the generated keys features requires specifying the name(s) of the generated column(s)");
			}
			if (this.getGeneratedKeyNames().length > 1) {
				throw new InvalidDataAccessApiUsageException(
						"Current database only supports retreiving the key for a single column. There are "
								+ this.getGeneratedKeyNames().length + " columns specified: " + Arrays
								.asList(this.getGeneratedKeyNames()));
			}
			// This is a hack to be able to get the generated key from a
			// database that doesn't support
			// get generated keys feature. HSQL is one, PostgreSQL is another.
			// Postgres uses a RETURNING
			// clause while HSQL uses a second query that has to be executed
			// with the same connection.
			final String keyQuery = this.tableMetaDataContext.getSimulationQueryForGetGeneratedKey(
					this.tableMetaDataContext.getTableName(), this.getGeneratedKeyNames()[0]);
			Assert.notNull(keyQuery, "Query for simulating get generated keys can't be null");
			if (keyQuery.toUpperCase().startsWith("RETURNING")) {
				final Long key = this.getJdbcTemplate()
						.queryForObject(holder.getInsertString() + " " + keyQuery,
								holder.getValues().toArray(new Object[holder.getValues().size()]),
								Long.class);
				final Map<String, Object> keys = new HashMap<>(1);
				keys.put(this.getGeneratedKeyNames()[0], key);
				keyHolder.getKeyList().add(keys);
			} else {
				this.getJdbcTemplate().execute(new ConnectionCallback<Object>() {

					@Override
					public Object doInConnection(final Connection con) throws SQLException, DataAccessException {
						// Do the insert
						PreparedStatement ps = null;
						try {
							ps = con.prepareStatement(holder.getInsertString());
							OntimizeJdbcDaoSupport.this.setParameterValues(ps, holder.getValues(),
									holder.getInsertTypes());
							ps.executeUpdate();
						} finally {
							JdbcUtils.closeStatement(ps);
						}
						// Get the key
						ResultSet rs = null;
						final Map<String, Object> keys = new HashMap<>(1);
						final Statement keyStmt = con.createStatement();
						try {
							rs = keyStmt.executeQuery(keyQuery);
							if (rs.next()) {
								final long key = rs.getLong(1);
								keys.put(OntimizeJdbcDaoSupport.this.getGeneratedKeyNames()[0], key);
								keyHolder.getKeyList().add(keys);
							}
						} finally {
							JdbcUtils.closeResultSet(rs);
							JdbcUtils.closeStatement(keyStmt);
						}
						return null;
					}
				});
			}
			return keyHolder;
		}
		this.getJdbcTemplate().update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
				final PreparedStatement ps = OntimizeJdbcDaoSupport.this.prepareInsertStatementForGeneratedKeys(con,
						holder.getInsertString());
				OntimizeJdbcDaoSupport.this.setParameterValues(ps, holder.getValues(), holder.getInsertTypes());
				return ps;
			}
		}, keyHolder);
		return keyHolder;
	}

	/**
	 * Create the PreparedStatement to be used for insert that have generated keys.
	 * @param con the connection used
	 * @return PreparedStatement to use
	 * @throws SQLException the sQL exception
	 */
	protected PreparedStatement prepareInsertStatementForGeneratedKeys(final Connection con, String insertString)
			throws SQLException {
		if (this.getGeneratedKeyNames().length < 1) {
			throw new InvalidDataAccessApiUsageException(
					"Generated Key Name(s) not specificed. "
							+ "Using the generated keys features requires specifying the name(s) of the generated column(s)");
		}
		PreparedStatement ps;
		if (this.tableMetaDataContext.isGeneratedKeysColumnNameArraySupported()) {
			OntimizeJdbcDaoSupport.logger.debug("Using generated keys support with array of column names.");
			ps = con.prepareStatement(insertString, this.getGeneratedKeyNames());
		} else {
			OntimizeJdbcDaoSupport.logger.debug("Using generated keys support with Statement.RETURN_GENERATED_KEYS.");
			ps = con.prepareStatement(insertString, Statement.RETURN_GENERATED_KEYS);
		}
		return ps;
	}

	/**
	 * Method that provides execution of a batch insert using the passed in Maps of parameters.
	 * @param batch array of Maps with parameter names and values to be used in batch insert
	 * @return array of number of rows affected
	 */
	@Override
	public int[] insertBatch(final Map<String, Object>[] batch) {
		this.checkCompiled();
		final List<Object>[] batchValues = new ArrayList[batch.length];
		int i = 0;
		for (final Map<String, Object> args : batch) {
			final List<Object> values = this.matchInParameterValuesWithInsertColumnsForBatch(args);
			batchValues[i++] = values;
		}
		return this.executeInsertBatchInternal(batchValues,
				this.tableMetaDataContext.createInsertString(this.getGeneratedKeyNames()),
				this.tableMetaDataContext.createInsertTypes());
	}

	/**
	 * Method that provides execution of a batch insert using the passed in array of
	 * {@link SqlParameterSource}.
	 * @param batch array of SqlParameterSource with parameter names and values to be used in insert
	 * @return array of number of rows affected
	 */
	protected int[] doExecuteInsertBatch(final SqlParameterSource[] batch, final String insertString,
			final int[] insertTypes) {
		this.checkCompiled();
		final List<Object>[] batchValues = new ArrayList[batch.length];
		int i = 0;
		for (final SqlParameterSource parameterSource : batch) {
			final List<Object> values = this.matchInParameterValuesWithInsertColumnsForBatch(parameterSource);
			batchValues[i++] = values;
		}
		return this.executeInsertBatchInternal(batchValues, insertString, insertTypes);
	}

	/**
	 * Method to execute the batch insert.
	 * @param batchValues the batch values
	 * @return the int[]
	 */
	protected int[] executeInsertBatchInternal(final List<Object>[] batchValues, final String insertString,
			final int[] insertTypes) {
		OntimizeJdbcDaoSupport.logger.debug("Executing statement {} with batch of size: {}", insertString,
				batchValues.length);
		return this.getJdbcTemplate().batchUpdate(insertString, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(final PreparedStatement ps, final int i) throws SQLException {
				final List<Object> values = batchValues[i];
				OntimizeJdbcDaoSupport.this.setParameterValues(ps, values, insertTypes);
			}

			@Override
			public int getBatchSize() {
				return batchValues.length;
			}
		});
	}

	/**
	 * Internal implementation for setting parameter values.
	 * @param preparedStatement the PreparedStatement
	 * @param values the values to be set
	 * @param columnTypes the column types
	 * @throws SQLException the sQL exception
	 */
	protected void setParameterValues(final PreparedStatement preparedStatement, final List<Object> values,
			final int[] columnTypes) throws SQLException {

		int colIndex = 0;
		for (Object value : values) {
			colIndex++;
			if ((columnTypes == null) || (colIndex > columnTypes.length)) {
				StatementCreatorUtils.setParameterValue(preparedStatement, colIndex, SqlTypeValue.TYPE_UNKNOWN, value);
			} else {
				final int sqlType = columnTypes[colIndex - 1];
				if (ObjectTools.isIn(sqlType, Types.BLOB, Types.BINARY, Types.VARBINARY)
						&& ((value instanceof byte[]) || (value instanceof InputStream))) {
					if (value instanceof byte[]) {
						preparedStatement.setBytes(colIndex, (byte[]) value);
					} else {
						try {
							// TODO esto no esta soportado por los drivers jdbc 4.0
							// TODO segun el driver puede ser que sea mas rapido llamar al metodo con la longitud
							preparedStatement.setBlob(colIndex, (InputStream) value);
						} catch (AbstractMethodError ex) {
							OntimizeJdbcDaoSupport.logger.debug(null, ex);
							try {
								preparedStatement.setBinaryStream(colIndex, (InputStream) value,
										((InputStream) value).available());
							} catch (IOException error) {
								throw new SQLException(error);
							}
						}
					}
				} else if (value instanceof NullValue) {
					// TODO At this point we could retrieve sqlType from ((NullValue)value).getSQLDataType()
					// but it is preferable to use the sqlType retrieved from table metadata.
					value = new SqlParameterValue(sqlType, null);
					StatementCreatorUtils.setParameterValue(preparedStatement, colIndex, sqlType, value);
				} else {
					StatementCreatorUtils.setParameterValue(preparedStatement, colIndex, sqlType, value);
				}
			}
		}
	}

	/**
	 * Match the provided in parameter values with regitered parameters and parameters defined via
	 * metadata processing.
	 * @param parameterSource the parameter vakues provided as a {@link SqlParameterSource}
	 * @return Map with parameter names and values
	 */
	protected InsertMetaInfoHolder matchInParameterValuesWithInsertColumns(final SqlParameterSource parameterSource) {
		return this.tableMetaDataContext.getInsertMetaInfo(parameterSource);
	}

	/**
	 * Match the provided in parameter values with regitered parameters and parameters defined via
	 * metadata processing.
	 * @param args the parameter values provided in a Map
	 * @return Map with parameter names and values
	 */
	protected InsertMetaInfoHolder matchInParameterValuesWithInsertColumns(final Map<String, Object> args) {
		return this.tableMetaDataContext.getInsertMetaInfo(args);
	}

	/**
	 * Match the provided in parameter values with regitered parameters and parameters defined via
	 * metadata processing.
	 * @param parameterSource the parameter vakues provided as a {@link SqlParameterSource}
	 * @return Map with parameter names and values
	 */
	protected List<Object> matchInParameterValuesWithInsertColumnsForBatch(final SqlParameterSource parameterSource) {
		return this.tableMetaDataContext.matchInParameterValuesWithInsertColumns(parameterSource);
	}

	/**
	 * Match the provided in parameter values with regitered parameters and parameters defined via
	 * metadata processing.
	 * @param args the parameter values provided in a Map
	 * @return Map with parameter names and values
	 */
	protected List<Object> matchInParameterValuesWithInsertColumnsForBatch(final Map<String, Object> args) {
		return this.tableMetaDataContext.matchInParameterValuesWithInsertColumns(args);
	}

	/**
	 * Gets the schema table.
	 * @return the schema table
	 */
	protected String getSchemaTable() {
		String sTableToUse = this.getTableName();
		if (this.getSchemaName() != null) {
			sTableToUse = this.getSchemaName() + "." + sTableToUse;
		}
		return sTableToUse;
	}

	/**
	 * Establish SQL statement builder.
	 * @param statementHandler the new statement handler
	 */
	public void setStatementHandler(final SQLStatementHandler statementHandler) {
		this.statementHandler = statementHandler;
	}

	/**
	 * Get the SQL statement builder.
	 * @return the statement builder
	 */
	public SQLStatementHandler getStatementHandler() {
		return this.statementHandler;
	}

	/**
	 * Sets the delete keys.
	 * @param deleteKeys the new delete keys
	 */
	public void setDeleteKeys(final List<String> deleteKeys) {
		this.deleteKeys = deleteKeys;
	}

	/**
	 * Gets the delete keys.
	 * @return the delete keys
	 */
	public List<String> getDeleteKeys() {
		return this.deleteKeys;
	}

	/**
	 * Sets the update keys.
	 * @param updateKeys the new update keys
	 */
	public void setUpdateKeys(final List<String> updateKeys) {
		this.updateKeys = updateKeys;
	}

	/**
	 * Gets the update keys.
	 * @return the update keys
	 */
	public List<String> getUpdateKeys() {
		return this.updateKeys;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
	 * (org.springframework.context.ApplicationContext)
	 */
	/**
	 * Sets the application context.
	 * @param applicationContext the new application context
	 * @throws BeansException the beans exception
	 */
	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * Gets the application context.
	 * @return the application context
	 */
	public ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.server.dao.IOntimizeDaoSupport#getCudProperties()
	 */
	@Override
	public List<DaoProperty> getCudProperties() {
		this.compile();
		List<DaoProperty> res = new ArrayList<>();
		for (TableParameterMetaData data : this.tableMetaDataContext.getTableParameters()) {
			String name = data.getParameterName();
			int type = data.getSqlType();
			DaoProperty property = new DaoProperty();
			property.setSqlType(type);
			property.setPropertyName(name);
			res.add(property);
		}
		return res;
	}

	/**
	 * Gets the table meta data context.
	 * @return the table meta data context
	 */
	public OntimizeTableMetaDataContext getTableMetaDataContext() {
		if (!this.tableMetaDataContext.isProcessed()) {
			this.compile();
		}
		return this.tableMetaDataContext;
	}

	protected OntimizeTableMetaDataContext createTableMetadataContext() {
		return new OntimizeTableMetaDataContext();
	}

	@Override
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		OntimizeJdbcDaoSupport.logger.trace("Creating new JdbcTemplate with fetchSize=1000");
		JdbcTemplate template = super.createJdbcTemplate(dataSource);
		template.setFetchSize(1000);
		OntimizeJdbcDaoSupport.logger
		.trace("Creating new JdbcTemplate has finally fetchSize=" + template.getFetchSize());
		return template;
	}

	private Integer getColumnSQLType(final String column) {
		if (!this.tableMetaDataContext.isProcessed()) {
			this.compile();
		}
		for (final TableParameterMetaData data : this.tableMetaDataContext.getTableParameters()) {
			if (column.equalsIgnoreCase(data.getParameterName())) {
				return data.getSqlType();
			}
		}
		return null;
	}
}
