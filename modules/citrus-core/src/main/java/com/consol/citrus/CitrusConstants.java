package com.consol.citrus;

/**
 * Class defining some constants used in the test suite
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class CitrusConstants {
    /** Prefix/sufix used to identify variable names */
    public static final String VARIABLE_PREFIX = "${";
    public static final char VARIABLE_SUFFIX = '}';

    /** Search wildcard */
    public static final String SEARCH_WILDCARD = "*";

    public static final String DEFAULT_APPLICATIONCONTEXT = "citrus-context.xml";

    public static final String DEFAULT_JAVA_DIRECTORY = "src/citrus/java/";
    public static final String DEFAULT_TEST_DIRECTORY = "src/citrus/tests/";
    
    public static final String DEFAULT_SUITE_NAME = "citrus-default-testsuite";
    public static final String DEFAULT_TEST_NAME = "citrus-test";
}
