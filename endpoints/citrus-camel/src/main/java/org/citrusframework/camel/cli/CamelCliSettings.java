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

package org.citrusframework.camel.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.citrusframework.jbang.JBangSettings;
import org.citrusframework.jbang.JBangSupport;
import org.citrusframework.jbang.ProcessLauncher;
import org.citrusframework.util.StringUtils;

public final class CamelCliSettings {

    private static final String CLI_PROPERTY_PREFIX = "citrus.camel.cli.";
    private static final String CLI_ENV_PREFIX = "CITRUS_CAMEL_CLI_";

    private static final String JBANG_PROPERTY_PREFIX = "citrus.camel.jbang.";
    private static final String JBANG_ENV_PREFIX = "CITRUS_CAMEL_JBANG_";

    private static final String WORK_DIR_PROPERTY = CLI_PROPERTY_PREFIX + "work.dir";
    private static final String WORK_DIR_ENV = CLI_ENV_PREFIX + "WORK_DIR";
    private static final String WORK_DIR_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "work.dir";
    private static final String WORK_DIR_ENV_FALLBACK = JBANG_ENV_PREFIX + "WORK_DIR";

    private static final String CAMEL_APP_PROPERTY = CLI_PROPERTY_PREFIX + "app";
    private static final String CAMEL_APP_ENV = CLI_ENV_PREFIX + "APP";
    private static final String CAMEL_APP_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "app";
    private static final String CAMEL_APP_ENV_FALLBACK = JBANG_ENV_PREFIX + "APP";
    private static final String CAMEL_APP_DEFAULT = "camel@apache/camel";

    private static final String CAMEL_VERSION_PROPERTY = CLI_PROPERTY_PREFIX + "version";
    private static final String CAMEL_VERSION_ENV = CLI_ENV_PREFIX + "VERSION";
    private static final String CAMEL_VERSION_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "version";
    private static final String CAMEL_VERSION_ENV_FALLBACK = JBANG_ENV_PREFIX + "VERSION";
    private static final String CAMEL_VERSION_DEFAULT = "latest";

    private static final String KAMELETS_VERSION_PROPERTY = CLI_PROPERTY_PREFIX + "kamelets.version";
    private static final String KAMELETS_VERSION_ENV = CLI_ENV_PREFIX + "KAMELETS_VERSION";
    private static final String KAMELETS_VERSION_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "kamelets.version";
    private static final String KAMELETS_VERSION_ENV_FALLBACK = JBANG_ENV_PREFIX + "KAMELETS_VERSION";
    private static final String KAMELETS_VERSION_DEFAULT = "";

    private static final String KAMELETS_LOCAL_DIR_PROPERTY = CLI_PROPERTY_PREFIX + "kamelets.local.dir";
    private static final String KAMELETS_LOCAL_DIR_ENV = CLI_ENV_PREFIX + "KAMELETS_LOCAL_DIR";
    private static final String KAMELETS_LOCAL_DIR_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "kamelets.local.dir";
    private static final String KAMELETS_LOCAL_DIR_ENV_FALLBACK = JBANG_ENV_PREFIX + "KAMELETS_LOCAL_DIR";

    private static final String TRUST_URL_PROPERTY = CLI_PROPERTY_PREFIX + "trust.url";
    private static final String TRUST_URL_ENV = CLI_ENV_PREFIX + "TRUST_URL";
    private static final String TRUST_URL_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "trust.url";
    private static final String TRUST_URL_ENV_FALLBACK = JBANG_ENV_PREFIX + "TRUST_URL";
    private static final String TRUST_URL_DEFAULT = "https://github.com/apache/camel/";

    private static final String CAMEL_DUMP_INTEGRATION_OUTPUT_PROPERTY = CLI_PROPERTY_PREFIX + "dump.integration.output";
    private static final String CAMEL_DUMP_INTEGRATION_OUTPUT_ENV = CLI_ENV_PREFIX + "DUMP_INTEGRATION_OUTPUT";
    private static final String CAMEL_DUMP_INTEGRATION_OUTPUT_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "dump.integration.output";
    private static final String CAMEL_DUMP_INTEGRATION_OUTPUT_ENV_FALLBACK = JBANG_ENV_PREFIX + "DUMP_INTEGRATION_OUTPUT";
    private static final String CAMEL_DUMP_INTEGRATION_OUTPUT_DEFAULT = "false";

    private static final String VERBOSE_PROPERTY = CLI_PROPERTY_PREFIX + "verbose";
    private static final String VERBOSE_ENV = CLI_ENV_PREFIX + "VERBOSE";
    private static final String VERBOSE_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "verbose";
    private static final String VERBOSE_ENV_FALLBACK = JBANG_ENV_PREFIX + "VERBOSE";
    private static final String VERBOSE_DEFAULT = "false";

    private static final String AUTO_REMOVE_RESOURCES_PROPERTY = CLI_PROPERTY_PREFIX + "auto.remove.resources";
    private static final String AUTO_REMOVE_RESOURCES_ENV = CLI_ENV_PREFIX + "AUTO_REMOVE_RESOURCES";
    private static final String AUTO_REMOVE_RESOURCES_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "auto.remove.resources";
    private static final String AUTO_REMOVE_RESOURCES_ENV_FALLBACK = JBANG_ENV_PREFIX + "AUTO_REMOVE_RESOURCES";
    private static final String AUTO_REMOVE_RESOURCES_DEFAULT = "true";

    private static final String AUTO_REMOVE_PLUGINS_PROPERTY = CLI_PROPERTY_PREFIX + "auto.remove.plugins";
    private static final String AUTO_REMOVE_PLUGINS_ENV = CLI_ENV_PREFIX + "AUTO_REMOVE_PLUGINS";
    private static final String AUTO_REMOVE_PLUGINS_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "auto.remove.plugins";
    private static final String AUTO_REMOVE_PLUGINS_ENV_FALLBACK = JBANG_ENV_PREFIX + "AUTO_REMOVE_PLUGINS";
    private static final String AUTO_REMOVE_PLUGINS_DEFAULT = "false";

    private static final String WAIT_FOR_RUNNING_STATE_PROPERTY = CLI_PROPERTY_PREFIX + "wait.for.running.state";
    private static final String WAIT_FOR_RUNNING_STATE_ENV = CLI_ENV_PREFIX + "WAIT_FOR_RUNNING_STATE";
    private static final String WAIT_FOR_RUNNING_STATE_PROPERTY_FALLBACK = JBANG_PROPERTY_PREFIX + "wait.for.running.state";
    private static final String WAIT_FOR_RUNNING_STATE_ENV_FALLBACK = JBANG_ENV_PREFIX + "WAIT_FOR_RUNNING_STATE";
    private static final String WAIT_FOR_RUNNING_STATE_DEFAULT = "true";

    private static final String CLI_TYPE_PROPERTY = CLI_PROPERTY_PREFIX + "type";
    private static final String CLI_TYPE_ENV = CLI_ENV_PREFIX + "TYPE";
    private static final String CLI_TYPE_DEFAULT = "jbang";

    private static final String LAUNCHER_JAR_PATH_PROPERTY = CLI_PROPERTY_PREFIX + "launcher.jar.path";
    private static final String LAUNCHER_JAR_PATH_ENV = CLI_ENV_PREFIX + "LAUNCHER_JAR_PATH";
    private static final String LAUNCHER_JAR_PATH_SYSTEM_PROPERTY_FALLBACK = "camel.launcher.jar";

    private CamelCliSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Resolves a setting value by checking the new CLI property/env first, then falling back to the fallback JBang property/env.
     */
    private static String resolve(String property, String env, String fallbackProperty, String fallbackEnv) {
        String value = System.getProperty(property);
        if (value != null) {
            return value;
        }

        value = System.getenv(env);
        if (value != null) {
            return value;
        }

        value = System.getProperty(fallbackProperty);
        if (value != null) {
            return value;
        }

        return System.getenv(fallbackEnv);
    }

    /**
     * Camel CLI local work dir.
     */
    public static Path getWorkDir() {
        String workDir = Optional.ofNullable(resolve(WORK_DIR_PROPERTY, WORK_DIR_ENV,
                WORK_DIR_PROPERTY_FALLBACK, WORK_DIR_ENV_FALLBACK)).orElse("");

        if (!StringUtils.hasText(workDir)) {
            return JBangSettings.getWorkDir();
        }

        Path path = Paths.get(workDir);
        if (path.isAbsolute()) {
            return path.toAbsolutePath();
        } else {
            return Paths.get("").toAbsolutePath().resolve(workDir).toAbsolutePath();
        }
    }

    /**
     * Camel CLI local Kamelets dir.
     */
    public static Path getKameletsLocalDir() {
        return Optional.ofNullable(resolve(KAMELETS_LOCAL_DIR_PROPERTY, KAMELETS_LOCAL_DIR_ENV,
                KAMELETS_LOCAL_DIR_PROPERTY_FALLBACK, KAMELETS_LOCAL_DIR_ENV_FALLBACK)).map(dir -> {
            Path path = Paths.get(dir);
            if (path.isAbsolute()) {
                return path.toAbsolutePath();
            } else {
                return getWorkDir().resolve(dir).toAbsolutePath();
            }
        }).orElse(null);
    }

    /**
     * Camel CLI trust URLs.
     */
    public static String[] getTrustUrl() {
        return Optional.ofNullable(resolve(TRUST_URL_PROPERTY, TRUST_URL_ENV,
                TRUST_URL_PROPERTY_FALLBACK, TRUST_URL_ENV_FALLBACK))
                .orElse(TRUST_URL_DEFAULT).split(",");
    }

    /**
     * When set to true Camel CLI process output for integrations will be redirected to a file in the current working directory.
     */
    public static boolean isDumpIntegrationOutput() {
        return Boolean.parseBoolean(Optional.ofNullable(resolve(CAMEL_DUMP_INTEGRATION_OUTPUT_PROPERTY, CAMEL_DUMP_INTEGRATION_OUTPUT_ENV,
                CAMEL_DUMP_INTEGRATION_OUTPUT_PROPERTY_FALLBACK, CAMEL_DUMP_INTEGRATION_OUTPUT_ENV_FALLBACK))
                .orElse(CAMEL_DUMP_INTEGRATION_OUTPUT_DEFAULT));
    }

    /**
     * Camel CLI app name.
     */
    public static String getCamelApp() {
        return Optional.ofNullable(resolve(CAMEL_APP_PROPERTY, CAMEL_APP_ENV,
                CAMEL_APP_PROPERTY_FALLBACK, CAMEL_APP_ENV_FALLBACK))
                .orElse(CAMEL_APP_DEFAULT);
    }

    /**
     * Camel CLI version.
     */
    public static String getCamelVersion() {
        return Optional.ofNullable(resolve(CAMEL_VERSION_PROPERTY, CAMEL_VERSION_ENV,
                CAMEL_VERSION_PROPERTY_FALLBACK, CAMEL_VERSION_ENV_FALLBACK))
                .orElse(CAMEL_VERSION_DEFAULT);
    }

    /**
     * Kamelets version used by the Camel CLI runtime.
     */
    public static String getKameletsVersion() {
        return Optional.ofNullable(resolve(KAMELETS_VERSION_PROPERTY, KAMELETS_VERSION_ENV,
                KAMELETS_VERSION_PROPERTY_FALLBACK, KAMELETS_VERSION_ENV_FALLBACK))
                .orElse(KAMELETS_VERSION_DEFAULT);
    }

    /**
     * When set to true Camel CLI will print detailed messages
     * (e.g. while exporting to Maven projects and building and deploying Kubernetes container images).
     */
    public static boolean isVerbose() {
        return Boolean.parseBoolean(Optional.ofNullable(resolve(VERBOSE_PROPERTY, VERBOSE_ENV,
                VERBOSE_PROPERTY_FALLBACK, VERBOSE_ENV_FALLBACK))
                .orElse(VERBOSE_DEFAULT));
    }

    /**
     * When set to true Camel CLI resources created during the test are
     * automatically removed after the test.
     */
    public static boolean isAutoRemoveResources() {
        return Boolean.parseBoolean(Optional.ofNullable(resolve(AUTO_REMOVE_RESOURCES_PROPERTY, AUTO_REMOVE_RESOURCES_ENV,
                AUTO_REMOVE_RESOURCES_PROPERTY_FALLBACK, AUTO_REMOVE_RESOURCES_ENV_FALLBACK))
                .orElse(AUTO_REMOVE_RESOURCES_DEFAULT));
    }

    /**
     * When set to true Camel CLI plugins added during the test are
     * automatically removed after the test.
     */
    public static boolean isAutoRemovePlugins() {
        return Boolean.parseBoolean(Optional.ofNullable(resolve(AUTO_REMOVE_PLUGINS_PROPERTY, AUTO_REMOVE_PLUGINS_ENV,
                AUTO_REMOVE_PLUGINS_PROPERTY_FALLBACK, AUTO_REMOVE_PLUGINS_ENV_FALLBACK))
                .orElse(AUTO_REMOVE_PLUGINS_DEFAULT));
    }

    /**
     * When set to true Camel CLI will automatically wait for each integration created to be in running state.
     */
    public static boolean isWaitForRunningState() {
        return Boolean.parseBoolean(Optional.ofNullable(resolve(WAIT_FOR_RUNNING_STATE_PROPERTY, WAIT_FOR_RUNNING_STATE_ENV,
                WAIT_FOR_RUNNING_STATE_PROPERTY_FALLBACK, WAIT_FOR_RUNNING_STATE_ENV_FALLBACK))
                .orElse(WAIT_FOR_RUNNING_STATE_DEFAULT));
    }

    /**
     * Camel CLI launcher type ("jbang" or "launcher").
     */
    public static String getCliType() {
        String value = System.getProperty(CLI_TYPE_PROPERTY);
        if (value != null) {
            return value;
        }

        value = System.getenv(CLI_TYPE_ENV);
        if (value != null) {
            return value;
        }

        return CLI_TYPE_DEFAULT;
    }

    /**
     * Path to the Camel Launcher jar file. Falls back to system property "camel.launcher.jar".
     */
    public static String getLauncherJarPath() {
        String value = System.getProperty(LAUNCHER_JAR_PATH_PROPERTY);
        if (value != null) {
            return value;
        }

        value = System.getenv(LAUNCHER_JAR_PATH_ENV);
        if (value != null) {
            return value;
        }

        return System.getProperty(LAUNCHER_JAR_PATH_SYSTEM_PROPERTY_FALLBACK);
    }

    /**
     * Creates a ProcessLauncher based on the configured CLI type.
     */
    public static ProcessLauncher createLauncher() {
        String type = getCliType();

        if ("launcher".equalsIgnoreCase(type)) {
            String jarPath = getLauncherJarPath();
            if (jarPath == null || jarPath.isBlank()) {
                throw new IllegalStateException(
                        "Camel CLI type is set to 'launcher' but no jar path is configured. " +
                        "Set '%s' system property, '%s' environment variable, or '%s' system property."
                                .formatted(LAUNCHER_JAR_PATH_PROPERTY, LAUNCHER_JAR_PATH_ENV, LAUNCHER_JAR_PATH_SYSTEM_PROPERTY_FALLBACK));
            }
            return new CamelLauncherSupport(jarPath);
        }

        return JBangSupport.jbang().app(getCamelApp());
    }
}
