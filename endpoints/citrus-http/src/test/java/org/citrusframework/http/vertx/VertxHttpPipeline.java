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

import java.util.List;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;

/** Immutable registrations; invocation state is private to each request. */
final class VertxHttpPipeline {
    private final List<VertxHttpFilter> filters;
    private final List<VertxHttpHandlerMapping> handlers;
    VertxHttpPipeline(List<VertxHttpFilter> filters, List<VertxHttpHandlerMapping> handlers) {
        this.filters = List.copyOf(filters);
        this.handlers = List.copyOf(handlers);
    }
    Message handle(HttpMessage request, VertxHttpHandler fallback) throws Exception {
        return invoke(0, request, fallback);
    }

    private Message invoke(int index, HttpMessage request, VertxHttpHandler fallback) throws Exception {
        if (index == filters.size()) {
            for (var mapping : handlers) {
                if (mapping.matches().test(request)) return mapping.handler().handle(request);
            }
            return fallback.handle(request);
        }
        Thread owner = Thread.currentThread();
        class Continuation implements VertxHttpFilter.Chain {
            private boolean active = true;
            private boolean called;
            @Override public Message proceed() throws Exception {
                if (Thread.currentThread() != owner || !active || called) {
                    throw new IllegalStateException("Filter continuation must run once, synchronously on its worker");
                }
                called = true;
                return invoke(index + 1, request, fallback);
            }
        }
        var next = new Continuation();
        try {
            return filters.get(index).filter(request, next);
        } finally {
            next.active = false;
        }
    }
}
