package com.ontimize.jee.common.util.logging.log4j;

import com.ontimize.jee.common.util.logging.ILogManager;
import com.ontimize.jee.common.util.logging.Level;
import com.ontimize.jee.common.util.remote.BytesBlock;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.slf4j.Log4jLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Log4jManager implements ILogManager {

    private static final Logger logger = LoggerFactory.getLogger(Log4jManager.class);

    @Override
    public Logger getLogger(String name) {
        return LoggerFactory.getILoggerFactory().getLogger(name);
    }

    @Override
    public List<Logger> getLoggerList() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (!loggerFactory.getClass().isAssignableFrom(Log4jManager.getLog4jFactory())) {
            return null;
        }

        List<org.slf4j.Logger> loggers = this.getLoggetList(loggerFactory);
        List<org.slf4j.Logger> loggersComplete = this.completeLoggersHierarchly(loggers);
        Collections.sort(loggersComplete, new LoggerNameComparetor());
        return loggersComplete;
    }

    private List<Logger> completeLoggersHierarchly(List<Logger> loggers) {
        // TODO Consider to create "parent" unexisting package loggers
        return loggers;
    }

    protected static Class getLog4jFactory() {
        try {
            return Class.forName("org.apache.logging.slf4j.Log4jLoggerFactory");
        } catch (ClassNotFoundException e) {
            Log4jManager.logger.trace(null, e);
        }
        return null;
    }

    @Override
    public Level getLevel(Logger logger) {
        if (logger == null) {
            return null;
        }

        // Look again for logger by name UNDER PROPERLY CONTEXT -> The input logger is not correct, uses
        // "Default" context and has not the properly config
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        Map loggersToUse = this.getValidLoggersToUse(loggerFactory);
        org.apache.logging.log4j.core.Logger innerLogger = this.getInnerLogger(loggersToUse.get(logger.getName()));
        org.apache.logging.log4j.Level cLevel = innerLogger.getLevel();
        if (cLevel == null) {
            return null;
        }
        if (cLevel == org.apache.logging.log4j.Level.TRACE) {
            return Level.TRACE;
        } else if (cLevel == org.apache.logging.log4j.Level.DEBUG) {
            return Level.DEBUG;
        } else if (cLevel == org.apache.logging.log4j.Level.INFO) {
            return Level.INFO;
        } else if (cLevel == org.apache.logging.log4j.Level.WARN) {
            return Level.WARN;
        } else if (cLevel == org.apache.logging.log4j.Level.ERROR) {
            return Level.ERROR;
        } else if (cLevel == org.apache.logging.log4j.Level.OFF) {
            return Level.OFF;
        }

        return null;
    }

    @Override
    public void setLevel(Logger logger, Level level) throws Exception {
        if (logger == null) {
            return;
        }

        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        Map loggersToUse = this.getValidLoggersToUse(loggerFactory);
        org.apache.logging.log4j.core.Logger innerLogger = this.getInnerLogger(loggersToUse.get(logger.getName()));

        org.apache.logging.log4j.Level cLevel = null;
        switch (level) {
            case TRACE:
                cLevel = org.apache.logging.log4j.Level.TRACE;
                break;
            case DEBUG:
                cLevel = org.apache.logging.log4j.Level.DEBUG;
                break;
            case INFO:
                cLevel = org.apache.logging.log4j.Level.INFO;
                break;
            case WARN:
                cLevel = org.apache.logging.log4j.Level.WARN;
                break;
            case ERROR:
                cLevel = org.apache.logging.log4j.Level.ERROR;
                break;
            case OFF:
                cLevel = org.apache.logging.log4j.Level.OFF;
                break;
            default:
                break;
        }
        innerLogger.setLevel(cLevel);
    }


    @Override
    public BytesBlock getFileLogger() {
        BytesBlock bb = null;
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (!loggerFactory.getClass().isAssignableFrom(Log4jManager.getLog4jFactory())) {
            return bb;
        }
        try {
            Logger logger = loggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            Object innerLogger = getInnerLogger(logger);
            if (innerLogger instanceof org.apache.logging.log4j.core.Logger) {
                org.apache.logging.log4j.core.Logger logbackLogger = (org.apache.logging.log4j.core.Logger) innerLogger;
                Map<String, Appender> appenderMap = logbackLogger.getAppenders();
                Iterator<Appender> appenders = appenderMap.values().iterator();
                while (appenders.hasNext()) {
                    Appender appender = appenders.next();
                    if (appender instanceof RollingFileAppender) {
                        RollingFileAppender rp = (RollingFileAppender) appender;
                        String data = this.readFile(new File(rp.getFileName()));
                        bb = new BytesBlock(data.getBytes());
                    }
                }
            }
        } catch (Exception e) {
            Log4jManager.logger.error("Error retrieving data from log file.", e);
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
            Log4jManager.logger.error("File not found. ERROR: {}", e.getMessage(), e);
        } catch (IOException e) {
            Log4jManager.logger.error("File can't be read. ERROR: {}", e.getMessage(), e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                Log4jManager.logger.error("Buffered reader can't be closed. ERROR:{}", e.getMessage(), e);
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

    ///////////////////////////// REFLECTION UTILITIES /////////////////////////////////
    // protected org.apache.log4j.Logger getInnerLogger(Log4jLoggerAdapter adapter) {
    // return (org.apache.log4j.Logger) Log4jManager.getReflectionFieldValue(adapter, "logger");
    // }

    private org.apache.logging.log4j.core.Logger getInnerLogger(Logger logger2) {
        return (org.apache.logging.log4j.core.Logger) Log4jManager.getReflectionFieldValue(logger2, "logger");
    }

    private org.apache.logging.log4j.core.Logger getInnerLogger(Object logger2) {
        return (org.apache.logging.log4j.core.Logger) Log4jManager.getReflectionFieldValue(logger2, "logger");
    }

    // For some extrange reason, when a lloger is requested to logerFactory it gets from a "Default"
    // context, and not from our own context.
    private Map getValidLoggersToUse(ILoggerFactory loggerFactory) {
        Map<Object, Map<String, Log4jLogger>> registry = (Map<Object, Map<String, Log4jLogger>>) Log4jManager
            .getReflectionFieldValue(loggerFactory, "registry");
        Map loggersToUse = registry.get(org.apache.logging.log4j.core.LoggerContext.getContext(false));
        return loggersToUse;
    }

    private List<org.slf4j.Logger> getLoggetList(ILoggerFactory loggerFactory) {
        Map loggersToUse = this.getValidLoggersToUse(loggerFactory);
        List<org.slf4j.Logger> list = new ArrayList<Logger>();
        for (Object o : loggersToUse.values()) {
            Logger logger2 = loggerFactory.getLogger((String) Log4jManager.getReflectionFieldValue(o, "name"));
            list.add(logger2);
        }
        return list;
    }

    public static Object getReflectionFieldValue(Object toInvoke, String fieldName) {
        try {
            Field field = Log4jManager
                .getReflectionField(toInvoke instanceof Class ? (Class<?>) toInvoke : toInvoke.getClass(), fieldName);
            field.setAccessible(true);
            return field.get(toInvoke instanceof Class ? null : toInvoke);
        } catch (Exception error) {
            throw new RuntimeException(error);
        }
    }

    public static Field getReflectionField(Class<?> cl, String fieldName) {
        for (Class<?> innerClass = cl; innerClass != null; innerClass = innerClass.getSuperclass()) {
            try {
                return innerClass.getDeclaredField(fieldName);
            } catch (Exception error) {
                Log4jManager.logger.trace(null, error);
                // do nothing
                for (Class<?> interfaceClass : innerClass.getInterfaces()) {
                    try {
                        return interfaceClass.getDeclaredField(fieldName);
                    } catch (Exception err) {
                        Log4jManager.logger.trace(null, err);
                    }
                }
            }
        }

        throw new RuntimeException("Field " + fieldName + " not found in" + cl);
    }

    private final class LoggerNameComparetor implements Comparator<Logger> {

        @Override
        public int compare(org.slf4j.Logger o1, org.slf4j.Logger o2) {
            if ((o1 == null) || (o2 == null)) {
                return 0;
            }
            return o1.getName().compareTo(o2.getName());
        }

    }

    @Override
    public Object findAppenderOfType(Class interfaceOfAppender) {
        return null;
    }

}
