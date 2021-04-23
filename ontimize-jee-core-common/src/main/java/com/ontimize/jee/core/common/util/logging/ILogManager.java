package com.ontimize.jee.core.common.util.logging;

import com.ontimize.jee.core.common.util.remote.BytesBlock;
import org.slf4j.Logger;

import java.util.List;

public interface ILogManager {

    public List<Logger> getLoggerList();

    public Logger getLogger(String name);

    public Level getLevel(Logger logger);

    public void setLevel(Logger logger, Level level) throws Exception;

    public Object findAppenderOfType(Class interfaceOfAppender);
    // public Object serverMonitorAppender();
    // public void registerServerMonitor(ExtendedServerMonitor monitor);

    public BytesBlock getFileLogger();

}
