/*
 * File: CitrusFileUtils.java
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
 * last modified: Monday, January 2, 2012 (17:39) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import java.io.File;

/**
 * Utility class for CITRUS file static methods.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class CitrusFileUtils {

    // ~ Static fields/initializers ------------------------------------------------------------------------------------

    /** CHAR_PATH_SEPARATOR. */
    public static final char CHAR_PATH_SEPARATOR = '/';

    /** STRING_PATH_SEPARATOR. */
    public static final String STRING_PATH_SEPARATOR = "/";

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusFileUtils} class.
     */
    private CitrusFileUtils() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * Build file name for given file enumeration. This method is missing in the CITRUS core, so an implementation is
     * needed here. Manually watch that this implementation does not differentiate from the CITRUS core implementation.
     *
     * @param fileEnum
     *            Type of file name to generate.
     * @param bean
     *            CITRUS test case bean holding the name and target package of the file.
     *
     * @return Newly created file name depending on file type, test case name and target package.
     */
    public static final String buildFileName(final CitrusFileEnum fileEnum, final CitrusBean bean) {

        final StringBuilder builder = new StringBuilder(fileEnum.getPath());
        builder.append(bean.getTargetPackage().replace('.', CitrusFileUtils.CHAR_PATH_SEPARATOR));
        builder.append(CitrusFileUtils.STRING_PATH_SEPARATOR);
        builder.append(bean.getName());
        builder.append(fileEnum.getExtension());

        return builder.toString();
    }

    /**
     * Verify if file given by this file name exists, is a file, can be read and has some content.
     *
     * @param fileName
     *            Name of file.
     *
     * @return {@code True} if file name is not empty, file exists, is a file, can be read and has a length greater
     *         zero.
     */
    public static final boolean isValidFile(final String fileName) {

        // make sure file name is not null or empty
        if ((null != fileName) && (!fileName.isEmpty())) {

            final File file = new File(fileName);

            // criteria for a valid file
            return (file.exists() && file.isFile() && file.canRead() && (file.length() > 0L));
        }

        // there was no file name, so return null
        return false;
    }

    /**
     * Get the absolute file name of the file. Makes sure the file is valid.
     *
     * @param fileName
     *            File name to get the absolute path for.
     *
     * @return {@code Absolute path} for the given file or {@code null} if the file name is not valid or the file is not
     *         valid.
     */
    public static final String getAbsolutePath(final String fileName) {

        // make sure file name and file are valid
        if (CitrusFileUtils.isValidFile(fileName)) {

            final File file = new File(fileName);

            // return absolute file path
            return file.getAbsolutePath();
        }

        // either file name or file are not valid, return null
        return null;
    }

}
