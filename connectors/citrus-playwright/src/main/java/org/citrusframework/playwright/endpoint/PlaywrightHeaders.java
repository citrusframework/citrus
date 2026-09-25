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

package org.citrusframework.playwright.endpoint;

public final class PlaywrightHeaders {

    public static final String PLAYWRIGHT_BROWSER = "citrus_playwright_browser";

    /** Prefix of the transport details added to responses of browser-session HTTP requests. */
    public static final String PLAYWRIGHT_API_PREFIX = "citrus_playwright_api_";

    public static final String PLAYWRIGHT_API_SERVER_IP = PLAYWRIGHT_API_PREFIX + "server_ip";
    public static final String PLAYWRIGHT_API_SERVER_PORT = PLAYWRIGHT_API_PREFIX + "server_port";

    public static final String PLAYWRIGHT_API_TLS_PROTOCOL = PLAYWRIGHT_API_PREFIX + "tls_protocol";
    public static final String PLAYWRIGHT_API_TLS_SUBJECT_NAME = PLAYWRIGHT_API_PREFIX + "tls_subject_name";
    public static final String PLAYWRIGHT_API_TLS_ISSUER = PLAYWRIGHT_API_PREFIX + "tls_issuer";
    /** Start of the certificate validity, in epoch seconds. */
    public static final String PLAYWRIGHT_API_TLS_VALID_FROM = PLAYWRIGHT_API_PREFIX + "tls_valid_from";
    /** End of the certificate validity, in epoch seconds. */
    public static final String PLAYWRIGHT_API_TLS_VALID_TO = PLAYWRIGHT_API_PREFIX + "tls_valid_to";

    /** Request start, in epoch milliseconds. The other timing headers are milliseconds relative to it. */
    public static final String PLAYWRIGHT_API_TIMING_START_TIME = PLAYWRIGHT_API_PREFIX + "timing_start_time";
    public static final String PLAYWRIGHT_API_TIMING_DOMAIN_LOOKUP_START = PLAYWRIGHT_API_PREFIX + "timing_domain_lookup_start";
    public static final String PLAYWRIGHT_API_TIMING_DOMAIN_LOOKUP_END = PLAYWRIGHT_API_PREFIX + "timing_domain_lookup_end";
    public static final String PLAYWRIGHT_API_TIMING_CONNECT_START = PLAYWRIGHT_API_PREFIX + "timing_connect_start";
    public static final String PLAYWRIGHT_API_TIMING_SECURE_CONNECTION_START = PLAYWRIGHT_API_PREFIX + "timing_secure_connection_start";
    public static final String PLAYWRIGHT_API_TIMING_CONNECT_END = PLAYWRIGHT_API_PREFIX + "timing_connect_end";
    public static final String PLAYWRIGHT_API_TIMING_REQUEST_START = PLAYWRIGHT_API_PREFIX + "timing_request_start";
    public static final String PLAYWRIGHT_API_TIMING_RESPONSE_START = PLAYWRIGHT_API_PREFIX + "timing_response_start";
    public static final String PLAYWRIGHT_API_TIMING_RESPONSE_END = PLAYWRIGHT_API_PREFIX + "timing_response_end";

    private PlaywrightHeaders() {
    }
}
