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

import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class VertxHttpServerOptionsTest {

    @Test
    public void defaultOptionsMatchEvaluationContract() {
        VertxHttpServerOptions options = VertxHttpServerOptions.defaults();

        assertEquals(options.getHost(), "127.0.0.1");
        assertEquals(options.getPort(), 0);
        assertEquals(options.getDefaultStatusCode(), 200);
        assertEquals(options.getReplyTimeoutMs(), 5_000L);
        assertEquals(options.getStartupTimeoutMs(), 30_000L);
        assertEquals(options.getShutdownTimeoutMs(), 10_000L);
        assertEquals(options.getBodyReadTimeoutMs(), 5_000L);
        assertEquals(options.getMaxBodySizeBytes(), 1024 * 1024L);
        assertEquals(options.getExecutorThreads(), 8);
        assertEquals(options.getMaxConcurrentExchanges(), 32);
        assertEquals(options.getCharset(), "UTF-8");
        assertEquals(options.getContentType(), "text/plain");
        assertEquals(options.getBinaryMediaTypes().size(), 6);
        assertTrue(options.getBinaryMediaTypes().contains(MediaType.APPLICATION_OCTET_STREAM));
        assertFalse(options.isHandleCookies());
        assertFalse(options.isHandleAttributeHeaders());
        assertTrue(options.isRemoveSemicolonPathContent());
    }

    @Test
    public void builderOverridesAreApplied() {
        VertxHttpServerOptions options = VertxHttpServerOptions.builder()
                .host("127.0.0.1")
                .port(18080)
                .defaultStatusCode(201)
                .replyTimeoutMs(1_000L)
                .executorThreads(4)
                .maxConcurrentExchanges(8)
                .handleCookies(true)
                .build();

        assertEquals(options.getPort(), 18080);
        assertEquals(options.getDefaultStatusCode(), 201);
        assertEquals(options.getReplyTimeoutMs(), 1_000L);
        assertEquals(options.getExecutorThreads(), 4);
        assertEquals(options.getMaxConcurrentExchanges(), 8);
        assertTrue(options.isHandleCookies());
    }

    @Test
    public void portZeroSelectsEphemeralPort() {
        VertxHttpServerOptions options = VertxHttpServerOptions.builder().port(0).build();
        assertEquals(options.getPort(), 0);
    }

    @Test
    public void invalidPortsAreRejected() {
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().port(-1).build());
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().port(65_536).build());
    }

    @Test
    public void blankHostIsRejected() {
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().host("  ").build());
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().host(null).build());
    }

    @Test
    public void nonPositiveTimeoutsAndLimitsAreRejected() {
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().replyTimeoutMs(0).build());
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().startupTimeoutMs(-1).build());
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().shutdownTimeoutMs(0).build());
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().bodyReadTimeoutMs(0).build());
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().maxBodySizeBytes(0).build());
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().executorThreads(0).build());
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().maxConcurrentExchanges(0).build());
    }

    @Test
    public void invalidStatusCodeIsRejected() {
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().defaultStatusCode(99).build());
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().defaultStatusCode(600).build());
    }

    @Test
    public void unknownCharsetIsRejected() {
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().charset("not-a-charset-xyz").build());
    }

    @Test
    public void blankContentTypeIsRejected() {
        expectThrows(IllegalArgumentException.class, () -> VertxHttpServerOptions.builder().contentType(" ").build());
    }

    @Test
    public void binaryMediaTypesAreDefensiveCopies() {
        VertxHttpServerOptions options = VertxHttpServerOptions.defaults();
        expectThrows(UnsupportedOperationException.class, () -> options.getBinaryMediaTypes().add(MediaType.TEXT_PLAIN));
    }

    @Test
    public void buildsAreIndependentSnapshots() {
        VertxHttpServerOptions.Builder builder = VertxHttpServerOptions.builder().port(18081);
        VertxHttpServerOptions first = builder.build();
        builder.port(18082);
        VertxHttpServerOptions second = builder.build();

        assertEquals(first.getPort(), 18081);
        assertEquals(second.getPort(), 18082);
    }
}
