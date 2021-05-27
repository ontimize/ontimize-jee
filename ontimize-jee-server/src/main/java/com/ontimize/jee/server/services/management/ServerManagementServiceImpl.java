package com.ontimize.jee.server.services.management;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.common.session.SessionDto;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.common.util.logging.ILogManager;
import com.ontimize.jee.common.util.logging.Level;
import com.ontimize.jee.common.util.logging.LogManagerFactory;

@Service("ServerManagementService")
@Lazy(value = true)
public class ServerManagementServiceImpl implements ApplicationContextAware, IServerManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ServerManagementServiceImpl.class);

    @Autowired
    private ILoggerHelper loggerHelper;

    @Autowired
    private HeapDumperHelper heapDumpHelper;

    @Autowired
    private ThreadDumperHelper threadDumpHelper;

    @Autowired
    private RequestStatisticsHelper requestStatisticsHelper;

    @Autowired(required = false)
    private SessionHelper sessionHelper;

    @Autowired(required = false)
    private DeleteRequestStatisticsHistory deleteThread;

    private ApplicationContext applicationContext;

    public ServerManagementServiceImpl() {
        super();
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public InputStream openLogStream() throws OntimizeJEEException {
        try {
            return this.loggerHelper.openLogStream();
        } catch (IOException error) {
            throw new OntimizeJEEException(error);
        }
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public InputStream createHeapDump() throws OntimizeJEEException {
        try {
            Path dumpHeap = this.heapDumpHelper.dumpHeap(true);
            dumpHeap.toFile().deleteOnExit();
            return Files.newInputStream(dumpHeap);
        } catch (IOException error) {
            throw new OntimizeJEEException(error);
        }
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public String createThreadDump() throws OntimizeJEEException {
        try {
            return this.threadDumpHelper.dumpThreads();
        } catch (IOException error) {
            throw new OntimizeJEEException(error);
        }
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult executeSql(final String sql, final String dataSourceName) {
        /*
         * JdbcTemplate template = new JdbcTemplate(); DataSource dataSource =
         * this.applicationContext.getBean(dataSourceName, DataSource.class); final SQLStatementHandler
         * sqlStatementHandler = this.applicationContext.getBean(SQLStatementHandler.class);
         * template.setDataSource(dataSource); return template.execute(new StatementCallback<EntityResult>()
         * {
         *
         * @Override public EntityResult doInStatement(Statement stmt) throws SQLException,
         * DataAccessException { if (stmt.execute(sql)) { ResultSet rs = stmt.getResultSet(); EntityResult
         * res = new EntityResult(); try { sqlStatementHandler.resultSetToEntityResult(rs, res, null); }
         * catch (Exception error) { throw new DataRetrievalFailureException(error.getMessage(), error); }
         * return res; } return new EntityResult(EntityResult.OPERATION_SUCCESSFUL,
         * EntityResult.NODATA_RESULT, "Operaci�n realizada. " + stmt.getUpdateCount() +
         * " filas modificadas"); } });
         */
        throw new OntimizeJEERuntimeException("NOT_IMPLEMENTED");
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public List<String> getAvailableDataSources() {
        Map<String, DataSource> beansOfType = this.applicationContext.getBeansOfType(DataSource.class, false, true);
        return new ArrayList<>(beansOfType.keySet());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public List<OntimizeJEELogger> getLoggerList() throws Exception {
        List<OntimizeJEELogger> loggerListFinal = new ArrayList<>();
        ILogManager managerLog = LogManagerFactory.getLogManager();
        List<Logger> loggerList = managerLog.getLoggerList();
        if ((loggerList != null) && !loggerList.isEmpty()) {
            for (Logger logger : loggerList) {
                String name = logger.getName();
                Level level = managerLog.getLevel(logger);
                loggerListFinal.add(new OntimizeJEELogger(loggerList.indexOf(logger), name, level));
            }
        }
        return loggerListFinal;
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public Logger getLogger(String name) throws Exception {
        return LogManagerFactory.getLogManager().getLogger(name);
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public Level getLevel(OntimizeJEELogger logger) throws Exception {
        ILogManager managerLog = LogManagerFactory.getLogManager();
        List<Logger> loggerList = managerLog.getLoggerList();
        return managerLog.getLevel(loggerList.get(logger.getId()));
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public void setLevel(OntimizeJEELogger logger) throws Exception {
        ILogManager managerLog = LogManagerFactory.getLogManager();
        List<Logger> loggerList = managerLog.getLoggerList();
        managerLog.setLevel(loggerList.get(logger.getId()), logger.getLoggerLevel());
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult getLogFiles() throws Exception {
        return this.loggerHelper.getLogFiles();
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public InputStream getLogFileContent(String fileName) throws Exception {
        return this.loggerHelper.getLogFileContent(fileName);
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public Collection<SessionDto> getActiveSessions() throws OntimizeJEEException {
        if (this.sessionHelper == null) {
            return Collections.emptyList();
        }
        return this.sessionHelper.getActiveSessions();
    }

    @Override
    public void setServiceStatistics(String serviceName, String methodName, Object params, String user, Date date,
            long timeExecution, String exception) {
        if (this.deleteThread != null) {
            synchronized (this.deleteThread) {
                if (!this.deleteThread.isRunning()) {
                    this.deleteThread.start();
                }
            }
        }
        this.requestStatisticsHelper.insertRequestStatistics(serviceName, methodName, params, user, date, timeExecution,
                exception);
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult getStatistics() {
        return this.requestStatisticsHelper.queryRequestStatistics();
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult getServiceStatistics(String serviceName, String methodName, Date dateBefore, Date dateAfter) {
        return this.requestStatisticsHelper.queryRequestStatistics(serviceName, methodName, dateBefore, dateAfter);
    }

    @Override
    public EntityResult deleteStatistics(int days) {
        return this.requestStatisticsHelper.deleteRequestStatistics(days);
    }

    @Override
    public void reloadDaos() {
        Map<String, IOntimizeDaoSupport> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.applicationContext,
                IOntimizeDaoSupport.class);
        for (IOntimizeDaoSupport dao : beans.values()) {
            try {
                dao.reload();
            } catch (Exception e) {
                ServerManagementServiceImpl.logger.error("Error reload dao: ", dao != null ? dao.getClass() : "nullDao",
                        e);
            }
        }
    }

}
