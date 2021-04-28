package com.ontimize.jee.common.util.multilanguage;

import com.ontimize.jee.core.common.dto.EntityResult;

import java.rmi.Remote;
import java.util.Map;

/**
 * Interface that allows using the multi-language translation system.
 *
 * @see TextDataField
 * @see FormMultilanguageTable
 * @since 5.11.0
 */
public interface MultilanguageEntity extends Remote {

    public static final String CHECK_FOR_UPDATE_STRING = "checkForUpdate";

    public static final String LOCALE_NAME_STRING = "LOCALE_NAME";

    public static final int UPDATE_STATE_INT = 1;

    public static final int INSERT_STATE_INT = 0;

    /**
     * This method checks if the entity is multi-language, allowing the field to activated the
     * multilanguage table system or not, making the following checks:
     * <ul>
     * <li>{@link TableEntity#getvLocaleColumns()} is not null</li>
     * <li>{@link TableEntity#getvLocaleColumns()} is not empty</li>
     * <li>{@link TableEntity#getvLocaleColumns()} contains the attribute passed by parameter</li>
     * </ul>
     * @param entityName Name of the entity to be checked
     * @param attribute Column to check if it is a multi-language column of that entity
     * @return <code>true</code> if the conditions specified above are met, <code>false</code>
     *         otherwise.
     * @throws Exception
     */
    public boolean checkMultilanguageEntity(String entityName, String attribute) throws Exception;

    /**
     * Returns an {@link EntityResult} with field translation data to all languages available in the
     * LocaleEntity
     * @param entityName The name of the entity to which the field to be translated belongs
     * @param attribute The name of the field to be translated stored in the database
     * @param formKeys The keys belonging to the form that identify the record to which the field
     *        belongs
     * @param sessionId The session identifier of the client
     * @return An {@link EntityResult} with field translation data to all languages available in the
     *         LocaleEntity
     * @throws Exception
     */
    public EntityResult populateMultilanguageTranslationTable(String entityName, String attribute,
            Map<String, Object> formKeys, int sessionId) throws Exception;

    /**
     * Updates the changes made to the multi-language translation table in the DB.
     * @param tableChanges An {@link EntityResult} containing the new translations to be stored and the
     *        locale to which they belong.
     * @param formKeys The form keys of the record from which the columns are maintained
     * @param attributeName The name of the attribute being translated
     * @param attributeLocalename The name of the attribute that contains the name of the column that
     *        serves as identification of the languages of the locale entity.
     * @param sessionId The session identifier of the client
     * @throws Exception
     */
    public void upgradeMultilanguageTranslationTable(EntityResult tableChanges, Map<String, Object> formKeys,
            String attributeName, String attributeLocalename, int sessionId)
            throws Exception;

}
