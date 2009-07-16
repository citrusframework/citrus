package com.consol.citrus;

/**
 * Class defining some constants used in the test suite
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class TestConstants {
    /** TestCase file extension */
    public static final String TESTCASE_FILE_EXTENSION = "xml";

    /** Prefix/sufix used to identify variable names */
    public static final String VARIABLE_PREFIX = "${";
    public static final char VARIABLE_SUFFIX = '}';

    public static final int CONST_NOTKNOWN = -1;
    public static final int CONST_REPLY_TO_QUEUE = 1;
    public static final int CUSTOM_FUNCTION = 2;

    /** Predefined variable name for a default queue to reply to. */
    public static final String REPLY_TO_QUEUE = "REPLY_TO_QUEUE";
    public static final String DEFAULT_QUEUE = "DefaultQUEUE";

    /** Default value for success in web services */
    public static final String RETURN_CODE_SUCCESS = "0";

    /** Search wildcard */
    public static final String SEARCH_WILDCARD = "*";

    public static final String NAMESPACE_WILDCARD = "%{ns}";

    public static final String DEFAULT_APPLICATIONCONTEXT = "application-ctx.xml";

    public static final String DEFAULT_TEST_DIRECTORY = "tests";
}
