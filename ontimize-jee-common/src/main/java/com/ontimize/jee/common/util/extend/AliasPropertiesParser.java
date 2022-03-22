package com.ontimize.jee.common.util.extend;

import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;

public class AliasPropertiesParser extends PropertiesParser {

	private static final Logger logger = LoggerFactory.getLogger(AliasPropertiesParser.class);

	public static final String ADD_ALIAS_OP = "ADD_ALIAS";

	public static final String REMOVE_ALIAS_OP = "REMOVE_ALIAS";

	@Override
	protected void executeOperation(Properties propertiesFile, String operation, String parameters,
			String operationValues) throws Exception {

		// ADD OPERATION
		if (operation.equalsIgnoreCase(AliasPropertiesParser.ADD_ALIAS_OP)) {
			this.executeAddOperation(propertiesFile, operation, parameters, operationValues);
		}

		// REMOVE OPERATION
		if (operation.equalsIgnoreCase(AliasPropertiesParser.REMOVE_ALIAS_OP)) {
			this.executeRemoveOperation(propertiesFile, parameters);
		}

	}

	private void executeRemoveOperation(Properties propertiesFile, String parameters) {
		ArrayList<String> parametersList = this.getValuesList(parameters, PropertiesParser.VALUES_SEPARATOR);
		for (String parameter : parametersList) {

			if (parameter != null) {
				propertiesFile.remove(parameter);
			}
		}
	}

	private void executeAddOperation(Properties propertiesFile, String operation, String parameters,
			String operationValues) throws OntimizeJEEException {
		ArrayList<String> parametersList = this.getValuesList(parameters, PropertiesParser.VALUES_SEPARATOR);
		ArrayList<String> operationValuesList = this.getValuesList(operationValues,
				PropertiesParser.VALUES_SEPARATOR);

		if (parametersList.size() != operationValuesList.size()) {
			throw new OntimizeJEEException(operation + ": PARAMETER_LENGHT_DISTINCT_VALUES_LENGHT");
		}

		for (int i = 0; i < parametersList.size(); i++) {
			String parameter = null;
			String value = null;
			try {
				parameter = parametersList.get(i);
				value = operationValuesList.get(i);
			} catch (Exception e) {
				AliasPropertiesParser.logger.error(null, e);
			}

			if ((parameter != null) && (value != null)) {
				propertiesFile.put(parameter, value);
			}
		}
	}

}
