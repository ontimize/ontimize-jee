package com.ontimize.jee.core.common.util.logging.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.ontimize.jee.core.common.util.logging.ILogManager;
import com.ontimize.jee.core.common.util.logging.Level;
import com.ontimize.jee.core.common.util.remote.BytesBlock;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LogbackManager implements ILogManager {

    private static final Logger logger = LoggerFactory.getLogger(LogbackManager.class);

    @Override
    public Logger getLogger(String name) {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        return loggerFactory.getLogger(name);
    }

    @Override
    public List<Logger> getLoggerList() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();

        if (loggerFactory.getClass().isAssignableFrom(LogbackManager.getLogbackFactory())) {
            LoggerContext loggerContext = (LoggerContext) loggerFactory;
            List<ch.qos.logback.classic.Logger> loggers = loggerContext.getLoggerList();
            List<Logger> enabledLoggers = new ArrayList<Logger>();
            for (Iterator<ch.qos.logback.classic.Logger> i = loggers.iterator(); i.hasNext();) {
                ch.qos.logback.classic.Logger currentLogger = i.next();
                if (currentLogger instanceof Logger) {
                    enabledLoggers.add(currentLogger);
                }
            }
            return enabledLoggers;
        }

        return null;
    }

    protected static Class getLogbackFactory() {
        try {
            return Class.forName("ch.qos.logback.classic.LoggerContext");
        } catch (ClassNotFoundException e) {
            LogbackManager.logger.trace(null, e);
        }
        return null;
    }

    @Override
    public Level getLevel(Logger logger) {
        if (logger == null) {
            return null;
        }
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
            ch.qos.logback.classic.Level cLevel = logbackLogger.getLevel();
            if (cLevel == null) {
                return null;
            }
            switch (cLevel.levelInt) {
                case ch.qos.logback.classic.Level.TRACE_INT:
                    return Level.TRACE;
                case ch.qos.logback.classic.Level.DEBUG_INT:
                    return Level.DEBUG;
                case ch.qos.logback.classic.Level.INFO_INT:
                    return Level.INFO;
                case ch.qos.logback.classic.Level.WARN_INT:
                    return Level.WARN;
                case ch.qos.logback.classic.Level.ERROR_INT:
                    return Level.ERROR;
                case ch.qos.logback.classic.Level.OFF_INT:
                    return Level.OFF;
                default:
                    break;
            }

        }
        return null;
    }

    @Override
    public void setLevel(Logger logger, Level level) throws Exception {
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
            if (level == null) {
                logbackLogger.setLevel(null);
                return;
            }
            ch.qos.logback.classic.Level cLevel = null;
            switch (level) {
                case TRACE:
                    cLevel = ch.qos.logback.classic.Level.TRACE;
                    break;
                case DEBUG:
                    cLevel = ch.qos.logback.classic.Level.DEBUG;
                    break;
                case INFO:
                    cLevel = ch.qos.logback.classic.Level.INFO;
                    break;
                case WARN:
                    cLevel = ch.qos.logback.classic.Level.WARN;
                    break;
                case ERROR:
                    cLevel = ch.qos.logback.classic.Level.ERROR;
                    break;
                case OFF:
                    cLevel = ch.qos.logback.classic.Level.OFF;
                    break;
                default:
                    break;
            }

            logbackLogger.setLevel(cLevel);
        }
    }

    public Object findAppenderOfType(Class interfaceOfAppender) {
        Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
            Iterator<Appender<ILoggingEvent>> appenders = logbackLogger.iteratorForAppenders();
            while (appenders.hasNext()) {
                Appender<ILoggingEvent> appender = appenders.next();
                if (interfaceOfAppender.isAssignableFrom(appender.getClass())) {
                    return appender;
                }
            }
        }

        return null;
    }


    // @Override
    // public void registerServerMonitor(ExtendedServerMonitor monitor) {
    // Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    // if (logger instanceof ch.qos.logback.classic.Logger) {
    // ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
    // Iterator<Appender<ILoggingEvent>> appenders = logbackLogger.iteratorForAppenders();
    // while (appenders.hasNext()) {
    // Appender<ILoggingEvent> appender = appenders.next();
    // if (appender instanceof ServerMonitorAppender) {
    // ((ServerMonitorAppender) appender).setMonitor(monitor);
    // }
    // }
    // }
    // }

    @Override
    public BytesBlock getFileLogger() {

        BytesBlock bb = null;

        try {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
            if (logger instanceof ch.qos.logback.classic.Logger) {
                ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
                Iterator<Appender<ILoggingEvent>> iteratorAppenders = logbackLogger.iteratorForAppenders();
                while (iteratorAppenders.hasNext()) {
                    Appender<ILoggingEvent> a = iteratorAppenders.next();
                    if (a instanceof RollingFileAppender) {
                        RollingFileAppender rp = (RollingFileAppender) a;
                        String data = this.readFile(new File(rp.getFile()));
                        bb = new BytesBlock(data.getBytes());
                    }
                }
            }
        } catch (Exception e) {
            LogbackManager.logger.error("Error retrieving data from log file.", e);
        }
        return bb;
    }

    public String readFile(File f) {
        ArrayDeque<String> queue = new ArrayDeque<String>(500);
        this.readFromLast(queue, f, 500);
        StringBuilder builder = new StringBuilder();
        while (!queue.isEmpty()) {
            builder.append(queue.removeFirst());
            builder.append("\n");
        }
        return builder.toString();
    }

    public void readFromLast(ArrayDeque<String> queue, File file, int lines) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = br.readLine();

            while (line != null) {
                this.addToList(queue, line, lines);
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            LogbackManager.logger.error("File not found. ERROR: {}", e.getMessage(), e);
        } catch (IOException e) {
            LogbackManager.logger.error("File can't be read. ERROR: {}", e.getMessage(), e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                LogbackManager.logger.error("Buffered reader can't be closed. ERROR:{}", e.getMessage(), e);
            }
        }
    }

    protected void addToList(ArrayDeque<String> queue, String element, int lines) {
        if (queue.size() >= lines) {
            queue.removeFirst();
            queue.offer(element);
        } else {
            queue.offer(element);
        }
    }

}
