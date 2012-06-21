/*
 * File: CitrusFileEnum.java
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
 * last modified: Saturday, January 21, 2012 (17:15) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import com.consol.citrus.CitrusConstants;


/**
 * Enumeration defining the default file values for the different kind of files used by CITRUS.
 *
 * @author  Matthias Beil
 * @since   CITRUS 1.2 M2
 */
public enum CitrusFileEnum {

    /** Define JAVA default values. */
    JAVA(CitrusConstants.DEFAULT_JAVA_DIRECTORY, ".java"),

    /** Define TEST default values. */
    TEST(CitrusConstants.DEFAULT_TEST_DIRECTORY, ".xml");

    /**
     * Returns the value of the {@code path} field.
     *
     * @return  {@code path} field.
     */
    public String getPath() {

        return this.path;
    }

    /**
     * Returns the value of the {@code extension} field.
     *
     * @return  {@code extension} field.
     */
    public String getExtension() {

        return this.extension;
    }

    /** path. */
    private final String path;

    /** extension. */
    private final String extension;

    /**
     * Constructor for {@code CitrusFileEnum} class.
     *
     * @param  pathIn       Default path.
     * @param  extensionIn  Default extension.
     */
    private CitrusFileEnum(final String pathIn, final String extensionIn) {

        this.path = pathIn;
        this.extension = extensionIn;
    }

}
