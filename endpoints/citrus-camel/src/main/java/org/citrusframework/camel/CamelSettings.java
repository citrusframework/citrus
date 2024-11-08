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

package org.citrusframework.camel;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class CamelSettings {

    private static final String CAMEL_PROPERTY_PREFIX = "citrus.camel.";
    private static final String CAMEL_ENV_PREFIX = "CITRUS_CAMEL_";

    private static final String CONTEXT_NAME_PROPERTY = CAMEL_PROPERTY_PREFIX + "context.name";
    private static final String CONTEXT_NAME_ENV = CAMEL_ENV_PREFIX + "CONTEXT_NAME";
    private static final String CONTEXT_NAME_DEFAULT = "camelContext";

    private static final String TIMEOUT_PROPERTY = CAMEL_PROPERTY_PREFIX + "timeout";
    private static final String TIMEOUT_ENV = CAMEL_ENV_PREFIX + "TIMEOUT";
    private static final long TIMEOUT_DEFAULT = TimeUnit.SECONDS.toMillis(5);

    private static final String MAX_ATTEMPTS_PROPERTY = CAMEL_PROPERTY_PREFIX + "max.attempts";
    private static final String MAX_ATTEMPTS_ENV = CAMEL_ENV_PREFIX + "MAX_ATTEMPTS";
    private static final String MAX_ATTEMPTS_DEFAULT = "60";

    private static final String DELAY_BETWEEN_ATTEMPTS_PROPERTY = CAMEL_PROPERTY_PREFIX + "delay.between.attempts";
    private static final String DELAY_BETWEEN_ATTEMPTS_ENV = CAMEL_ENV_PREFIX + "DELAY_BETWEEN_ATTEMPTS";
    private static final String DELAY_BETWEEN_ATTEMPTS_DEFAULT = "2000";

    private static final String PRINT_LOGS_PROPERTY = CAMEL_PROPERTY_PREFIX + "print.logs";
    private static final String PRINT_LOGS_ENV = CAMEL_ENV_PREFIX + "PRINT_LOGS";
    private static final String PRINT_LOGS_DEFAULT = "true";

    private CamelSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Request timeout when receiving messages.
     * @return
     */
    public static long getTimeout() {
        return Optional.ofNullable(System.getProperty(TIMEOUT_PROPERTY, System.getenv(TIMEOUT_ENV)))
                .map(Long::parseLong)
                .orElse(TIMEOUT_DEFAULT);
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
     * When set to true test will print Camel JBang route logs e.g. while waiting for a log message.
     * @return
     */
    public static boolean isPrintLogs() {
        return Boolean.parseBoolean(System.getProperty(PRINT_LOGS_PROPERTY,
                System.getenv(PRINT_LOGS_ENV) != null ? System.getenv(PRINT_LOGS_ENV) : PRINT_LOGS_DEFAULT));
    }
}
