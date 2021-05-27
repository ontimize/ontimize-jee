package com.ontimize.jee.common.gui.attachment;

import java.io.Serializable;
import java.util.List;

/**
 * This class creates a special attribute for attachments, which contains the name of the attachment
 * entity and the keys, in order to establish the dispatcher that will be used when consulting the
 * entity's attachments.
 */
public class AttachmentAttribute implements Serializable {

    /**
     * Name of the attribute to compare
     */
    public static final String ATTR_STRING = "ATTR_ATTACHMENT_ATTRIBUTE";

    /**
     * Name of the attachment entity
     */
    protected String entityName;

    /**
     * Name of the form keys needed to recover the attachments
     */
    protected List<String> formKeys;

    /**
     * Constructor for {@link AttachmentAttribute}
     * @param entityName Name of the entity
     * @param formKeys Keys of the form associated to the attachment
     */
    public AttachmentAttribute(String entityName, List<String> formKeys) {
        this.entityName = entityName;
        this.formKeys = formKeys;
    }

    /**
     * Returns the attribute
     * @return A {@link String} with the attribute.
     */
    public String getAttribute() {
        return AttachmentAttribute.ATTR_STRING;
    }

    /**
     * Returns the name of the attachment entity
     * @return A {@link String} with the name of the entity name
     */
    public String getEntityName() {
        return this.entityName;
    }

    /**
     * Return a {@link List} with the keys of the form
     * @return A {@link List} with the keys of the form
     */
    public List<String> getFormKeys() {
        return this.formKeys;
    }

    /**
     * Set the name of the attachment entity
     * @param entityName A {@link String} with the name of the attachment entity
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Set the keys of the form to the attachment
     * @param formKeys The keys of the form
     */
    public void setFormKeys(List<String> formKeys) {
        this.formKeys = formKeys;
    }

}
