/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.http.server.vertx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

/**
 * Unit test for the Vert.x gzip support. The expectations are the Jetty outcomes recorded by the
 * shared Http server contract suite.
 */
public class VertxGzipTest extends AbstractTestNGUnitTest {

    @Test
    public void shouldDetectGzipEncoding() {
        assertThat(VertxGzip.isGzipEncoding("gzip")).isTrue();
        assertThat(VertxGzip.isGzipEncoding("gzip, deflate")).isTrue();
        assertThat(VertxGzip.isGzipEncoding("deflate")).isFalse();
        assertThat(VertxGzip.isGzipEncoding("")).isFalse();
        assertThat(VertxGzip.isGzipEncoding(null)).isFalse();
    }

    @Test
    public void shouldDecodeGzipBody() throws IOException {
        byte[] compressed = VertxGzip.encode("Hello gzip".getBytes(StandardCharsets.UTF_8));

        assertThat(VertxGzip.decode(compressed)).isEqualTo("Hello gzip".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void shouldFailOnCorruptGzipBody() {
        assertThatThrownBy(() -> VertxGzip.decode("no gzip at all".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IOException.class);
    }

    @Test
    public void shouldEncodeGzipBody() throws IOException {
        byte[] encoded = VertxGzip.encode("Hello gzip response".getBytes(StandardCharsets.UTF_8));

        assertThat(encoded).isNotEqualTo("Hello gzip response".getBytes(StandardCharsets.UTF_8));
        assertThat(VertxGzip.decode(encoded)).isEqualTo("Hello gzip response".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void shouldEncodeEmptyBody() throws IOException {
        byte[] encoded = VertxGzip.encode(new byte[0]);

        assertThat(encoded).isNotEmpty();
        assertThat(VertxGzip.decode(encoded)).isEmpty();
    }
}
