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

package org.citrusframework.playwright.http;

/**
 * Per-client driver settings applied to every browser-session request. A {@code null} value keeps
 * the driver default.
 *
 * @param timeout request timeout in milliseconds
 * @param maxRedirects redirects to follow; 0 returns the redirect response itself
 * @param maxRetries retries after a connection reset
 * @param ignoreHttpsErrors true to accept invalid TLS certificates; false leaves the browser
 *                          context's own setting in charge
 */
record TransportOptions(Double timeout, Integer maxRedirects, Integer maxRetries, boolean ignoreHttpsErrors) {

    static final TransportOptions DEFAULTS = new TransportOptions(null, null, null, false);

    TransportOptions withTimeout(double timeout) {
        return new TransportOptions(timeout, maxRedirects, maxRetries, ignoreHttpsErrors);
    }

    TransportOptions withMaxRedirects(int maxRedirects) {
        return new TransportOptions(timeout, maxRedirects, maxRetries, ignoreHttpsErrors);
    }

    TransportOptions withMaxRetries(int maxRetries) {
        return new TransportOptions(timeout, maxRedirects, maxRetries, ignoreHttpsErrors);
    }

    TransportOptions withIgnoreHttpsErrors(boolean ignoreHttpsErrors) {
        return new TransportOptions(timeout, maxRedirects, maxRetries, ignoreHttpsErrors);
    }
}
