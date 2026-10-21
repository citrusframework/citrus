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

import io.vertx.core.MultiMap;
import jakarta.servlet.http.Cookie;

/**
 * Plain snapshot of an incoming Http request. The snapshot is taken on the event loop and holds
 * all data that is required to map the request to a Citrus message. No Vert.x specific type is
 * used any more once the snapshot has been created.
 *
 * @param method the request method such as {@code GET}
 * @param uri the absolute request uri
 * @param path the raw (still encoded) request path
 * @param query the raw (still encoded) query string
 * @param version the protocol version such as {@code HTTP/1.1}
 * @param headers the request headers
 * @param cookies the request cookies
 * @param body the raw request body
 */
record VertxRequestSnapshot(String method, String uri, String path, String query, String version,
                            MultiMap headers, List<Cookie> cookies, byte[] body) {
}
