package com.ontimize.jee.common.util.share;

import java.io.Serializable;

/**
 * <code>Shared Element</code> is a serializable class, used to create an object that contains
 * information prepared to shared with other users in the application.
 *
 * @author Imatia Innovation
 * @since 5.8.0-SNAPSHOT
 *
 */
public class SharedElement implements Serializable {

    /**
     * Field that contains an optional message when the user shared any data.
     *
     * @see {@link SharedElement#SharedElement(String, String, Object, String, String)} -> Constructor
     *      without identifier
     * @see {@link SharedElement#SharedElement(int, String, String, Object, String, String)} ->
     *      Constructor with identifier
     */
    protected String message;

    /**
     * Field that contains the shared type, in other words, an specific key to group all shared elements
     * for an specific component. For example: shared data related to a customers table can be
     * 'formCustomers_ECustomer_CustomerPersonalData'
     *
     * @see {@link SharedElement#SharedElement(String, String, Object, String, String)} -> Constructor
     *      without identifier
     * @see {@link SharedElement#SharedElement(int, String, String, Object, String, String)} ->
     *      Constructor with identifier
     */
    protected String shareType;

    /**
     * Field that contains the content to share, it must be a String in order to store in a database. If
     * the content isn't a {@link String}, you must extend this class and override the method
     * {@link SharedElement#createDefaultContent(Object)}
     *
     * @see {@link SharedElement#SharedElement(String, String, Object, String, String)} -> Constructor
     *      without identifier
     * @see {@link SharedElement#SharedElement(int, String, String, Object, String, String)} ->
     *      Constructor with identifier
     */
    protected String contentShare;

    /**
     * Field that contains the name of the user who share the content
     *
     * @see {@link SharedElement#SharedElement(String, String, Object, String, String)} -> Constructor
     *      without identifier
     * @see {@link SharedElement#SharedElement(int, String, String, Object, String, String)} ->
     *      Constructor with identifier
     */
    protected String userSource;

    /**
     * Field that contains the name of the name of the shared content. For example, if an user shares a
     * filter, this field would be the name of the filter
     *
     * @see {@link SharedElement#SharedElement(String, String, Object, String, String)} -> Constructor
     *      without identifier
     * @see {@link SharedElement#SharedElement(int, String, String, Object, String, String)} ->
     *      Constructor with identifier
     */
    protected String name;

    /**
     * Field that contains the identifier of the shared element. This field is optional. Is useful if
     * you want to retrieve a shared data from the database with the database identifier. If you don't
     * provide any identifier when creates the object, it wil be -1
     *
     * @see {@link SharedElement#SharedElement(int, String, String, Object, String, String)} ->
     *      Constructor with identifier
     */
    protected int idShare = -1;

    /**
     * Class constructor without and identifier. This constructor uses only to pass as parameter to
     * store a new shared element in DB
     * @param message {@link #message}
     * @param shareType {@link #shareType}
     * @param contentShare {@link #contentShare}
     * @param userSource {@link #userSource}
     * @param name {@link #name}
     */
    public SharedElement(String message, String shareType, Object contentShare, String userSource, String name) {
        this.message = message;
        this.shareType = shareType;
        this.contentShare = this.createDefaultContent(contentShare);
        this.userSource = userSource;
        this.name = name;
    }

    /**
     * Class constructor with an identifier. This constructor is used when you retrieve a shared element
     * form the DB.
     * @param idShare {@link #idShare}
     * @param message {@link #message}
     * @param shareType {@link #shareType}
     * @param contentShare {@link #contentShare}
     * @param userSource {@link #userSource}
     * @param name {@link #name}
     */
    public SharedElement(int idShare, String message, String shareType, Object contentShare, String userSource,
            String name) {
        this.idShare = idShare;
        this.message = message;
        this.shareType = shareType;
        this.contentShare = this.createDefaultContent(contentShare);
        this.userSource = userSource;
        this.name = name;

    }

    /**
     * Convert the {@link Object} to share into a {@link String} representation to store in the
     * database.
     * @param rawContent {@link Object} to convert into a {@link String} for storage in database
     * @return A {@link String} representation of content value
     */
    protected String createDefaultContent(Object rawContent) {
        return (String) rawContent;
    }

    /**
     * Return the message associated with the content share.
     * @return The message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Return the associated key to this content share.
     * @return The associated key
     */
    public String getShareType() {
        return this.shareType;
    }

    /**
     * Return the content to share
     * @return The content to share
     */
    public String getContentShare() {
        return this.contentShare;
    }

    /**
     * Return the name of the user who share the content
     * @return The user
     */
    public String getUserSource() {
        return this.userSource;
    }

    /**
     * Return the identifier of the content share.
     * @return {@literal -1} if the user not specify the identifier in the constructor, or the
     *         identifier if this was given in the constructor
     */
    public int getIdShare() {
        return this.idShare;
    }

    /**
     * Return the name associated with the content shared
     * @return The name associated with the content shared
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ID share: => ");
        builder.append(this.getIdShare());
        builder.append("\n");
        builder.append("User source: => ");
        builder.append(this.getUserSource());
        builder.append("\n");
        builder.append("Message: => ");
        builder.append(this.getMessage());
        builder.append("\n");
        builder.append("Share key: => ");
        builder.append(this.getShareType());
        builder.append("\n");
        builder.append("Name: => ");
        builder.append(this.getName());
        builder.append("\n");
        builder.append("Content share: => ");
        builder.append(this.getContentShare());
        return builder.toString();
    }

}
