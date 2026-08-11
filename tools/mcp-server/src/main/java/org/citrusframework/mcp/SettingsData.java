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

package org.citrusframework.mcp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import org.citrusframework.CitrusSettings;
import org.citrusframework.log.CitrusLogSettings;

/**
 * Provides a catalog of all available Citrus system property and environment variable settings.
 * Settings are collected from all Settings classes across the codebase and exposed as structured data.
 */
@ApplicationScoped
public class SettingsData {

    private static final String STRING = "string";
    private static final String STRING_ARRAY = "string[]";
    private static final String BOOLEAN = "boolean";
    private static final String INT = "int";
    private static final String LONG = "long";

    private static final Map<String, SettingsGroup> GROUPS = new LinkedHashMap<>();

    static {
        GROUPS.put("core", createCoreSettings());
        GROUPS.put("logging", createLogSettings());
        GROUPS.put("reporting", createReportingSettings());
        GROUPS.put("spring", createSpringSettings());
        GROUPS.put("agent", createAgentSettings());
        GROUPS.put("camel", createCamelSettings());
        GROUPS.put("camel-cli", createCamelCliSettings());
        GROUPS.put("camel-infra", createCamelInfraSettings());
        GROUPS.put("http", createHttpSettings());
        GROUPS.put("ftp", createFtpSettings());
        GROUPS.put("mail", createMailSettings());
        GROUPS.put("openapi", createOpenApiSettings());
        GROUPS.put("json", createJsonSettings());
        GROUPS.put("yaml-validation", createYamlValidationSettings());
        GROUPS.put("jbang", createJBangSettings());
        GROUPS.put("kubernetes", createKubernetesSettings());
        GROUPS.put("knative", createKnativeSettings());
        GROUPS.put("kafka", createKafkaEndpointSettings());
        GROUPS.put("testcontainers", createTestContainersSettings());
        GROUPS.put("testcontainers-kafka", createKafkaSettings());
        GROUPS.put("testcontainers-localstack", createLocalStackSettings());
        GROUPS.put("testcontainers-postgresql", createPostgreSQLSettings());
        GROUPS.put("testcontainers-mongodb", createMongoDBSettings());
        GROUPS.put("testcontainers-redpanda", createRedpandaSettings());
    }

    public Map<String, SettingsGroup> getGroups() {
        return GROUPS;
    }

    public SettingsGroup getGroup(String name) {
        return GROUPS.get(name);
    }

    public List<String> getGroupNames() {
        return List.copyOf(GROUPS.keySet());
    }

    public List<String> getModuleNames() {
        return GROUPS.values().stream()
                .map(SettingsGroup::module)
                .distinct()
                .toList();
    }

    public List<SettingsGroup> getGroupsByModule(String module) {
        return GROUPS.values().stream()
                .filter(g -> g.module().equals(module))
                .toList();
    }

    public List<SettingEntry> getAllSettings() {
        List<SettingEntry> all = new ArrayList<>();
        for (SettingsGroup group : GROUPS.values()) {
            all.addAll(group.settings());
        }
        return all;
    }

    private static SettingsGroup createCoreSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.application.properties", "CITRUS_APPLICATION_PROPERTIES",
                "citrus-application.properties", STRING,
                "Path to the Citrus application properties file"));
        settings.add(new SettingEntry(CitrusSettings.CITRUS_FILE_ENCODING_PROPERTY, CitrusSettings.CITRUS_FILE_ENCODING_ENV,
                "system default charset", STRING,
                "Default file encoding used by Citrus"));
        settings.add(new SettingEntry(CitrusSettings.DEFAULT_MESSAGE_TYPE_PROPERTY, CitrusSettings.DEFAULT_MESSAGE_TYPE_ENV,
                "XML", STRING,
                "Default message type for validation"));
        settings.add(new SettingEntry(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY_PROPERTY, CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY_ENV,
                "src/test/", STRING,
                "Default source directory for test files"));
        settings.add(new SettingEntry(CitrusSettings.DEFAULT_CONFIG_CLASS_PROPERTY, CitrusSettings.DEFAULT_CONFIG_CLASS_ENV,
                null, STRING,
                "Java configuration class for Citrus context"));
        settings.add(new SettingEntry(CitrusSettings.TEST_NAME_VARIABLE_PROPERTY, CitrusSettings.TEST_NAME_VARIABLE_ENV,
                "citrus.test.name", STRING,
                "Variable name for the current test name"));
        settings.add(new SettingEntry(CitrusSettings.TEST_PACKAGE_VARIABLE_PROPERTY, CitrusSettings.TEST_PACKAGE_VARIABLE_ENV,
                "citrus.test.package", STRING,
                "Variable name for the current test package"));
        settings.add(new SettingEntry(CitrusSettings.TYPE_CONVERTER_PROPERTY, CitrusSettings.TYPE_CONVERTER_ENV,
                CitrusSettings.TYPE_CONVERTER_DEFAULT, STRING,
                "Type converter implementation to use"));
        settings.add(new SettingEntry(CitrusSettings.RESOURCES_WORKDIR_PROPERTY, CitrusSettings.RESOURCES_WORKDIR_ENV,
                CitrusSettings.RESOURCES_WORKDIR_DEFAULT, STRING,
                "Working directory for resource resolution"));
        settings.add(new SettingEntry(CitrusSettings.MESSAGE_TRACE_DIRECTORY_PROPERTY, CitrusSettings.MESSAGE_TRACE_DIRECTORY_ENV,
                CitrusSettings.MESSAGE_TRACE_DIRECTORY_DEFAULT, STRING,
                "Directory for message trace logs"));
        settings.add(new SettingEntry(CitrusSettings.PRINT_BANNER_PROPERTY, CitrusSettings.PRINT_BANNER_ENV,
                CitrusSettings.PRINT_BANNER_DEFAULT, BOOLEAN,
                "Enable/disable Citrus startup banner"));
        settings.add(new SettingEntry(CitrusSettings.PERFORM_DEFAULT_VALIDATION_PROPERTY, CitrusSettings.PERFORM_DEFAULT_VALIDATION_ENV,
                CitrusSettings.PERFORM_DEFAULT_VALIDATION_DEFAULT, BOOLEAN,
                "Enable/disable default message validation"));
        settings.add(new SettingEntry(CitrusSettings.CACHE_INPUT_STREAM_PROPERTY, CitrusSettings.CACHE_INPUT_STREAM_ENV,
                CitrusSettings.CACHE_INPUT_STREAM_DEFAULT, BOOLEAN,
                "Cache input stream message payloads"));
        settings.add(new SettingEntry(CitrusSettings.PRETTY_PRINT_PROPERTY, CitrusSettings.PRETTY_PRINT_ENV,
                CitrusSettings.PRETTY_PRINT_DEFAULT, BOOLEAN,
                "Pretty print message payloads in logs"));
        settings.add(new SettingEntry(CitrusSettings.AUTO_CLOSE_DYNAMIC_ENDPOINTS_PROPERTY, CitrusSettings.AUTO_CLOSE_DYNAMIC_ENDPOINTS_ENV,
                CitrusSettings.AUTO_CLOSE_DYNAMIC_ENDPOINTS_DEFAULT, BOOLEAN,
                "Auto-close dynamic endpoints after test"));
        settings.add(new SettingEntry(CitrusSettings.AUTO_REMOVE_DYNAMIC_ENDPOINTS_PROPERTY, CitrusSettings.AUTO_REMOVE_DYNAMIC_ENDPOINTS_ENV,
                CitrusSettings.AUTO_REMOVE_DYNAMIC_ENDPOINTS_DEFAULT, BOOLEAN,
                "Auto-remove dynamic endpoints after test"));
        settings.add(new SettingEntry(CitrusSettings.ENV_VAR_PROPERTY_BINDING_ENABLED_PROPERTY, CitrusSettings.ENV_VAR_PROPERTY_BINDING_ENABLED_ENV,
                CitrusSettings.ENV_VAR_PROPERTY_BINDING_ENABLED_DEFAULT, BOOLEAN,
                "Enable environment variable property binding for components"));
        settings.add(new SettingEntry(CitrusSettings.COMPONENT_PROPERTY_BINDING_ENABLED_PROPERTY, CitrusSettings.COMPONENT_PROPERTY_BINDING_ENABLED_ENV,
                "true", BOOLEAN,
                "Enable property binding for Citrus components"));
        settings.add(new SettingEntry(CitrusSettings.ENDPOINT_PROPERTY_BINDING_ENABLED_PROPERTY, CitrusSettings.ENDPOINT_PROPERTY_BINDING_ENABLED_ENV,
                "true", BOOLEAN,
                "Enable property binding for Citrus endpoints"));
        settings.add(new SettingEntry(CitrusSettings.CUSTOM_VALIDATOR_STRATEGY_PROPERTY, CitrusSettings.CUSTOM_VALIDATOR_STRATEGY_ENV,
                CitrusSettings.CUSTOM_VALIDATOR_STRATEGY_DEFAULT.name(), STRING,
                "Custom validator strategy (EXCLUSIVE or COMPLEMENTARY)"));
        settings.add(new SettingEntry(CitrusSettings.ALLOW_FUNCTION_OVERRIDE_PROPERTY, CitrusSettings.ALLOW_FUNCTION_OVERRIDE_ENV,
                CitrusSettings.ALLOW_FUNCTION_OVERRIDE_DEFAULT, BOOLEAN,
                "Allow overriding of Citrus functions"));
        settings.add(new SettingEntry(CitrusSettings.ALLOW_VALIDATION_MATCHER_OVERRIDE_PROPERTY, CitrusSettings.ALLOW_VALIDATION_MATCHER_OVERRIDE_ENV,
                CitrusSettings.ALLOW_VALIDATION_MATCHER_OVERRIDE_DEFAULT, BOOLEAN,
                "Allow overriding of validation matchers"));
        settings.add(new SettingEntry(CitrusSettings.OUTBOUND_SCHEMA_VALIDATION_ENABLED_PROPERTY, CitrusSettings.OUTBOUND_SCHEMA_VALIDATION_ENABLED_ENV,
                null, BOOLEAN,
                "Enable outbound schema validation"));
        settings.add(new SettingEntry(CitrusSettings.OUTBOUND_JSON_SCHEMA_VALIDATION_ENABLED_PROPERTY, CitrusSettings.OUTBOUND_JSON_SCHEMA_VALIDATION_ENABLED_ENV,
                null, BOOLEAN,
                "Enable outbound JSON schema validation"));
        settings.add(new SettingEntry(CitrusSettings.OUTBOUND_XML_SCHEMA_VALIDATION_ENABLED_PROPERTY, CitrusSettings.OUTBOUND_XML_SCHEMA_VALIDATION_ENABLED_ENV,
                null, BOOLEAN,
                "Enable outbound XML schema validation"));
        settings.add(new SettingEntry(CitrusSettings.FILE_PATH_CHARSET_PARAMETER_PROPERTY, CitrusSettings.FILE_PATH_CHARSET_PARAMETER_ENV,
                CitrusSettings.FILE_PATH_CHARSET_PARAMETER_DEFAULT, STRING,
                "Charset parameter appended to file path content type"));
        settings.add(new SettingEntry(CitrusSettings.HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_PROPERTY,
                CitrusSettings.HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_ENV,
                CitrusSettings.HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_DEFAULT, BOOLEAN,
                "Force Citrus HTTP header update in message builder"));
        settings.add(new SettingEntry(CitrusSettings.DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_PROPERTY,
                CitrusSettings.DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_ENV,
                CitrusSettings.DEFAULT_LOGGING_REPORTER_PRINT_STACK_TRACES_DEFAULT, BOOLEAN,
                "Print stack traces in logging reporter"));
        settings.add(new SettingEntry(CitrusSettings.YAML_TEST_FILE_NAME_PATTERN_PROPERTY, CitrusSettings.YAML_TEST_FILE_NAME_PATTERN_ENV,
                ".*\\.citrus\\.yaml,.*\\.citrus\\.yml", STRING,
                "File name patterns for YAML DSL test files"));
        settings.add(new SettingEntry(CitrusSettings.XML_TEST_FILE_NAME_PATTERN_PROPERTY, CitrusSettings.XML_TEST_FILE_NAME_PATTERN_ENV,
                ".*Test\\.xml,.*IT\\.xml", STRING,
                "File name patterns for XML DSL test files"));
        settings.add(new SettingEntry(CitrusSettings.JAVA_TEST_FILE_NAME_PATTERN_PROPERTY, CitrusSettings.JAVA_TEST_FILE_NAME_PATTERN_ENV,
                ".*Test\\.java,.*IT\\.java", STRING,
                "File name patterns for Java test files"));
        settings.add(new SettingEntry(CitrusSettings.GROOVY_TEST_FILE_NAME_PATTERN_PROPERTY, CitrusSettings.GROOVY_TEST_FILE_NAME_PATTERN_ENV,
                ".*\\.citrus\\.groovy", STRING,
                "File name patterns for Groovy DSL test files"));
        settings.add(new SettingEntry(CitrusSettings.GROOVY_STATIC_IMPORTS_PROPERTY, CitrusSettings.GROOVY_STATIC_IMPORTS_ENV,
                CitrusSettings.GROOVY_STATIC_IMPORTS_DEFAULT, STRING,
                "Static imports for Groovy DSL scripts"));

        return new SettingsGroup("core", "Core Settings",
                "Core Citrus framework settings from CitrusSettings", "citrus-api", settings);
    }

    private static SettingsGroup createLogSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry(CitrusLogSettings.LOG_MODIFIER_PROPERTY, CitrusLogSettings.LOG_MODIFIER_ENV,
                CitrusLogSettings.LOG_MODIFIER_DEFAULT, BOOLEAN,
                "Enable/disable log modifier for masking sensitive data"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_MASK_KEYWORDS_PROPERTY, CitrusLogSettings.LOG_MASK_KEYWORDS_ENV,
                CitrusLogSettings.LOG_MASK_KEYWORDS_DEFAULT, STRING,
                "Comma-separated keywords to mask in log output"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_MASK_VALUE_PROPERTY, CitrusLogSettings.LOG_MASK_VALUE_ENV,
                CitrusLogSettings.LOG_MASK_VALUE_DEFAULT, STRING,
                "Replacement value for masked keywords"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_MASK_XML_PROPERTY, CitrusLogSettings.LOG_MASK_XML_ENV,
                CitrusLogSettings.LOG_MASK_XML_DEFAULT, BOOLEAN,
                "Enable log masking for XML payloads"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_MASK_JSON_PROPERTY, CitrusLogSettings.LOG_MASK_JSON_ENV,
                CitrusLogSettings.LOG_MASK_JSON_DEFAULT, BOOLEAN,
                "Enable log masking for JSON payloads"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_MASK_YAML_PROPERTY, CitrusLogSettings.LOG_MASK_YAML_ENV,
                CitrusLogSettings.LOG_MASK_YAML_DEFAULT, BOOLEAN,
                "Enable log masking for YAML payloads"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_MASK_KEY_VALUE_PROPERTY, CitrusLogSettings.LOG_MASK_KEY_VALUE_ENV,
                CitrusLogSettings.LOG_MASK_KEY_VALUE_DEFAULT, BOOLEAN,
                "Enable log masking for key-value pair payloads"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_MASK_FORM_URL_ENCODED_PROPERTY, CitrusLogSettings.LOG_MASK_FORM_URL_ENCODED_ENV,
                CitrusLogSettings.LOG_MASK_FORM_URL_ENCODED_DEFAULT, BOOLEAN,
                "Enable log masking for form URL encoded payloads"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_PRINT_MESSAGE_CONTENT_PROPERTY, CitrusLogSettings.LOG_PRINT_MESSAGE_CONTENT_ENV,
                CitrusLogSettings.LOG_PRINT_MESSAGE_CONTENT_DEFAULT, BOOLEAN,
                "Print full message content in logs"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_PRINT_INBOUND_MESSAGE_CONTENT_PROPERTY, CitrusLogSettings.LOG_PRINT_INBOUND_MESSAGE_CONTENT_ENV,
                null, BOOLEAN,
                "Print inbound message content (falls back to print.message.content)"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_PRINT_OUTBOUND_MESSAGE_CONTENT_PROPERTY, CitrusLogSettings.LOG_PRINT_OUTBOUND_MESSAGE_CONTENT_ENV,
                null, BOOLEAN,
                "Print outbound message content (falls back to print.message.content)"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_PRINT_MESSAGE_LAYOUT_PROPERTY, CitrusLogSettings.LOG_PRINT_MESSAGE_LAYOUT_ENV,
                CitrusLogSettings.LOG_PRINT_MESSAGE_LAYOUT_DEFAULT, STRING,
                "Message log layout (verbose or compact)"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_MESSAGE_PAYLOAD_MAX_LENGTH_PROPERTY, CitrusLogSettings.LOG_MESSAGE_PAYLOAD_MAX_LENGTH_ENV,
                CitrusLogSettings.LOG_MESSAGE_PAYLOAD_MAX_LENGTH_DEFAULT, INT,
                "Maximum length of message payload in log output"));
        settings.add(new SettingEntry(CitrusLogSettings.LOG_COLOR_PROPERTY, CitrusLogSettings.LOG_COLOR_ENV,
                CitrusLogSettings.LOG_COLOR_DEFAULT, STRING,
                "Log output color mode (auto, always, never)"));

        return new SettingsGroup("logging", "Logging Settings",
                "Log masking and message logging settings from CitrusLogSettings", "citrus-api", settings);
    }

    private static SettingsGroup createReportingSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.summary.report.enabled", "CITRUS_SUMMARY_REPORT_ENABLED",
                "true", BOOLEAN,
                "Enable/disable summary report generation"));
        settings.add(new SettingEntry("citrus.summary.report.template", "CITRUS_SUMMARY_REPORT_TEMPLATE",
                "classpath:org/citrusframework/report/summary-report.xml", STRING,
                "Template for summary report"));
        settings.add(new SettingEntry("citrus.summary.report.file", "CITRUS_SUMMARY_REPORT_FILE",
                "citrus-summary.xml", STRING,
                "Output file name for summary report"));
        settings.add(new SettingEntry("citrus.report.auto.clear", "CITRUS_REPORT_AUTO_CLEAR",
                "true", BOOLEAN,
                "Auto-clear test results after report generation"));
        settings.add(new SettingEntry("citrus.report.ignore.errors", "CITRUS_REPORT_IGNORE_ERRORS",
                "true", BOOLEAN,
                "Ignore errors during report generation"));
        settings.add(new SettingEntry("citrus.report.directory", "CITRUS_REPORT_DIRECTORY",
                "target/citrus-reports", STRING,
                "Directory for Citrus report output"));
        settings.add(new SettingEntry("citrus.html.report.enabled", "CITRUS_HTML_REPORT_ENABLED",
                "true", BOOLEAN,
                "Enable/disable HTML report generation"));
        settings.add(new SettingEntry("citrus.html.report.template", "CITRUS_HTML_REPORT_TEMPLATE",
                "classpath:org/citrusframework/base/report/test-report.html", STRING,
                "Template for HTML test report"));
        settings.add(new SettingEntry("citrus.html.report.file", "CITRUS_HTML_REPORT_FILE",
                "citrus-test-results.html", STRING,
                "Output file name for HTML report"));
        settings.add(new SettingEntry("citrus.html.report.directory", "CITRUS_HTML_REPORT_DIRECTORY",
                "", STRING,
                "Directory for HTML report output"));
        settings.add(new SettingEntry("citrus.junit.report.enabled", "CITRUS_JUNIT_REPORT_ENABLED",
                "true", BOOLEAN,
                "Enable/disable JUnit report generation"));
        settings.add(new SettingEntry("citrus.junit.report.directory", "CITRUS_JUNIT_REPORT_DIRECTORY",
                "junitreports", STRING,
                "Directory for JUnit report output"));
        settings.add(new SettingEntry("citrus.junit.report.suite.name", "CITRUS_JUNIT_REPORT_SUITE_NAME",
                "TestSuite", STRING,
                "Test suite name in JUnit report"));
        settings.add(new SettingEntry("citrus.test.flow.report.enabled", "CITRUS_TEST_FLOW_REPORT_ENABLED",
                "true", BOOLEAN,
                "Enable/disable test flow report generation"));
        settings.add(new SettingEntry("citrus.test.flow.report.output", "CITRUS_TEST_FLOW_REPORT_OUTPUT",
                "json", STRING,
                "Test flow report output format"));

        return new SettingsGroup("reporting", "Reporting Settings",
                "Test report settings from SummaryReporterSettings, TestReporterSettings, HtmlReporterSettings, " +
                        "JUnitReporterSettings, and TestFlowReporterSettings",
                "citrus-api, citrus-base", settings);
    }

    private static SettingsGroup createSpringSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.spring.application.context", "CITRUS_SPRING_APPLICATION_CONTEXT",
                "classpath*:citrus-context.xml", STRING,
                "Spring application context file path"));
        settings.add(new SettingEntry("citrus.spring.java.config", "CITRUS_SPRING_JAVA_CONFIG",
                null, STRING,
                "Spring Java configuration class (falls back to citrus.java.config)"));

        return new SettingsGroup("spring", "Spring Settings",
                "Spring Framework integration settings from CitrusSpringSettings", "citrus-spring", settings);
    }

    private static SettingsGroup createAgentSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.agent.name", "CITRUS_AGENT_NAME",
                "citrus-agent", STRING,
                "Name of the Citrus agent"));
        settings.add(new SettingEntry("citrus.agent.test.engine", "CITRUS_AGENT_TEST_ENGINE",
                "junit-jupiter", STRING,
                "Test engine to use (junit-jupiter, testng)"));
        settings.add(new SettingEntry("citrus.agent.work.directory", "CITRUS_AGENT_WORK_DIRECTORY",
                null, STRING,
                "Working directory for agent operations"));
        settings.add(new SettingEntry("citrus.agent.server.port", "CITRUS_AGENT_SERVER_PORT",
                "4567", INT,
                "HTTP server port for the agent"));
        settings.add(new SettingEntry("citrus.agent.time.to.live", "CITRUS_AGENT_TIME_TO_LIVE",
                "-1", INT,
                "Agent time to live in seconds (-1 for unlimited)"));
        settings.add(new SettingEntry("citrus.agent.system.exit", "CITRUS_AGENT_SYSTEM_EXIT",
                "false", BOOLEAN,
                "Call System.exit() when agent shuts down"));
        settings.add(new SettingEntry("citrus.agent.skip.tests", "CITRUS_AGENT_SKIP_TESTS",
                "false", BOOLEAN,
                "Skip test execution"));
        settings.add(new SettingEntry("citrus.agent.verbose", "CITRUS_AGENT_VERBOSE",
                "true", BOOLEAN,
                "Enable verbose agent output"));
        settings.add(new SettingEntry("citrus.agent.reset", "CITRUS_AGENT_RESET",
                "true", BOOLEAN,
                "Reset agent state between test runs"));
        settings.add(new SettingEntry("citrus.agent.offline", "CITRUS_AGENT_OFFLINE",
                "false", BOOLEAN,
                "Run agent in offline mode without downloading dependencies"));
        settings.add(new SettingEntry("citrus.agent.inspect.code", "CITRUS_AGENT_INSPECT_CODE",
                "true", BOOLEAN,
                "Enable code inspection capabilities"));
        settings.add(new SettingEntry("citrus.agent.includes", "CITRUS_AGENT_INCLUDES",
                "^.*IT$,^.*ITCase$,^IT.*$", STRING_ARRAY,
                "Test class name patterns to include"));
        settings.add(new SettingEntry("citrus.agent.packages", "CITRUS_AGENT_PACKAGES",
                null, STRING_ARRAY,
                "Packages to scan for test classes"));
        settings.add(new SettingEntry("citrus.agent.test.sources", "CITRUS_AGENT_TEST_SOURCES",
                null, STRING_ARRAY,
                "Test source files or directories"));
        settings.add(new SettingEntry("citrus.agent.config.class", "CITRUS_AGENT_CONFIG_CLASS",
                null, STRING,
                "Custom configuration class for agent"));
        settings.add(new SettingEntry("citrus.agent.test.jar", "CITRUS_AGENT_TEST_JAR",
                "classpath:citrus-agent-tests.jar", STRING,
                "Path to the test JAR for agent execution"));
        settings.add(new SettingEntry("citrus.agent.modules", "CITRUS_AGENT_MODULES",
                "", STRING,
                "Comma-separated list of additional modules"));
        settings.add(new SettingEntry("citrus.agent.dependencies", "CITRUS_AGENT_DEPENDENCIES",
                "", STRING,
                "Comma-separated list of additional dependencies"));
        settings.add(new SettingEntry("citrus.agent.cors.allowed.origin", "CITRUS_AGENT_CORS_ALLOWED_ORIGIN",
                "https?://localhost:\\d+", STRING,
                "Allowed CORS origin pattern for agent HTTP server"));

        return new SettingsGroup("agent", "Agent Settings",
                "Citrus agent settings from CitrusAgentSettings", "citrus-api", settings);
    }

    private static SettingsGroup createCamelSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.camel.context.name", "CITRUS_CAMEL_CONTEXT_NAME",
                "camelContext", STRING,
                "Default Camel context bean name"));
        settings.add(new SettingEntry("citrus.camel.timeout", "CITRUS_CAMEL_TIMEOUT",
                "5000", LONG,
                "Default timeout for Camel operations in milliseconds"));
        settings.add(new SettingEntry("citrus.camel.max.attempts", "CITRUS_CAMEL_MAX_ATTEMPTS",
                "60", INT,
                "Maximum retry attempts for Camel operations"));
        settings.add(new SettingEntry("citrus.camel.delay.between.attempts", "CITRUS_CAMEL_DELAY_BETWEEN_ATTEMPTS",
                "2000", LONG,
                "Delay between retry attempts in milliseconds"));
        settings.add(new SettingEntry("citrus.camel.print.logs", "CITRUS_CAMEL_PRINT_LOGS",
                "true", BOOLEAN,
                "Print Camel route logs"));
        settings.add(new SettingEntry("citrus.camel.filter.internal.headers", "CITRUS_CAMEL_FILTER_INTERNAL_HEADERS",
                "true", BOOLEAN,
                "Filter internal Camel headers from messages"));

        return new SettingsGroup("camel", "Camel Settings",
                "Apache Camel integration settings from CamelSettings", "citrus-camel", settings);
    }

    private static SettingsGroup createCamelCliSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.camel.cli.app", "CITRUS_CAMEL_CLI_APP",
                "camel@apache/camel", STRING,
                "Camel CLI application identifier"));
        settings.add(new SettingEntry("citrus.camel.cli.version", "CITRUS_CAMEL_CLI_VERSION",
                "latest", STRING,
                "Camel CLI version to use"));
        settings.add(new SettingEntry("citrus.camel.cli.work.dir", "CITRUS_CAMEL_CLI_WORK_DIR",
                null, STRING,
                "Working directory for Camel CLI operations"));
        settings.add(new SettingEntry("citrus.camel.cli.kamelets.version", "CITRUS_CAMEL_CLI_KAMELETS_VERSION",
                "", STRING,
                "Kamelets catalog version"));
        settings.add(new SettingEntry("citrus.camel.cli.kamelets.local.dir", "CITRUS_CAMEL_CLI_KAMELETS_LOCAL_DIR",
                null, STRING,
                "Local directory for Kamelets"));
        settings.add(new SettingEntry("citrus.camel.cli.trust.url", "CITRUS_CAMEL_CLI_TRUST_URL",
                "https://github.com/apache/camel/", STRING,
                "Trusted URL for Camel CLI operations"));
        settings.add(new SettingEntry("citrus.camel.cli.verbose", "CITRUS_CAMEL_CLI_VERBOSE",
                "false", BOOLEAN,
                "Enable verbose Camel CLI output"));
        settings.add(new SettingEntry("citrus.camel.cli.dump.integration.output", "CITRUS_CAMEL_CLI_DUMP_INTEGRATION_OUTPUT",
                "false", BOOLEAN,
                "Dump Camel integration process output"));
        settings.add(new SettingEntry("citrus.camel.cli.auto.remove.resources", "CITRUS_CAMEL_CLI_AUTO_REMOVE_RESOURCES",
                "true", BOOLEAN,
                "Auto-remove resources after Camel CLI operations"));
        settings.add(new SettingEntry("citrus.camel.cli.auto.remove.plugins", "CITRUS_CAMEL_CLI_AUTO_REMOVE_PLUGINS",
                "false", BOOLEAN,
                "Auto-remove plugins after Camel CLI operations"));
        settings.add(new SettingEntry("citrus.camel.cli.wait.for.running.state", "CITRUS_CAMEL_CLI_WAIT_FOR_RUNNING_STATE",
                "true", BOOLEAN,
                "Wait for Camel integration to reach running state"));
        settings.add(new SettingEntry("citrus.camel.cli.type", "CITRUS_CAMEL_CLI_TYPE",
                "jbang", STRING,
                "Camel CLI launcher type (jbang or launcher)"));

        return new SettingsGroup("camel-cli", "Camel CLI Settings",
                "Camel CLI (formerly JBang) settings from CamelCliSettings", "citrus-camel", settings);
    }

    private static SettingsGroup createCamelInfraSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.camel.infra.auto.remove.services", "CITRUS_CAMEL_INFRA_AUTO_REMOVE_SERVICES",
                "true", BOOLEAN,
                "Auto-remove infrastructure services after test"));
        settings.add(new SettingEntry("citrus.camel.infra.dump.service.output", "CITRUS_CAMEL_INFRA_DUMP_SERVICE_OUTPUT",
                "false", BOOLEAN,
                "Dump infrastructure service output"));
        settings.add(new SettingEntry("citrus.camel.infra.port", "CITRUS_CAMEL_INFRA_PORT",
                "-1", INT,
                "Default port for infrastructure services (-1 for random)"));
        settings.add(new SettingEntry("citrus.camel.infra.fixedPort", "CITRUS_CAMEL_INFRA_FIXED_PORT",
                "false", BOOLEAN,
                "Use fixed port for infrastructure services"));

        return new SettingsGroup("camel-infra", "Camel Infrastructure Settings",
                "Camel infrastructure service settings from CamelInfraSettings", "citrus-camel", settings);
    }

    private static SettingsGroup createHttpSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.http.server.response.cache.size", "CITRUS_HTTP_SERVER_RESPONSE_CACHE_SIZE",
                "100", INT,
                "Maximum number of cached responses in HTTP server"));
        settings.add(new SettingEntry("citrus.http.server.use.default.filters", "CITRUS_HTTP_SERVER_USE_DEFAULT_FILTERS",
                "true", BOOLEAN,
                "Use default servlet filters in HTTP server"));

        return new SettingsGroup("http", "HTTP Settings",
                "HTTP server settings from HttpServerSettings", "citrus-http", settings);
    }

    private static SettingsGroup createFtpSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.ftp.marshaller.type", "CITRUS_FTP_MARSHALLER_TYPE",
                "XML", STRING,
                "Marshaller type for FTP messages (XML or JSON)"));

        return new SettingsGroup("ftp", "FTP Settings",
                "FTP endpoint settings from FtpSettings", "citrus-ftp", settings);
    }

    private static SettingsGroup createMailSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.mail.marshaller.type", "CITRUS_MAIL_MARSHALLER_TYPE",
                "XML", STRING,
                "Marshaller type for mail messages (XML or JSON)"));

        return new SettingsGroup("mail", "Mail Settings",
                "Mail endpoint settings from MailSettings", "citrus-mail", settings);
    }

    private static SettingsGroup createOpenApiSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.openapi.generate.optional.fields", "CITRUS_OPENAPI_GENERATE_OPTIONAL_FIELDS",
                "true", BOOLEAN,
                "Generate optional fields in OpenAPI request/response payloads"));
        settings.add(new SettingEntry("citrus.openapi.validation.enabled.request", "CITRUS_OPENAPI_VALIDATION_ENABLED_REQUEST",
                "true", BOOLEAN,
                "Enable OpenAPI request validation"));
        settings.add(new SettingEntry("citrus.openapi.validation.enabled.response", "CITRUS_OPENAPI_VALIDATION_ENABLED_RESPONSE",
                "true", BOOLEAN,
                "Enable OpenAPI response validation"));
        settings.add(new SettingEntry("citrus.openapi.neglect.base.path", "CITRUS_OPENAPI_NEGLECT_BASE_PATH",
                "false", BOOLEAN,
                "Ignore the base path defined in the OpenAPI spec"));
        settings.add(new SettingEntry("citrus.openapi.request.fill.random.values", "CITRUS_OPENAPI_REQUEST_FILL_RANDOM_VALUES",
                "REQUIRED", STRING,
                "Auto-fill random values in request (REQUIRED, ALL, or NONE)"));
        settings.add(new SettingEntry("citrus.openapi.response.fill.random.values", "CITRUS_OPENAPI_RESPONSE_FILL_RANDOM_VALUES",
                "REQUIRED", STRING,
                "Auto-fill random values in response (REQUIRED, ALL, or NONE)"));
        settings.add(new SettingEntry("citrus.openapi.validation.policy", "CITRUS_OPENAPI_VALIDATION_POLICY",
                "REPORT", STRING,
                "OpenAPI validation policy (REPORT, STRICT, or LENIENT)"));

        return new SettingsGroup("openapi", "OpenAPI Settings",
                "OpenAPI connector settings from OpenApiSettings", "citrus-openapi", settings);
    }

    private static SettingsGroup createJsonSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.json.message.validation.strict", "CITRUS_JSON_MESSAGE_VALIDATION_STRICT",
                "true", BOOLEAN,
                "Enable strict JSON message validation (no additional properties allowed)"));
        settings.add(new SettingEntry("citrus.json.permissive.mode", "CITRUS_JSON_PERMISSIVE_MODE",
                null, INT,
                "JSON parser permissive mode flags"));

        return new SettingsGroup("json", "JSON Validation Settings",
                "JSON validation settings from JsonSettings", "citrus-validation-json", settings);
    }

    private static SettingsGroup createYamlValidationSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.yaml.message.validation.strict", "CITRUS_YAML_MESSAGE_VALIDATION_STRICT",
                "true", BOOLEAN,
                "Enable strict YAML message validation"));

        return new SettingsGroup("yaml-validation", "YAML Validation Settings",
                "YAML validation settings from YamlSettings", "citrus-validation-yaml", settings);
    }

    private static SettingsGroup createJBangSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.jbang.app", "CITRUS_JBANG_APP",
                "citrus@citrusframework/citrus", STRING,
                "JBang application identifier for Citrus"));
        settings.add(new SettingEntry("citrus.jbang.work.dir", "CITRUS_JBANG_WORK_DIR",
                ".citrus-jbang", STRING,
                "Working directory for JBang operations"));
        settings.add(new SettingEntry("citrus.jbang.auto.download", "CITRUS_JBANG_AUTO_DOWNLOAD",
                "true", BOOLEAN,
                "Auto-download JBang if not installed"));
        settings.add(new SettingEntry("citrus.jbang.download.url", "CITRUS_JBANG_DOWNLOAD_URL",
                "https://jbang.dev/releases/latest/download/jbang.zip", STRING,
                "URL for downloading JBang"));
        settings.add(new SettingEntry("citrus.jbang.auto.trust", "CITRUS_JBANG_AUTO_TRUST",
                "true", BOOLEAN,
                "Auto-trust JBang sources"));
        settings.add(new SettingEntry("citrus.jbang.trust.url", "CITRUS_JBANG_TRUST_URL",
                "https://github.com/citrusframework/citrus/", STRING,
                "Trusted URL for JBang operations"));
        settings.add(new SettingEntry("citrus.jbang.dump.process.output", "CITRUS_JBANG_DUMP_PROCESS_OUTPUT",
                "false", BOOLEAN,
                "Dump JBang process output"));
        settings.add(new SettingEntry("citrus.jbang.executable", "CITRUS_JBANG_EXECUTABLE",
                "", STRING,
                "Path to JBang executable"));

        return new SettingsGroup("jbang", "JBang Settings",
                "JBang connector settings from JBangSettings", "citrus-jbang-connector", settings);
    }

    private static SettingsGroup createKubernetesSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.kubernetes.enabled", "CITRUS_KUBERNETES_ENABLED",
                "true", BOOLEAN,
                "Enable/disable Kubernetes support"));
        settings.add(new SettingEntry("citrus.kubernetes.namespace", "CITRUS_KUBERNETES_NAMESPACE",
                "default", STRING,
                "Kubernetes namespace for test operations"));
        settings.add(new SettingEntry("citrus.kubernetes.cluster.type", "CITRUS_KUBERNETES_CLUSTER_TYPE",
                "KUBERNETES", STRING,
                "Cluster type (KUBERNETES, OPENSHIFT, KIND, MINIKUBE)"));
        settings.add(new SettingEntry("citrus.kubernetes.auto.remove.resources", "CITRUS_KUBERNETES_AUTO_REMOVE_RESOURCES",
                "true", BOOLEAN,
                "Auto-remove Kubernetes resources after test"));
        settings.add(new SettingEntry("citrus.kubernetes.use.default.actor", "CITRUS_KUBERNETES_USE_DEFAULT_ACTOR",
                "false", BOOLEAN,
                "Use default test actor for Kubernetes actions"));
        settings.add(new SettingEntry("citrus.kubernetes.auto.create.server.binding", "CITRUS_KUBERNETES_AUTO_CREATE_SERVER_BINDING",
                "true", BOOLEAN,
                "Auto-create server binding for Kubernetes services"));
        settings.add(new SettingEntry("citrus.kubernetes.service.name", "CITRUS_KUBERNETES_SERVICE_NAME",
                "citrus-k8s-service", STRING,
                "Default Kubernetes service name"));
        settings.add(new SettingEntry("citrus.kubernetes.service.port", "CITRUS_KUBERNETES_SERVICE_PORT",
                "8080", INT,
                "Default Kubernetes service port"));
        settings.add(new SettingEntry("citrus.kubernetes.service.timeout", "CITRUS_KUBERNETES_SERVICE_TIMEOUT",
                "2000", LONG,
                "Timeout for Kubernetes service operations in milliseconds"));
        settings.add(new SettingEntry("citrus.kubernetes.connect.timeout", "CITRUS_KUBERNETES_CONNECT_TIMEOUT",
                "5000", LONG,
                "Kubernetes client connection timeout in milliseconds"));
        settings.add(new SettingEntry("citrus.kubernetes.max.attempts", "CITRUS_KUBERNETES_MAX_ATTEMPTS",
                "150", INT,
                "Maximum retry attempts for Kubernetes operations"));
        settings.add(new SettingEntry("citrus.kubernetes.delay.between.attempts", "CITRUS_KUBERNETES_DELAY_BETWEEN_ATTEMPTS",
                "2000", LONG,
                "Delay between retry attempts in milliseconds"));
        settings.add(new SettingEntry("citrus.kubernetes.print.pod.logs", "CITRUS_KUBERNETES_PRINT_POD_LOGS",
                "true", BOOLEAN,
                "Print pod logs during test execution"));
        settings.add(new SettingEntry("citrus.kubernetes.watch.logs.timeout", "CITRUS_KUBERNETES_WATCH_LOGS_TIMEOUT",
                "60000", LONG,
                "Timeout for watching pod logs in milliseconds"));
        settings.add(new SettingEntry("citrus.kubernetes.default.labels", "CITRUS_KUBERNETES_DEFAULT_LABELS",
                "app=citrus", STRING,
                "Default labels for Kubernetes resources"));
        settings.add(new SettingEntry("citrus.kubernetes.api.version", "CITRUS_KUBERNETES_API_VERSION",
                "v1", STRING,
                "Kubernetes API version"));
        settings.add(new SettingEntry("citrus.kubernetes.cluster.wildcard.domain", "CITRUS_KUBERNETES_CLUSTER_WILDCARD_DOMAIN",
                "<namespace>.svc.cluster.local", STRING,
                "Cluster wildcard domain pattern"));
        settings.add(new SettingEntry("citrus.kubernetes.test.id.label", "CITRUS_KUBERNETES_TEST_ID_LABEL",
                "citrusframework.org/test-id", STRING,
                "Label key for test ID on Kubernetes resources"));

        return new SettingsGroup("kubernetes", "Kubernetes Settings",
                "Kubernetes connector settings from KubernetesSettings", "citrus-kubernetes", settings);
    }

    private static SettingsGroup createKnativeSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.knative.namespace", "CITRUS_KNATIVE_NAMESPACE",
                null, STRING,
                "Knative namespace (falls back to Kubernetes namespace)"));
        settings.add(new SettingEntry("citrus.knative.api.version", "CITRUS_KNATIVE_API_VERSION",
                "v1", STRING,
                "Knative API version"));
        settings.add(new SettingEntry("citrus.knative.broker.name", "CITRUS_KNATIVE_BROKER_NAME",
                "default", STRING,
                "Knative broker name"));
        settings.add(new SettingEntry("citrus.knative.broker.host", "CITRUS_KNATIVE_BROKER_HOST",
                "broker-ingress.knative-eventing.svc.cluster.local", STRING,
                "Knative broker host address"));
        settings.add(new SettingEntry("citrus.knative.broker.port", "CITRUS_KNATIVE_BROKER_PORT",
                "8080", STRING,
                "Knative broker port"));
        settings.add(new SettingEntry("citrus.knative.broker.url", "CITRUS_KNATIVE_BROKER_URL",
                null, STRING,
                "Full Knative broker URL (overrides host/port)"));
        settings.add(new SettingEntry("citrus.knative.service.name", "CITRUS_KNATIVE_SERVICE_NAME",
                "citrus-knative-service", STRING,
                "Default Knative service name"));
        settings.add(new SettingEntry("citrus.knative.service.port", "CITRUS_KNATIVE_SERVICE_PORT",
                null, INT,
                "Knative service port (falls back to Kubernetes service port)"));
        settings.add(new SettingEntry("citrus.knative.event.producer.timeout", "CITRUS_KNATIVE_EVENT_PRODUCER_TIMEOUT",
                "2000", LONG,
                "Timeout for Knative event producer in milliseconds"));
        settings.add(new SettingEntry("citrus.knative.event.consumer.timeout", "CITRUS_KNATIVE_EVENT_CONSUMER_TIMEOUT",
                "2000", LONG,
                "Timeout for Knative event consumer in milliseconds"));
        settings.add(new SettingEntry("citrus.knative.auto.remove.resources", "CITRUS_KNATIVE_AUTO_REMOVE_RESOURCES",
                "true", BOOLEAN,
                "Auto-remove Knative resources after test"));
        settings.add(new SettingEntry("citrus.knative.verify.broker.resources", "CITRUS_KNATIVE_VERIFY_BROKER_RESPONSE",
                "true", BOOLEAN,
                "Verify Knative broker response status"));

        return new SettingsGroup("knative", "Knative Settings",
                "Knative connector settings from KnativeSettings", "citrus-knative", settings);
    }

    private static SettingsGroup createKafkaEndpointSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.kafka.dynamic.consumer.group", "CITRUS_KAFKA_DYNAMIC_CONSUMER_GROUP",
                "true", BOOLEAN,
                "When enabled, dynamic Kafka endpoint URIs automatically use a unique consumer group for each endpoint"));

        return new SettingsGroup("kafka", "Kafka Endpoint Settings",
                "Kafka endpoint settings from KafkaSettings", "citrus-kafka", settings);
    }

    private static SettingsGroup createTestContainersSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.testcontainers.enabled", "CITRUS_TESTCONTAINERS_ENABLED",
                "true", BOOLEAN,
                "Enable/disable Testcontainers support"));
        settings.add(new SettingEntry("citrus.testcontainers.registry", "CITRUS_TESTCONTAINERS_REGISTRY",
                "docker.io", STRING,
                "Docker registry for container images"));
        settings.add(new SettingEntry("citrus.testcontainers.registry.mirror.enabled", "CITRUS_TESTCONTAINERS_REGISTRY_MIRROR_ENABLED",
                "false", BOOLEAN,
                "Enable Docker registry mirror"));
        settings.add(new SettingEntry("citrus.testcontainers.registry.mirror", "CITRUS_TESTCONTAINERS_REGISTRY_MIRROR",
                "mirror.gcr.io", STRING,
                "Docker registry mirror URL"));
        settings.add(new SettingEntry("citrus.testcontainers.auto.remove.resources", "CITRUS_TESTCONTAINERS_AUTO_REMOVE_RESOURCES",
                "true", BOOLEAN,
                "Auto-remove containers after test"));
        settings.add(new SettingEntry("citrus.testcontainers.kubedock.enabled", "CITRUS_TESTCONTAINERS_KUBEDOCK_ENABLED",
                "false", BOOLEAN,
                "Enable KubeDock for running containers on Kubernetes"));
        settings.add(new SettingEntry("citrus.testcontainers.startup.timeout", "CITRUS_TESTCONTAINERS_STARTUP_TIMEOUT",
                "180", INT,
                "Container startup timeout in seconds"));
        settings.add(new SettingEntry("citrus.testcontainers.connect.timeout", "CITRUS_TESTCONTAINERS_CONNECT_TIMEOUT",
                "5000", LONG,
                "Container connection timeout in milliseconds"));

        return new SettingsGroup("testcontainers", "Testcontainers Settings",
                "Testcontainers integration settings from TestContainersSettings", "citrus-testcontainers", settings);
    }

    private static SettingsGroup createKafkaSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.testcontainers.kafka.service.name", "CITRUS_TESTCONTAINERS_KAFKA_SERVICE_NAME",
                "citrus-kafka", STRING,
                "Kafka container service name"));
        settings.add(new SettingEntry("citrus.testcontainers.kafka.implementation", "CITRUS_TESTCONTAINERS_KAFKA_IMPLEMENTATION",
                "CONFLUENT", STRING,
                "Kafka implementation (CONFLUENT, APACHE, APACHE_NATIVE, STRIMZI)"));
        settings.add(new SettingEntry("citrus.testcontainers.kafka.container.name", "CITRUS_TESTCONTAINERS_KAFKA_CONTAINER_NAME",
                "kafkaContainer", STRING,
                "Kafka container bean name"));
        settings.add(new SettingEntry("citrus.testcontainers.kafka.startup.timeout", "CITRUS_TESTCONTAINERS_KAFKA_STARTUP_TIMEOUT",
                "180", INT,
                "Kafka container startup timeout in seconds"));
        settings.add(new SettingEntry("citrus.testcontainers.kafka.image.name", "CITRUS_TESTCONTAINERS_KAFKA_IMAGE_NAME",
                "confluentinc/cp-kafka", STRING,
                "Default Kafka container image name"));
        settings.add(new SettingEntry("citrus.testcontainers.kafka.version", "CITRUS_TESTCONTAINERS_KAFKA_VERSION",
                "7.9.5", STRING,
                "Default Kafka container image version"));

        return new SettingsGroup("testcontainers-kafka", "Kafka Testcontainers Settings",
                "Kafka container settings from KafkaSettings", "citrus-testcontainers", settings);
    }

    private static SettingsGroup createLocalStackSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.testcontainers.localstack.version", "CITRUS_TESTCONTAINERS_LOCALSTACK_VERSION",
                "4.14.0", STRING,
                "LocalStack container version"));
        settings.add(new SettingEntry("citrus.testcontainers.localstack.image.name", "CITRUS_TESTCONTAINERS_LOCALSTACK_IMAGE_NAME",
                "localstack/localstack", STRING,
                "LocalStack container image name"));
        settings.add(new SettingEntry("citrus.testcontainers.localstack.service.name", "CITRUS_TESTCONTAINERS_LOCALSTACK_SERVICE_NAME",
                "citrus-localstack", STRING,
                "LocalStack container service name"));
        settings.add(new SettingEntry("citrus.testcontainers.localstack.container.name", "CITRUS_TESTCONTAINERS_LOCALSTACK_CONTAINER_NAME",
                "aws2Container", STRING,
                "LocalStack container bean name"));
        settings.add(new SettingEntry("citrus.testcontainers.localstack.auto.create.clients", "CITRUS_TESTCONTAINERS_LOCALSTACK_AUTO_CREATE_CLIENTS",
                "true", BOOLEAN,
                "Auto-create AWS clients from LocalStack container"));
        settings.add(new SettingEntry("citrus.testcontainers.localstack.region", "CITRUS_TESTCONTAINERS_LOCALSTACK_REGION",
                "us-east-1", STRING,
                "AWS region for LocalStack"));
        settings.add(new SettingEntry("citrus.testcontainers.localstack.access.key", "CITRUS_TESTCONTAINERS_LOCALSTACK_ACCESS_KEY",
                "accesskey", STRING,
                "AWS access key for LocalStack"));
        settings.add(new SettingEntry("citrus.testcontainers.localstack.secret.key", "CITRUS_TESTCONTAINERS_LOCALSTACK_SECRET_KEY",
                "secretkey", STRING,
                "AWS secret key for LocalStack"));

        return new SettingsGroup("testcontainers-localstack", "LocalStack Testcontainers Settings",
                "LocalStack/AWS container settings from LocalStackSettings", "citrus-testcontainers", settings);
    }

    private static SettingsGroup createPostgreSQLSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.testcontainers.postgresql.image.name", "CITRUS_TESTCONTAINERS_POSTGRESQL_IMAGE_NAME",
                "postgres", STRING,
                "PostgreSQL container image name"));
        settings.add(new SettingEntry("citrus.testcontainers.postgresql.version", "CITRUS_TESTCONTAINERS_POSTGRESQL_VERSION",
                null, STRING,
                "PostgreSQL container version"));
        settings.add(new SettingEntry("citrus.testcontainers.postgresql.service.name", "CITRUS_TESTCONTAINERS_POSTGRESQL_SERVICE_NAME",
                "citrus-postgresql", STRING,
                "PostgreSQL container service name"));
        settings.add(new SettingEntry("citrus.testcontainers.postgresql.container.name", "CITRUS_TESTCONTAINERS_POSTGRESQL_CONTAINER_NAME",
                "postgreSQLContainer", STRING,
                "PostgreSQL container bean name"));
        settings.add(new SettingEntry("citrus.testcontainers.postgresql.db.name", "CITRUS_TESTCONTAINERS_POSTGRESQL_DB_NAME",
                "test", STRING,
                "Default database name"));
        settings.add(new SettingEntry("citrus.testcontainers.postgresql.username", "CITRUS_TESTCONTAINERS_POSTGRESQL_USERNAME",
                "test", STRING,
                "Default database username"));
        settings.add(new SettingEntry("citrus.testcontainers.postgresql.password", "CITRUS_TESTCONTAINERS_POSTGRESQL_PASSWORD",
                "test", STRING,
                "Default database password"));
        settings.add(new SettingEntry("citrus.testcontainers.postgresql.startup.timeout", "CITRUS_TESTCONTAINERS_POSTGRESQL_STARTUP_TIMEOUT",
                "180", INT,
                "PostgreSQL container startup timeout in seconds"));

        return new SettingsGroup("testcontainers-postgresql", "PostgreSQL Testcontainers Settings",
                "PostgreSQL container settings from PostgreSQLSettings", "citrus-testcontainers", settings);
    }

    private static SettingsGroup createMongoDBSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.testcontainers.mongodb.image.name", "CITRUS_TESTCONTAINERS_MONGODB_IMAGE_NAME",
                "mongo", STRING,
                "MongoDB container image name"));
        settings.add(new SettingEntry("citrus.testcontainers.mongodb.version", "CITRUS_TESTCONTAINERS_MONGODB_VERSION",
                "8.2.2", STRING,
                "MongoDB container version"));
        settings.add(new SettingEntry("citrus.testcontainers.mongodb.service.name", "CITRUS_TESTCONTAINERS_MONGODB_SERVICE_NAME",
                "citrus-mongodb", STRING,
                "MongoDB container service name"));
        settings.add(new SettingEntry("citrus.testcontainers.mongodb.container.name", "CITRUS_TESTCONTAINERS_MONGODB_CONTAINER_NAME",
                "mongoDBContainer", STRING,
                "MongoDB container bean name"));
        settings.add(new SettingEntry("citrus.testcontainers.mongodb.startup.timeout", "CITRUS_TESTCONTAINERS_MONGODB_STARTUP_TIMEOUT",
                "180", INT,
                "MongoDB container startup timeout in seconds"));

        return new SettingsGroup("testcontainers-mongodb", "MongoDB Testcontainers Settings",
                "MongoDB container settings from MongoDBSettings", "citrus-testcontainers", settings);
    }

    private static SettingsGroup createRedpandaSettings() {
        List<SettingEntry> settings = new ArrayList<>();

        settings.add(new SettingEntry("citrus.testcontainers.redpanda.version", "CITRUS_TESTCONTAINERS_REDPANDA_VERSION",
                "v25.3.2", STRING,
                "Redpanda container version"));
        settings.add(new SettingEntry("citrus.testcontainers.redpanda.image.name", "CITRUS_TESTCONTAINERS_REDPANDA_IMAGE_NAME",
                "redpandadata/redpanda", STRING,
                "Redpanda container image name"));
        settings.add(new SettingEntry("citrus.testcontainers.redpanda.service.name", "CITRUS_TESTCONTAINERS_REDPANDA_SERVICE_NAME",
                "citrus-redpanda", STRING,
                "Redpanda container service name"));
        settings.add(new SettingEntry("citrus.testcontainers.redpanda.container.name", "CITRUS_TESTCONTAINERS_REDPANDA_CONTAINER_NAME",
                "redpandaContainer", STRING,
                "Redpanda container bean name"));
        settings.add(new SettingEntry("citrus.testcontainers.redpanda.startup.timeout", "CITRUS_TESTCONTAINERS_REDPANDA_STARTUP_TIMEOUT",
                "180", INT,
                "Redpanda container startup timeout in seconds"));

        return new SettingsGroup("testcontainers-redpanda", "Redpanda Testcontainers Settings",
                "Redpanda container settings from RedpandaSettings", "citrus-testcontainers", settings);
    }

    public record SettingsGroup(String name, String title, String description, String module,
                                List<SettingEntry> settings) {
    }

    public record SettingEntry(String property, String envVariable, String defaultValue, String type,
                               String description) {
    }
}
