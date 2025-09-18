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

package org.citrusframework.cucumber.steps.camel;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class CamelSettings {

    private static final String CAMEL_PROPERTY_PREFIX = "citrus.camel.";
    private static final String CAMEL_ENV_PREFIX = "CITRUS_CAMEL_";

    static final String AUTO_REMOVE_RESOURCES_PROPERTY = CAMEL_PROPERTY_PREFIX + "auto.remove.resources";
    static final String AUTO_REMOVE_RESOURCES_ENV = CAMEL_ENV_PREFIX + "AUTO_REMOVE_RESOURCES";
    static final String AUTO_REMOVE_RESOURCES_DEFAULT = "false";

    private static final String CONTEXT_NAME_PROPERTY = CAMEL_PROPERTY_PREFIX + "context.name";
    private static final String CONTEXT_NAME_ENV = CAMEL_ENV_PREFIX + "CONTEXT_NAME";
    private static final String CONTEXT_NAME_DEFAULT = "citrus-camel-context";

    private static final String TIMEOUT_PROPERTY = CAMEL_PROPERTY_PREFIX + "timeout";
    private static final String TIMEOUT_ENV = CAMEL_ENV_PREFIX + "TIMEOUT";

    private static final String MAX_ATTEMPTS_PROPERTY = CAMEL_PROPERTY_PREFIX + "max.attempts";
    private static final String MAX_ATTEMPTS_ENV = CAMEL_ENV_PREFIX + "MAX_ATTEMPTS";
    private static final String MAX_ATTEMPTS_DEFAULT = "10";

    private static final String DELAY_BETWEEN_ATTEMPTS_PROPERTY = CAMEL_PROPERTY_PREFIX + "delay.between.attempts";
    private static final String DELAY_BETWEEN_ATTEMPTS_ENV = CAMEL_ENV_PREFIX + "DELAY_BETWEEN_ATTEMPTS";
    private static final String DELAY_BETWEEN_ATTEMPTS_DEFAULT = "2000";

    private static final String STOP_ON_ERROR_STATUS_PROPERTY = CAMEL_PROPERTY_PREFIX + "stop.on.error.status";
    private static final String STOP_ON_ERROR_STATUS_ENV = CAMEL_ENV_PREFIX + "STOP_ON_ERROR_STATUS";
    private static final String STOP_ON_ERROR_STATUS_DEFAULT = "true";

    private static final String PRINT_POD_LOGS_PROPERTY = CAMEL_PROPERTY_PREFIX + "print.pod.logs";
    private static final String PRINT_POD_LOGS_ENV = CAMEL_ENV_PREFIX + "PRINT_POD_LOGS";
    private static final String PRINT_POD_LOGS_DEFAULT = "true";

    private CamelSettings() {
        // prevent instantiation of utility class
    }

    /**
     * When set to true Camel resources (CamelContext, Routes etc.) created during the test are
     * automatically removed after the test.
     * @return
     */
    public static boolean isAutoRemoveResources() {
        return Boolean.parseBoolean(System.getProperty(AUTO_REMOVE_RESOURCES_PROPERTY,
                System.getenv(AUTO_REMOVE_RESOURCES_ENV) != null ? System.getenv(AUTO_REMOVE_RESOURCES_ENV) : AUTO_REMOVE_RESOURCES_DEFAULT));
    }

    /**
     * Request timeout when receiving messages.
     * @return
     */
    public static long getTimeout() {
        return Optional.ofNullable(System.getProperty(TIMEOUT_PROPERTY, System.getenv(TIMEOUT_ENV)))
                .map(Long::parseLong)
                .orElse(TimeUnit.SECONDS.toMillis(60));
    }

    /**
     * Default Camel context name to use when creating routes.
     * @return
     */
    public static String getContextName() {
        return System.getProperty(CONTEXT_NAME_PROPERTY,
                System.getenv(CONTEXT_NAME_ENV) != null ? System.getenv(CONTEXT_NAME_ENV) : CONTEXT_NAME_DEFAULT);
    }

    /**
     * Maximum number of attempts when polling for running state and log messages.
     * @return
     */
    public static int getMaxAttempts() {
        return Integer.parseInt(System.getProperty(MAX_ATTEMPTS_PROPERTY,
                System.getenv(MAX_ATTEMPTS_ENV) != null ? System.getenv(MAX_ATTEMPTS_ENV) : MAX_ATTEMPTS_DEFAULT));
    }

    /**
     * Delay in milliseconds to wait after polling attempt.
     * @return
     */
    public static long getDelayBetweenAttempts() {
        return Long.parseLong(System.getProperty(DELAY_BETWEEN_ATTEMPTS_PROPERTY,
                System.getenv(DELAY_BETWEEN_ATTEMPTS_ENV) != null ? System.getenv(DELAY_BETWEEN_ATTEMPTS_ENV) : DELAY_BETWEEN_ATTEMPTS_DEFAULT));
    }

    /**
     * When set to true Citrus will stop the integration verification when integration is in error state.
     * When verifying the integration status and pod logs the retry mechanism stops when integration status condition is in error state.
     * @return
     */
    public static boolean isStopOnErrorStatus() {
        return Boolean.parseBoolean(System.getProperty(STOP_ON_ERROR_STATUS_PROPERTY,
                System.getenv(STOP_ON_ERROR_STATUS_ENV) != null ? System.getenv(STOP_ON_ERROR_STATUS_ENV) : STOP_ON_ERROR_STATUS_DEFAULT));
    }

    /**
     * When set to true test will print pod logs e.g. while waiting for a pod log message.
     * @return
     */
    public static boolean isPrintPodLogs() {
        return Boolean.parseBoolean(System.getProperty(PRINT_POD_LOGS_PROPERTY,
                System.getenv(PRINT_POD_LOGS_ENV) != null ? System.getenv(PRINT_POD_LOGS_ENV) : PRINT_POD_LOGS_DEFAULT));
    }
}
