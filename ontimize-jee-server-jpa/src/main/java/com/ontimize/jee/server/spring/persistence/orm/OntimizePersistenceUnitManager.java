/**
 *
 */
package com.ontimize.jee.server.spring.persistence.orm;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;

public class OntimizePersistenceUnitManager extends DefaultPersistenceUnitManager {

    private static final Logger logger = LoggerFactory.getLogger(OntimizePersistenceUnitManager.class);
    private static final String EXTENDS_SEPARATOR = " extends ";
    private static final String PERSISTENCE_UNIT_INFOS_PARENT_CLASS_FIELD_NAME = "persistenceUnitInfos";
    private static final String PERSISTENCE_UNIT_INFO_NAMES_PARENT_CLASS_FIELD_NAME = "persistenceUnitInfoNames";

    private final List<String> alreadyObtained = new ArrayList<String>();

    /**
     * Gets the persistence unit infos using reflection because our parent didn't granted us access
     *
     * @return the persistence unit infos
     */
    private Map<String, PersistenceUnitInfo> getPersistenceUnitInfos() {
        try {
            Field f = this.getClass().getSuperclass().getField(OntimizePersistenceUnitManager.PERSISTENCE_UNIT_INFOS_PARENT_CLASS_FIELD_NAME);
            f.setAccessible(true);
            return (Map<String, PersistenceUnitInfo>) f.get(this);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the persistence unit info namess using reflection because our parent didn't granted us access
     *
     * @return the persistence unit infos
     */
    private Set<String> getPersistenceUnitInfoNames() {
        try {
            Field f = this.getClass().getSuperclass().getField(OntimizePersistenceUnitManager.PERSISTENCE_UNIT_INFO_NAMES_PARENT_CLASS_FIELD_NAME);
            f.setAccessible(true);
            return (Set<String>) f.get(this);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public PersistenceUnitInfo obtainPersistenceUnitInfo(String persistenceUnitName) {
        if (this.alreadyObtained.contains(persistenceUnitName)) {
            throw new IllegalStateException(
                    "Persistence unit with name '" + persistenceUnitName + "' already obtained");
        }

        Map<String, PersistenceUnitInfo> persistenceUnitInfos = this.getPersistenceUnitInfos();

        List<SpringPersistenceUnitInfo> persistenceUnitInfosToMerge = this.getPersistenceUnitInfosToMerge(persistenceUnitName);
        SpringPersistenceUnitInfo spui = null;
        if ((persistenceUnitInfosToMerge != null) && (persistenceUnitInfosToMerge.size() > 0)) {
            String realPUName = persistenceUnitInfosToMerge.get(0).getPersistenceUnitName();
            persistenceUnitInfos.remove(realPUName);
            spui = this.mergePersistenceUnitInfos(persistenceUnitInfosToMerge, persistenceUnitName);
        }


        if (spui == null) {
                throw new IllegalArgumentException(
                        "No persistence unit with name '" + persistenceUnitName + "' found");
        } else {
            if (spui.isPossibleChild() && (
                    !spui.isExcludeUnlistedClassesSetted()
                            || !spui.isJtaDataSourceSetted()
                            || !spui.isNonJtaDataSourceSetted()
                            || !spui.isPersistenceProviderClassNameSetted()
                            || !spui.isSharedCacheModeSetted()
                            || !spui.isTransactionTypeSetted()
                            || !spui.isValidationModeSetted())) {
                throw new IllegalStateException(
                        "Persistence unit with name '" + persistenceUnitName + "' seems to be a child persistence unit but it (or its parent) hasn't defined all mandatory attributes for child persistence units");
            }
        }
        this.alreadyObtained.add(persistenceUnitName);
        return spui;
    }

    private SpringPersistenceUnitInfo mergePersistenceUnitInfos(List<SpringPersistenceUnitInfo> persistenceUnitInfosToMerge, String persistenceUnitName) {
        SpringPersistenceUnitInfo spui = null;
        if ((persistenceUnitInfosToMerge != null) && !persistenceUnitInfosToMerge.isEmpty()) {
            spui = persistenceUnitInfosToMerge.get(0); // base pu, now we would add parent pu info
            if (persistenceUnitInfosToMerge.size() > 1) {
                SpringPersistenceUnitInfo parentPersistenceUnitInfo = this.mergePersistenceUnitInfos(persistenceUnitInfosToMerge.subList(1, persistenceUnitInfosToMerge.size()), persistenceUnitInfosToMerge.get(1).getPersistenceUnitName());
                if (parentPersistenceUnitInfo != null) {
                    spui = this.merge(spui, parentPersistenceUnitInfo);
                }
            }
            spui.setPersistenceUnitName(persistenceUnitName);
        }
        return spui;
    }

    private SpringPersistenceUnitInfo merge(SpringPersistenceUnitInfo original, SpringPersistenceUnitInfo parent) {
        if (!original.isExcludeUnlistedClassesSetted()) {
            original.setExcludeUnlistedClasses(parent.excludeUnlistedClasses());
            original.setExcludeUnlistedClassesSetted(parent.isExcludeUnlistedClassesSetted());
        }
        if (!original.isJtaDataSourceSetted()) {
            original.setJtaDataSource(parent.getJtaDataSource());
            original.setJtaDataSourceSetted(parent.isJtaDataSourceSetted());
        }
        if (!original.isNonJtaDataSourceSetted()) {
            original.setNonJtaDataSource(parent.getNonJtaDataSource());
            original.setNonJtaDataSourceSetted(parent.isNonJtaDataSourceSetted());
        }
        if (!original.isPersistenceProviderClassNameSetted()) {
            original.setPersistenceProviderClassName(parent.getPersistenceProviderClassName());
            original.setPersistenceProviderClassNameSetted(parent.isPersistenceProviderClassNameSetted());
        }
        if (!original.isSharedCacheModeSetted()) {
            original.setSharedCacheMode(parent.getSharedCacheMode());
            original.setSharedCacheModeSetted(parent.isSharedCacheModeSetted());
        }
        if (!original.isTransactionTypeSetted()) {
            original.setTransactionType(parent.getTransactionType());
            original.setTransactionTypeSetted(parent.isTransactionTypeSetted());
        }
        if (!original.isValidationModeSetted()) {
            original.setValidationMode(parent.getValidationMode());
            original.setValidationModeSetted(parent.isValidationModeSetted());
        }
        this.addNonExistent(original.getJarFileUrls(), parent.getJarFileUrls());
        this.addNonExistent(original.getManagedClassNames(), parent.getManagedClassNames());
        this.addNonExistent(original.getMappingFileNames(), parent.getMappingFileNames());
        if ((original.getPersistenceProviderPackageName() == null) && (parent.getPersistenceProviderPackageName() != null)) {
            original.setPersistenceProviderPackageName(parent.getPersistenceProviderPackageName());
        }
        if ((original.getPersistenceUnitRootUrl() == null) && (parent.getPersistenceUnitRootUrl() != null)) {
            original.setPersistenceUnitRootUrl(parent.getPersistenceUnitRootUrl());
        }
        if ((original.getPersistenceXMLSchemaVersion() == null) && (parent.getPersistenceXMLSchemaVersion() != null)) {
            original.setPersistenceXMLSchemaVersion(parent.getPersistenceXMLSchemaVersion());
        }
        this.addNonExistentProperties(original.getProperties(), parent.getProperties());

        return original;
    }

    private void addNonExistentProperties(Properties originalProperties, Properties tryProperties) {
        for (Entry<Object, Object> prop : tryProperties.entrySet()) {
            if (!originalProperties.keySet().contains(prop.getKey())) {
                originalProperties.put(prop.getKey(), prop.getValue());
            }
        }

    }

    private <T extends Object> void addNonExistent(List<T> originalList, List<T> tryList) {
        for (T ob : tryList) {
            if (!originalList.contains(ob)) {
                originalList.add(ob);
            }
        }

    }

    private List<SpringPersistenceUnitInfo> getPersistenceUnitInfosToMerge(String persistenceUnitName) {
        Map<String, PersistenceUnitInfo> persistenceUnitInfos = this.getPersistenceUnitInfos();
        List<SpringPersistenceUnitInfo> spis = new ArrayList<SpringPersistenceUnitInfo>();
        PersistenceUnitInfo persistenceUnitInfo = persistenceUnitInfos.get(persistenceUnitName);
        if (persistenceUnitInfo != null) {
            spis.add(this.toSpringPersistenceUnitInfo(persistenceUnitInfo));
        } else {
            for (Entry<String, PersistenceUnitInfo> entry : persistenceUnitInfos.entrySet()) {
                String currentPU = entry.getKey();
                if (currentPU.startsWith(persistenceUnitName)) {
                    spis.add(this.toSpringPersistenceUnitInfo(entry.getValue()));
                    if (currentPU.substring(persistenceUnitName.length()).startsWith(OntimizePersistenceUnitManager.EXTENDS_SEPARATOR)) {
                        String extendedPU = StringUtils.substringAfter(currentPU, OntimizePersistenceUnitManager.EXTENDS_SEPARATOR);
                        if ((extendedPU != null) && !extendedPU.isEmpty()) {
                            spis.addAll(this.getPersistenceUnitInfosToMerge(extendedPU));
                        }
                    }
                }
            }


        }

        return spis;

    }

    private SpringPersistenceUnitInfo toSpringPersistenceUnitInfo(PersistenceUnitInfo persistenceUnitInfo) {
        SpringPersistenceUnitInfo mpui = null;
        if (persistenceUnitInfo instanceof SpringPersistenceUnitInfo) {
            mpui = (SpringPersistenceUnitInfo) persistenceUnitInfo;
        } else if (persistenceUnitInfo != null) {
            OntimizePersistenceUnitManager.logger.warn("trying to convert from " + persistenceUnitInfo.getClass().getCanonicalName() + " to logger");
            mpui = new SpringPersistenceUnitInfo();
            List<URL> jarFileUrls = persistenceUnitInfo.getJarFileUrls();
            for (URL url : jarFileUrls) {
                mpui.addJarFileUrl(url);
            }
            mpui.setJtaDataSource(persistenceUnitInfo.getJtaDataSource());
            List<String> managedClassNames = persistenceUnitInfo.getManagedClassNames();
            for (String managedClassName : managedClassNames) {
                mpui.addManagedClassName(managedClassName);
            }
            List<String> mappingFileNames = persistenceUnitInfo.getMappingFileNames();
            for (String mappingFileName : mappingFileNames) {
                mpui.addMappingFileName(mappingFileName);
            }
            mpui.setNonJtaDataSource(persistenceUnitInfo.getNonJtaDataSource());
            mpui.setPersistenceProviderClassName(persistenceUnitInfo.getPersistenceProviderClassName());
            mpui.setPersistenceUnitName(persistenceUnitInfo.getPersistenceUnitName());
            mpui.setPersistenceUnitRootUrl(persistenceUnitInfo.getPersistenceUnitRootUrl());
            mpui.setPersistenceXMLSchemaVersion(persistenceUnitInfo.getPersistenceXMLSchemaVersion());
            mpui.setProperties(persistenceUnitInfo.getProperties());
            mpui.setSharedCacheMode(persistenceUnitInfo.getSharedCacheMode());
            mpui.setTransactionType(persistenceUnitInfo.getTransactionType());
            mpui.setValidationMode(persistenceUnitInfo.getValidationMode());
        }
        return mpui;
    }
}
