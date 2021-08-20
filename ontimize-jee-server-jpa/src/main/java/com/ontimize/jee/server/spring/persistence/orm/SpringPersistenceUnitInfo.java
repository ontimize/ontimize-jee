/*
 * Copyright 2002-2013 the original author or authors. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.ontimize.jee.server.spring.persistence.orm;

import javax.persistence.spi.ClassTransformer;

import org.springframework.core.DecoratingClassLoader;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.instrument.classloading.SimpleThrowawayClassLoader;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.util.Assert;

/**
 * Subclass of {@link MutablePersistenceUnitInfo} that adds instrumentation hooks based on Spring's
 * {@link org.springframework.instrument.classloading.LoadTimeWeaver} abstraction.
 *
 * <p>
 * This class is restricted to package visibility, in contrast to its superclass.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Costin Leau
 * @see PersistenceUnitManager
 * @since 2.0
 */
class SpringPersistenceUnitInfo extends MutablePersistenceUnitInfo {

    /** The load time weaver. */
    private LoadTimeWeaver loadTimeWeaver;

    /** The class loader. */
    private ClassLoader classLoader;

    /** The possible child. */
    private boolean possibleChild = false;

    /** The transaction type setted. */
    private boolean transactionTypeSetted = false;

    /** The jta data source setted. */
    private boolean jtaDataSourceSetted = false;

    /** The non jta data source setted. */
    private boolean nonJtaDataSourceSetted = false;

    /** The persistence provider class name setted. */
    private boolean persistenceProviderClassNameSetted = false;

    /** The exclude unlisted classes setted. */
    private boolean excludeUnlistedClassesSetted = false;

    /** The shared cache mode setted. */
    private boolean sharedCacheModeSetted = false;

    /** The validation mode setted. */
    private boolean validationModeSetted = false;

    /**
     * Initialize this PersistenceUnitInfo with the LoadTimeWeaver SPI interface used by Spring to add
     * instrumentation to the current class loader.
     * @param loadTimeWeaver the load time weaver
     */
    public void init(LoadTimeWeaver loadTimeWeaver) {
        Assert.notNull(loadTimeWeaver, "LoadTimeWeaver must not be null");
        this.loadTimeWeaver = loadTimeWeaver;
        this.classLoader = loadTimeWeaver.getInstrumentableClassLoader();
    }

    /**
     * Initialize this PersistenceUnitInfo with the current class loader (instead of with a
     * LoadTimeWeaver).
     * @param classLoader the class loader
     */
    public void init(ClassLoader classLoader) {
        Assert.notNull(classLoader, "ClassLoader must not be null");
        this.classLoader = classLoader;
    }

    /**
     * This implementation returns the LoadTimeWeaver's instrumentable ClassLoader, if specified.
     * @return the class loader
     */
    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * This implementation delegates to the LoadTimeWeaver, if specified.
     * @param classTransformer the class transformer
     */
    @Override
    public void addTransformer(ClassTransformer classTransformer) {
        if (this.loadTimeWeaver == null) {
            throw new IllegalStateException("Cannot apply class transformer without LoadTimeWeaver specified");
        }
        this.loadTimeWeaver.addTransformer(new ClassFileTransformerAdapter(classTransformer));
    }

    /**
     * This implementation delegates to the LoadTimeWeaver, if specified.
     * @return the new temp class loader
     */
    @Override
    public ClassLoader getNewTempClassLoader() {
        ClassLoader tcl = this.loadTimeWeaver != null ? this.loadTimeWeaver.getThrowawayClassLoader()
                : new SimpleThrowawayClassLoader(this.classLoader);
        String packageToExclude = this.getPersistenceProviderPackageName();
        if ((packageToExclude != null) && (tcl instanceof DecoratingClassLoader)) {
            ((DecoratingClassLoader) tcl).excludePackage(packageToExclude);
        }
        return tcl;
    }

    /**
     * Sets the possible child.
     * @param possibleChild the new possible child
     */
    public void setPossibleChild(boolean possibleChild) {
        this.possibleChild = possibleChild;
    }

    /**
     * Checks if is possible child.
     * @return true, if is possible child
     */
    public boolean isPossibleChild() {
        return this.possibleChild;
    }

    /**
     * Sets the transaction type setted.
     * @param b the new transaction type setted
     */
    public void setTransactionTypeSetted(boolean b) {
        this.transactionTypeSetted = b;
    }

    /**
     * Checks if is transaction type setted.
     * @return true, if is transaction type setted
     */
    public boolean isTransactionTypeSetted() {
        return this.transactionTypeSetted;
    }

    /**
     * Sets the jta data source setted.
     * @param b the new jta data source setted
     */
    public void setJtaDataSourceSetted(boolean b) {
        this.jtaDataSourceSetted = b;
    }

    /**
     * Checks if is jta data source setted.
     * @return true, if is jta data source setted
     */
    public boolean isJtaDataSourceSetted() {
        return this.jtaDataSourceSetted;
    }

    /**
     * Sets the non jta data source setted.
     * @param b the new non jta data source setted
     */
    public void setNonJtaDataSourceSetted(boolean b) {
        this.nonJtaDataSourceSetted = b;

    }

    /**
     * Checks if is non jta data source setted.
     * @return true, if is non jta data source setted
     */
    public boolean isNonJtaDataSourceSetted() {
        return this.nonJtaDataSourceSetted;
    }

    /**
     * Sets the persistence provider class name setted.
     * @param b the new persistence provider class name setted
     */
    public void setPersistenceProviderClassNameSetted(boolean b) {
        this.persistenceProviderClassNameSetted = b;
    }

    /**
     * Checks if is persistence provider class name setted.
     * @return true, if is persistence provider class name setted
     */
    public boolean isPersistenceProviderClassNameSetted() {
        return this.persistenceProviderClassNameSetted;
    }

    /**
     * Sets the exclude unlisted classes setted.
     * @param b the new exclude unlisted classes setted
     */
    public void setExcludeUnlistedClassesSetted(boolean b) {
        this.excludeUnlistedClassesSetted = b;

    }

    /**
     * Checks if is exclude unlisted classes setted.
     * @return true, if is exclude unlisted classes setted
     */
    public boolean isExcludeUnlistedClassesSetted() {
        return this.excludeUnlistedClassesSetted;
    }

    /**
     * Sets the shared cache mode setted.
     * @param b the new shared cache mode setted
     */
    public void setSharedCacheModeSetted(boolean b) {
        this.sharedCacheModeSetted = b;

    }

    /**
     * Checks if is shared cache mode setted.
     * @return true, if is shared cache mode setted
     */
    public boolean isSharedCacheModeSetted() {
        return this.sharedCacheModeSetted;
    }

    /**
     * Sets the validation mode setted.
     * @param b the new validation mode setted
     */
    public void setValidationModeSetted(boolean b) {
        this.validationModeSetted = b;
    }

    /**
     * Checks if is validation mode setted.
     * @return true, if is validation mode setted
     */
    public boolean isValidationModeSetted() {
        return this.validationModeSetted;
    }

}
