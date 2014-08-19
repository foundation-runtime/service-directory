/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory.utils;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;

/**
 * Convenient Directory util methods.
 * 
 * @author zuxiang
 *
 */
public class DirectoryUtils {
	
	/**
	 * Delete the file recursively.
	 * @param file
	 * @throws IOException
	 */
	public static void deleteRecursively(File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectoryContents(file);
		}
		if (file.exists() && !file.delete()) {
			throw new IOException("Failed to delete " + file);
		}
	}

	/**
	 * Delete the files in directory.
	 * 
	 * @param directory
	 * @throws IOException
	 */
	public static void deleteDirectoryContents(File directory)
			throws IOException {
		Preconditions.checkArgument(directory.isDirectory(),
				"Not a directory: %s", directory);
		File[] files = directory.listFiles();
		if (files == null) {
			throw new IOException("Error listing files for " + directory);
		}
		for (File file : files) {
			deleteRecursively(file);
		}
	}
}
