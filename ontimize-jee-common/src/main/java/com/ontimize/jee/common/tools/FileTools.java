package com.ontimize.jee.common.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FileTools.
 */
public final class FileTools {

	/**
	 * Instantiates a new file tools.
	 */
	private FileTools() {
		super();
	}

	/** The Constant logger. */
	private static final Logger	logger	= LoggerFactory.getLogger(FileTools.class);

	/**
	 * Delete file quitely.
	 *
	 * @param file
	 *            the file
	 */
	public static void deleteQuitely(Path file) {
		if (file == null) {
			return;
		}

		try {
			Files.deleteIfExists(file);
		} catch (IOException e) {
			FileTools.logger.error("could not remove file {}", file);
			file.toFile().deleteOnExit();
		}
	}

	/**
	 * To path.
	 *
	 * @param resFile
	 *            the res file
	 * @return the list
	 */
	public static List<Path> toPath(List<File> resFile) {
		List<Path> res = new ArrayList<>(resFile.size());
		for (File file : resFile) {
			res.add(file.toPath());
		}
		return res;
	}

}
