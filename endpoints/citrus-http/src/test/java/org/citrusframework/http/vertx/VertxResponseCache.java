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

package org.citrusframework.http.vertx;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.citrusframework.http.vertx.VertxHttpMessageMapper.ResponsePlan;

/** Bounded response tracing, never a source of replies to subsequent requests. */
final class VertxResponseCache {
    private final int capacity;
    private final Map<String, ResponsePlan> responses = new LinkedHashMap<>();
    private boolean closed;

    VertxResponseCache(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("Response cache size must not be negative");
        this.capacity = capacity;
    }

    synchronized void put(String exchangeId, ResponsePlan response) {
        if (closed || capacity == 0) return;
        responses.put(exchangeId, copy(response));
        while (responses.size() > capacity) responses.remove(responses.keySet().iterator().next());
    }

    synchronized ResponsePlan get(String exchangeId) {
        ResponsePlan response = responses.get(exchangeId);
        return response == null ? null : copy(response);
    }

    synchronized Map<String, ResponsePlan> snapshot() {
        Map<String, ResponsePlan> snapshot = new LinkedHashMap<>();
        responses.forEach((id, response) -> snapshot.put(id, copy(response)));
        return Collections.unmodifiableMap(snapshot);
    }

    synchronized void close() {
        closed = true;
        responses.clear();
    }

    private static ResponsePlan copy(ResponsePlan source) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        source.headers().forEach((name, values) -> headers.put(name, List.copyOf(values)));
        return new ResponsePlan(source.statusCode(), Collections.unmodifiableMap(headers),
                source.body().clone(), source.suppressBody());
    }
}
