/*
 * File: FileUtils.java
 *
 * Copyright (c) 2006-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * last modified: Friday, May 18, 2012 (18:52) by: Matthias Beil
 */
package com.consol.citrus.testlink.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for CITRUS file static methods.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class FileUtils {

    // ~ Static fields/initializers --------------------------------------------------------------

    /** CHAR_PATH_SEPARATOR. */
    public static final char CHAR_PATH_SEPARATOR = '/';

    /** STRING_PATH_SEPARATOR. */
    public static final String STRING_PATH_SEPARATOR = "/";

    /** DATE_FORMAT. */
    private static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-SSS";

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusFileUtils} class.
     */
    private FileUtils() {

        super();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Delete file or directory. If directory has some files, those files are deleted as well, before
     * the directory is deleted.
     *
     * @param file
     *            File or directory to delete.
     *
     * @return {@code True} if the file could be deleted otherwise {@code false}.
     *
     * @see {@link File#delete()}
     */
    public static final boolean delete(final File file) {

        if (null == file) {

            return false;
        }

        if (!file.exists()) {

            // a file which does not exist was successfully deleted
            return true;
        }

        if (file.isFile()) {

            return file.delete();
        } else if (file.isDirectory()) {

            final File[] files = file.listFiles();

            if ((null != files) && (files.length > 0)) {

                for (final File tfile : files) {

                    // use recursive to delete as well child directories
                    if (!delete(tfile)) {

                        // be sure that really all files are deleted
                        return false;
                    }
                }
            }

            // if all file are deleted, delete directory
            return file.delete();
        }

        return false;
    }

    /**
     * Read all files from the given directory folder.
     *
     * @param folder
     *            Directory folder where to read files from.
     * @param fileNameRegEx
     *            If given the file name of a file must match this regular expression.
     *
     * @return List of files found in the given folder. In case there are some error, no files found
     *         the list might be {@code empty} but never {@code null}.
     */
    public static final List<File> readFiles(final File folder, final String fileNameRegEx) {

        final List<File> result = new ArrayList<File>();

        if (!isValidDirectory(folder)) {

            return result;
        }

        FilenameFilter filter = null;

        if ((null != fileNameRegEx) && (!fileNameRegEx.isEmpty())) {

            filter = new FilenameFilter() {

                public boolean accept(final File dir, final String name) {

                    if ((null != name) && (!name.isEmpty())) {

                        return name.matches(fileNameRegEx);
                    }

                    return false;
                }
            };
        }

        final File[] files = folder.listFiles(filter);

        if ((null != files) && (files.length > 0)) {

            for (final File file : files) {

                result.add(file);
            }
        }

        return result;
    }

    /**
     * Handle the creation / existence of a directory. If this directory does not exist, create this
     * directory. Validate that the directory name is not null and not empty, check if the directory
     * exists, that it is a directory and that it is writable.
     *
     * @param directory
     *            Name of directory.
     *
     * @return Newly created file folder.
     *
     * @throws IOException
     *             Thrown in case that directory name is invalid, that directory could not be created,
     *             that directory is not of type directory or directory is not writable.
     */
    public static final File createDirectory(final String directory) throws IOException {

        if ((null == directory) || (directory.isEmpty())) {

            throw new IOException("Invalid directory name [ " + directory + " ]");
        }

        final File folder = new File(directory);

        // make sure directory exists
        if (!folder.exists()) {

            // see if directory could be created
            if (!folder.mkdirs()) {

                throw new IOException("Could not create directory [ " + folder.getAbsolutePath()
                        + " ]");
            }
        }

        if (!folder.isDirectory()) {

            throw new IOException("Directory [ " + folder.getAbsolutePath()
                    + " ] is not of type [ directory ]");
        }

        if (!folder.canWrite()) {

            throw new IOException("Directory [ " + folder.getAbsolutePath() + " ] is not writable");
        }

        return folder;
    }

    /**
     * Check if directory is readable. Validate that the directory name is not null and not empty,
     * check if the directory exists, that it is a directory and that it is readable.
     *
     * @param directory
     *            Name of directory.
     *
     * @return Newly readable directory file folder.
     *
     * @throws IOException
     *             Thrown in case that directory name is invalid, that directory does not exist, that
     *             directory is not of type directory or directory is not writable.
     */
    public static final File readDirectory(final String directory) throws IOException {

        if ((null == directory) || (directory.isEmpty())) {

            throw new IOException("Invalid directory name [ " + directory + " ]");
        }

        final File folder = new File(directory);

        // make sure directory exists
        if (!folder.exists()) {

            throw new IOException("Directory [ " + folder.getAbsolutePath() + " ] does not exist!");
        }

        if (!folder.isDirectory()) {

            throw new IOException("Directory [ " + folder.getAbsolutePath()
                    + " ] is not of type [ directory ]");
        }

        if (!folder.canRead()) {

            throw new IOException("Directory [ " + folder.getAbsolutePath() + " ] is not readable");
        }

        return folder;
    }

    /**
     * Convert date value to a string, using the format {@value #DATE_FORMAT}. This string is made up
     * in such a way that it could be used as part of a filename.
     *
     * @param date
     *            Date to be converted.
     *
     * @return Date converted to a string.
     */
    public static final String dateAsString(final Date date) {

        if (null == date) {

            return null;
        }

        // SimpleDateFormat seems not to be thread safe,
        // assure to use a new instance for each formatting
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        return sdf.format(date);
    }

    /**
     * Verify if file given by this file name exists, is a file, can be read and has some content.
     *
     * @param fileName
     *            Name of file.
     *
     * @return {@code True} if file name is not empty, file exists, is a file, can be read and has a
     *         length greater zero.
     */
    public static final boolean isValidFile(final String fileName) {

        // make sure file name is not null or empty
        if ((null == fileName) || (fileName.isEmpty())) {

            // there was no file name, so return false
            return false;
        }

        // criteria for a valid file
        return isValidFile(new File(fileName));
    }

    /**
     * Verify if file given by this file name exists, is a file, can be read and has some content.
     *
     * @param file
     *            Name of file.
     *
     * @return {@code True} if file name is not empty, file exists, is a file, can be read and has a
     *         length greater zero.
     */
    public static final boolean isValidFile(final File file) {

        // criteria for a valid file
        return ((null != file) && file.exists() && file.isFile() && file.canRead() && (file.length() > 0L));
    }

    /**
     * Validates if folder is not null, exists and is a directory.
     *
     * @param folder
     *            File folder to validate.
     *
     * @return {@code True} if directory is valid otherwise {@code false}.
     */
    public static final boolean isValidDirectory(final File folder) {

        return ((null != folder) && (folder.exists()) && (folder.isDirectory()));
    }

    /**
     * Get the absolute file name of the file. Makes sure the file is valid.
     *
     * @param fileName
     *            File name to get the absolute path for.
     *
     * @return {@code Absolute path} for the given file or {@code null} if the file name is not valid
     *         or the file is not valid.
     */
    public static final String getAbsolutePath(final String fileName) {

        if ((null == fileName) || (fileName.isEmpty())) {

            return null;
        }

        final File file = new File(fileName);

        // return absolute file path
        return file.getAbsolutePath();
    }

    /**
     * Close any object of instance {@link Closeable}.
     *
     * @param obj
     *            Instance of closeable.
     */
    public static final void close(final Object obj) {

        if (obj instanceof Closeable) {

            try {

                ((Closeable) obj).close();
            } catch (final Exception ex) {

                // ignore exception
            }
        }
    }

}
