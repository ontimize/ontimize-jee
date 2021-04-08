package com.ontimize.jee.common.services.servermanagement;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.ontimize.dto.EntityResult;
import org.slf4j.Logger;


import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.session.SessionDto;
import com.ontimize.util.logging.Level;

/**
 * The Interface IServerManagementService.
 */
public interface IServerManagementService {

    /**
     * Open log stream.
     * @return the input stream
     * @throws OntimizeJEEException the ontimize jee exception
     */
    InputStream openLogStream() throws OntimizeJEEException;

    /**
     * Creates the heap dump.
     * @return the input stream
     * @throws OntimizeJEEException the ontimize jee exception
     */
    InputStream createHeapDump() throws OntimizeJEEException;

    /**
     * Creates the thread dump.
     * @return the string
     * @throws OntimizeJEEException the ontimize jee exception
     */
    String createThreadDump() throws OntimizeJEEException;

    /**
     * Execute sql.
     * @param sql the sql
     * @param dataSourceName the data source name
     * @return the entity result
     */
    EntityResult executeSql(final String sql, final String dataSourceName);

    /**
     * Gets the available data sources.
     * @return the available data sources
     */
    List<String> getAvailableDataSources();

    List<OntimizeJEELogger> getLoggerList() throws Exception;

    Logger getLogger(String name) throws Exception;

    Level getLevel(OntimizeJEELogger logger) throws Exception;

    void setLevel(OntimizeJEELogger logger) throws Exception;

    public static class OntimizeJEELogger implements Serializable {

        private int id;

        private String loggerName;

        private Level loggerLevel;

        public OntimizeJEELogger() {
        }

        public OntimizeJEELogger(int id, String name, Level level) {
            this.id = id;
            this.setLoggerName(name);
            this.setLoggerLevel(level);
        }

        public Level getLoggerLevel() {
            return this.loggerLevel;
        }

        public String getLoggerName() {
            return this.loggerName;
        }

        public void setLoggerLevel(Level loggerLevel) {
            this.loggerLevel = loggerLevel;
        }

        public void setLoggerName(String loggerName) {
            this.loggerName = loggerName;
        }

        public int getId() {
            return this.id;
        }

        public void setId(int id) {
            this.id = id;
        }

    }

    EntityResult getLogFiles() throws Exception;

    InputStream getLogFileContent(String fileName) throws Exception;

    /**
     * Gets the active sessions.
     * @return the active sessions
     * @throws OntimizeJEEException the ontimize jee exception
     */
    Collection<SessionDto> getActiveSessions() throws OntimizeJEEException;

    EntityResult getStatistics();

    EntityResult getServiceStatistics(String serviceName, String methodName, Date dateBefore, Date dateAfter);

    void setServiceStatistics(String serviceName, String methodName, Object params, String user, Date date,
            long timeExecution, String exception);

    EntityResult deleteStatistics(int days);

    void reloadDaos();

}
