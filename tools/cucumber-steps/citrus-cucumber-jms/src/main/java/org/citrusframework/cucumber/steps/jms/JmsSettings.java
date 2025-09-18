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

package org.citrusframework.cucumber.steps.jms;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class JmsSettings {

    private static final String JMS_PROPERTY_PREFIX = "citrus.jms.";
    private static final String JMS_ENV_PREFIX = "CITRUS_JMS_";

    private static final String ENDPOINT_NAME_PROPERTY = JMS_PROPERTY_PREFIX + "endpoint.name";
    private static final String ENDPOINT_NAME_ENV = JMS_ENV_PREFIX + "ENDPOINT_NAME";
    private static final String ENDPOINT_NAME_DEFAULT = "citrus-jms-endpoint";

    private static final String TIMEOUT_PROPERTY = JMS_PROPERTY_PREFIX + "timeout";
    private static final String TIMEOUT_ENV = JMS_ENV_PREFIX + "TIMEOUT";

    private JmsSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Request timeout when receiving messages.
     */
    public static long getTimeout() {
        return Optional.ofNullable(System.getProperty(TIMEOUT_PROPERTY, System.getenv(TIMEOUT_ENV)))
                .map(Long::parseLong)
                .orElse(TimeUnit.SECONDS.toMillis(60));
    }

    /**
     * Default endpoint name to use when creating a Kafka endpoint.
     */
    public static String getEndpointName() {
        return System.getProperty(ENDPOINT_NAME_PROPERTY,
                System.getenv(ENDPOINT_NAME_ENV) != null ? System.getenv(ENDPOINT_NAME_ENV) : ENDPOINT_NAME_DEFAULT);
    }
}
