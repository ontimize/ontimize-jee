package com.ontimize.jee.core.common.util.extend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

public class EntitiesPropertiesParser extends PropertiesParser {

    private static final Logger logger = LoggerFactory.getLogger(EntitiesPropertiesParser.class);

    public static final String ADD_OP = "ADD";

    public static final String REMOVE_OP = "REMOVE";

    public static final String REPLACE_OP = "REPLACE";

    public static final String ORDER_OP = "ORDER";

    public static final String SUPPRESS_OP = "SUPPRESS";

    public static String resolvePath(URL url) throws IOException {
        String urlString = url.toString();
        StringTokenizer stringTokenizer = new StringTokenizer(urlString, "/");
        StringBuilder buffer = new StringBuilder();
        ClassLoader classLoader = EntitiesPropertiesParser.class.getClassLoader();

        while (stringTokenizer.hasMoreElements()) {
            buffer.insert(0, stringTokenizer.nextToken() + "/");
        }

        buffer.deleteCharAt(buffer.length() - 1);

        stringTokenizer = new StringTokenizer(buffer.toString(), "/");
        buffer = new StringBuilder();

        while (stringTokenizer.hasMoreElements()) {
            buffer.insert(0, stringTokenizer.nextToken());
            if (classLoader.getResources(buffer.toString()).hasMoreElements()) {
                return buffer.toString();
            } else {
                buffer.insert(0, "/");
            }
        }

        return null;
    }

    @Override
    protected void executeOperation(Properties propertiesFile, String operation, String parameters,
            String operationValues) throws Exception {

        ArrayList parametersList = this.getValuesList(parameters, PropertiesParser.VALUES_SEPARATOR);

        // for each parameter in the list
        for (int i = 0; i < parametersList.size(); i++) {
            String parameter = (String) parametersList.get(i);

            // ADD OPERATION
            if (operation.equalsIgnoreCase(EntitiesPropertiesParser.ADD_OP)) {

                String originalValues = (String) propertiesFile.get(parameter);
                // If the original values in properties file is empty, add new
                // values to properties file
                if (originalValues == null) {
                    propertiesFile.put(parameter, operationValues);
                } else {
                    // If original values is not empty, concat new values at end
                    originalValues = originalValues + PropertiesParser.VALUES_SEPARATOR + operationValues;
                    propertiesFile.put(parameter, originalValues);
                }
            }

            // REMOVE OPERATION
            if (operation.equalsIgnoreCase(EntitiesPropertiesParser.REMOVE_OP)) {

                String originalValues = (String) propertiesFile.get(parameter);
                if (originalValues != null) {
                    ArrayList originalValuesList = this.getValuesList(originalValues,
                            PropertiesParser.VALUES_SEPARATOR);
                    ArrayList operationValuesList = this.getValuesList(operationValues,
                            PropertiesParser.VALUES_SEPARATOR);

                    // for each value to delete, try to remove it in the
                    // original
                    // values list
                    for (Iterator iter = operationValuesList.iterator(); iter.hasNext();) {
                        String element = (String) iter.next();
                        originalValuesList.remove(element);
                    }

                    // If original values is empty, remove the entire key in
                    // properties file
                    if (originalValuesList.isEmpty()) {
                        propertiesFile.remove(parameter);
                    } else {
                        // If original values is not empty, update the new
                        // values in
                        // properties file
                        String valuesString = this.getValuesString(originalValuesList,
                                PropertiesParser.VALUES_SEPARATOR);
                        propertiesFile.put(parameter, valuesString);
                    }
                }
            }

            // REPLACE OPERATION
            if (operation.equalsIgnoreCase(EntitiesPropertiesParser.REPLACE_OP)) {
                // Replaces the original values with new operation values. If
                // original values is empty, add new values
                if (operationValues != null) {
                    propertiesFile.put(parameter, operationValues);
                }
            }

            // SUPPRESS OPERATION
            if (operation.equalsIgnoreCase(EntitiesPropertiesParser.SUPPRESS_OP)) {
                // Remove the line that match the property value

                // logger.debug("Remove Operation. Operation: " +
                // operation + " Parameters: " + parameters +
                // " OperationValues: " + operationValues);
                propertiesFile.remove(parameter);

            }
        }

    }

    public Properties checkPropertiesExtensions(String location, Properties originalProperties) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(location.toString().substring(0, location.toString().indexOf(".")));
        buffer.append("_extends.properties");
        return this.mergeExtendedProperties(buffer.toString(), originalProperties);

    }

    public Properties mergeExtendedProperties(String extendedproperty, Properties originalProperties)
            throws IOException {
        Enumeration<URL> _extends = EntitiesPropertiesParser.class.getClassLoader().getResources(extendedproperty);
        if (_extends.hasMoreElements()) {
            Map extendsProperties = new HashMap();

            while (_extends.hasMoreElements()) {
                Properties extendedProperties = new Properties();
                URL urlStream = _extends.nextElement();
                InputStream stream = urlStream.openStream();
                extendedProperties.clear();
                extendedProperties.load(stream);
                int index = this.parseExtendPropertiesOrder(extendedProperties);
                extendsProperties.put(extendedProperties, index);
            }

            Set<Entry<Object, Integer>> set = extendsProperties.entrySet();
            List<Entry<Object, Integer>> list = new ArrayList<Entry<Object, Integer>>(set);
            Collections.sort(list, new Comparator<Entry<Object, Integer>>() {

                @Override
                public int compare(Entry<Object, Integer> o1, Entry<Object, Integer> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            Properties extendedProperties = new Properties();

            for (Map.Entry<Object, Integer> entry : list) {
                extendedProperties = (Properties) entry.getKey();
                originalProperties = this.parseProperties(originalProperties, extendedProperties);

                EntitiesPropertiesParser.logger.debug("Server properties extended, Load order -> {}", entry.getValue());
            }
        }

        return originalProperties;
    }

    public Properties parseExtendedEntityClasses(String location) throws IOException {
        Properties newClassesProperties = new Properties();
        StringBuilder buffer = new StringBuilder();
        buffer.append(location.toString());
        buffer.append("/");
        buffer.append("entitiesclasses_extends.properties");
        Enumeration<URL> _extends = EntitiesPropertiesParser.class.getClassLoader().getResources(buffer.toString());

        if (_extends.hasMoreElements()) {
            EntitiesPropertiesParser parser = new EntitiesPropertiesParser();
            List<OrderProperties> extendsProperties = new ArrayList<OrderProperties>();

            while (_extends.hasMoreElements()) {
                Properties extendedProperties = new Properties();
                URL urlStream = _extends.nextElement();
                InputStream stream = urlStream.openStream();
                extendedProperties.clear();
                extendedProperties.load(stream);
                int index = parser.parseExtendPropertiesOrder(extendedProperties);
                extendsProperties.add(new OrderProperties(index, extendedProperties));
            }

            Collections.sort(extendsProperties);
            Properties extendedProperties = new Properties();

            for (OrderProperties entry : extendsProperties) {
                extendedProperties = entry.getProperties();
                newClassesProperties = parser.parseProperties(newClassesProperties, extendedProperties);
                EntitiesPropertiesParser.logger.debug("Server properties extended, Load order -> {}", entry.getIndex());
            }
        }

        return newClassesProperties;
    }

}
