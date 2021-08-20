package com.ontimize.jee.server.services.management;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.ontimize.jee.common.dto.EntityResultMapImpl;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.common.util.logging.LogManagerFactory;
import com.ontimize.jee.common.util.logging.log4j.Log4jManager;


public class Log4j2LoggerHelper implements ILoggerHelper {

    private static final Logger logger = LoggerFactory.getLogger(Log4j2LoggerHelper.class);

    public Log4j2LoggerHelper() {
    }

    @Override
    public InputStream openLogStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityResult getLogFiles() throws Exception {
        Path folder = this.getLogFolder();
        if (folder == null) {
            return new EntityResultMapImpl(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE, EntityResult.NODATA_RESULT,
                    "No hay ficheros que mostrar");
        }
        final EntityResult res = new EntityResultMapImpl(Arrays.asList(new String[] { "FILE_NAME", "FILE_SIZE" }));
        Files.walkFileTree(folder, new java.nio.file.SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                res.addRecord(
                        EntityResultTools.keysvalues("FILE_NAME", file.toString(), "FILE_SIZE", Files.size(file)));
                return FileVisitResult.CONTINUE;
            }
        });
        return res;
    }

    @Override
    public InputStream getLogFileContent(String fileName) throws Exception {
        Path folder = this.getLogFolder();
        final Path file = folder.resolve(fileName);
        if (!Files.exists(file)) {
            throw new OntimizeJEEException("File not found");
        }
        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        new Thread(() -> {
            try (ZipOutputStream zos = new ZipOutputStream(pos)) {
                zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
                StreamUtils.copy(Files.newInputStream(file), zos);
                zos.closeEntry();
            } catch (IOException e) {
                Log4j2LoggerHelper.logger.error(null, e);
            }
        }, "LoggerHelper copy stream").start();

        return pis;
    }

    private Path getLogFolder() {
        for (Logger logger : LogManagerFactory.getLogManager().getLoggerList()) {
            ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
            Map loggersToUse = this.getValidLoggersToUse(loggerFactory);
            org.apache.logging.log4j.core.Logger innerLogger = this.getInnerLogger(loggersToUse.get(logger.getName()));

            for (Appender appender : innerLogger.getAppenders().values()) {
                if (appender instanceof FileAppender) {
                    Path file = Paths.get(((FileAppender) appender).getFileName());
                    Path folder = file.getParent();
                    return folder;
                } else if (appender instanceof RollingFileAppender) {
                    Path file = Paths.get(((RollingFileAppender) appender).getFileName());
                    Path folder = file.getParent();
                    return folder;
                }
            }
        }
        return null;
    }

    ///////////////////////////// REFLECTION UTILITIES /////////////////////////////////

    private org.apache.logging.log4j.core.Logger getInnerLogger(Object logger2) {
        return (org.apache.logging.log4j.core.Logger) Log4jManager.getReflectionFieldValue(logger2, "logger");
    }

    // For some extrange reason, when a lloger is requested to logerFactory it gets from a "Default"
    // context, and not from our own context.
    private Map getValidLoggersToUse(ILoggerFactory loggerFactory) {
        Map<Object, Map> registry = (Map<Object, Map>) Log4jManager.getReflectionFieldValue(loggerFactory,
                "registry");
        Map loggersToUse = registry.get(org.apache.logging.log4j.core.LoggerContext.getContext(false));
        return loggersToUse;
    }

}
