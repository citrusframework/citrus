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
import java.util.stream.Collectors;

public final class CitrusAgentSettings {

    private static final String AGENT_PROPERTY_PREFIX = "citrus.agent.";
    private static final String AGENT_ENV_PREFIX = "CITRUS_AGENT_";

    private static final String AGENT_NAME_PROPERTY = AGENT_PROPERTY_PREFIX + "name";
    private static final String AGENT_NAME_ENV = AGENT_ENV_PREFIX + "NAME";
    private static final String AGENT_NAME_DEFAULT = "citrus-agent";

    private static final String TEST_ENGINE_PROPERTY = AGENT_PROPERTY_PREFIX + "test.engine";
    private static final String TEST_ENGINE_ENV = AGENT_ENV_PREFIX + "TEST_ENGINE";
    private static final String TEST_ENGINE_DEFAULT = "junit5";

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

    private CitrusAgentSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Citrus agent name.
     * @return
     */
    public static String getAgentName() {
        return System.getProperty(AGENT_NAME_PROPERTY,
                System.getenv(AGENT_NAME_ENV) != null ? System.getenv(AGENT_NAME_ENV) : AGENT_NAME_DEFAULT);
    }

    public static String getTestEngine() {
        return Optional.ofNullable(System.getProperty(TEST_ENGINE_PROPERTY, System.getenv(TEST_ENGINE_ENV)))
                .orElse(TEST_ENGINE_DEFAULT);
    }

    public static int getServerPort() {
        return Integer.parseInt(Optional.ofNullable(System.getProperty(SERVER_PORT_PROPERTY, System.getenv(SERVER_PORT_ENV)))
                .orElse(SERVER_PORT_DEFAULT));
    }

    public static int getTimeToLive() {
        return Integer.parseInt(Optional.ofNullable(System.getProperty(TIME_TO_LIVE_PROPERTY, System.getenv(TIME_TO_LIVE_ENV)))
                .orElse(TIME_TO_LIVE_DEFAULT));
    }

    public static boolean isSystemExit() {
        return Boolean.parseBoolean(Optional.ofNullable(System.getProperty(SYSTEM_EXIT_PROPERTY, System.getenv(SYSTEM_EXIT_ENV)))
                .orElse(SYSTEM_EXIT_DEFAULT));
    }

    public static boolean isSkipTests() {
        return Boolean.parseBoolean(Optional.ofNullable(System.getProperty(SKIP_TESTS_PROPERTY, System.getenv(SKIP_TESTS_ENV)))
                .orElse(SKIP_TESTS_DEFAULT));
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
        return Optional.ofNullable(System.getProperty(TEST_JAR_PROPERTY, System.getenv(TEST_JAR_ENV)))
                .orElse(TEST_JAR_DEFAULT);
    }

}
