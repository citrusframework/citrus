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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.citrusframework.http.vertx.VertxHttpMessageMapper.ResponsePlan;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxResponseCacheTest {
    private static ResponsePlan plan() { return new ResponsePlan(200, Map.of(), new byte[] {1}, false); }

    @Test
    public void capacityOneZeroAndNegative() {
        expectThrows(IllegalArgumentException.class, () -> new VertxResponseCache(-1));
        var disabled = new VertxResponseCache(0); disabled.put("a", plan());
        assertTrue(disabled.snapshot().isEmpty());
        var one = new VertxResponseCache(1); one.put("a", plan());
        assertNotNull(one.get("a")); one.put("b", plan());
        assertNull(one.get("a")); assertNotNull(one.get("b"));
    }

    @Test
    public void snapshotsDoNotExposeStoredBytesOrHeaders() {
        var cache = new VertxResponseCache(2);
        byte[] body = {42};
        var values = new ArrayList<>(List.of("original"));
        Map<String, List<String>> headers = new LinkedHashMap<>(); headers.put("X-Test", values);
        cache.put("a", new ResponsePlan(201, headers, body, true));
        body[0] = 0; values.set(0, "changed"); headers.clear();
        var read = cache.get("a");
        assertEquals(read.body()[0], (byte) 42); assertTrue(read.suppressBody());
        assertEquals(read.headers().get("X-Test"), List.of("original"));
        expectThrows(UnsupportedOperationException.class, () -> read.headers().get("X-Test").add("other"));
        var snapshot = cache.snapshot(); snapshot.get("a").body()[0] = 0;
        expectThrows(UnsupportedOperationException.class, snapshot::clear);
        assertEquals(cache.get("a").body()[0], (byte) 42);
    }

    @Test
    public void concurrentWritersRemainBoundedAndCloseRejectsLateWork() {
        var cache = new VertxResponseCache(7);
        IntStream.range(0, 1000).parallel().forEach(i -> cache.put("x" + i, plan()));
        assertEquals(cache.snapshot().size(), 7);
        cache.close();
        IntStream.range(0, 1000).parallel().forEach(i -> cache.put("late" + i, plan()));
        assertTrue(cache.snapshot().isEmpty());
    }

    @Test
    public void existingControllerEvictsAtItsThreshold() {
        var controller = new org.citrusframework.http.controller.HttpMessageController();
        controller.setResponseCacheSize(2);
        var first = new org.springframework.mock.web.MockHttpServletRequest("GET", "/trace");
        var second = new org.springframework.mock.web.MockHttpServletRequest("GET", "/trace");
        try {
            org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                    new org.springframework.web.context.request.ServletRequestAttributes(first));
            controller.handleGetRequest(new org.springframework.http.HttpEntity<>(""));
            assertNotNull(controller.getResponseCache(first));
            org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                    new org.springframework.web.context.request.ServletRequestAttributes(second));
            controller.handleGetRequest(new org.springframework.http.HttpEntity<>(""));
            assertNull(controller.getResponseCache(first));
            assertNotNull(controller.getResponseCache(second));
        } finally { org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes(); }
    }
}
