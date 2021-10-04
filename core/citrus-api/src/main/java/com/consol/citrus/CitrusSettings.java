package com.consol.citrus;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.consol.citrus.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public final class CitrusSettings {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CitrusSettings.class);

    private CitrusSettings() {
        // prevent instantiation
    }

    /** Optional application property file */
    private static final String APPLICATION_PROPERTY_FILE_PROPERTY = "citrus.application.properties";
    private static final String APPLICATION_PROPERTY_FILE_ENV = "CITRUS_APPLICATION_PROPERTIES";
    private static final String APPLICATION_PROPERTY_FILE = System.getProperty(APPLICATION_PROPERTY_FILE_PROPERTY, System.getenv(APPLICATION_PROPERTY_FILE_ENV) != null ?
            System.getenv(APPLICATION_PROPERTY_FILE_ENV) : "classpath:citrus-application.properties");

    /* Load application properties */
    static {
        Resource appPropertiesResource = new PathMatchingResourcePatternResolver().getResource(APPLICATION_PROPERTY_FILE);
        if (appPropertiesResource.exists()) {
            try (final InputStream in = appPropertiesResource.getInputStream()) {
                Properties applicationProperties = new Properties();
                applicationProperties.load(in);

                LOG.debug("Loading Citrus application properties");

                for (Map.Entry<Object, Object> property : applicationProperties.entrySet()) {
                    if (StringUtils.isEmpty(System.getProperty(property.getKey().toString()))) {
                        LOG.debug(String.format("Setting application property %s=%s", property.getKey(), property.getValue()));
                        System.setProperty(property.getKey().toString(), property.getValue().toString());
                    }
                }
            } catch (Exception e) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Unable to locate Citrus application properties", e);
                } else {
                    LOG.info("Unable to locate Citrus application properties");
                }
            }
        }
    }

    /** Default variable names */
    public static final String TEST_NAME_VARIABLE_PROPERTY = "citrus.test.name.variable";
    public static final String TEST_NAME_VARIABLE_ENV = "CITRUS_TEST_NAME_VARIABLE";
    public static final String TEST_NAME_VARIABLE = System.getProperty(TEST_NAME_VARIABLE_PROPERTY, System.getenv(TEST_NAME_VARIABLE_ENV) != null ?
            System.getenv(TEST_NAME_VARIABLE_ENV) : "citrus.test.name");

    public static final String TEST_PACKAGE_VARIABLE_PROPERTY = "citrus.test.package.variable";
    public static final String TEST_PACKAGE_VARIABLE_ENV = "CITRUS_TEST_PACKAGE_VARIABLE";
    public static final String TEST_PACKAGE_VARIABLE = System.getProperty(TEST_PACKAGE_VARIABLE_PROPERTY, System.getenv(TEST_PACKAGE_VARIABLE_ENV) != null ?
            System.getenv(TEST_PACKAGE_VARIABLE_ENV) : "citrus.test.package");

    /** File encoding system property */
    public static final String CITRUS_FILE_ENCODING_PROPERTY = "citrus.file.encoding";
    public static final String CITRUS_FILE_ENCODING_ENV = "CITRUS_FILE_ENCODING";
    public static final String CITRUS_FILE_ENCODING = System.getProperty(CITRUS_FILE_ENCODING_PROPERTY, System.getenv(CITRUS_FILE_ENCODING_ENV) != null ?
            System.getenv(CITRUS_FILE_ENCODING_ENV) : Charset.defaultCharset().displayName());

    /** Prefix/sufix used to identify variable expressions */
    public static final String VARIABLE_PREFIX = "${";
    public static final String VARIABLE_SUFFIX = "}";
    public static final String VARIABLE_ESCAPE = "//";

    /** Default application context class */
    public static final String DEFAULT_CONFIG_CLASS_PROPERTY = "citrus.java.config";
    public static final String DEFAULT_CONFIG_CLASS_ENV = "CITRUS_JAVA_CONFIG";
    public static final String DEFAULT_CONFIG_CLASS = System.getProperty(DEFAULT_CONFIG_CLASS_PROPERTY, System.getenv(DEFAULT_CONFIG_CLASS_ENV));

    /** Default test directories */
    public static final String DEFAULT_TEST_SRC_DIRECTORY_PROPERTY = "citrus.default.src.directory";
    public static final String DEFAULT_TEST_SRC_DIRECTORY_ENV = "CITRUS_DEFAULT_SRC_DIRECTORY";
    public static final String DEFAULT_TEST_SRC_DIRECTORY = System.getProperty(DEFAULT_TEST_SRC_DIRECTORY_PROPERTY, System.getenv(DEFAULT_TEST_SRC_DIRECTORY_ENV) != null ?
            System.getenv(DEFAULT_TEST_SRC_DIRECTORY_ENV) : "src" + File.separator + "test" + File.separator);

    /** Placeholder used in messages to ignore elements */
    public static final String IGNORE_PLACEHOLDER = "@ignore@";

    /** Prefix/suffix used to identify validation matchers */
    public static final String VALIDATION_MATCHER_PREFIX = "@";
    public static final String VALIDATION_MATCHER_SUFFIX = "@";

    public static final String XML_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.xml.file.name.pattern";
    public static final String XML_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_XML_FILE_NAME_PATTERN";
    public static final String XML_TEST_FILE_NAME_PATTERN = System.getProperty(XML_TEST_FILE_NAME_PATTERN_PROPERTY, System.getenv(XML_TEST_FILE_NAME_PATTERN_ENV) != null ?
            System.getenv(XML_TEST_FILE_NAME_PATTERN_ENV) : "/**/*Test.xml,/**/*IT.xml");

    public static final String JAVA_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.java.file.name.pattern";
    public static final String JAVA_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_JAVA_FILE_NAME_PATTERN";
    public static final String JAVA_TEST_FILE_NAME_PATTERN = System.getProperty(JAVA_TEST_FILE_NAME_PATTERN_PROPERTY, System.getenv(JAVA_TEST_FILE_NAME_PATTERN_ENV) != null ?
            System.getenv(JAVA_TEST_FILE_NAME_PATTERN_ENV) : "/**/*Test.java,/**/*IT.java");

    /** Default message type used in message validation mechanism */
    public static final String DEFAULT_MESSAGE_TYPE_PROPERTY = "citrus.default.message.type";
    public static final String DEFAULT_MESSAGE_TYPE_ENV = "CITRUS_DEFAULT_MESSAGE_TYPE";
    public static final String DEFAULT_MESSAGE_TYPE = System.getProperty(DEFAULT_MESSAGE_TYPE_PROPERTY,  System.getenv(DEFAULT_MESSAGE_TYPE_ENV) != null ?
            System.getenv(DEFAULT_MESSAGE_TYPE_ENV) : MessageType.XML.toString());

    /** Default message trace output directory */
    public static final String MESSAGE_TRACE_DIRECTORY_PROPERTY = "citrus.message.trace.directory";
    public static final String MESSAGE_TRACE_DIRECTORY_ENV = "CITRUS_MESSAGE_TRACE_DIRECTORY";
    public static final String MESSAGE_TRACE_DIRECTORY_DEFAULT = "target/citrus-logs/trace/messages";

    /** Default type converter */
    public static final String TYPE_CONVERTER_PROPERTY = "citrus.type.converter";
    public static final String TYPE_CONVERTER_ENV = "CITRUS_TYPE_CONVERTER";
    public static final String TYPE_CONVERTER_DEFAULT = "default";

    /** Flag to enable/disable log modifier */
    public static final String LOG_MODIFIER_PROPERTY = "citrus.log.modifier";
    public static final String LOG_MODIFIER_ENV = "CITRUS_LOG_MODIFIER";
    public static final String LOG_MODIFIER_DEFAULT = Boolean.TRUE.toString();

    /** Default log modifier mask value */
    public static final String LOG_MASK_VALUE_PROPERTY = "citrus.log.mask.value";
    public static final String LOG_MASK_VALUE_ENV = "CITRUS_LOG_MASK_VALUE";
    public static final String LOG_MASK_VALUE_DEFAULT = "****";

    /** Default log modifier keywords */
    public static final String LOG_MASK_KEYWORDS_PROPERTY = "citrus.log.mask.keywords";
    public static final String LOG_MASK_KEYWORDS_ENV = "CITRUS_LOG_MASK_KEYWORDS";
    public static final String LOG_MASK_KEYWORDS_DEFAULT = "password,secret,secretKey";

    /**
     * Gets set of file name patterns for XML test files.
     * @return
     */
    public static Set<String> getXmlTestFileNamePattern() {
        return StringUtils.commaDelimitedListToSet(XML_TEST_FILE_NAME_PATTERN);
    }

    /**
     * Gets set of file name patterns for Java test files.
     * @return
     */
    public static Set<String> getJavaTestFileNamePattern() {
        return StringUtils.commaDelimitedListToSet(JAVA_TEST_FILE_NAME_PATTERN);
    }

    /**
     * Gets the directory where to put message trace files.
     * @return
     */
    public static String getMessageTraceDirectory() {
        return System.getProperty(MESSAGE_TRACE_DIRECTORY_PROPERTY,  System.getenv(MESSAGE_TRACE_DIRECTORY_ENV) != null ?
                System.getenv(MESSAGE_TRACE_DIRECTORY_ENV) : MESSAGE_TRACE_DIRECTORY_DEFAULT);
    }

    /**
     * Gets the type converter to use by default.
     * @return
     */
    public static String getTypeConverter() {
        return System.getProperty(TYPE_CONVERTER_PROPERTY,  System.getenv(TYPE_CONVERTER_ENV) != null ?
                System.getenv(TYPE_CONVERTER_ENV) : TYPE_CONVERTER_DEFAULT);
    }

    /**
     * Gets the log modifier enabled/disabled setting.
     * @return
     */
    public static boolean isLogModifierEnabled() {
        return Boolean.parseBoolean(System.getProperty(LOG_MODIFIER_PROPERTY,  System.getenv(LOG_MODIFIER_ENV) != null ?
                System.getenv(LOG_MODIFIER_ENV) : LOG_MODIFIER_DEFAULT));
    }

    /**
     * Get log mask value.
     * @return
     */
    public static String getLogMaskValue() {
        return System.getProperty(LOG_MASK_VALUE_PROPERTY,  System.getenv(LOG_MASK_VALUE_ENV) != null ?
                System.getenv(LOG_MASK_VALUE_ENV) : LOG_MASK_VALUE_DEFAULT);
    }

    /**
     * Get log mask keywords.
     * @return
     */
    public static Set<String> getLogMaskKeywords() {
        return Stream.of(System.getProperty(LOG_MASK_KEYWORDS_PROPERTY,  System.getenv(LOG_MASK_KEYWORDS_ENV) != null ?
                System.getenv(LOG_MASK_KEYWORDS_ENV) : LOG_MASK_KEYWORDS_DEFAULT).split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
    }
}
