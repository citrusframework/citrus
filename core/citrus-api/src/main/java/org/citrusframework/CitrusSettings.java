package org.citrusframework;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.citrusframework.common.TestLoader;
import org.citrusframework.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public final class CitrusSettings {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusSettings.class);

    private CitrusSettings() {
        // prevent instantiation
    }

    /** Optional application property file */
    private static final String APPLICATION_PROPERTY_FILE_PROPERTY = "citrus.application.properties";
    private static final String APPLICATION_PROPERTY_FILE_ENV = "CITRUS_APPLICATION_PROPERTIES";
    private static final String APPLICATION_PROPERTY_FILE = System.getProperty(APPLICATION_PROPERTY_FILE_PROPERTY, System.getenv(APPLICATION_PROPERTY_FILE_ENV) != null ?
            System.getenv(APPLICATION_PROPERTY_FILE_ENV) : "citrus-application.properties");

    public  static final String OUTBOUND_SCHEMA_VALIDATION_ENABLED_PROPERTY = "citrus.validation.outbound.schema.enabled";
    public static final String OUTBOUND_SCHEMA_VALIDATION_ENABLED_ENV = "CITRUS_VALIDATION_OUTBOUND_SCHEMA_ENABLED";

    public  static final String OUTBOUND_JSON_SCHEMA_VALIDATION_ENABLED_PROPERTY = "citrus.validation.outbound.json.schema.enabled";
    public static final String OUTBOUND_JSON_SCHEMA_VALIDATION_ENABLED_ENV = "CITRUS_VALIDATION_OUTBOUND_JSON_SCHEMA_ENABLED";

    public  static final String OUTBOUND_XML_SCHEMA_VALIDATION_ENABLED_PROPERTY = "citrus.validation.outbound.xml.schema.enabled";
    public static final String OUTBOUND_XML_SCHEMA_VALIDATION_ENABLED_ENV = "CITRUS_VALIDATION_OUTBOUND_XML_SCHEMA_ENABLED";

    /* Load application properties */
    static {
        String applicationPropertiesFile = APPLICATION_PROPERTY_FILE;
        if (applicationPropertiesFile.startsWith("classpath:")) {
            applicationPropertiesFile = applicationPropertiesFile.substring("classpath:".length());
        }

        try (final InputStream in = CitrusSettings.class.getClassLoader().getResourceAsStream(applicationPropertiesFile)) {
            Properties applicationProperties = new Properties();
            applicationProperties.load(in);

            logger.debug("Loading Citrus application properties");

            for (Map.Entry<Object, Object> property : applicationProperties.entrySet()) {
                if (System.getProperty(property.getKey().toString(), "").isEmpty()) {
                    logger.debug(String.format("Setting application property %s=%s", property.getKey(), property.getValue()));
                    System.setProperty(property.getKey().toString(), property.getValue().toString());
                }
            }
        } catch (Exception e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Unable to locate Citrus application properties", e);
            } else {
                logger.debug("Unable to locate Citrus application properties");
            }
        }

        try (InputStream is = CitrusSettings.class.getClassLoader().getResourceAsStream("logging.properties")) {
            if (is != null) {
                LogManager.getLogManager().readConfiguration(is);
            }
        } catch (Exception e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Unable to configure Java util logging", e);
            } else {
                logger.info("Unable to configure Java util logging");
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

    public static final String GROOVY_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.groovy.file.name.pattern";
    public static final String GROOVY_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_GROOVY_FILE_NAME_PATTERN";
    public static final String GROOVY_TEST_FILE_NAME_PATTERN = System.getProperty(GROOVY_TEST_FILE_NAME_PATTERN_PROPERTY, System.getenv(GROOVY_TEST_FILE_NAME_PATTERN_ENV) != null ?
            System.getenv(GROOVY_TEST_FILE_NAME_PATTERN_ENV) : ".*test\\.groovy,.*it\\.groovy");

    public static final String YAML_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.yaml.file.name.pattern";
    public static final String YAML_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_YAML_FILE_NAME_PATTERN";
    public static final String YAML_TEST_FILE_NAME_PATTERN = System.getProperty(YAML_TEST_FILE_NAME_PATTERN_PROPERTY, System.getenv(YAML_TEST_FILE_NAME_PATTERN_ENV) != null ?
            System.getenv(YAML_TEST_FILE_NAME_PATTERN_ENV) : ".*test\\.yaml,.*it\\.yaml");

    public static final String XML_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.xml.file.name.pattern";
    public static final String XML_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_XML_FILE_NAME_PATTERN";
    public static final String XML_TEST_FILE_NAME_PATTERN = System.getProperty(XML_TEST_FILE_NAME_PATTERN_PROPERTY, System.getenv(XML_TEST_FILE_NAME_PATTERN_ENV) != null ?
            System.getenv(XML_TEST_FILE_NAME_PATTERN_ENV) : ".*Test\\.xml,.*IT\\.xml,.*test\\.xml,.*it\\.xml");

    public static final String JAVA_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.java.file.name.pattern";
    public static final String JAVA_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_JAVA_FILE_NAME_PATTERN";
    public static final String JAVA_TEST_FILE_NAME_PATTERN = System.getProperty(JAVA_TEST_FILE_NAME_PATTERN_PROPERTY, System.getenv(JAVA_TEST_FILE_NAME_PATTERN_ENV) != null ?
            System.getenv(JAVA_TEST_FILE_NAME_PATTERN_ENV) : ".*Test\\.java,.*IT\\.java");

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

    /** Flag to enable/disable message pretty print */
    public static final String PRETTY_PRINT_PROPERTY = "citrus.message.pretty.print";
    public static final String PRETTY_PRINT_ENV = "CITRUS_MESSAGE_PRETTY_PRINT";
    public static final String PRETTY_PRINT_DEFAULT = Boolean.TRUE.toString();

    /** Flag to enable/disable logger modifier */
    public static final String LOG_MODIFIER_PROPERTY = "citrus.logger.modifier";
    public static final String LOG_MODIFIER_ENV = "CITRUS_LOG_MODIFIER";
    public static final String LOG_MODIFIER_DEFAULT = Boolean.TRUE.toString();

    /** Default logger modifier mask value */
    public static final String LOG_MASK_VALUE_PROPERTY = "citrus.logger.mask.value";
    public static final String LOG_MASK_VALUE_ENV = "CITRUS_LOG_MASK_VALUE";
    public static final String LOG_MASK_VALUE_DEFAULT = "****";

    /** Default logger modifier keywords */
    public static final String LOG_MASK_KEYWORDS_PROPERTY = "citrus.logger.mask.keywords";
    public static final String LOG_MASK_KEYWORDS_ENV = "CITRUS_LOG_MASK_KEYWORDS";
    public static final String LOG_MASK_KEYWORDS_DEFAULT = "password,secret,secretKey";

    /** File path charset parameter */
    public static final String FILE_PATH_CHARSET_PARAMETER_PROPERTY = "citrus.file.path.charset.parameter";
    public static final String FILE_PATH_CHARSET_PARAMETER_ENV = "CITRUS_FILE_PATH_CHARSET_PARAMETER";
    public static final String FILE_PATH_CHARSET_PARAMETER_DEFAULT = "; charset=";

    /**
     * Gets set of file name patterns for Groovy test files.
     * @return
     */
    public static Set<String> getGroovyTestFileNamePattern() {
        return Stream.of(GROOVY_TEST_FILE_NAME_PATTERN.split(",")).collect(Collectors.toSet());
    }

    /**
     * Gets set of file name patterns for YAML test files.
     * @return
     */
    public static Set<String> getYamlTestFileNamePattern() {
        return Stream.of(YAML_TEST_FILE_NAME_PATTERN.split(",")).collect(Collectors.toSet());
    }

    /**
     * Gets set of file name patterns for XML test files.
     * @return
     */
    public static Set<String> getXmlTestFileNamePattern() {
        return Stream.of(XML_TEST_FILE_NAME_PATTERN.split(",")).collect(Collectors.toSet());
    }

    /**
     * Gets set of file name patterns for Java test files.
     * @return
     */
    public static Set<String> getJavaTestFileNamePattern() {
        return Stream.of(JAVA_TEST_FILE_NAME_PATTERN.split(",")).collect(Collectors.toSet());
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
     * Gets the message payload pretty print enabled/disabled setting.
     * @return
     */
    public static boolean isPrettyPrintEnabled() {
        return Boolean.parseBoolean(System.getProperty(PRETTY_PRINT_PROPERTY,  System.getenv(PRETTY_PRINT_ENV) != null ?
                System.getenv(PRETTY_PRINT_ENV) : PRETTY_PRINT_DEFAULT));
    }

    /**
     * Gets the logger modifier enabled/disabled setting.
     * @return
     */
    public static boolean isLogModifierEnabled() {
        return Boolean.parseBoolean(System.getProperty(LOG_MODIFIER_PROPERTY,  System.getenv(LOG_MODIFIER_ENV) != null ?
                System.getenv(LOG_MODIFIER_ENV) : LOG_MODIFIER_DEFAULT));
    }

    /**
     * Get logger mask value.
     * @return
     */
    public static String getLogMaskValue() {
        return System.getProperty(LOG_MASK_VALUE_PROPERTY,  System.getenv(LOG_MASK_VALUE_ENV) != null ?
                System.getenv(LOG_MASK_VALUE_ENV) : LOG_MASK_VALUE_DEFAULT);
    }

    /**
     * Get the file path charset parameter.
     * @return
     */
    public static String getFilePathCharsetParameter() {
        return System.getProperty(FILE_PATH_CHARSET_PARAMETER_PROPERTY,  System.getenv(FILE_PATH_CHARSET_PARAMETER_ENV) != null ?
                System.getenv(FILE_PATH_CHARSET_PARAMETER_ENV) : FILE_PATH_CHARSET_PARAMETER_DEFAULT);
    }

    /**
     * Get logger mask keywords.
     * @return
     */
    public static Set<String> getLogMaskKeywords() {
        return Stream.of(System.getProperty(LOG_MASK_KEYWORDS_PROPERTY,  System.getenv(LOG_MASK_KEYWORDS_ENV) != null ?
                System.getenv(LOG_MASK_KEYWORDS_ENV) : LOG_MASK_KEYWORDS_DEFAULT).split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
    }

    /**
     * Gets the test file name pattern for given type or empty patterns for unknown type.
     * @param type
     * @return
     */
    public static Set<String> getTestFileNamePattern(String type) {
        switch (type) {
            case TestLoader.XML:
            case TestLoader.SPRING:
                return CitrusSettings.getXmlTestFileNamePattern();
            case TestLoader.GROOVY:
                return CitrusSettings.getGroovyTestFileNamePattern();
            case TestLoader.YAML:
                return CitrusSettings.getYamlTestFileNamePattern();
            default:
                return Collections.emptySet();
        }
    }
}
