package com.ontimize.jee.core.common.util.notice;

import com.ontimize.jee.core.common.dto.EntityResult;

import java.rmi.Remote;
import java.util.Map;
import java.util.List;

public interface INoticeSystem extends Remote {

    /**
     * Parameter that must exists in the reference locator (local and remote) to allow that all notices
     * threads start.
     */
    public static final String NOTICE_SYSTEM = "NoticeSystem";

    public static String PROP_NOTICE_ENTITY_NAME = "noticesentityname";

    public static String PROP_NOTICE_ENTITY_DESTINY_NAME = "noticesentitydestinyname";

    public static String PROP_NOTICE_CONFIG_MAIL = "propconfigmail";

    public static String PROP_NOTICE_CLASS_MAIL = "propconfigclassmail";

    public static String PROP_NOTICE_CONFIG_TEMPLATES = "propconfigtemplates";

    public static String PROP_NOTICE_CONFIG_DEFAULT_TEMPLATE = "propconfigdefaulttemplate";

    public static String PROP_NOTICE_CONFIG_REQUEST_TEMPLATE = "propconfigrequesttemplate";

    public static String MAIL_CONF_PROPERTIES = "com/imatia/avisos/utils/avisos/mailconf.properties";

    public static final String INTERNAL_NOTICE_PERIOD = "checkinternalnoticesperiod";

    public static final String SEND_MAIL_PERIOD = "sendnewmailsperiod";

    public static String NOTICE_ENTITY_TABLE_DB = "TNOTICES";

    public static String NOTICE_ENTITY_DESTINY_TABLE_DB = "TNOTICEDESTINY";

    public static final String NOTICE_MESSAGE_TYPE_COLUMN_NAME = "NoticeType";

    public static final String NOTICE_OTHER_COLUMNS = "OtherColumns";

    /**
     * Notice entity key
     */
    public static final String NOTICE_KEY = "NoticeId";

    /**
     * Key name of the notice destiny entity.
     */
    public static final String NOTICE_DESTINY_KEY = "DestinyId";

    /**
     * Column name of the destiny entity where user name is stored
     */
    public static final String NOTICE_TO_PARAMETER = "DestinyUser";

    /**
     * Column name of the notices entity where is stored the name of the user that generates the notice
     */
    public static final String NOTICE_FROM_PARAMETER = "SendUser";

    /**
     * Destiny entity column name which the mail addresses the notices are send to are stored in
     */
    public static final String NOTICE_MAILTO_PARAMETER = "DestinyEMail";

    /**
     * Notices entity column name which indicates if this notice requests confirmation (0 = without
     * response request)
     */
    public static final String NOTICE_RESPONSE_REQUEST = "ResponseRequest";

    /**
     * Destiny entity column name which indicates if this notice must be send in a email (0 = internal
     * notice, 1= email notice)
     */
    public static final String NOTICE_SEND_MAIL = "SendMail";

    /**
     * Destiny entity column name which indicates if this notice has been read (this column is only used
     * for internal notices)
     */
    public static final String NOTICE_READ = "Read";

    /**
     * Destiny entity column name which indicates the notice read date
     */
    public static final String NOTICE_READ_DATE = "ReadingDate";

    /**
     * Destiny entity column name which indicates if this notice has been sent, when notice is internal
     * notice this columns indicates if destiny user has queried the notice.
     */
    public static final String NOTICE_SEND = "Sent";

    /**
     * Notice entity column name that specifies the notice generation date
     */
    public static final String NOTICE_CREATE_DATE = "CreationDate";

    /**
     * Destiny entity column name which specifies the notice sent date
     */
    public static final String NOTICE_SEND_DATE = "SendingDate";

    /**
     * Notice entity column name which specifies se notice subject
     */
    public static final String NOTICE_SUBJECT = "Subject";

    /**
     * Notice entity column name which specifies the notice content
     */
    public static final String NOTICE_CONTENT = "Content";

    /**
     * Notice entity column name which indicates if this is a mandatory read notice
     */
    public static final String NOTICE_FORCE_READ = "MandatoryRead";

    /**
     * This column sets the sent notices as deleted
     */
    public static final String NOTICE_DELETE_SEND_NOTICE = "Deleted";

    /**
     * This column sets the received notice as deleted
     */
    public static final String NOTICE_DELETE_RECEIVE_NOTICE = "Deleted";

    public static final String NOTICE_MESSAGE_COLUMN_NAME = "Message";

    /**
     * Method used to send notices. This method insert the notice in the notice entity and insert all
     * destinies in the destiny notice entity <br>
     * @param messageValues
     * @param sessionId
     * @param request If this is true, the notices is a request notice. In this case the correct
     *        template must be selected
     * @throws Exception
     */
    public void sendNotice(Map messageValues, int sessionId, boolean request) throws Exception;

    /**
     * Method to get all notices for the specified user. <br>
     * In this method is possible specified if the user wants all messages or only unread ones
     * @param sessionId User session identifier
     * @param unsend When this is true only return the unsend notices
     * @param unread When this is true ontly return the unread notices
     * @return
     * @throws Exception
     */
    public EntityResult getInternalNotices(int sessionId, boolean unsend, boolean unread, boolean deleted)
            throws Exception;

    /**
     * Method to set some notices as read
     * @param sessionId User session identifier
     * @param idNotices Array with the notice keys
     * @throws Exception
     */
    public void setNoticesRead(int sessionId, List idNotices) throws Exception;

    /**
     * Method to query all notices sent by the specified user
     * @param sessionId User session identifier
     * @param deleted deleted = true only query the deleted notices <br>
     *        deleted = false only query not deleted notices<br>
     *        deleted = null query all sent notices
     * @return
     * @throws Exception
     */
    public EntityResult getSentNotices(int sessionId, Boolean deleted) throws Exception;

    /**
     * This method sets a notices as sent
     * @param sessionId User session identifier
     * @param noticeKey Notice key.
     * @throws Exception
     */
    public void checkNoticeSent(int sessionId, Object noticeKey) throws Exception;

    /**
     * This method sets the specified notices as deleted
     * @param sessionId User session identifier
     * @param idNotices Keys of the notices to remove
     * @throws Exception
     */
    public void removeSentNotices(int sessionId, List idNotices) throws Exception;

    /**
     * This method removes the specified received notices for the user with this session identifier.
     * @param sessionId User session identifier
     * @param idNotices Notice keys
     * @throws Exception
     */
    public void removeReceivedNotices(int sessionId, List idNotices) throws Exception;

    /**
     * Get the identifies for all notices types in the application
     * @return
     * @throws Exception
     */
    public List getNoticeTypes() throws Exception;

    /**
     * Get the name of the notice entity
     * @return
     * @throws Exception
     */
    public String getNoticeEntityName() throws Exception;

    /**
     * Get the name of the notice destiny entity
     * @return
     * @throws Exception
     */
    public String getNoticeEntityDestinyName() throws Exception;

    /**
     * Get the identification of the user that must be stored in the notice system to identify this
     * application user. In this way the identification of a user can be not only a string.
     * @param sessionId User session identifier
     * @return
     * @throws Exception
     */
    public Object getUserId(int sessionId) throws Exception;

}
