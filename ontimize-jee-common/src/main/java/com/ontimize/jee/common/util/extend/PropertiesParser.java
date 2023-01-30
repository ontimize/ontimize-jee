package com.ontimize.jee.common.util.extend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PropertiesParser {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesParser.class);

    protected abstract void executeOperation(Properties propertiesFile, String operation, String parameters,
            String operationValues) throws OntimizeJEEException;

    protected static final String VALUES_SEPARATOR = ";";

    protected static final String OP_PREFIX = "@";

    protected static final String PARAMETER_LEFT = "[";

    protected static final String PARAMETER_RIGHT = "]";

    protected static final String OPERATION_MATCH = "@.+\\[.+\\]";

    protected static final String OPERATION_ORDER_MATCH = "^@ORDER$";

    public Properties parseProperties(Properties propertiesFile, Properties extendFile) {
        return this.parse(propertiesFile, extendFile);
    }

    public int parseExtendPropertiesOrder(Properties extendFile) {
        int index = -1;
        Enumeration<Object> extKeys = extendFile.keys();
        while (extKeys.hasMoreElements()) {
            String extOperationKey = (String) extKeys.nextElement();
            String operationValue = extendFile.getProperty(extOperationKey);

            if (extOperationKey.matches(PropertiesParser.OPERATION_ORDER_MATCH)) {
                index = Integer.parseInt(operationValue);
            }
        }
        return index;
    }

    protected Properties parse(Properties propertiesFile, Properties extendFile) {

        Enumeration<Object> extendKeys = extendFile.keys();
        while (extendKeys.hasMoreElements()) {
            // Key of operation must be in the format @OPERATION[PARAMETERS] or
            // defined in OPERATION_MATCH
            String extendOperationKey = (String) extendKeys.nextElement();

            // Values of operation
            String operationValues = extendFile.getProperty(extendOperationKey);

            // If key matches the format
            if (extendOperationKey.matches(PropertiesParser.OPERATION_MATCH)) {
                try {
                    // Try to extract operation parameters and values
                    String[] operationElements = this.extractOperationElements(extendOperationKey);
                    String operation = operationElements[0];
                    String operationParameters = operationElements[1];

                    // Try to execute the operation with parameters and values
                    this.executeOperation(propertiesFile, operation, operationParameters, operationValues);
                } catch (Exception e) {
                    PropertiesParser.logger.error(null, e);
                }
            } else if (!extendOperationKey.matches(PropertiesParser.OPERATION_ORDER_MATCH)) {
                // If key not matches the format, simply adds the key and value
                // in extendFile to the properties file
                propertiesFile.put(extendOperationKey, operationValues);
            }
        }

        // Returns the modified properties file
        return propertiesFile;
    }

    protected String[] extractOperationElements(String operation) {

        String[] operationElements = new String[2];

        int leftParameterIndex = operation.indexOf(PropertiesParser.PARAMETER_LEFT);
        int rightParameterIndex = operation.indexOf(PropertiesParser.PARAMETER_RIGHT);

        if ((leftParameterIndex != -1) && (rightParameterIndex != -1)) {
            String opLiteral = operation.substring(1, leftParameterIndex);
            operationElements[0] = opLiteral;

            String parametersLiteral = operation.substring(leftParameterIndex + 1, rightParameterIndex);
            operationElements[1] = parametersLiteral;
        }

        return operationElements;
    }

    protected ArrayList<String> getValuesList(String values) {

        ArrayList<String> valuesList = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(values, PropertiesParser.VALUES_SEPARATOR);

        while (st.hasMoreTokens()) {
            valuesList.add(st.nextToken());
        }

        return valuesList;
    }

    protected String getValuesString(ArrayList<String> valuesList) {

        StringBuilder finalString = new StringBuilder();

        for (Iterator<String> iter = valuesList.iterator(); iter.hasNext();) {
            String element = iter.next();
            finalString.append(element);
            finalString.append(PropertiesParser.VALUES_SEPARATOR);
        }

        finalString.setLength(finalString.length() - 1);

        return finalString.toString();
    }

    protected Properties loadPropertiesFile(File file) {

        Properties properties = new Properties();


        try (InputStream fis = new FileInputStream(file)){
            properties.load(fis);
        } catch (IOException e) {
            PropertiesParser.logger.error(null, e);
        }

        return properties;
    }
}
