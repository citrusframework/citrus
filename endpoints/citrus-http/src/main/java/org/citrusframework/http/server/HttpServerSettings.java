/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.http.server;

/**
 * @author Christoph Deppisch
 */
public class HttpServerSettings {

    private static final String RESPONSE_CACHE_SIZE_PROPERTY = "citrus.http.server.response.cache.size";
    private static final String RESPONSE_CACHE_SIZE_ENV = "CITRUS_HTTP_SERVER_RESPONSE_CACHE_SIZE";
    private static final String RESPONSE_CACHE_SIZE_DEFAULT = "100";

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
}
