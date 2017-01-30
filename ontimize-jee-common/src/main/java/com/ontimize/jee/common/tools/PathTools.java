package com.ontimize.jee.common.tools;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PathTools.
 */
public final class PathTools {
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(PathTools.class);

	/**
	 * Instantiates a new path tools.
	 */
	private PathTools() {
		super();
	}

	/**
	 * Delete folder, ant never throws Exception, log it.
	 *
	 * @param file
	 * @return true if successfully deleted, false anyway
	 */
	public static boolean deleteFileSafe(Path file) {
		if (file != null) {
			try {
				if (Files.isDirectory(file)) {
					PathTools.deleteFolder(file);
				} else {
					Files.delete(file);
				}
			} catch (IOException e) {
				PathTools.logger.error("E_DELETING_FILE", e);
			}
		}
		return !Files.exists(file);
	}

	/**
	 * Delete folder, ant never throws Exception, log it.
	 *
	 * @param folder
	 */
	public static boolean deleteFolderSafe(Path folder) {
		if (folder != null) {
			try {
				PathTools.deleteFolder(folder);
			} catch (IOException e) {
				PathTools.logger.error("E_DELETING_FOLDER", e);
			}
		}
		return !Files.exists(folder);
	}

	/**
	 * Delete folder.
	 *
	 * @param folder
	 *            the folder
	 * @return true if successfully deleted, false anyway
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static boolean deleteFolder(Path folder) throws IOException {
		Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

		});
		return !Files.exists(folder);
	}

	/**
	 * Delete all files under folder, nut maintain folder.
	 *
	 * @param folder
	 * @return true if successfully deleted and empty, false anyway.
	 * @throws IOException
	 */
	public static boolean deleteFolderContent(Path folder) throws IOException {
		if (PathTools.isFolderEmpty(folder)){
			return true;
		}
		Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
		});
		return PathTools.isFolderEmpty(folder);
	}

	/**
	 *
	 * @param folder
	 * @return true if empty, false anyway
	 * @throws IOException
	 */
	public static boolean isFolderEmpty(final Path folder) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(folder)) {
			return !dirStream.iterator().hasNext();
		}
	}

}
