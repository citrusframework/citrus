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
    private static final String CONTEXT_NAME_DEFAULT = "citrusCamelContext";

    private static final String TIMEOUT_PROPERTY = CAMEL_PROPERTY_PREFIX + "timeout";
    private static final String TIMEOUT_ENV = CAMEL_ENV_PREFIX + "TIMEOUT";
    private static final long TIMEOUT_DEFAULT = TimeUnit.SECONDS.toMillis(5);

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
}
