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

package org.citrusframework.http.server.vertx;

import java.util.List;
import java.util.Map;

/**
 * Plain snapshot of the Http response that has to be written to the client. All headers are
 * transport neutral, the transport specific headers such as {@code Transfer-Encoding},
 * {@code Connection} and {@code Content-Length} are computed by the server.
 *
 * @param status the response status code
 * @param headers the response headers
 * @param body the response body
 */
record VertxResponseSnapshot(int status, Map<String, List<String>> headers, byte[] body) {

    /**
     * Gets the first value of the given header.
     *
     * @param name the header name.
     * @return the first header value or {@code null} if the header is not set.
     */
    String getHeader(String name) {
        List<String> values = headers.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }
}
