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

package org.citrusframework.agent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.citrusframework.CitrusSettings;

public final class CitrusAgentSettings {

    private static final String AGENT_PROPERTY_PREFIX = "citrus.agent.";
    private static final String AGENT_ENV_PREFIX = "CITRUS_AGENT_";

    private static final String AGENT_NAME_PROPERTY = AGENT_PROPERTY_PREFIX + "name";
    private static final String AGENT_NAME_ENV = AGENT_ENV_PREFIX + "NAME";
    private static final String AGENT_NAME_DEFAULT = "citrus-agent";

    private static final String TEST_ENGINE_PROPERTY = AGENT_PROPERTY_PREFIX + "test.engine";
    private static final String TEST_ENGINE_ENV = AGENT_ENV_PREFIX + "TEST_ENGINE";
    private static final String TEST_ENGINE_DEFAULT = "junit5";

    private static final String WORK_DIRECTORY_PROPERTY = AGENT_PROPERTY_PREFIX + "work.directory";
    private static final String WORK_DIRECTORY_ENV = AGENT_ENV_PREFIX + "WORK_DIRECTORY";

    private static final String SERVER_PORT_PROPERTY = AGENT_PROPERTY_PREFIX + "server.port";
    private static final String SERVER_PORT_ENV = AGENT_ENV_PREFIX + "SERVER_PORT";
    private static final String SERVER_PORT_DEFAULT = "4567";

    private static final String TIME_TO_LIVE_PROPERTY = AGENT_PROPERTY_PREFIX + "time.to.live";
    private static final String TIME_TO_LIVE_ENV = AGENT_ENV_PREFIX + "TIME_TO_LIVE";
    private static final String TIME_TO_LIVE_DEFAULT = "-1";

    private static final String SYSTEM_EXIT_PROPERTY = AGENT_PROPERTY_PREFIX + "system.exit";
    private static final String SYSTEM_EXIT_ENV = AGENT_ENV_PREFIX + "SYSTEM_EXIT";
    private static final String SYSTEM_EXIT_DEFAULT = "false";

    private static final String SKIP_TESTS_PROPERTY = AGENT_PROPERTY_PREFIX + "skip.tests";
    private static final String SKIP_TESTS_ENV = AGENT_ENV_PREFIX + "SKIP_TESTS";
    private static final String SKIP_TESTS_DEFAULT = "false";

    private static final String VERBOSE_PROPERTY = AGENT_PROPERTY_PREFIX + "verbose";
    private static final String VERBOSE_ENV = AGENT_ENV_PREFIX + "VERBOSE";
    private static final String VERBOSE_DEFAULT = "true";

    private static final String RESET_PROPERTY = AGENT_PROPERTY_PREFIX + "reset";
    private static final String RESET_ENV = AGENT_ENV_PREFIX + "RESET";
    private static final String RESET_DEFAULT = "true";

    private static final String INCLUDES_PROPERTY = AGENT_PROPERTY_PREFIX + "includes";
    private static final String INCLUDES_ENV = AGENT_ENV_PREFIX + "INCLUDES";
    private static final String[] INCLUDES_DEFAULT = new String[] { "^.*IT$", "^.*ITCase$", "^IT.*$" };

    private static final String PACKAGES_PROPERTY = AGENT_PROPERTY_PREFIX + "packages";
    private static final String PACKAGES_ENV = AGENT_ENV_PREFIX + "PACKAGES";

    private static final String DEFAULT_PROPERTIES_PROPERTY = AGENT_PROPERTY_PREFIX + "default.properties";
    private static final String DEFAULT_PROPERTIES_ENV = AGENT_ENV_PREFIX + "DEFAULT_PROPERTIES";

    private static final String TEST_SOURCES_PROPERTY = AGENT_PROPERTY_PREFIX + "test.sources";
    private static final String TEST_SOURCES_ENV = AGENT_ENV_PREFIX + "TEST_SOURCES";

    private static final String CONFIG_CLASS_PROPERTY = AGENT_PROPERTY_PREFIX + "config.class";
    private static final String CONFIG_CLASS_ENV = AGENT_ENV_PREFIX + "CONFIG_CLASS";

    private static final String TEST_JAR_PROPERTY = AGENT_PROPERTY_PREFIX + "test.jar";
    private static final String TEST_JAR_ENV = AGENT_ENV_PREFIX + "TEST_JAR";
    private static final String TEST_JAR_DEFAULT = "classpath:citrus-agent-tests.jar";

    private static final String CORS_ALLOWED_ORIGIN_PROPERTY = AGENT_PROPERTY_PREFIX + "cors.allowed.origin";
    private static final String CORS_ALLOWED_ORIGIN_ENV = AGENT_ENV_PREFIX + "CORS_ALLOWED_ORIGIN";
    private static final String CORS_ALLOWED_ORIGIN_DEFAULT = "https?://localhost:\\d+";

    private static final String MODULES_PROPERTY = AGENT_PROPERTY_PREFIX + "modules";
    private static final String MODULES_ENV = AGENT_ENV_PREFIX + "MODULES";

    private static final String DEPENDENCIES_PROPERTY = AGENT_PROPERTY_PREFIX + "dependencies";
    private static final String DEPENDENCIES_ENV = AGENT_ENV_PREFIX + "DEPENDENCIES";

    private static final String OFFLINE_PROPERTY = AGENT_PROPERTY_PREFIX + "offline";
    private static final String OFFLINE_ENV = AGENT_ENV_PREFIX + "OFFLINE";
    private static final String OFFLINE_DEFAULT = "false";

    private static final String INSPECT_CODE_PROPERTY = AGENT_PROPERTY_PREFIX + "inspect.code";
    private static final String INSPECT_CODE_ENV = AGENT_ENV_PREFIX + "INSPECT_CODE";
    private static final String INSPECT_CODE_DEFAULT = "true";

    private CitrusAgentSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Citrus agent name.
     */
    public static String getAgentName() {
        return CitrusSettings.getPropertyEnvOrDefault(AGENT_NAME_PROPERTY, AGENT_NAME_ENV, AGENT_NAME_DEFAULT);
    }

    public static String getTestEngine() {
        return CitrusSettings.getPropertyEnvOrDefault(TEST_ENGINE_PROPERTY, TEST_ENGINE_ENV, TEST_ENGINE_DEFAULT);
    }

    public static String getWorkDir() {
        return System.getProperty(WORK_DIRECTORY_PROPERTY, System.getenv(WORK_DIRECTORY_ENV));
    }

    public static int getServerPort() {
        return Integer.parseInt(CitrusSettings.getPropertyEnvOrDefault(SERVER_PORT_PROPERTY, SERVER_PORT_ENV, SERVER_PORT_DEFAULT));
    }

    public static int getTimeToLive() {
        return Integer.parseInt(CitrusSettings.getPropertyEnvOrDefault(TIME_TO_LIVE_PROPERTY, TIME_TO_LIVE_ENV, TIME_TO_LIVE_DEFAULT));
    }

    public static boolean isSystemExit() {
        return Boolean.parseBoolean(CitrusSettings.getPropertyEnvOrDefault(SYSTEM_EXIT_PROPERTY, SYSTEM_EXIT_ENV, SYSTEM_EXIT_DEFAULT));
    }

    public static boolean isSkipTests() {
        return Boolean.parseBoolean(CitrusSettings.getPropertyEnvOrDefault(SKIP_TESTS_PROPERTY, SKIP_TESTS_ENV, SKIP_TESTS_DEFAULT));
    }

    public static boolean isVerbose() {
        return Boolean.parseBoolean(CitrusSettings.getPropertyEnvOrDefault(VERBOSE_PROPERTY, VERBOSE_ENV, VERBOSE_DEFAULT));
    }

    public static boolean isReset() {
        return Boolean.parseBoolean(CitrusSettings.getPropertyEnvOrDefault(RESET_PROPERTY, RESET_ENV, RESET_DEFAULT));
    }

    public static String[] getPackages() {
        return Optional.ofNullable(System.getProperty(PACKAGES_PROPERTY, System.getenv(PACKAGES_ENV)))
                .map(tokens -> tokens.replaceAll("\\s", ""))
                .map(tokens -> tokens.split(","))
                .orElseGet(() -> new String[]{});
    }

    public static Map<String, String> getDefaultProperties() {
        return Optional.ofNullable(System.getProperty(DEFAULT_PROPERTIES_PROPERTY, System.getenv(DEFAULT_PROPERTIES_ENV)))
                .map(tokens -> tokens.replaceAll("\\s", ""))
                .map(tokens -> Arrays.stream(tokens.split(","))
                .filter(token -> token.contains("="))
                .map(token -> token.split("=", 2))
                .filter(keyValue -> keyValue.length == 2)
                .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1])))
                .orElseGet(HashMap::new);
    }

    public static String[] getTestSources() {
        return Optional.ofNullable(System.getProperty(TEST_SOURCES_PROPERTY, System.getenv(TEST_SOURCES_ENV)))
                .map(tokens -> tokens.replaceAll("\\s", ""))
                .map(tokens -> tokens.split(","))
                .orElseGet(() -> new String[]{});
    }

    public static String[] getIncludes() {
        return Optional.ofNullable(System.getProperty(INCLUDES_PROPERTY, System.getenv(INCLUDES_ENV)))
                .map(tokens -> tokens.replaceAll("\\s", ""))
                .map(tokens -> tokens.split(","))
                .orElse(INCLUDES_DEFAULT);
    }

    public static String getConfigClass() {
        return System.getProperty(CONFIG_CLASS_PROPERTY, System.getenv(CONFIG_CLASS_ENV));
    }

    public static String getTestJar() {
        return CitrusSettings.getPropertyEnvOrDefault(TEST_JAR_PROPERTY, TEST_JAR_ENV, TEST_JAR_DEFAULT);
    }

    public static String getCorsAllowedOrigin() {
        return CitrusSettings.getPropertyEnvOrDefault(CORS_ALLOWED_ORIGIN_PROPERTY, CORS_ALLOWED_ORIGIN_ENV, CORS_ALLOWED_ORIGIN_DEFAULT);
    }

    /**
     * Gets Citrus modules that should be loaded as additional dependencies and added to the classpath.
     */
    public static Set<String> getModules() {
        return Arrays.stream(CitrusSettings.getPropertyEnvOrDefault(MODULES_PROPERTY, MODULES_ENV, "")
                .split(","))
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Gets additional dependencies in the form of Maven GAVs that should be added to the classpath.
     */
    public static Set<String> getDependencies() {
        return Arrays.stream(CitrusSettings.getPropertyEnvOrDefault(DEPENDENCIES_PROPERTY, DEPENDENCIES_ENV, "")
                .split(","))
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .collect(Collectors.toSet());
    }

    public static boolean isOffline() {
        return Boolean.parseBoolean(CitrusSettings.getPropertyEnvOrDefault(OFFLINE_PROPERTY, OFFLINE_ENV, OFFLINE_DEFAULT));
    }

    public static boolean isInspectCode() {
        return Boolean.parseBoolean(CitrusSettings.getPropertyEnvOrDefault(INSPECT_CODE_PROPERTY, INSPECT_CODE_ENV, INSPECT_CODE_DEFAULT));
    }


}
