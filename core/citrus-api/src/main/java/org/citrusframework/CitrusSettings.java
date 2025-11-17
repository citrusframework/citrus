/*
 * Copyright the original author or authors.
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

package org.citrusframework;

import org.citrusframework.common.TestLoader;
import org.citrusframework.validation.CustomValidatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.lang.System.setProperty;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.emptySet;
import static java.util.logging.LogManager.getLogManager;
import static java.util.stream.Collectors.toSet;
import static org.citrusframework.common.TestLoader.GROOVY;
import static org.citrusframework.common.TestLoader.JAVA;
import static org.citrusframework.common.TestLoader.SPRING;
import static org.citrusframework.common.TestLoader.YAML;
import static org.citrusframework.message.MessageType.XML;

public final class CitrusSettings {

    private static final Logger logger = LoggerFactory.getLogger(CitrusSettings.class);

    private CitrusSettings() {
        // prevent instantiation
    }

    /**
     * Optional application property file
     */
    private static final String APPLICATION_PROPERTY_FILE_PROPERTY = "citrus.application.properties";
    private static final String APPLICATION_PROPERTY_FILE_ENV = "CITRUS_APPLICATION_PROPERTIES";
    private static final String APPLICATION_PROPERTY_FILE = getProperty(
            APPLICATION_PROPERTY_FILE_PROPERTY,
            getenv(APPLICATION_PROPERTY_FILE_ENV) != null ? getenv(APPLICATION_PROPERTY_FILE_ENV) : "citrus-application.properties");

    public static final String OUTBOUND_SCHEMA_VALIDATION_ENABLED_PROPERTY = "citrus.validation.outbound.schema.enabled";
    public static final String OUTBOUND_SCHEMA_VALIDATION_ENABLED_ENV = "CITRUS_VALIDATION_OUTBOUND_SCHEMA_ENABLED";

    public static final String OUTBOUND_JSON_SCHEMA_VALIDATION_ENABLED_PROPERTY = "citrus.validation.outbound.json.schema.enabled";
    public static final String OUTBOUND_JSON_SCHEMA_VALIDATION_ENABLED_ENV = "CITRUS_VALIDATION_OUTBOUND_JSON_SCHEMA_ENABLED";

    public static final String OUTBOUND_XML_SCHEMA_VALIDATION_ENABLED_PROPERTY = "citrus.validation.outbound.xml.schema.enabled";
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
                if (getProperty(property.getKey().toString(), "").isEmpty()) {
                    logger.debug("Setting application property {}={}", property.getKey(), property.getValue());
                    setProperty(property.getKey().toString(), property.getValue().toString());
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
                getLogManager().readConfiguration(is);
            }
        } catch (Exception e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Unable to configure Java util logging", e);
            } else {
                logger.info("Unable to configure Java util logging");
            }
        }
    }

    /**
     * Default variable names
     */
    public static final String TEST_NAME_VARIABLE_PROPERTY = "citrus.test.name.variable";
    public static final String TEST_NAME_VARIABLE_ENV = "CITRUS_TEST_NAME_VARIABLE";
    public static final String TEST_NAME_VARIABLE = getPropertyEnvOrDefault(
            TEST_NAME_VARIABLE_PROPERTY,
            TEST_NAME_VARIABLE_ENV,
            "citrus.test.name");

    public static final String TEST_PACKAGE_VARIABLE_PROPERTY = "citrus.test.package.variable";
    public static final String TEST_PACKAGE_VARIABLE_ENV = "CITRUS_TEST_PACKAGE_VARIABLE";
    public static final String TEST_PACKAGE_VARIABLE = getPropertyEnvOrDefault(
            TEST_PACKAGE_VARIABLE_PROPERTY,
            TEST_PACKAGE_VARIABLE_ENV,
            "citrus.test.package");

    /**
     * File encoding system property
     */
    public static final String CITRUS_FILE_ENCODING_PROPERTY = "citrus.file.encoding";
    public static final String CITRUS_FILE_ENCODING_ENV = "CITRUS_FILE_ENCODING";
    public static final String CITRUS_FILE_ENCODING = getPropertyEnvOrDefault(
            CITRUS_FILE_ENCODING_PROPERTY,
            CITRUS_FILE_ENCODING_ENV,
            defaultCharset().displayName());

    /**
     * Prefix/sufix used to identify variable expressions
     */
    public static final String VARIABLE_PREFIX = "${";
    public static final String VARIABLE_SUFFIX = "}";
    public static final String VARIABLE_ESCAPE = "//";

    /**
     * Default application context class
     */
    public static final String DEFAULT_CONFIG_CLASS_PROPERTY = "citrus.java.config";
    public static final String DEFAULT_CONFIG_CLASS_ENV = "CITRUS_JAVA_CONFIG";
    public static final String DEFAULT_CONFIG_CLASS = getPropertyEnvOrDefault(
            DEFAULT_CONFIG_CLASS_PROPERTY,
            DEFAULT_CONFIG_CLASS_ENV,
            null);

    /**
     * Default test directories
     */
    public static final String DEFAULT_TEST_SRC_DIRECTORY_PROPERTY = "citrus.default.src.directory";
    public static final String DEFAULT_TEST_SRC_DIRECTORY_ENV = "CITRUS_DEFAULT_SRC_DIRECTORY";
    public static final String DEFAULT_TEST_SRC_DIRECTORY = getPropertyEnvOrDefault(
            DEFAULT_TEST_SRC_DIRECTORY_PROPERTY,
            DEFAULT_TEST_SRC_DIRECTORY_ENV,
            "src" + File.separator + "test" + File.separator);

    /**
     * Placeholder used in messages to ignore elements
     */
    public static final String IGNORE_PLACEHOLDER = "@ignore@";

    /**
     * Prefix/suffix used to identify validation matchers
     */
    public static final String VALIDATION_MATCHER_PREFIX = "@";
    public static final String VALIDATION_MATCHER_SUFFIX = "@";

    public static final String GROOVY_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.groovy.file.name.pattern";
    public static final String GROOVY_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_GROOVY_FILE_NAME_PATTERN";
    public static final String GROOVY_TEST_FILE_NAME_PATTERN = getPropertyEnvOrDefault(
            GROOVY_TEST_FILE_NAME_PATTERN_PROPERTY,
            GROOVY_TEST_FILE_NAME_PATTERN_ENV,
            ".*test\\.groovy,.*it\\.groovy");

    public static final String YAML_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.yaml.file.name.pattern";
    public static final String YAML_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_YAML_FILE_NAME_PATTERN";
    public static final String YAML_TEST_FILE_NAME_PATTERN = getPropertyEnvOrDefault(
            YAML_TEST_FILE_NAME_PATTERN_PROPERTY,
            YAML_TEST_FILE_NAME_PATTERN_ENV,
            ".*test\\.yaml,.*it\\.yaml");

    public static final String XML_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.xml.file.name.pattern";
    public static final String XML_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_XML_FILE_NAME_PATTERN";
    public static final String XML_TEST_FILE_NAME_PATTERN = getPropertyEnvOrDefault(
            XML_TEST_FILE_NAME_PATTERN_PROPERTY,
            XML_TEST_FILE_NAME_PATTERN_ENV,
            ".*Test\\.xml,.*IT\\.xml,.*test\\.xml,.*it\\.xml");

    public static final String JAVA_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.java.file.name.pattern";
    public static final String JAVA_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_JAVA_FILE_NAME_PATTERN";
    public static final String JAVA_TEST_FILE_NAME_PATTERN = getPropertyEnvOrDefault(
            JAVA_TEST_FILE_NAME_PATTERN_PROPERTY,
            JAVA_TEST_FILE_NAME_PATTERN_ENV,
            ".*Test\\.java,.*IT\\.java");

    /**
     * Default message type used in message validation mechanism
     */
    public static final String DEFAULT_MESSAGE_TYPE_PROPERTY = "citrus.default.message.type";
    public static final String DEFAULT_MESSAGE_TYPE_ENV = "CITRUS_DEFAULT_MESSAGE_TYPE";
    public static final String DEFAULT_MESSAGE_TYPE = getPropertyEnvOrDefault(
            DEFAULT_MESSAGE_TYPE_PROPERTY,
            DEFAULT_MESSAGE_TYPE_ENV,
            XML.toString());

    /**
     * Flag to allow deactivation of the http message builder citrus header update. See <a href="https://github.com/citrusframework/citrus/issues/1143">ISSUE-1143</a> for details.
     */
    public static final String HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_PROPERTY = "citrus.http.message.builder.force.citrus.header.update.enabled";
    public static final String HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_ENV = "CITRUS_HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED";
    public static final String HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_DEFAULT = TRUE.toString();

    /**
     * Flag to enable/disable environment variable based endpoint and component configuration.
     */
    public static final String ENV_VAR_PROPERTY_BINDING_ENABLED_PROPERTY = "citrus.env.var.property.binding.enabled";
    public static final String ENV_VAR_PROPERTY_BINDING_ENABLED_ENV = "CITRUS_ENV_VAR_PROPERTY_BINDING_ENABLED";
    public static final String ENV_VAR_PROPERTY_BINDING_ENABLED_DEFAULT = TRUE.toString();

    /**
     * Flag to enable/disable environment variable based endpoint and component configuration.
     */
    public static final String COMPONENT_PROPERTY_BINDING_ENABLED_PROPERTY = "citrus.component.property.binding.enabled";
    public static final String COMPONENT_PROPERTY_BINDING_ENABLED_ENV = "CITRUS_COMPONENT_PROPERTY_BINDING_ENABLED";
    public static final String COMPONENT_PROPERTY_BINDING_ENABLED_DEFAULT = getPropertyEnvOrDefault(
            ENV_VAR_PROPERTY_BINDING_ENABLED_PROPERTY,
            ENV_VAR_PROPERTY_BINDING_ENABLED_ENV,
            TRUE.toString());

    /**
     * Flag to enable/disable environment variable based endpoint and component configuration.
     */
    public static final String ENDPOINT_PROPERTY_BINDING_ENABLED_PROPERTY = "citrus.endpoint.property.binding.enabled";
    public static final String ENDPOINT_PROPERTY_BINDING_ENABLED_ENV = "CITRUS_ENDPOINT_PROPERTY_BINDING_ENABLED";
    public static final String ENDPOINT_PROPERTY_BINDING_ENABLED_DEFAULT = getPropertyEnvOrDefault(
            ENV_VAR_PROPERTY_BINDING_ENABLED_PROPERTY,
            ENV_VAR_PROPERTY_BINDING_ENABLED_ENV,
            TRUE.toString());

    /**
     * Default message trace output directory
     */
    public static final String MESSAGE_TRACE_DIRECTORY_PROPERTY = "citrus.message.trace.directory";
    public static final String MESSAGE_TRACE_DIRECTORY_ENV = "CITRUS_MESSAGE_TRACE_DIRECTORY";
    public static final String MESSAGE_TRACE_DIRECTORY_DEFAULT = "target/citrus-logs/trace/messages";

    /**
     * Default type converter
     */
    public static final String TYPE_CONVERTER_PROPERTY = "citrus.type.converter";
    public static final String TYPE_CONVERTER_ENV = "CITRUS_TYPE_CONVERTER";
    public static final String TYPE_CONVERTER_DEFAULT = "default";

    /**
     * Flag to enable/disable fallback to default text equals validation
     */
    public static final String PERFORM_DEFAULT_VALIDATION_PROPERTY = "citrus.perform.default.validation";
    public static final String PERFORM_DEFAULT_VALIDATION_ENV = "CITRUS_PERFORM_DEFAULT_VALIDATION";
    public static final String PERFORM_DEFAULT_VALIDATION_DEFAULT = Boolean.FALSE.toString();

    public static final String CUSTOM_VALIDATOR_STRATEGY_PROPERTY = "citrus.custom.validator.strategy";
    public static final String CUSTOM_VALIDATOR_STRATEGY_ENV = "CITRUS_CUSTOM_VALIDATOR_STRATEGY";
    public static final CustomValidatorStrategy CUSTOM_VALIDATOR_STRATEGY_DEFAULT = CustomValidatorStrategy.EXCLUSIVE;

    /**
     * Flag to enable/disable input stream caching
     */
    public static final String CACHE_INPUT_STREAM_PROPERTY = "citrus.message.cache.input.stream";
    public static final String CACHE_INPUT_STREAM_ENV = "CITRUS_MESSAGE_CACHE_INPUT_STREAM";
    public static final String CACHE_INPUT_STREAM_DEFAULT = TRUE.toString();

    /**
     * Flag to enable/disable message pretty print
     */
    public static final String PRETTY_PRINT_PROPERTY = "citrus.message.pretty.print";
    public static final String PRETTY_PRINT_ENV = "CITRUS_MESSAGE_PRETTY_PRINT";
    public static final String PRETTY_PRINT_DEFAULT = TRUE.toString();

    /**
     * File path charset parameter
     */
    public static final String FILE_PATH_CHARSET_PARAMETER_PROPERTY = "citrus.file.path.charset.parameter";
    public static final String FILE_PATH_CHARSET_PARAMETER_ENV = "CITRUS_FILE_PATH_CHARSET_PARAMETER";
    public static final String FILE_PATH_CHARSET_PARAMETER_DEFAULT = "; charset=";

    /**
     * List of static imports for Groovy test action scripts.
     */
    public static final String GROOVY_STATIC_IMPORTS_PROPERTY = "citrus.groovy.static.imports";
    public static final String GROOVY_STATIC_IMPORTS_ENV = "CITRUS_GROOVY_STATIC_IMPORTS";
    public static final String GROOVY_STATIC_IMPORTS_DEFAULT = "print,sleep";

    /**
     * Flag to enable/disable stack trace output in default logging reporter.
     */
    public static final String DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY = "citrus.default.logging.reporter.print-stack-traces";
    public static final String DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV = "CITRUS_DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES";
    public static final String DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_DEFAULT = FALSE.toString();

    /**
     * Flag to enable/disable auto close of dynamic endpoints.
     */
    public static final String AUTO_CLOSE_DYNAMIC_ENDPOINTS_PROPERTY = "citrus.dynamic.endpoints.auto.close";
    public static final String AUTO_CLOSE_DYNAMIC_ENDPOINTS_ENV = "CITRUS_DYNAMIC_ENDPOINTS_AUTO_CLOSE";
    public static final String AUTO_CLOSE_DYNAMIC_ENDPOINTS_DEFAULT = FALSE.toString();

    /**
     * Flag to enable/disable auto removal of dynamic endpoints.
     */
    public static final String AUTO_REMOVE_DYNAMIC_ENDPOINTS_PROPERTY = "citrus.dynamic.endpoints.auto.remove";
    public static final String AUTO_REMOVE_DYNAMIC_ENDPOINTS_ENV = "CITRUS_DYNAMIC_ENDPOINTS_AUTO_REMOVE";
    public static final String AUTO_REMOVE_DYNAMIC_ENDPOINTS_DEFAULT = FALSE.toString();

    /**
     * Flag to enable/disable Citrus banner.
     */
    public static final String PRINT_BANNER_PROPERTY = "citrus.print.banner";
    public static final String PRINT_BANNER_ENV = "CITRUS_PRINT_BANNER";
    public static final String PRINT_BANNER_DEFAULT = TRUE.toString();

    /**
     * Gets set of file name patterns for Groovy test files.
     */
    public static Set<String> getGroovyTestFileNamePattern() {
        return Stream.of(GROOVY_TEST_FILE_NAME_PATTERN.split(",")).collect(toSet());
    }

    /**
     * Gets set of file name patterns for YAML test files.
     */
    public static Set<String> getYamlTestFileNamePattern() {
        return Stream.of(YAML_TEST_FILE_NAME_PATTERN.split(",")).collect(toSet());
    }

    /**
     * Gets set of file name patterns for XML test files.
     */
    public static Set<String> getXmlTestFileNamePattern() {
        return Stream.of(XML_TEST_FILE_NAME_PATTERN.split(",")).collect(toSet());
    }

    /**
     * Gets set of file name patterns for Java test files.
     */
    public static Set<String> getJavaTestFileNamePattern() {
        return Stream.of(JAVA_TEST_FILE_NAME_PATTERN.split(",")).collect(toSet());
    }

    /**
     * Gets the directory where to put message trace files.
     */
    public static String getMessageTraceDirectory() {
        return getPropertyEnvOrDefault(
                MESSAGE_TRACE_DIRECTORY_PROPERTY,
                MESSAGE_TRACE_DIRECTORY_ENV,
                MESSAGE_TRACE_DIRECTORY_DEFAULT);
    }

    /**
     * Gets the type converter to use by default.
     */
    public static String getTypeConverter() {
        return getPropertyEnvOrDefault(
                TYPE_CONVERTER_PROPERTY,
                TYPE_CONVERTER_ENV,
                TYPE_CONVERTER_DEFAULT);
    }

    /**
     * Gets the setting whether fallback default message validation is enabled/disabled.
     */
    public static boolean isPerformDefaultValidation() {
        return parseBoolean(getPropertyEnvOrDefault(
                PERFORM_DEFAULT_VALIDATION_PROPERTY,
                PERFORM_DEFAULT_VALIDATION_ENV,
                PERFORM_DEFAULT_VALIDATION_DEFAULT));
    }

    public static CustomValidatorStrategy getCustomValidatorStrategy() {
        var customValidatorStrategy = getPropertyEnvOrDefault(
                CUSTOM_VALIDATOR_STRATEGY_PROPERTY,
                CUSTOM_VALIDATOR_STRATEGY_ENV,
                CUSTOM_VALIDATOR_STRATEGY_DEFAULT.toString());

        return CustomValidatorStrategy.valueOf(customValidatorStrategy.toUpperCase());
    }

    /**
     * Gets the setting whether caching of input stream is enabled/disabled.
     */
    public static boolean isCacheInputStream() {
        return parseBoolean(getPropertyEnvOrDefault(
                CACHE_INPUT_STREAM_PROPERTY,
                CACHE_INPUT_STREAM_ENV,
                CACHE_INPUT_STREAM_DEFAULT));
    }

    /**
     * Gets the message payload pretty print enabled/disabled setting.
     */
    public static boolean isPrettyPrintEnabled() {
        return parseBoolean(getPropertyEnvOrDefault(
                PRETTY_PRINT_PROPERTY,
                PRETTY_PRINT_ENV,
                PRETTY_PRINT_DEFAULT));
    }

    /**
     * Get the file path charset parameter.
     */
    public static String getFilePathCharsetParameter() {
        return getPropertyEnvOrDefault(
                FILE_PATH_CHARSET_PARAMETER_PROPERTY,
                FILE_PATH_CHARSET_PARAMETER_ENV,
                FILE_PATH_CHARSET_PARAMETER_DEFAULT);
    }

    /**
     * Gets the http message builder force citrus header update enabled/disabled setting, which controls
     * whether the citrus message builder always creates messages with unique ids.
     */
    public static boolean isHttpMessageBuilderForceCitrusHeaderUpdateEnabled() {
        return parseBoolean(getPropertyEnvOrDefault(
                HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_PROPERTY,
                HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_ENV,
                HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_DEFAULT));
    }

    /**
     * Gets the test file name pattern for given type or empty patterns for unknown type.
     */
    public static Set<String> getTestFileNamePattern(String type) {
        return switch (type) {
            case TestLoader.XML, SPRING -> getXmlTestFileNamePattern();
            case GROOVY -> getGroovyTestFileNamePattern();
            case YAML -> getYamlTestFileNamePattern();
            case JAVA -> getJavaTestFileNamePattern();
            default -> emptySet();
        };
    }

    /**
     * Gets in the respective order, a system property, an environment variable or the default
     *
     * @param prop the name of the system property to get
     * @param env  the name of the environment variable to get
     * @param def  the default value
     * @return first value encountered, which is not null. May return null, if default value is null.
     */
    public static String getPropertyEnvOrDefault(String prop, String env, String def) {
        return getProperty(prop, getenv(env) != null ? getenv(env) : def);
    }

    /**
     * Gets the setting that marks environment variable property binding enabled/disabled.
     * If enabled endpoints and components are configured using system properties and environment variables.
     */
    public static boolean isEnvVarPropertyBindingEnabled() {
        return parseBoolean(getPropertyEnvOrDefault(
                ENV_VAR_PROPERTY_BINDING_ENABLED_PROPERTY,
                ENV_VAR_PROPERTY_BINDING_ENABLED_ENV,
                ENV_VAR_PROPERTY_BINDING_ENABLED_DEFAULT));
    }

    /**
     * Gets the setting that marks environment variable property binding enabled/disabled for all components.
     * If enabled components are configured using system properties and environment variables.
     */
    public static boolean isComponentPropertyBindingEnabled() {
        return parseBoolean(getPropertyEnvOrDefault(
                COMPONENT_PROPERTY_BINDING_ENABLED_PROPERTY,
                COMPONENT_PROPERTY_BINDING_ENABLED_ENV,
                COMPONENT_PROPERTY_BINDING_ENABLED_DEFAULT));
    }

    /**
     * Gets the setting that marks environment variable property binding enabled/disabled for all endpoints.
     * If enabled endpoints are configured using system properties and environment variables.
     */
    public static boolean isEndpointPropertyBindingEnabled() {
        return parseBoolean(getPropertyEnvOrDefault(
                ENDPOINT_PROPERTY_BINDING_ENABLED_PROPERTY,
                ENDPOINT_PROPERTY_BINDING_ENABLED_ENV,
                ENDPOINT_PROPERTY_BINDING_ENABLED_DEFAULT));
    }

    /**
     * Gets the static imports used in Groovy test action scripts.
     */
    public static String getGroovyStaticImports() {
        return getPropertyEnvOrDefault(
                GROOVY_STATIC_IMPORTS_PROPERTY,
                GROOVY_STATIC_IMPORTS_ENV,
                GROOVY_STATIC_IMPORTS_DEFAULT);
    }

    /**
     * Whether stack traces should be printed in the default logging reporter or not.
     */
    public static boolean isStackTraceOutputEnabled() {
        return parseBoolean(
                getPropertyEnvOrDefault(
                        DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY,
                        DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV,
                        DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_DEFAULT
                )
        );
    }

    /**
     * Setting marks auto close for dynamic endpoints.
     * If set to true all dynamic endpoints will be closed automatically after the test.
     */
    public static boolean isAutoCloseDynamicEndpoints() {
        return parseBoolean(
                getPropertyEnvOrDefault(
                        AUTO_CLOSE_DYNAMIC_ENDPOINTS_PROPERTY,
                        AUTO_CLOSE_DYNAMIC_ENDPOINTS_ENV,
                        AUTO_CLOSE_DYNAMIC_ENDPOINTS_DEFAULT
                )
        );
    }

    /**
     * Setting marks auto removal for dynamic endpoints.
     * If set to true all dynamic endpoints will be closed and removed automatically after the test.
     */
    public static boolean isAutoRemoveDynamicEndpoints() {
        return parseBoolean(
                getPropertyEnvOrDefault(
                        AUTO_REMOVE_DYNAMIC_ENDPOINTS_PROPERTY,
                        AUTO_REMOVE_DYNAMIC_ENDPOINTS_ENV,
                        AUTO_REMOVE_DYNAMIC_ENDPOINTS_DEFAULT
                )
        );
    }

    /**
     * Should logging reporter print the Citrus banner at the very beginning of the test suite.
     */
    public static boolean isPrintBanner() {
        return parseBoolean(
                getPropertyEnvOrDefault(
                        PRINT_BANNER_PROPERTY,
                        PRINT_BANNER_ENV,
                        PRINT_BANNER_DEFAULT
                )
        );
    }
}
