package com.ontimize.jee.common.util.extend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Properties;

public class AliasPropertiesParser extends PropertiesParser {

    private static final Logger logger = LoggerFactory.getLogger(AliasPropertiesParser.class);

    public static final String ADD_ALIAS_OP = "ADD_ALIAS";

    public static final String REMOVE_ALIAS_OP = "REMOVE_ALIAS";

    @Override
    protected void executeOperation(Properties propertiesFile, String operation, String parameters,
            String operationValues) throws Exception {

        // ADD OPERATION
        if (operation.equalsIgnoreCase(AliasPropertiesParser.ADD_ALIAS_OP)) {
            ArrayList parametersList = this.getValuesList(parameters, VALUES_SEPARATOR);
            ArrayList operationValuesList = this.getValuesList(operationValues, VALUES_SEPARATOR);

            if (parametersList.size() != operationValuesList.size()) {
                throw new Exception(operation + ": PARAMETER_LENGHT_DISTINCT_VALUES_LENGHT");
            }

            for (int i = 0; i < parametersList.size(); i++) {
                String parameter = null;
                String value = null;
                try {
                    parameter = (String) parametersList.get(i);
                    value = (String) operationValuesList.get(i);
                } catch (Exception e) {
                    AliasPropertiesParser.logger.error(null, e);
                }

                if ((parameter != null) && (value != null)) {
                    propertiesFile.put(parameter, value);
                }
            }
        }

        // REMOVE OPERATION
        if (operation.equalsIgnoreCase(AliasPropertiesParser.REMOVE_ALIAS_OP)) {

            ArrayList parametersList = this.getValuesList(parameters, VALUES_SEPARATOR);
            for (int i = 0; i < parametersList.size(); i++) {
                String parameter = (String) parametersList.get(i);

                if (parameter != null) {
                    propertiesFile.remove(parameter);
                }
            }
        }

    }

}
