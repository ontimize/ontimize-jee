package com.ontimize.jee.server.dao.jdbc.extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.ontimize.jee.common.tools.ObjectTools;
import com.ontimize.jee.common.tools.StringTools;
import com.ontimize.jee.common.tools.streamfilter.ReplaceTokensFilterReader;
import com.ontimize.jee.server.dao.jdbc.setup.AmbiguousColumnType;
import com.ontimize.jee.server.dao.jdbc.setup.AmbiguousColumnsType;
import com.ontimize.jee.server.dao.jdbc.setup.FunctionColumnType;
import com.ontimize.jee.server.dao.jdbc.setup.FunctionColumnsType;
import com.ontimize.jee.server.dao.jdbc.setup.JdbcEntitySetupType;
import com.ontimize.jee.server.dao.jdbc.setup.OrderColumnType;
import com.ontimize.jee.server.dao.jdbc.setup.OrderColumnsType;
import com.ontimize.jee.server.dao.jdbc.setup.QueriesType;
import com.ontimize.jee.server.dao.jdbc.setup.QueryType;

/**
 * Default implementation.
 */
public class DefaultDaoExtensionHelper implements IDaoExtensionHelper {

    /** The CONSTANT logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultDaoExtensionHelper.class);

    @Override
    public JdbcEntitySetupType checkDaoExtensions(JdbcEntitySetupType baseSetup, String path,
            String pathToPlaceHolder) {
        logger.debug("Checking dao extensions for dao '{}'.", path);
        List<URL> extensionFiles = this.getExtensionFiles(path);
        if (!extensionFiles.isEmpty()) {
            return this.processDaoExtensions(baseSetup, extensionFiles, pathToPlaceHolder);
        }
        return baseSetup;
    }

    /**
     * Look for all extension files (same path, same name but adding "_extends*" before file extension).
     * @param currentDao
     * @param inputFile
     * @return
     */
    protected List<URL> getExtensionFiles(String inputFile) {
        List<URL> extensions = new ArrayList<URL>();
        try {
            String basePath = inputFile.contains("/") ? inputFile.substring(0, inputFile.lastIndexOf("/")) : "";
            String baseFileName = inputFile.contains("/") ? inputFile.substring(inputFile.lastIndexOf("/") + 1)
                    : inputFile;
            int pointIndex = baseFileName.lastIndexOf(".");
            if (pointIndex > 0) {
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver
                    .getResources("classpath*:" + basePath + "/" + baseFileName.substring(0, pointIndex) + "_extends*"
                            + baseFileName.substring(pointIndex));

                for (Resource res : resources) {
                    extensions.add(res.getURL());
                }

                Collections.sort(extensions, new Comparator<URL>() {
                    @Override
                    public int compare(URL o1, URL o2) {
                        if ((o1 == null) || (o2 == null) || (o1.getPath() == null) || (o2.getPath() == null)) {
                            return 0;
                        }
                        return o1.getPath().compareTo(o2.getPath());
                    }

                });
            }
        } catch (Exception e) {
            logger.warn("Error getting extension files of " + inputFile, e);
        }
        if (extensions.isEmpty()) {
            logger.debug("Dao extensions not found for dao '{}'.", inputFile);
        } else {
            logger.debug("Dao extensions found for dao '{}' : ({}):", inputFile, extensions.size());
            for (URL url : extensions) {
                logger.debug("\t . {}", url);
            }
        }
        return extensions;
    }

    /**
     * Process dao extensions
     * @param baseSetup
     * @param currentDao
     * @param extensionFiles
     * @param pathToPlaceHolder
     */
    protected JdbcEntitySetupType processDaoExtensions(JdbcEntitySetupType baseSetup, List<URL> extensionFiles,
            String pathToPlaceHolder) {
        for (URL urlExt : extensionFiles) {
            try {
                logger.debug("Processing dao extension file {}:", urlExt);
                Reader reader = this.readWithPlaceHolders(urlExt.openStream(), pathToPlaceHolder);
                final JdbcEntitySetupType setupExtension = JAXB.unmarshal(reader, JdbcEntitySetupType.class);

                baseSetup = this.mergeSetups(baseSetup, setupExtension);
            } catch (Exception err) {
                logger.error("Error parsing dao extension file '{}'. Ignoring it.", urlExt, err);
            }
        }
        return baseSetup;
    }

    /**
     * Ensures to replace input configuration file with placeHolders
     * @param is
     * @param pathToPlaceHolder
     * @return
     * @throws IOException
     */
    protected Reader readWithPlaceHolders(InputStream is, final String pathToPlaceHolder) throws IOException {
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

                return new ReplaceTokensFilterReader(new InputStreamReader(is),
                        mapProperties);

            }
        } else {
            return new InputStreamReader(is);
        }
    }

    /**
     * Combine baseSetup and extensionSetup
     * @param baseSetup
     * @param setupExtension
     * @return
     */
    protected JdbcEntitySetupType mergeSetups(JdbcEntitySetupType baseSetup, JdbcEntitySetupType setupExtension) {
        if (setupExtension.getTable() != null) {
            logger.trace("\t .Overriding dao table: {}", setupExtension.getTable());
            baseSetup.setTable(setupExtension.getTable());
        }
        if (setupExtension.getSchema() != null) {
            logger.trace("\t .Overriding dao schema: {}", setupExtension.getSchema());
            baseSetup.setSchema(setupExtension.getSchema());
        }
        if (setupExtension.getCatalog() != null) {
            logger.trace("\t .Overriding dao catalog: {}", setupExtension.getCatalog());
            baseSetup.setCatalog(setupExtension.getCatalog());
        }
        if (setupExtension.getDeleteKeys() != null) {
            logger.trace("\t .Overriding dao deteleKeys: {}", setupExtension.getDeleteKeys().getColumn());
            baseSetup.setDeleteKeys(setupExtension.getDeleteKeys());
        }
        if (setupExtension.getUpdateKeys() != null) {
            logger.trace("\t .Overriding dao updateKeys: {}", setupExtension.getUpdateKeys().getColumn());
            baseSetup.setUpdateKeys(setupExtension.getUpdateKeys());
        }
        if (setupExtension.getQueries() != null) {
            for (final QueryType newQuery : setupExtension.getQueries().getQuery()) {
                QueryType baseQuery = this.getQueryById(baseSetup.getQueries(), newQuery.getId());
                boolean queryAlreadyDefined = baseQuery != null;
                if (queryAlreadyDefined) {
                    logger.trace("\t Overriding dao query '{}'", newQuery.getId());
                    QueryType mergedQuery = this.mergeQueries(baseQuery, newQuery);
                    baseSetup.getQueries().getQuery().remove(baseQuery);
                    baseSetup.getQueries().getQuery().add(mergedQuery);
                } else {
                    logger.trace("\t .Adding dao query '{}'", newQuery.getId());
                    baseSetup.getQueries().getQuery().add(newQuery);
                }
            }
        }
        if (setupExtension.getGeneratedKey() != null) {
            logger.trace("\t�Overriding dao generatedKey: {}", setupExtension.getGeneratedKey());
            baseSetup.setGeneratedKey(setupExtension.getGeneratedKey());
        }
        if (setupExtension.getDatasource() != null) {
            logger.trace("\t .Overriding dao datasource: {}", setupExtension.getDatasource());
            baseSetup.setDatasource(setupExtension.getDatasource());
        }
        if (setupExtension.getSqlhandler() != null) {
            logger.trace("\t .Overriding dao sqlhandler: {}", setupExtension.getSqlhandler());
            baseSetup.setSqlhandler(setupExtension.getSqlhandler());
        }

        if (setupExtension.getNameconverter() != null) {
            logger.trace("\t .Overriding dao nameConverter: {}", setupExtension.getNameconverter());
            baseSetup.setNameconverter(setupExtension.getNameconverter());
        }
        return baseSetup;
    }

    protected QueryType getQueryById(QueriesType queries, String id) {
        if (queries == null) {
            return null;
        }
        for (QueryType qry : queries.getQuery()) {
            if (ObjectTools.safeIsEquals(qry.getId(), id)) {
                return qry;
            }
        }
        return null;
    }

    /**
     * Ensures to merge queries defined in dao extension
     * @param existentQuery
     * @param newQuery
     */
    protected QueryType mergeQueries(QueryType baseQuery, QueryType newQuery) {

        // //----> Mode two : Merge ------------------------------------------------------------------------
        // // // Sentence: if present in new, override
        if ((newQuery.getSentence() != null) && (!StringTools.isEmpty(newQuery.getSentence().getValue()))) {
            logger.trace("\t\t .Overriding dao query '{}' -> sentence changed", newQuery.getId());
            baseQuery.setSentence(newQuery.getSentence());
        }
        // // // AmbiguousColumns: Match by "name": if empty "prefix" then remove, else override
        if (newQuery.getAmbiguousColumns() != null) {
            for (AmbiguousColumnType newAmb : newQuery.getAmbiguousColumns().getAmbiguousColumn()) {

                AmbiguousColumnType oldAmb = this.getAmbiguousColumnByName(baseQuery.getAmbiguousColumns(),
                        newAmb.getName());
                if (StringTools.isEmpty(newAmb.getPrefix())) {
                    // Remove it
                    logger.trace("\t\t .Overriding dao query '{}' -> ambiguous column removed : '{}' ",
                            newQuery.getId(),
                            newAmb.getName());
                    baseQuery.getAmbiguousColumns().getAmbiguousColumn().remove(oldAmb);
                } else if (oldAmb != null) {
                    logger.trace("\t\t .Overriding dao query '{}' -> ambiguous column replaced : '{}' ",
                            newQuery.getId(), newAmb.getName());
                    baseQuery.getAmbiguousColumns().getAmbiguousColumn().remove(oldAmb);
                    baseQuery.getAmbiguousColumns().getAmbiguousColumn().add(newAmb);
                } else {
                    logger.trace("\t\t .Overriding dao query '{}' -> ambiguous column added : '{}' ", newQuery.getId(),
                            newAmb.getName());
                    baseQuery.getAmbiguousColumns().getAmbiguousColumn().add(newAmb);
                }
            }
        }
        // // // � getFunctionColumns: Match by "name": if empty "value" then remove, else override
        if (newQuery.getFunctionColumns() != null) {
            for (FunctionColumnType newFnc : newQuery.getFunctionColumns().getFunctionColumn()) {
                FunctionColumnType oldFnc = this.getFunctionColumnByName(baseQuery.getFunctionColumns(),
                        newFnc.getName());
                if (StringTools.isEmpty(newFnc.getValue())) {
                    // Remove it
                    logger.trace("\t\t .Overriding dao query '{}' -> function column removed : '{}' ", newQuery.getId(),
                            newFnc.getName());
                    baseQuery.getFunctionColumns().getFunctionColumn().remove(oldFnc);
                } else if (oldFnc != null) {
                    logger.trace("\t\t .Overriding dao query '{}' -> function column replaced : '{}' ",
                            newQuery.getId(),
                            newFnc.getName());
                    baseQuery.getFunctionColumns().getFunctionColumn().remove(oldFnc);
                    baseQuery.getFunctionColumns().getFunctionColumn().add(newFnc);
                } else {

                    logger.trace("\t\t .Overriding dao query '{}' -> function column added : '{}' ", newQuery.getId(),
                            newFnc.getName());
                    baseQuery.getFunctionColumns().getFunctionColumn().add(newFnc);
                }
            }
        }
        // // // � ValidColumns: Always override, does not exists a criteria to map
        if (newQuery.getValidColumns() != null) {
            logger.trace("\t\t .Overriding dao query '{}' -> valid columns changed", newQuery.getId());
            baseQuery.setValidColumns(newQuery.getValidColumns());
        }
        // // // � OrderColumns: Match by "name": if empty "type" then remove, else override
        if (newQuery.getOrderColumns() != null) {
            for (OrderColumnType newOrd : newQuery.getOrderColumns().getOrderColumn()) {
                OrderColumnType oldOrd = this.getOrderColumnByName(baseQuery.getOrderColumns(), newOrd.getName());
                if (StringTools.isEmpty(newOrd.getType())) {
                    // Remove it
                    logger.trace("\t\t .Overriding dao query '{}' -> order column removed : '{}' ", newQuery.getId(),
                            newOrd.getName());
                    baseQuery.getOrderColumns().getOrderColumn().remove(oldOrd);
                } else if (oldOrd != null) {
                    logger.trace("\t\t .Overriding dao query '{}' -> order column replaced : '{}' ", newQuery.getId(),
                            newOrd.getName());
                    baseQuery.getOrderColumns().getOrderColumn().remove(oldOrd);
                    baseQuery.getOrderColumns().getOrderColumn().add(newOrd);
                } else {
                    logger.trace("\t\t .Overriding dao query '{}' -> order column added : '{}' ", newQuery.getId(),
                            newOrd.getName());
                    baseQuery.getOrderColumns().getOrderColumn().add(newOrd);
                }
            }
        }
        return baseQuery;
    }

    protected AmbiguousColumnType getAmbiguousColumnByName(AmbiguousColumnsType ambiguousColumns, String name) {
        if ((ambiguousColumns == null) || (ambiguousColumns.getAmbiguousColumn() == null)) {
            return null;
        }
        for (AmbiguousColumnType amb : ambiguousColumns.getAmbiguousColumn()) {
            if (ObjectTools.safeIsEquals(amb.getName(), name)) {
                return amb;
            }
        }
        return null;
    }

    protected FunctionColumnType getFunctionColumnByName(FunctionColumnsType functionColumns, String name) {
        if ((functionColumns == null) || (functionColumns.getFunctionColumn() == null)) {
            return null;
        }
        for (FunctionColumnType fnc : functionColumns.getFunctionColumn()) {
            if (ObjectTools.safeIsEquals(fnc.getName(), name)) {
                return fnc;
            }
        }
        return null;
    }

    protected OrderColumnType getOrderColumnByName(OrderColumnsType orderColumns, String name) {
        if ((orderColumns == null) || (orderColumns.getOrderColumn() == null)) {
            return null;
        }
        for (OrderColumnType fnc : orderColumns.getOrderColumn()) {
            if (ObjectTools.safeIsEquals(fnc.getName(), name)) {
                return fnc;
            }
        }
        return null;
    }

}
