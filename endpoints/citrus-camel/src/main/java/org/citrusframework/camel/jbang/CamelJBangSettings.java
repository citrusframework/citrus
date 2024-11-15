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

package org.citrusframework.camel.jbang;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.citrusframework.jbang.JBangSettings;
import org.citrusframework.util.StringUtils;

public final class CamelJBangSettings {

    private static final String JBANG_PROPERTY_PREFIX = "citrus.camel.jbang.";
    private static final String JBANG_ENV_PREFIX = "CITRUS_CAMEL_JBANG_";

    private static final String WORK_DIR_PROPERTY = JBANG_PROPERTY_PREFIX + "work.dir";
    private static final String WORK_DIR_ENV = JBANG_ENV_PREFIX + "WORK_DIR";

    private static final String CAMEL_APP_PROPERTY = JBANG_PROPERTY_PREFIX + "app";
    private static final String CAMEL_APP_ENV = JBANG_ENV_PREFIX + "APP";
    private static final String CAMEL_APP_DEFAULT = "camel@apache/camel";

    private static final String CAMEL_VERSION_PROPERTY = JBANG_PROPERTY_PREFIX + "version";
    private static final String CAMEL_VERSION_ENV = JBANG_ENV_PREFIX + "VERSION";
    private static final String CAMEL_VERSION_DEFAULT = "latest";

    private static final String KAMELETS_VERSION_PROPERTY = JBANG_PROPERTY_PREFIX + "kamelets.version";
    private static final String KAMELETS_VERSION_ENV = JBANG_ENV_PREFIX + "KAMELETS_VERSION";
    private static final String KAMELETS_VERSION_DEFAULT = "";

    private static final String KAMELETS_LOCAL_DIR_PROPERTY = JBANG_PROPERTY_PREFIX + "kamelets.local.dir";
    private static final String KAMELETS_LOCAL_DIR_ENV = JBANG_ENV_PREFIX + "KAMELETS_LOCAL_DIR";

    private static final String TRUST_URL_PROPERTY = JBANG_PROPERTY_PREFIX + "trust.url";
    private static final String TRUST_URL_ENV = JBANG_ENV_PREFIX + "TRUST_URL";
    private static final String TRUST_URL_DEFAULT = "https://github.com/apache/camel/";

    private static final String CAMEL_DUMP_INTEGRATION_OUTPUT_PROPERTY = JBANG_PROPERTY_PREFIX + "dump.integration.output";
    private static final String CAMEL_DUMP_INTEGRATION_OUTPUT_ENV = JBANG_ENV_PREFIX + "DUMP_INTEGRATION_OUTPUT";
    private static final String CAMEL_DUMP_INTEGRATION_OUTPUT_DEFAULT = "false";

    private static final String AUTO_REMOVE_RESOURCES_PROPERTY = JBANG_PROPERTY_PREFIX + "auto.remove.resources";
    private static final String AUTO_REMOVE_RESOURCES_ENV = JBANG_ENV_PREFIX + "AUTO_REMOVE_RESOURCES";
    private static final String AUTO_REMOVE_RESOURCES_DEFAULT = "true";

    private static final String WAIT_FOR_RUNNING_STATE_PROPERTY = JBANG_PROPERTY_PREFIX + "wait.for.running.state";
    private static final String WAIT_FOR_RUNNING_STATE_ENV = JBANG_ENV_PREFIX + "WAIT_FOR_RUNNING_STATE";
    private static final String WAIT_FOR_RUNNING_STATE_DEFAULT = "true";

    private CamelJBangSettings() {
        // prevent instantiation of utility class
    }

    /**
     * JBang local work dir.
     * @return
     */
    public static Path getWorkDir() {
        String workDir = Optional.ofNullable(System.getProperty(WORK_DIR_PROPERTY, System.getenv(WORK_DIR_ENV)))
                .orElse("");

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
     * JBang local Kamelets dir.
     * @return
     */
    public static Path getKameletsLocalDir() {
        return Optional.ofNullable(System.getProperty(KAMELETS_LOCAL_DIR_PROPERTY, System.getenv(KAMELETS_LOCAL_DIR_ENV))).map(dir -> {
            Path path = Paths.get(dir);
            if (path.isAbsolute()) {
                return path.toAbsolutePath();
            } else {
                return getWorkDir().resolve(dir).toAbsolutePath();
            }
        }).orElse(null);
    }

    /**
     * JBang trust URLs.
     * @return
     */
    public static String[] getTrustUrl() {
        return System.getProperty(TRUST_URL_PROPERTY,
                System.getenv(TRUST_URL_ENV) != null ? System.getenv(TRUST_URL_ENV) : TRUST_URL_DEFAULT).split(",");
    }

    /**
     * When set to true JBang process output for Camel integrations will be redirected to a file in the current working directory.
     * @return
     */
    public static boolean isDumpIntegrationOutput() {
        return Boolean.parseBoolean(System.getProperty(CAMEL_DUMP_INTEGRATION_OUTPUT_PROPERTY,
                System.getenv(CAMEL_DUMP_INTEGRATION_OUTPUT_ENV) != null ? System.getenv(CAMEL_DUMP_INTEGRATION_OUTPUT_ENV) : CAMEL_DUMP_INTEGRATION_OUTPUT_DEFAULT));
    }

    /**
     * Camel JBang app name.
     * @return
     */
    public static String getCamelApp() {
        return System.getProperty(CAMEL_APP_PROPERTY,
                System.getenv(CAMEL_APP_ENV) != null ? System.getenv(CAMEL_APP_ENV) : CAMEL_APP_DEFAULT);
    }

    /**
     * Camel JBang version.
     * @return
     */
    public static String getCamelVersion() {
        return System.getProperty(CAMEL_VERSION_PROPERTY,
                System.getenv(CAMEL_VERSION_ENV) != null ? System.getenv(CAMEL_VERSION_ENV) : CAMEL_VERSION_DEFAULT);
    }

    /**
     * Kamelets version used by the JBang runtime.
     * @return
     */
    public static String getKameletsVersion() {
        return System.getProperty(KAMELETS_VERSION_PROPERTY,
                System.getenv(KAMELETS_VERSION_ENV) != null ? System.getenv(KAMELETS_VERSION_ENV) : KAMELETS_VERSION_DEFAULT);
    }

    /**
     * When set to true Camel JBang resources created during the test are
     * automatically removed after the test.
     * @return
     */
    public static boolean isAutoRemoveResources() {
        return Boolean.parseBoolean(System.getProperty(AUTO_REMOVE_RESOURCES_PROPERTY,
                System.getenv(AUTO_REMOVE_RESOURCES_ENV) != null ? System.getenv(AUTO_REMOVE_RESOURCES_ENV) : AUTO_REMOVE_RESOURCES_DEFAULT));
    }

    /**
     * When set to true Camel JBang will automatically wait for each integration created to be in running state.
     * @return
     */
    public static boolean isWaitForRunningState() {
        return Boolean.parseBoolean(System.getProperty(WAIT_FOR_RUNNING_STATE_PROPERTY,
                System.getenv(WAIT_FOR_RUNNING_STATE_ENV) != null ? System.getenv(WAIT_FOR_RUNNING_STATE_ENV) : WAIT_FOR_RUNNING_STATE_DEFAULT));
    }
}
