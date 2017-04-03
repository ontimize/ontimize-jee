package com.ontimize.jee.server.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ontimize.db.EntityResult;
import com.ontimize.db.NullValue;
import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.ParseUtilsExtended;
import com.ontimize.jee.common.tools.ReflectionTools;

public abstract class ORestController<S> {

	/** The logger. */
	private final static Logger logger = LoggerFactory.getLogger(ORestController.class);

	public static final String QUERY = "Query";
	public static final String INSERT = "Insert";
	public static final String DELETE = "Delete";
	public static final String UPDATE = "Update";

	public static final String BASIC_EXPRESSION = "@basic_expression";

	public abstract S getService();

	@Autowired
	OntimizeMapper mapper;

	@RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> query(@PathVariable("name") String name,
			@RequestParam(name = "filter", required = false) String filter,
			@RequestParam(name = "columns", required = false) String columns) {
		CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
		StringBuffer buffer = new StringBuffer();
		buffer.append(name).append(ORestController.QUERY);
		try {
			Map<Object, Object> keysValues = this.createKeysValues(filter);
			List<Object> attributes = this.createAttributesValues(columns);
			EntityResult eR = (EntityResult) ReflectionTools.invoke(this.getService(), buffer.toString(), keysValues, attributes);
			return new ResponseEntity<EntityResult>(eR, HttpStatus.OK);
		} catch (Exception error) {
			ORestController.logger.error(null, error);
			EntityResult entityResult = new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.BEST_COMPRESSION);
			entityResult.setMessage(error.getMessage());
			return new ResponseEntity<EntityResult>(entityResult, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/{name}/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> query(@PathVariable("name") String name, @RequestBody QueryParameter queryParameter)
			throws Exception {
		ORestController.logger.debug("Invoked /{}/search", name);
		CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
		ORestController.logger.debug("Service name: {}", this.getService());
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append(name).append(ORestController.QUERY);

			Map<Object, Object> kvQueryParameter = queryParameter.getFilter();
			List<Object> avQueryParameter = queryParameter.getColumns();
			HashMap<Object, Object> hSqlTypes = queryParameter.getSqltypes();

			Map<Object, Object> keysValues = this.createKeysValues(kvQueryParameter, hSqlTypes);
			List<Object> attributesValues = this.createAttributesValues(avQueryParameter, hSqlTypes);

			EntityResult eR = (EntityResult) ReflectionTools.invoke(this.getService(), buffer.toString(), keysValues, attributesValues);
			return new ResponseEntity<EntityResult>(eR, HttpStatus.OK);
		} catch (Exception e) {
			ORestController.logger.error("{}", e.getMessage(), e);
			EntityResult entityResult = new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.BEST_COMPRESSION);
			entityResult.setMessage(e.getMessage());
			return new ResponseEntity<EntityResult>(entityResult, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> insert(@PathVariable("name") String name, @RequestBody InsertParameter insertParameter) {
		CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
		StringBuffer buffer = new StringBuffer();
		buffer.append(name).append(ORestController.INSERT);
		try {
			Map<Object, Object> avInsertParameter = insertParameter.getData();
			Map<Object, Object> hSqlTypes = insertParameter.getSqltypes();
			Map<Object, Object> attributes = this.createKeysValues(avInsertParameter, hSqlTypes);
			EntityResult eR = (EntityResult) ReflectionTools.invoke(this.getService(), buffer.toString(), attributes);
			return new ResponseEntity<EntityResult>(eR, HttpStatus.OK);
		} catch (Exception e) {
			ORestController.logger.error("{}", e.getMessage(), e);
			EntityResult entityResult = new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.BEST_COMPRESSION);
			entityResult.setMessage(e.getMessage());
			return new ResponseEntity<EntityResult>(entityResult, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> delete(@PathVariable("name") String name, @RequestBody DeleteParameter deleteParameter) {
		Object service = this.getService();
		CheckingTools.failIf(service == null, NullPointerException.class, "Service is null");
		StringBuffer buffer = new StringBuffer();
		buffer.append(name).append(ORestController.DELETE);
		try {
			Map<Object, Object> kvDeleteParameter = deleteParameter.getFilter();
			Map<Object, Object> hSqlTypes = deleteParameter.getSqltypes();
			Map<Object, Object> attributes = this.createKeysValues(kvDeleteParameter, hSqlTypes);
			EntityResult eR = (EntityResult) ReflectionTools.invoke(this.getService(), buffer.toString(), attributes);
			return new ResponseEntity<EntityResult>(eR, HttpStatus.OK);
		} catch (Exception e) {
			ORestController.logger.error("{}", e.getMessage(), e);
			EntityResult entityResult = new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.BEST_COMPRESSION);
			entityResult.setMessage(e.getMessage());
			return new ResponseEntity<EntityResult>(entityResult, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> update(@PathVariable("name") String name, @RequestBody UpdateParameter updateParameter) {
		CheckingTools.failIf(this.getService() == null, NullPointerException.class, "Service is null");
		StringBuffer buffer = new StringBuffer();
		buffer.append(name).append(ORestController.UPDATE);
		try {
			Map<Object, Object> filter = updateParameter.getFilter();
			Map<Object, Object> data = updateParameter.getData();
			Map<Object, Object> hSqlTypes = updateParameter.getSqltypes();
			Map<Object, Object> kv = this.createKeysValues(filter, hSqlTypes);
			Map<Object, Object> av = this.createKeysValues(data, hSqlTypes);
			EntityResult eR = (EntityResult) ReflectionTools.invoke(this.getService(), buffer.toString(), av, kv);
			return new ResponseEntity<EntityResult>(eR, HttpStatus.OK);
		} catch (Exception e) {
			ORestController.logger.error("{}", e.getMessage(), e);
			EntityResult entityResult = new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.BEST_COMPRESSION);
			entityResult.setMessage(e.getMessage());
			return new ResponseEntity<EntityResult>(entityResult, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	protected Map<Object, Object> createKeysValues(String filter) {
		Map<Object, Object> keysValues = new HashMap<>();
		if ((filter == null) || (filter.length() == 0)) {
			return keysValues;
		}
		StringTokenizer tokens = new StringTokenizer(filter, "&");
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			int index = token.indexOf("=");
			if (index >= 0) {
				String columnName = token.substring(0, index).trim();
				String columnValue = token.substring(index + 1).trim();
				keysValues.put(columnName, columnValue);
			}
		}
		return keysValues;
	}

	protected List<Object> createAttributesValues(String columns) {
		List<Object> attributes = new ArrayList<>();
		if ((columns == null) || (columns.length() == 0)) {
			attributes.add("*");
			return attributes;
		}

		StringTokenizer tokens = new StringTokenizer(columns, ",");
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			attributes.add(token);
		}
		return attributes;
	}

	protected Map<Object, Object> createKeysValues(Map<Object, Object> kvQueryParam, Map<Object, Object> hSqlTypes) {
		Map<Object, Object> kv = new HashMap<Object, Object>();
		if ((kvQueryParam == null) || kvQueryParam.isEmpty()) {
			return kv;
		}


		if (kvQueryParam.containsKey(ORestController.BASIC_EXPRESSION)) {
            Object basicExpressionValue = kvQueryParam.remove(ORestController.BASIC_EXPRESSION);
            this.processBasicExpression(kv, basicExpressionValue, hSqlTypes);
        }


		Set<Entry<Object, Object>> eSet = kvQueryParam.entrySet();
		Iterator<Entry<Object, Object>> iter = eSet.iterator();
		while (iter.hasNext()) {
			Entry<Object, Object> next = iter.next();
			Object key = next.getKey();
			Object value = next.getValue();
			if ((hSqlTypes != null) && hSqlTypes.containsKey(key)) {
				int sqlType = (Integer) hSqlTypes.get(key);
				value = ParseUtilsExtended.getValueForSQLType(value, sqlType);
				if (value == null) {
					value = new NullValue(sqlType);
				}
			} else if (value == null) {
				value = new NullValue();
			}
			kv.put(key, value);
		}
		return kv;
	}

	protected List<Object> createAttributesValues(List<Object> avQueryParam, Map<Object, Object> hSqlTypes) {
		List<Object> av = new ArrayList<Object>();
		if ((avQueryParam == null) || avQueryParam.isEmpty()) {
			av.add("*");
			return av;
		}
		av.addAll(avQueryParam);
		return av;
	}

	protected void processBasicExpression(Map<Object, Object> keysValues, Object basicExpression, Map<Object, Object> hSqlTypes) {
		if (basicExpression instanceof Map) {
			try {
				BasicExpression bE = BasicExpressionProcessor.getInstance().processBasicEspression(basicExpression,
						hSqlTypes);
				keysValues.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, bE);
			} catch (Exception error) {
				ORestController.logger.error(null, error);
			}
		}

	}

	protected void processBasicExpression(Map<Object, Object> keysValues, Object basicExpression) {
		this.processBasicExpression(keysValues, basicExpression, new HashMap<Object, Object>());
	}

}
