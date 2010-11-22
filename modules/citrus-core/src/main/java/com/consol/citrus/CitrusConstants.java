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

/**
 * Constants used throughout Citrus.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class CitrusConstants {
    /** Prefix/sufix used to identify variable expressions */
    public static final String VARIABLE_PREFIX = "${";
    public static final char VARIABLE_SUFFIX = '}';

    /** Search wildcard */
    public static final String SEARCH_WILDCARD = "*";

    /** Default application context name */
    public static final String DEFAULT_APPLICATIONCONTEXT = "citrus-context.xml";

    /** Default test directories */
    public static final String DEFAULT_JAVA_DIRECTORY = "src/citrus/java/";
    public static final String DEFAULT_TEST_DIRECTORY = "src/citrus/tests/";
    
    /** Default test suite name */
    public static final String DEFAULT_SUITE_NAME = "citrus-default-testsuite";
    
    /** Default test name */
    public static final String DEFAULT_TEST_NAME = "citrus-test";
    
    /** Placeholder used in messages to ignore elements */
    public static final String IGNORE_PLACEHOLDER = "@ignore@";
}
