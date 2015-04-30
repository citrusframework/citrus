/*
 * Copyright 2006-2010 the original author or authors.
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
 */

package com.consol.citrus;

import com.consol.citrus.message.MessageType;

import java.io.File;

/**
 * Constants used throughout Citrus.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public final class CitrusConstants {

    /** Default variable names */
    public static final String TEST_NAME_VARIABLE = "citrus.test.name";
    public static final String TEST_PACKAGE_VARIABLE = "citrus.test.package";

    /** File encoding system property */
    public static final String CITRUS_FILE_ENCODING = "citrus.file.encoding";

    /** Prefix/sufix used to identify variable expressions */
    public static final String VARIABLE_PREFIX = "${";
    public static final char VARIABLE_SUFFIX = '}';

    /** Default application context name */
    public static final String DEFAULT_APPLICATION_CONTEXT = "citrus-context.xml";

    /** Default test directories */
    public static final String DEFAULT_JAVA_DIRECTORY = "src" + File.separator + "citrus" + File.separator + "java" + File.separator;
    public static final String DEFAULT_TEST_DIRECTORY = "src" + File.separator + "citrus" + File.separator + "tests" + File.separator;

    /** Placeholder used in messages to ignore elements */
    public static final String IGNORE_PLACEHOLDER = "@ignore@";

    /** Prefix/suffix used to identify validation matchers */
    public static final String VALIDATION_MATCHER_PREFIX = "@";
    public static final String VALIDATION_MATCHER_SUFFIX = "@";

    /** Default message type used in message validation mechanism */
    public static final String DEFAULT_MESSAGE_TYPE = MessageType.XML.toString();

    /**
     * Prevent instantiation.
     */
    private CitrusConstants() {
    }
}
