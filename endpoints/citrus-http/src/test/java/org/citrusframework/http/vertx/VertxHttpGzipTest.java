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
import java.util.Map;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpGzipTest {
    @Test
    public void negotiatesExplicitGzipBeforeWildcard() {
        assertTrue(VertxHttpGzip.acceptsGzip("br, GZip; q=0.5"));
        assertTrue(VertxHttpGzip.acceptsGzip("*;q=0.2"));
        for (String value : List.of("", "br", "gzip;q=0,*;q=1", "gzip;q=NaN", "gzip;q=2", "gzip;q=bad"))
            assertFalse(VertxHttpGzip.acceptsGzip(value), value);
    }

    @Test
    public void preservesFramingAndVaryAndSkipsAlreadyEncodedOrBodylessResponses() throws Exception {
        var request = new VertxHttpMessageMapper.RequestSnapshot("GET", "/", "/", null, "HTTP/1.1",
                Map.of("Accept-Encoding", List.of("gzip")), new byte[0], "test", 1);
        byte[] body = "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var plan = new VertxHttpMessageMapper.ResponsePlan(200,
                Map.of("vary", List.of("Origin"), "Transfer-Encoding", List.of("chunked")), body, false);
        var encoded = VertxHttpGzip.encode(plan, request);
        assertEquals(VertxHttpGzipIT.ungzip(encoded.body()), body);
        assertEquals(encoded.headers().get("Content-Length"), List.of(Integer.toString(encoded.body().length)));
        assertEquals(encoded.headers().get("Vary"), List.of("Origin, Accept-Encoding"));
        assertFalse(encoded.headers().containsKey("Transfer-Encoding"));
        for (int status : new int[] {101, 204, 205, 206, 304}) {
            var skipped = new VertxHttpMessageMapper.ResponsePlan(status, Map.of(), body, false);
            assertSame(VertxHttpGzip.encode(skipped, request), skipped);
        }
        for (var headers : List.of(Map.of("Content-Encoding", List.of("br")), Map.of("Content-Range", List.of("bytes 0-4/10")))) {
            var skipped = new VertxHttpMessageMapper.ResponsePlan(200, headers, body, false);
            assertSame(VertxHttpGzip.encode(skipped, request), skipped);
        }
        var head = new VertxHttpMessageMapper.ResponsePlan(200, Map.of(), body, true);
        assertSame(VertxHttpGzip.encode(head, request), head);
        var varied = new VertxHttpMessageMapper.ResponsePlan(200, Map.of("Vary", List.of("*")), body, false);
        assertEquals(VertxHttpGzip.encode(varied, request).headers().get("Vary"), List.of("*"));
    }
}
