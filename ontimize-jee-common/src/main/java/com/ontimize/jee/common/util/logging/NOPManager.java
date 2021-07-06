package com.ontimize.jee.common.util.logging;

import com.ontimize.jee.common.util.remote.BytesBlock;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// import com.ontimize.jee.common.gui.ExtendedServerMonitor;

public class NOPManager implements ILogManager {

    @Override
    public List<Logger> getLoggerList() {
        return null;
    }

    @Override
    public Logger getLogger(String name) {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        return loggerFactory.getLogger(name);
    }

    @Override
    public Level getLevel(Logger logger) {
        return null;
    }

    @Override
    public void setLevel(Logger logger, Level level) throws Exception {
    }

    public Object findAppenderOfType(Class interfaceOfAppender) {
        return null;
    }

    @Override
    public BytesBlock getFileLogger() {
        return null;
    }

}
