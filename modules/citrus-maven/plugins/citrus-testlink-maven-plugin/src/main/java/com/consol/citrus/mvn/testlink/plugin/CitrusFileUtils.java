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
 * last modified: Thursday, May 17, 2012 (12:11) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import com.consol.citrus.testlink.utils.FileUtils;

/**
 * Utility class for CITRUS file static methods.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class CitrusFileUtils {

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusFileUtils} class.
     */
    private CitrusFileUtils() {

        super();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Build file name for given file enumeration. This method is missing in the CITRUS core, so an
     * implementation is needed here. Manually watch that this implementation does not differentiate
     * from the CITRUS core implementation.
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
        builder.append(bean.getTargetPackage().replace('.', FileUtils.CHAR_PATH_SEPARATOR));
        builder.append(FileUtils.STRING_PATH_SEPARATOR);
        builder.append(bean.getName());
        builder.append(fileEnum.getExtension());

        return builder.toString();
    }

}
