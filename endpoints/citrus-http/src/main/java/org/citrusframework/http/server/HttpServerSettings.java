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

package org.citrusframework.http.server;

public class HttpServerSettings {

    private static final String HTTP_SERVER_PROPERTY_PREFIX = "citrus.http.server.";
    private static final String HTTP_SERVER_ENV_PREFIX = "CITRUS_HTTP_SERVER_";

    private static final String RESPONSE_CACHE_SIZE_PROPERTY = HTTP_SERVER_PROPERTY_PREFIX + "response.cache.size";
    private static final String RESPONSE_CACHE_SIZE_ENV = HTTP_SERVER_ENV_PREFIX + "RESPONSE_CACHE_SIZE";
    private static final String RESPONSE_CACHE_SIZE_DEFAULT = "100";

    private static final String USE_DEFAULT_FILTERS_PROPERTY = HTTP_SERVER_PROPERTY_PREFIX + "use.default.filters";
    private static final String USE_DEFAULT_FILTERS_ENV = HTTP_SERVER_ENV_PREFIX + "USE_DEFAULT_FILTERS";
    private static final String USE_DEFAULT_FILTERS_DEFAULT = "true";

    /**
     * Private constructor prevent instantiation of utility class
     */
    private HttpServerSettings() {
        // prevent instantiation
    }

    /**
     * The server response cache size. Each server instance holds this amount of response content in an in memory cache for
     * message tracing reasons.
     * @return
     */
    public static int responseCacheSize() {
        return Integer.parseInt(System.getProperty(RESPONSE_CACHE_SIZE_PROPERTY, System.getenv(RESPONSE_CACHE_SIZE_ENV) != null ?
                        System.getenv(RESPONSE_CACHE_SIZE_ENV) : RESPONSE_CACHE_SIZE_DEFAULT));
    }

    /**
     * When enabled the server uses default servlet filters on the servlet context such as request caching filter.
     * @return
     */
    public static boolean isUseDefaultFilters() {
        return Boolean.parseBoolean(System.getProperty(USE_DEFAULT_FILTERS_PROPERTY, System.getenv(USE_DEFAULT_FILTERS_ENV) != null ?
                        System.getenv(USE_DEFAULT_FILTERS_ENV) : USE_DEFAULT_FILTERS_DEFAULT));
    }
}
