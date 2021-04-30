package com.ontimize.jee.common.util.incidences;

import java.rmi.Remote;

public interface IIncidenceService extends Remote {

    public static final String REMOTE_NAME = "IncidenceService";

    public static final String INCIDENCES_ENTITY = "Entity";

    public static final String INCIDENCES_KEY = "Key";

    public static final String INCIDENCES_USER = "User";

    public static final String INCIDENCES_SCREENSHOT = "Screenshot";

    public static final String INCIDENCES_LOG = "Log";

    public static final String INCIDENCES_DESCRIPTION = "Description";

    public static final String INCIDENCES_REVISED = "Revised";

    public static final String INCIDENCES_DATE = "Date";

    public static final String INCIDENCES_SAVE_DIRECTORY = "SaveDirectory";

    public static final String INCIDENCES_SUBJECT = "Subject";

    public static final String INCIDENCES_CONFIGURATION_FILE = "IncidencesConfigurationFile";

    public static final String INCIDENCES_CRON_ENABLED = "CronEnabled";

    public static final String INCIDENCES_CRON_PERIOD = "CronPeriod";

    public void createIncidende(String message, String subject, byte[] img, byte[] log, int sessionId) throws Exception;

}
