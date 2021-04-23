package com.ontimize.jee.core.common.util.extend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PropertiesParser {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesParser.class);

    abstract protected void executeOperation(Properties propertiesFile, String operation, String parameters,
            String operationValues) throws Exception;

    protected static final String VALUES_SEPARATOR = ";";

    protected static final String OP_PREFIX = "@";

    protected static final String PARAMETER_LEFT = "[";

    protected static final String PARAMETER_RIGHT = "]";

    protected final String OPERATION_MATCH = "@.+\\[.+\\]";

    protected final String OPERATION_ORDER_MATCH = "^@ORDER$";

    public Properties parseProperties(Properties propertiesFile, Properties extendFile) {
        return this.parse(propertiesFile, extendFile);
    }

    public int parseExtendPropertiesOrder(Properties extendFile) {
        int index = -1;
        Enumeration extKeys = extendFile.keys();
        while (extKeys.hasMoreElements()) {
            String extOperationKey = (String) extKeys.nextElement();
            String operationValue = extendFile.getProperty(extOperationKey);

            if (extOperationKey.matches(this.OPERATION_ORDER_MATCH)) {
                index = Integer.parseInt(operationValue);
            }
        }
        return index;
    }

    protected Properties parse(Properties propertiesFile, Properties extendFile) {

        Enumeration extendKeys = extendFile.keys();
        while (extendKeys.hasMoreElements()) {
            // Key of operation must be in the format @OPERATION[PARAMETERS] or
            // defined in OPERATION_MATCH
            String extendOperationKey = (String) extendKeys.nextElement();

            // Values of operation
            String operationValues = extendFile.getProperty(extendOperationKey);

            // If key matches the format
            if (extendOperationKey.matches(this.OPERATION_MATCH)) {
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
            } else if (!extendOperationKey.matches(this.OPERATION_ORDER_MATCH)) {
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

    protected ArrayList getValuesList(String values, String valuesSeparator) {

        ArrayList valuesList = new ArrayList();

        StringTokenizer st = new StringTokenizer(values, PropertiesParser.VALUES_SEPARATOR);

        while (st.hasMoreTokens()) {
            valuesList.add(st.nextToken());
        }

        return valuesList;
    }

    protected String getValuesString(ArrayList valuesList, String valuesSeparator) {

        String finalString = "";

        for (Iterator iter = valuesList.iterator(); iter.hasNext();) {
            String element = (String) iter.next();

            finalString = finalString + element + PropertiesParser.VALUES_SEPARATOR;
        }

        finalString = finalString.substring(0, finalString.length() - 1);

        return finalString;
    }

    protected Properties loadPropertiesFile(File file) {

        Properties properties = new Properties();

        InputStream fis;
        try {
            fis = new FileInputStream(file);
            properties.load(fis);
        } catch (FileNotFoundException e) {
            PropertiesParser.logger.error(null, e);
        } catch (IOException e) {
            PropertiesParser.logger.error(null, e);
        }

        return properties;
    }

}
