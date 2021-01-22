package com.ontimize.jee.server.dao.cql;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cassandra.core.CqlTemplate;
import org.springframework.cassandra.core.PreparedStatementBinder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.querybuilder.Update.Assignments;
import com.ontimize.db.AdvancedEntityResult;
import com.ontimize.db.EntityResult;
import com.ontimize.db.NullValue;
import com.ontimize.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.gui.MultipleValue;
import com.ontimize.gui.field.MultipleTableAttribute;
import com.ontimize.gui.field.ReferenceFieldAttribute;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.naming.I18NNaming;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.streamfilter.ReplaceTokensFilterReader;
import com.ontimize.jee.server.dao.DaoProperty;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.dao.ISQLQueryAdapter;
import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.common.INameConvention;
import com.ontimize.jee.server.dao.cql.handler.CQLStatementHandler;
import com.ontimize.jee.server.dao.cql.metadata.CQLTableMetaDataContext;
import com.ontimize.jee.server.dao.cql.setup.AmbiguousColumnType;
import com.ontimize.jee.server.dao.cql.setup.CassandraEntitySetupType;
import com.ontimize.jee.server.dao.cql.setup.FunctionColumnType;
import com.ontimize.jee.server.dao.cql.setup.QueryType;

public class OntimizeCassandraDaoSupport implements IOntimizeDaoSupport, ApplicationContextAware {

    /** The logger. */
    protected final static Logger logger = LoggerFactory.getLogger(OntimizeCassandraDaoSupport.class);

    /** The application context. */
    private ApplicationContext applicationContext;

    private CqlTemplate cqlTemplate;

    /** The CQL statement builder. */
    private CQLStatementHandler statementHandler;

    private String tableName;

    private String keyspace;

    /** Mandatory delete keys. */
    private List<String> deleteKeys;

    /** Mandatory update keys. */
    private List<String> updateKeys;

    /** Queries. */
    protected final Map<String, CassandraQueryTemplateInformation> cqlQueries = new HashMap<>();

    /**
     * Configuration file
     */
    private String configurationFile = null;

    /**
     * Configuration file placeholder
     */
    private String configurationFilePlaceholder = null;

    /**
     * Has this operation been compiled? Compilation means at least checking that a CqlTemplate has been
     * provided, but subclasses may also implement their own custom validation.
     */
    private boolean compiled = false;

    private final CQLTableMetaDataContext tableMetaDataContext;

    /**
     * Name convention
     *
     */
    private INameConvention nameConvention;

    public OntimizeCassandraDaoSupport() {
        super();
        this.tableMetaDataContext = new CQLTableMetaDataContext();
    }

    public OntimizeCassandraDaoSupport(final String configurationFile, final String configurationFilePlaceholder) {
        this();
        this.configurationFile = configurationFile;
        this.configurationFilePlaceholder = configurationFilePlaceholder;

    }

    @Override
    public EntityResult query(Map<?, ?> keysValues, List<?> attributes, List<?> sort, String queryId) {
        return this.query(keysValues, attributes, sort, queryId, null);

    }

    @Override
    public EntityResult query(Map<?, ?> keysValues, List<?> attributes, List<?> sort, String queryId,
            ISQLQueryAdapter adapter) {

        this.checkCompiled();

        final CassandraQueryTemplateInformation cassandraQueryInformation = this
            .getCassandraQueryTemplateInformation(queryId);

        final CQLStatement stSQL = this.composeCQLQuery(queryId, attributes, keysValues, sort, adapter);

        return this.getCqlTemplate().query(stSQL.getCQLStatement(), new PreparedStatementBinder() {
            @Override
            public BoundStatement bindValues(PreparedStatement ps) throws DriverException {
                List values = stSQL.getValues();

                if ((values != null) && (values.size() > 0)) {
                    return ps.bind(stSQL.getValues().toArray());
                } else {
                    return ps.bind();
                }
            }
        }, new EntityResultResultSetExtractor(this.getStatementHandler(), cassandraQueryInformation,
                (List<String>) attributes));
    }

    protected CQLStatement composeCQLQuery(final String queryId, final List<?> attributes, final Map<?, ?> keysValues,
            final List<?> sort, ISQLQueryAdapter adapter) {
        if (adapter != null) {
            throw new OntimizeJEERuntimeException("NOT_IMPLEMENTED");
        }

        final CassandraQueryTemplateInformation cassandraQueryInformation = this
            .getCassandraQueryTemplateInformation(queryId);

        final Map<?, ?> kvWithoutReferenceAttributes = this.processReferenceDataFieldAttributes(keysValues);
        Map<Object, Object> kvValidKeysValues = new HashMap<>();
        final Map<?, ?> processMultipleValueAttributes = this
            .processMultipleValueAttributes(kvWithoutReferenceAttributes);
        if (processMultipleValueAttributes != null) {
            kvValidKeysValues.putAll(processMultipleValueAttributes);
        }

        List<?> vValidAttributes = this.processReferenceDataFieldAttributes(attributes);

        CQLStatement cqlStatement = null;
        if (cassandraQueryInformation == null) {
            CheckingTools.failIf(attributes.isEmpty(), "NO_ATTRIBUTES_TO_QUERY");
            // use table
            cqlStatement = this.getStatementHandler()
                .createSelectQuery(this.getKeyspaceTableName(), vValidAttributes, kvValidKeysValues,
                        new ArrayList<String>(),
                        (List<SQLOrder>) sort);
        }

        return cqlStatement;
    }

    /**
     * Gets the schema table.
     * @return the schema table
     */
    protected String getKeyspaceTableName() {
        String sTableToUse = this.getTableName();
        if (this.getKeyspace() != null) {
            sTableToUse = this.getKeyspace() + "." + sTableToUse;
        }
        return sTableToUse;
    }

    @Override
    public <T> List<T> query(Map<?, ?> keysValues, List<?> sort, String queryId, Class<T> clazz,
            ISQLQueryAdapter adapter) {
        return null;
    }

    @Override
    public <T> List<T> query(Map<?, ?> keysValues, List<?> sort, String queryId, Class<T> clazz) {
        return this.query(keysValues, sort, queryId, clazz, null);
    }

    @Override
    public AdvancedEntityResult paginationQuery(Map<?, ?> keysValues, List<?> attributes, int recordNumber,
            int startIndex, List<?> orderBy, String queryId) {
        return this.paginationQuery(keysValues, attributes, recordNumber, startIndex, orderBy, queryId, null);
    }

    @Override
    public AdvancedEntityResult paginationQuery(Map<?, ?> keysValues, List<?> attributes, int recordNumber,
            int startIndex, List<?> orderBy, String queryId,
            ISQLQueryAdapter adapter) {
        return null;
    }

    @Override
    public EntityResult insert(Map<?, ?> attributesValues) {
        this.checkCompiled();
        final EntityResult erResult = new EntityResult();

        final Map<?, ?> avWithoutMultipleTableAttributes = this.processMultipleTableAttribute(attributesValues);
        final Map<?, ?> avWithoutReferenceAttributes = this
            .processReferenceDataFieldAttributes(avWithoutMultipleTableAttributes);
        final Map<?, ?> avWithoutMultipleValueAttributes = this
            .processMultipleValueAttributes(avWithoutReferenceAttributes);
        final Map<String, Object> avValidPre = this
            .getValidAttributes(this.processStringKeys(avWithoutMultipleValueAttributes));
        final Map<String, Object> avValid = this.removeNullValues(avValidPre);
        if (avValid.isEmpty()) {
            // TODO se debería lanzar excepción, pero puede tener colaterales con la one-2-one
            OntimizeCassandraDaoSupport.logger.warn("Insert: Attributes does not contain any pair key-value valid");
            return erResult;
        }

        this.doExecuteInsert(avValid);
        erResult.setCode(EntityResult.OPERATION_SUCCESSFUL);
        return erResult;
    }

    /**
     * Method that provides execution of the insert using the passed in Map of parameters.
     * @param args Map with parameter names and values to be used in insert
     * @return number of rows affected
     */
    protected void doExecuteInsert(final Map<String, Object> args) {
        this.checkCompiled();

        Insert insertStatement = QueryBuilder.insertInto(this.getKeyspace(), this.getTableName());

        Iterator<Entry<String, Object>> iterator = args.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> current = iterator.next();
            insertStatement.value(current.getKey(), current.getValue());
        }

        this.cqlTemplate.execute(insertStatement);
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
        for (final Entry<?, ?> entry : inputAttributesValues.entrySet()) {
            final Object oKey = entry.getKey();
            final Object oValue = entry.getValue();
            if (this.getTableMetaDataContext().getNameConventionTableColumns().contains(oKey)) {
                hValidKeysValues.put((String) oKey, oValue);
            }
        }
        OntimizeCassandraDaoSupport.logger.debug(
                " Update valid attributes values: Input: " + inputAttributesValues + " -> Result: " + hValidKeysValues);
        return hValidKeysValues;
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
     * Processes the ReferenceFieldAttribute objects contained in <code>keysValues</code>.
     * <p>
     * Returns a Map containing all the objects contained in the argument <code>keysValues</code>
     * except in the case of keys that are ReferenceFieldAttribute objects, which are replaced by
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
     * Processes all the MultipleTableAttribute contained as keys ih the Map <code>av</code>. All
     * other objects are added to the resulting List with no changes. The MultipleTableAttribute
     * objects are replaced by their attribute.
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

    @Override
    public EntityResult update(Map<?, ?> attributesValues, Map<?, ?> keysValues) {
        this.checkCompiled();
        final EntityResult erResult = new EntityResult();

        // Check the primary keys
        final Map<?, ?> avWithoutMultipleTableAttributes = this.processMultipleTableAttribute(attributesValues);
        final Map<?, ?> avWithoutReferenceAttributes = this
            .processReferenceDataFieldAttributes(avWithoutMultipleTableAttributes);
        final Map<?, ?> avWithoutMultipleValue = this.processMultipleValueAttributes(avWithoutReferenceAttributes);

        final Map<?, ?> kvWithoutMulpleTableAttributes = this.processMultipleTableAttribute(keysValues);
        final Map<?, ?> kvWithoutReferenceAttributessRef = this
            .processReferenceDataFieldAttributes(kvWithoutMulpleTableAttributes);

        final Map<String, Object> hValidAttributesValues = this.getValidAttributes(avWithoutMultipleValue);

        Map<String, Object> hValidKeysValues = null;

        hValidKeysValues = this.getValidUpdatingKeysValues(kvWithoutReferenceAttributessRef);
        this.checkUpdateKeys(hValidKeysValues);

        Update update = QueryBuilder.update(this.getKeyspace(), this.getTableName());
        Assignments assignments = update.with();

        Iterator<Map.Entry<String, Object>> iterator = hValidAttributesValues.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, Object> current = iterator.next();
            assignments.and(QueryBuilder.set(current.getKey(), current.getValue()));
        }

        Iterator<Map.Entry<String, Object>> keyIterator = hValidKeysValues.entrySet().iterator();

        List<String> keys = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        while (keyIterator.hasNext()) {
            Entry<String, Object> current = keyIterator.next();
            keys.add(current.getKey());
            values.add(current.getValue());
        }

        update.where(QueryBuilder.eq(keys, values));

        this.cqlTemplate.execute(update);
        return erResult;
    }

    @Override
    public EntityResult delete(Map<?, ?> keysValues) {
        return null;
    }

    @Override
    public EntityResult unsafeDelete(Map<?, ?> keysValues) {
        return null;
    }

    @Override
    public EntityResult unsafeUpdate(Map<?, ?> attributesValues, Map<?, ?> keysValues) {
        return null;
    }

    @Override
    public int[] insertBatch(Map<String, Object>[] batch) {
        return null;
    }

    @Override
    public List<DaoProperty> getCudProperties() {
        return null;
    }

    @Override
    public void reload() {

    }

    /**
     * Sets the application context.
     * @param applicationContext the new application context
     * @throws BeansException the beans exception
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.setCqlTemplate(this.applicationContext.getBean(CqlTemplate.class));
    }

    /**
     * Gets the application context.
     * @return the application context
     */
    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    public CqlTemplate getCqlTemplate() {
        return this.cqlTemplate;
    }

    public void setCqlTemplate(CqlTemplate cqlTemplate) {
        this.cqlTemplate = cqlTemplate;
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
                    reader = new ReplaceTokensFilterReader(new InputStreamReader(is),
                            new HashMap<String, String>((Map) prop));
                }
            } else {
                reader = new InputStreamReader(is);
            }

            final CassandraEntitySetupType setup = JAXB.unmarshal(reader, CassandraEntitySetupType.class);
            this.setTableName(setup.getTable());
            this.setKeyspace(setup.getKeyspace());
            this.setDeleteKeys(setup.getDeleteKeys().getColumn());
            this.setUpdateKeys(setup.getUpdateKeys().getColumn());
            if (setup.getQueries() != null) {
                for (final QueryType query : setup.getQueries().getQuery()) {//
                    this.addCassandraQueryTemplateInformation(query.getId(), query.getSentence().getValue(), //
                            query.getAmbiguousColumns() == null ? null
                                    : query.getAmbiguousColumns().getAmbiguousColumn(), //
                            query.getFunctionColumns() == null ? null : query.getFunctionColumns().getFunctionColumn(), //
                            query.getValidColumns() != null ? query.getValidColumns().getColumn()
                                    : new ArrayList<String>());
                }
            }
            this.setCqlTemplate(this.applicationContext.getBean(CqlTemplate.class));
            this.setStatementHandler(this.applicationContext.getBean(CQLStatementHandler.class));

            this.tableMetaDataContext.setKeyspace(this.getKeyspace());
            this.tableMetaDataContext.setTableName(this.getTableName());
            this.nameConvention = this.applicationContext.getBean(INameConvention.class);
            this.tableMetaDataContext.setNameConvention(this.nameConvention);
        } catch (final IOException e) {
            throw new InvalidDataAccessApiUsageException(I18NNaming.M_ERROR_LOADING_CONFIGURATION_FILE, e);
        }
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
    protected void checkCompiled() {
        if (!this.isCompiled()) {
            OntimizeCassandraDaoSupport.logger.debug("CassandraCQL not compiled before execution - invoking compile");
            this.compile();
        }
    }

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
                this.loadConfigurationFile(annotation.configurationFile(), annotation.configurationFilePlaceholder());
            } else {
                this.loadConfigurationFile(this.configurationFile, this.configurationFilePlaceholder);
            }

            if (this.getCqlTemplate() == null) {
                throw new IllegalArgumentException("'cqlTemplate' is required");
            }

            this.tableMetaDataContext.processMetaData(this.getCqlTemplate().getSession());
            this.compiled = true;
            if (OntimizeCassandraDaoSupport.logger.isDebugEnabled()) {
                OntimizeCassandraDaoSupport.logger.debug("{} for table [ {} ] compiled", this.getClass(),
                        this.getTableName());
            }
        }
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getKeyspace() {
        return this.keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    /**
     * Adds a query, allowing determine valid columns to query to DB.
     * @param id
     * @param value
     * @param ambiguousColumns
     * @param functionColumns
     * @param validColumns
     */
    public void addCassandraQueryTemplateInformation(final String id, final String value,
            final List<AmbiguousColumnType> ambiguousColumns,
            final List<FunctionColumnType> functionColumns, List<String> validColumns) {
        this.cqlQueries.put(id,
                new CassandraQueryTemplateInformation(value, ambiguousColumns, functionColumns, validColumns));
    }

    /**
     * Gets the template query.
     * @param id the id
     * @return the template query
     */
    public CassandraQueryTemplateInformation getCassandraQueryTemplateInformation(final String id) {
        this.checkCompiled();
        return this.cqlQueries.get(id);
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
    public Map<String, Object> getValidUpdatingKeysValues(final Map<?, ?> keysValues) {
        final Map<String, Object> hValidKeysValues = new HashMap<>();
        for (int i = 0; i < this.updateKeys.size(); i++) {
            if (keysValues.containsKey(this.updateKeys.get(i))) {
                hValidKeysValues.put(this.updateKeys.get(i), keysValues.get(this.updateKeys.get(i)));
            }
        }
        OntimizeCassandraDaoSupport.logger
            .debug(" Update valid keys values: Input: " + keysValues + " -> Result: " + hValidKeysValues);
        return hValidKeysValues;
    }

    /**
     * Checks if <code>keysValues</code> contains a value for all columns defined in 'update_keys'
     * parameter.
     * <p>
     * @param keysValues the keys values
     */
    protected void checkUpdateKeys(final Map<?, ?> keysValues) {
        for (int i = 0; i < this.updateKeys.size(); i++) {
            if (!keysValues.containsKey(this.updateKeys.get(i).toString())) {
                throw new OntimizeJEERuntimeException("M_NECESSARY_" + this.updateKeys.get(i).toString().toUpperCase());
            }
        }
    }

    /**
     * Gets the update keys.
     * @return the update keys
     */
    public List<String> getUpdateKeys() {
        return this.updateKeys;
    }

    public CQLStatementHandler getStatementHandler() {
        return this.statementHandler;
    }

    public void setStatementHandler(CQLStatementHandler statementHandler) {
        this.statementHandler = statementHandler;
    }

    /**
     * Gets the table meta data context.
     * @return the table meta data context
     */
    public CQLTableMetaDataContext getTableMetaDataContext() {
        if (!this.tableMetaDataContext.isProcessed()) {
            this.compile();
        }
        return this.tableMetaDataContext;
    }

}
