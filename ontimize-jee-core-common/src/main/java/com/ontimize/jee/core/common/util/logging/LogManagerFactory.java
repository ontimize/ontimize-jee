package com.ontimize.jee.core.common.util.logging;

import com.ontimize.jee.core.common.util.logging.log4j.Log4jManager;
import com.ontimize.jee.core.common.util.logging.logback.LogbackManager;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(LogManagerFactory.class);

    private static ILogManager logManager;

    public static ILogManager getLogManager() {
        if (LogManagerFactory.logManager == null) {
            ILoggerFactory currentFactory = LoggerFactory.getILoggerFactory();

            if (LogManagerFactory.isLogback(currentFactory)) {
                LogManagerFactory.logManager = new LogbackManager();
            } else if (LogManagerFactory.isLog4j(currentFactory)) {
                LogManagerFactory.logManager = new Log4jManager();
            } else {
                try {
                    LogManagerFactory.logManager = new NOPManager();
                } catch (Exception ex) {
                    LogManagerFactory.logger.debug("NOP Logger is not implemented", ex);
                }
            }
        }
        return LogManagerFactory.logManager;
    }

    private static boolean isLogback(ILoggerFactory currentFactory) {
        try {
            Class logbackFactory = Class.forName("ch.qos.logback.classic.LoggerContext");
            if (currentFactory.getClass().isAssignableFrom(logbackFactory)) {
                return true;
            }
        } catch (Exception ex) {
            LogManagerFactory.logger.debug("Logback is not implemented", ex);
        }
        return false;
    }

    private static boolean isLog4j(ILoggerFactory currentFactory) {
        try {
            Class log4jFactory = Class.forName("org.apache.logging.slf4j.Log4jLoggerFactory");
            if (currentFactory.getClass().isAssignableFrom(log4jFactory)) {
                return true;
            }
        } catch (Exception ex) {
            LogManagerFactory.logger.debug("Log4j is not implemented", ex);
        }
        return false;
    }

}
