package com.ontimize.jee.server.services.management;

import java.io.IOException;
import java.io.InputStream;

import com.ontimize.jee.common.dto.EntityResult;

public interface ILoggerHelper {

    InputStream openLogStream() throws IOException;

    EntityResult getLogFiles() throws Exception;

    InputStream getLogFileContent(String fileName) throws Exception;

}
