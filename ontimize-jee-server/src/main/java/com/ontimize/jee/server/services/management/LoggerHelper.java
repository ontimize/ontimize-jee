package com.ontimize.jee.server.services.management;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.TimeLimiter;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;

@Component
@Lazy(value = true)
public class LoggerHelper {

	private static final Logger	logger			= LoggerFactory.getLogger(LoggerHelper.class);

	/** The Constant LAYOUT_PATTERN. */
	private static final String	LAYOUT_PATTERN	= "[%-5level] %d{dd/MM/yyyy HH:mm:ss.SSS} [%thread] %logger{5}: %msg%n";

	/**
	 * Query log.
	 *
	 * @param response
	 *            the response
	 * @throws IOException
	 */
	public InputStream openLogStream() throws IOException {
		final CustomPipedInputStream in = new CustomPipedInputStream();
		final PipedOutputStream out = new PipedOutputStream(in);
		synchronized (out) {
			final CustomOutputStreamAppender appender = this.registerAppender(in, out);

			new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (out) {
						try {
							out.wait();
						} catch (InterruptedException e1) {
							// do nothing
						}
						LoggerHelper.logger.debug("unregistering remote logger");
						LoggerHelper.this.unregisterAppender(appender);
						try {
							in.close();
							out.close();
						} catch (IOException e) {
							LoggerHelper.logger.error(null, e);
						}
					}

				}
			}, "Thread-close log stream").start();
		}
		return in;
	}

	/**
	 * Unregister appender.
	 *
	 * @param appender
	 *            the appender
	 */
	private void unregisterAppender(CustomOutputStreamAppender appender) {
		if (appender != null) {
			ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			logger.detachAppender(appender);
			appender.stop();
		}
	}

	/**
	 * Register appender.
	 *
	 * @param os
	 *            the os
	 * @return the custom output stream appender
	 * @throws IOException
	 */
	private CustomOutputStreamAppender registerAppender(final CustomPipedInputStream inPipe, final OutputStream os) throws IOException {
		Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		if (logger instanceof ch.qos.logback.classic.Logger) {
			ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
			CustomOutputStreamAppender outputStreamAppender = new CustomOutputStreamAppender();
			OutputStream wrapperOutputStream = new OutputStream() {

				TimeLimiter	limiter		= new TimeLimiter();
				boolean		hasError	= false;

				@Override
				public void write(final int b) throws IOException {
					if (this.hasError) {
						return;
					}
					try {
						if (inPipe.isDead()) {
							throw new IOException("in pipe dead");
						}
						this.limiter.callWithTimeout(new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								os.write(b);
								return null;
							}
						}, 2, TimeUnit.SECONDS);
					} catch (Exception ex) {
						this.hasError = true;
						try {
							LoggerHelper.logger.info("detected error in log stream");
							synchronized (os) {
								os.notify();
								try {
									os.close();
									inPipe.close();
								} catch (Exception err) {
									// do nothing
								}
							}
						} finally {
							this.limiter.shutdown();
						}
					}
				}

			};
			outputStreamAppender.setOutputStream(wrapperOutputStream);
			PatternLayout layout = new PatternLayout();
			layout.setPattern(LoggerHelper.LAYOUT_PATTERN);
			layout.setContext(logbackLogger.getLoggerContext());
			layout.start();
			outputStreamAppender.setContext(logbackLogger.getLoggerContext());
			logbackLogger.addAppender(outputStreamAppender);
			outputStreamAppender.start();
			logger.error("hola");
			return outputStreamAppender;
		}
		throw new OntimizeJEERuntimeException("No logback instance found");
	}

	public EntityResult getLogFiles() throws Exception {
		Path folder = this.getLogFolder();
		if (folder == null) {
			return new EntityResult(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE, EntityResult.NODATA_RESULT, "No hay ficheros que mostrar");
		}
		final EntityResult res = new EntityResult();
		this.initEntityResult(res, Arrays.asList(new String[] { "FILE_NAME", "FILE_SIZE" }), 0);
		Files.walkFileTree(folder, new java.nio.file.SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				res.addRecord((Hashtable) LoggerHelper.this.keysvalues("FILE_NAME", file.toString(), "FILE_SIZE", Files.size(file)));
				return FileVisitResult.CONTINUE;
			}
		});
		return res;
	}

	private void initEntityResult(EntityResult res, List<?> columns, int length) {
		for (Object col : columns) {
			res.put(col, new Vector<>(length > 0 ? length : 10));
		}
	}

	private Map<Object, Object> keysvalues(Object... objects) {
		if (objects == null) {
			return new Hashtable<>();
		}
		if ((objects.length % 2) != 0) {
			throw new RuntimeException("Review filters, it is mandatory to set dual <key><value>.");
		}
		for (Object o : objects) {
			if (o == null) {
				throw new RuntimeException("Review filters, it is not acceptable null <key> or null <value>.");
			}
		}

		Map<Object, Object> res = new Hashtable<>();
		int i = 0;
		while (i < objects.length) {
			res.put(objects[i++], objects[i++]);
		}
		return res;
	}

	private Path getLogFolder() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
			for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
				Appender<ILoggingEvent> appender = index.next();
				if (appender instanceof FileAppender) {
					FileAppender<?> fAppender = (FileAppender<?>) appender;
					Path file = Paths.get(fAppender.getFile());
					Path folder = file.getParent();
					return folder;
				}
			}
		}
		return null;
	}

	public InputStream getLogFileContent(String fileName) throws Exception {
		Path folder = this.getLogFolder();
		final Path file = folder.resolve(fileName);
		if (!Files.exists(file)) {
			throw new OntimizeJEEException("File not found");
		}
		final PipedInputStream pis = new PipedInputStream();
		final PipedOutputStream pos = new PipedOutputStream(pis);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (ZipOutputStream zos = new ZipOutputStream(pos)) {
					zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
					StreamUtils.copy(Files.newInputStream(file), zos);
					zos.closeEntry();
				} catch (IOException e) {
					LoggerHelper.logger.error(null, e);
				}
			}
		}, "LoggerHelper copy stream").start();

		return pis;
	}

	/**
	 * The Class CustomOutputStreamAppender.
	 */
	protected static class CustomOutputStreamAppender extends OutputStreamAppender<ILoggingEvent> {

		/**
		 * Instantiates a new custom output stream appender.
		 */
		public CustomOutputStreamAppender() {
			super();
			this.setEncoder(new CustomPatternLayoutEncoder());
		}
	}

	/**
	 * The Class CustomPatternLayoutEncoder.
	 */
	protected static class CustomPatternLayoutEncoder extends PatternLayoutEncoder {

		/**
		 * Instantiates a new custom pattern layout encoder.
		 */
		public CustomPatternLayoutEncoder() {
			super();
			this.layout = new PatternLayout();
			((PatternLayout) this.layout).setPattern(LoggerHelper.LAYOUT_PATTERN);
			this.layout.start();
			this.setPattern(LoggerHelper.LAYOUT_PATTERN);
		}

	}

	protected static class CustomPipedInputStream extends PipedInputStream {

		private static final long	DEAD_TIME	= 5000;
		private long				lastRead	= System.currentTimeMillis();
		private boolean				reading		= false;

		@Override
		public synchronized int read() throws IOException {
			try {
				this.reading = true;
				this.lastRead = System.currentTimeMillis();
				return super.read();
			} finally {
				this.reading = false;
			}
		}

		public boolean isDead() {
			return !this.reading && (this.lastRead > 0) && ((System.currentTimeMillis() - this.lastRead) > CustomPipedInputStream.DEAD_TIME);
		}
	}
}
