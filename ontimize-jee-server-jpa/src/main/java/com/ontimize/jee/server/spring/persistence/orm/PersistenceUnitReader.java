/*
 * Copyright 2002-2013 the original author or authors. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.ontimize.jee.server.spring.persistence.orm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * Internal helper class for reading JPA-compliant {@code persistence.xml} files.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 2.0
 */
class PersistenceUnitReader {

    private static final String PERSISTENCE_VERSION = "version";

    private static final String PERSISTENCE_UNIT = "persistence-unit";

    private static final String UNIT_NAME = "name";

    private static final String MAPPING_FILE_NAME = "mapping-file";

    private static final String JAR_FILE_URL = "jar-file";

    private static final String MANAGED_CLASS_NAME = "class";

    private static final String PROPERTIES = "properties";

    private static final String PROVIDER = "provider";

    private static final String TRANSACTION_TYPE = "transaction-type";

    private static final String JTA_DATA_SOURCE = "jta-data-source";

    private static final String NON_JTA_DATA_SOURCE = "non-jta-data-source";

    private static final String EXCLUDE_UNLISTED_CLASSES = "exclude-unlisted-classes";

    private static final String SHARED_CACHE_MODE = "shared-cache-mode";

    private static final String VALIDATION_MODE = "validation-mode";

    private static final String META_INF = "META-INF";

    private static final Logger logger = LoggerFactory.getLogger(PersistenceUnitReader.class);

    private final ResourcePatternResolver resourcePatternResolver;

    private final DataSourceLookup dataSourceLookup;

    /**
     * Create a new PersistenceUnitReader.
     * @param resourcePatternResolver the ResourcePatternResolver to use for loading resources
     * @param dataSourceLookup the DataSourceLookup to resolve DataSource names in
     *        {@code persistence.xml} files against
     */
    public PersistenceUnitReader(ResourcePatternResolver resourcePatternResolver, DataSourceLookup dataSourceLookup) {
        Assert.notNull(resourcePatternResolver, "ResourceLoader must not be null");
        Assert.notNull(dataSourceLookup, "DataSourceLookup must not be null");
        this.resourcePatternResolver = resourcePatternResolver;
        this.dataSourceLookup = dataSourceLookup;
    }

    /**
     * Parse and build all persistence unit infos defined in the specified XML file(s).
     * @param persistenceXmlLocation the resource location (can be a pattern)
     * @return the resulting PersistenceUnitInfo instances
     */
    public SpringPersistenceUnitInfo[] readPersistenceUnitInfos(String persistenceXmlLocation) {
        return this.readPersistenceUnitInfos(new String[] { persistenceXmlLocation });
    }

    /**
     * Parse and build all persistence unit infos defined in the given XML files.
     * @param persistenceXmlLocations the resource locations (can be patterns)
     * @return the resulting PersistenceUnitInfo instances
     */
    public SpringPersistenceUnitInfo[] readPersistenceUnitInfos(String[] persistenceXmlLocations) {
        ErrorHandler handler = new SimpleSaxErrorHandler(PersistenceUnitReader.logger);
        List<SpringPersistenceUnitInfo> infos = new LinkedList<>();
        String resourceLocation = null;
        try {
            for (String location : persistenceXmlLocations) {
                Resource[] resources = this.resourcePatternResolver.getResources(location);
                for (Resource resource : resources) {
                    resourceLocation = resource.toString();
                    InputStream stream = resource.getInputStream();
                    try {
                        Document document = this.buildDocument(handler, stream);
                        this.parseDocument(resource, document, infos);
                    } finally {
                        stream.close();
                    }
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot parse persistence unit from " + resourceLocation, ex);
        } catch (SAXException ex) {
            throw new IllegalArgumentException("Invalid XML in persistence unit from " + resourceLocation, ex);
        } catch (ParserConfigurationException ex) {
            throw new IllegalArgumentException("Internal error parsing persistence unit from " + resourceLocation, ex);
        }

        return infos.toArray(new SpringPersistenceUnitInfo[infos.size()]);
    }

    /**
     * Validate the given stream and return a valid DOM document for parsing.
     */
    protected Document buildDocument(ErrorHandler handler, InputStream stream)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder parser = dbf.newDocumentBuilder();
        parser.setErrorHandler(handler);
        return parser.parse(stream);
    }

    /**
     * Parse the validated document and add entries to the given unit info list.
     */
    protected List<SpringPersistenceUnitInfo> parseDocument(Resource resource, Document document,
            List<SpringPersistenceUnitInfo> infos) throws IOException {

        Element persistence = document.getDocumentElement();
        String version = persistence.getAttribute(PersistenceUnitReader.PERSISTENCE_VERSION);
        URL rootUrl = this.determinePersistenceUnitRootUrl(resource);

        List<Element> units = DomUtils.getChildElementsByTagName(persistence, PersistenceUnitReader.PERSISTENCE_UNIT);
        for (Element unit : units) {
            infos.add(this.parsePersistenceUnitInfo(unit, version, rootUrl));
        }

        return infos;
    }

    /**
     * Determine the persistence unit root URL based on the given resource (which points to the
     * {@code persistence.xml} file we're reading).
     * @param resource the resource to check
     * @return the corresponding persistence unit root URL
     * @throws IOException if the checking failed
     */
    protected URL determinePersistenceUnitRootUrl(Resource resource) throws IOException {
        URL originalURL = resource.getURL();

        // If we get an archive, simply return the jar URL (section 6.2 from the JPA spec)
        if (ResourceUtils.isJarURL(originalURL)) {
            return ResourceUtils.extractJarFileURL(originalURL);
        }

        // check META-INF folder
        String urlToString = originalURL.toExternalForm();
        if (!urlToString.contains(PersistenceUnitReader.META_INF)) {
            if (PersistenceUnitReader.logger.isInfoEnabled()) {
                PersistenceUnitReader.logger
                    .info(resource.getFilename()
                            + " should be located inside META-INF directory; cannot determine persistence unit root URL for "
                            + resource);
            }
            return null;
        }
        if (urlToString.lastIndexOf(PersistenceUnitReader.META_INF) == (urlToString.lastIndexOf('/')
                - (1 + PersistenceUnitReader.META_INF.length()))) {
            if (PersistenceUnitReader.logger.isInfoEnabled()) {
                PersistenceUnitReader.logger
                    .info(resource.getFilename()
                            + " is not located in the root of META-INF directory; cannot determine persistence unit root URL for "
                            + resource);
            }
            return null;
        }

        String persistenceUnitRoot = urlToString.substring(0, urlToString.lastIndexOf(PersistenceUnitReader.META_INF));
        if (persistenceUnitRoot.endsWith("/")) {
            persistenceUnitRoot = persistenceUnitRoot.substring(0, persistenceUnitRoot.length() - 1);
        }
        return new URL(persistenceUnitRoot);
    }

    /**
     * Parse the unit info DOM element.
     */
    protected SpringPersistenceUnitInfo parsePersistenceUnitInfo(Element persistenceUnit, String version, URL rootUrl)
            throws IOException {

        SpringPersistenceUnitInfo unitInfo = new SpringPersistenceUnitInfo();

        // set JPA version (1.0 or 2.0)
        unitInfo.setPersistenceXMLSchemaVersion(version);

        // set persistence unit root URL
        unitInfo.setPersistenceUnitRootUrl(rootUrl);

        // set unit name
        String puName = persistenceUnit.getAttribute(PersistenceUnitReader.UNIT_NAME).trim();
        unitInfo.setPersistenceUnitName(puName);
        unitInfo.setPossibleChild(puName.contains(" extends "));

        // set transaction type
        String txType = persistenceUnit.getAttribute(PersistenceUnitReader.TRANSACTION_TYPE).trim();
        if (StringUtils.hasText(txType)) {
            unitInfo.setTransactionType(PersistenceUnitTransactionType.valueOf(txType));
        }

        // evaluate data sources
        String jtaDataSource = DomUtils.getChildElementValueByTagName(persistenceUnit,
                PersistenceUnitReader.JTA_DATA_SOURCE);
        if (StringUtils.hasText(jtaDataSource)) {
            unitInfo.setJtaDataSource(this.dataSourceLookup.getDataSource(jtaDataSource.trim()));
        }

        String nonJtaDataSource = DomUtils.getChildElementValueByTagName(persistenceUnit,
                PersistenceUnitReader.NON_JTA_DATA_SOURCE);
        if (StringUtils.hasText(nonJtaDataSource)) {
            unitInfo.setNonJtaDataSource(this.dataSourceLookup.getDataSource(nonJtaDataSource.trim()));
        }

        // provider
        String provider = DomUtils.getChildElementValueByTagName(persistenceUnit, PersistenceUnitReader.PROVIDER);
        if (StringUtils.hasText(provider)) {
            unitInfo.setPersistenceProviderClassName(provider.trim());
        }

        // exclude unlisted classes
        Element excludeUnlistedClasses = DomUtils.getChildElementByTagName(persistenceUnit,
                PersistenceUnitReader.EXCLUDE_UNLISTED_CLASSES);
        if (excludeUnlistedClasses != null) {
            String excludeText = DomUtils.getTextValue(excludeUnlistedClasses);
            unitInfo.setExcludeUnlistedClasses(!StringUtils.hasText(excludeText) || Boolean.valueOf(excludeText));
        }

        // set JPA 2.0 shared cache mode
        String cacheMode = DomUtils.getChildElementValueByTagName(persistenceUnit,
                PersistenceUnitReader.SHARED_CACHE_MODE);
        if (StringUtils.hasText(cacheMode)) {
            unitInfo.setSharedCacheMode(SharedCacheMode.valueOf(cacheMode));
        }

        // set JPA 2.0 validation mode
        String validationMode = DomUtils.getChildElementValueByTagName(persistenceUnit,
                PersistenceUnitReader.VALIDATION_MODE);
        if (StringUtils.hasText(validationMode)) {
            unitInfo.setValidationMode(ValidationMode.valueOf(validationMode));
        }

        this.parseProperties(persistenceUnit, unitInfo);
        this.parseManagedClasses(persistenceUnit, unitInfo);
        this.parseMappingFiles(persistenceUnit, unitInfo);
        this.parseJarFiles(persistenceUnit, unitInfo);

        if (unitInfo.isPossibleChild()) {
            // now check if everything is defined, otherwise, one of its ancestors is responsible of defining it
            if (persistenceUnit.hasAttribute(PersistenceUnitReader.TRANSACTION_TYPE)) {
                unitInfo.setTransactionTypeSetted(true);
            }

            // evaluate data sources
            if (DomUtils.getChildElementValueByTagName(persistenceUnit,
                    PersistenceUnitReader.JTA_DATA_SOURCE) != null) {
                unitInfo.setJtaDataSourceSetted(true);
            }

            if (DomUtils.getChildElementValueByTagName(persistenceUnit,
                    PersistenceUnitReader.NON_JTA_DATA_SOURCE) != null) {
                unitInfo.setNonJtaDataSourceSetted(true);
            }

            // provider
            if (DomUtils.getChildElementValueByTagName(persistenceUnit, PersistenceUnitReader.PROVIDER) != null) {
                unitInfo.setPersistenceProviderClassNameSetted(true);
            }

            // exclude unlisted classes
            if (DomUtils.getChildElementByTagName(persistenceUnit,
                    PersistenceUnitReader.EXCLUDE_UNLISTED_CLASSES) != null) {
                unitInfo.setExcludeUnlistedClassesSetted(true);
            }

            // set JPA 2.0 shared cache mode
            if (DomUtils.getChildElementValueByTagName(persistenceUnit,
                    PersistenceUnitReader.SHARED_CACHE_MODE) != null) {
                unitInfo.setSharedCacheModeSetted(true);
            }

            // set JPA 2.0 validation mode
            if (DomUtils.getChildElementValueByTagName(persistenceUnit,
                    PersistenceUnitReader.VALIDATION_MODE) != null) {
                unitInfo.setValidationModeSetted(true);
            }
        } else {
            unitInfo.setTransactionTypeSetted(true);
            unitInfo.setJtaDataSourceSetted(true);
            unitInfo.setNonJtaDataSourceSetted(true);
            unitInfo.setPersistenceProviderClassNameSetted(true);
            unitInfo.setExcludeUnlistedClassesSetted(true);
            unitInfo.setSharedCacheModeSetted(true);
            unitInfo.setValidationModeSetted(true);
        }

        return unitInfo;
    }

    /**
     * Parse the {@code property} XML elements.
     */
    protected void parseProperties(Element persistenceUnit, SpringPersistenceUnitInfo unitInfo) {
        Element propRoot = DomUtils.getChildElementByTagName(persistenceUnit, PersistenceUnitReader.PROPERTIES);
        if (propRoot == null) {
            return;
        }
        List<Element> properties = DomUtils.getChildElementsByTagName(propRoot, "property");
        for (Element property : properties) {
            String name = property.getAttribute("name");
            String value = property.getAttribute("value");
            unitInfo.addProperty(name, value);
        }
    }

    /**
     * Parse the {@code class} XML elements.
     */
    protected void parseManagedClasses(Element persistenceUnit, SpringPersistenceUnitInfo unitInfo) {
        List<Element> classes = DomUtils.getChildElementsByTagName(persistenceUnit,
                PersistenceUnitReader.MANAGED_CLASS_NAME);
        for (Element element : classes) {
            String value = DomUtils.getTextValue(element).trim();
            if (StringUtils.hasText(value)) {
                unitInfo.addManagedClassName(value);
            }
        }
    }

    /**
     * Parse the {@code mapping-file} XML elements.
     */
    protected void parseMappingFiles(Element persistenceUnit, SpringPersistenceUnitInfo unitInfo) {
        List<Element> files = DomUtils.getChildElementsByTagName(persistenceUnit,
                PersistenceUnitReader.MAPPING_FILE_NAME);
        for (Element element : files) {
            String value = DomUtils.getTextValue(element).trim();
            if (StringUtils.hasText(value)) {
                unitInfo.addMappingFileName(value);
            }
        }
    }

    /**
     * Parse the {@code jar-file} XML elements.
     */
    protected void parseJarFiles(Element persistenceUnit, SpringPersistenceUnitInfo unitInfo) throws IOException {
        List<Element> jars = DomUtils.getChildElementsByTagName(persistenceUnit, PersistenceUnitReader.JAR_FILE_URL);
        for (Element element : jars) {
            String value = DomUtils.getTextValue(element).trim();
            if (StringUtils.hasText(value)) {
                Resource[] resources = this.resourcePatternResolver.getResources(value);
                boolean found = false;
                for (Resource resource : resources) {
                    if (resource.exists()) {
                        found = true;
                        unitInfo.addJarFileUrl(resource.getURL());
                    }
                }
                if (!found) {
                    // relative to the persistence unit root, according to the JPA spec
                    URL rootUrl = unitInfo.getPersistenceUnitRootUrl();
                    if (rootUrl != null) {
                        unitInfo.addJarFileUrl(new URL(rootUrl, value));
                    } else {
                        PersistenceUnitReader.logger
                            .warn("Cannot resolve jar-file entry [" + value + "] in persistence unit '"
                                    + unitInfo.getPersistenceUnitName() + "' without root URL");
                    }
                }
            }
        }
    }

}
