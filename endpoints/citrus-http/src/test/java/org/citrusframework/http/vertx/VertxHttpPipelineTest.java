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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.citrusframework.http.message.HttpMessage;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpPipelineTest {
    @Test public void ordersFiltersAroundFirstMatchingHandler() throws Exception {
        var order = new ArrayList<String>();
        var pipeline = new VertxHttpPipeline(List.of(
                (request, next) -> { order.add("a-before"); var reply = next.proceed(); order.add("a-after"); return reply; },
                (request, next) -> { order.add("b-before"); request.setPayload("changed"); var reply = next.proceed(); order.add("b-after"); return reply; }),
                List.of(new VertxHttpHandlerMapping(request -> false, request -> { throw new AssertionError(); }),
                        new VertxHttpHandlerMapping(request -> true, request -> { order.add("handler"); return new HttpMessage(request.getPayload()); }),
                        new VertxHttpHandlerMapping(request -> true, request -> { throw new AssertionError(); })));
        assertEquals(pipeline.handle(new HttpMessage("original"), request -> { throw new AssertionError(); }).getPayload(), "changed");
        assertEquals(order, List.of("a-before", "b-before", "handler", "b-after", "a-after"));
    }
    @Test public void shortCircuitsAndFallsBack() throws Exception {
        var pipeline = new VertxHttpPipeline(List.of((request, next) -> new HttpMessage("denied")), List.of());
        assertEquals(pipeline.handle(new HttpMessage(), request -> { throw new AssertionError(); }).getPayload(), "denied");
        assertEquals(new VertxHttpPipeline(List.of(), List.of()).handle(new HttpMessage("fallback"), request -> request).getPayload(), "fallback");
    }
    @Test public void rejectsRepeatedLateAndCrossThreadContinuation() throws Exception {
        var retained = new AtomicReference<VertxHttpFilter.Chain>();
        var calls = new AtomicInteger();
        var pipeline = new VertxHttpPipeline(List.of((request, next) -> {
            retained.set(next);
            var failure = new AtomicReference<Throwable>();
            var thread = new Thread(() -> { try { next.proceed(); } catch (Throwable e) { failure.set(e); } });
            thread.start(); thread.join(); assertTrue(failure.get() instanceof IllegalStateException);
            var reply = next.proceed();
            expectThrows(IllegalStateException.class, next::proceed);
            return reply;
        }), List.of());
        pipeline.handle(new HttpMessage(), request -> { calls.incrementAndGet(); return request; });
        assertEquals(calls.get(), 1);
        expectThrows(IllegalStateException.class, () -> retained.get().proceed());
    }
    @Test public void snapshotsRegistrations() throws Exception {
        var filters = new ArrayList<VertxHttpFilter>();
        filters.add((request, next) -> new HttpMessage("first"));
        var pipeline = new VertxHttpPipeline(filters, List.of()); filters.clear();
        assertEquals(pipeline.handle(new HttpMessage(), request -> new HttpMessage("fallback")).getPayload(), "first");
    }
}
