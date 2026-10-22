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

import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.http.Cookie;
import io.vertx.core.MultiMap;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.DelegatingHttpEntityMessageConverter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for the Vert.x request mapper. The expectations are the Jetty outcomes recorded by the
 * shared Http server contract suite.
 */
public class VertxRequestMapperTest extends AbstractTestNGUnitTest {

    private HttpEndpointConfiguration configuration;
    private VertxRequestMapper mapper;

    @BeforeMethod
    public void setup() {
        configuration = new HttpEndpointConfiguration();
        configuration.setHeaderMapper(DefaultHttpHeaderMapper.inboundMapper());
        mapper = new VertxRequestMapper(configuration, new DelegatingHttpEntityMessageConverter(), "/");
    }

    @Test
    public void shouldMapRequestMetadata() {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap().add("Host", "localhost").add("X-Trace", "abc");

        HttpMessage request = mapper.map(snapshot("GET", "/orders/42?type=gold&id=7", headers, null));

        assertThat(request.getRequestMethod().name()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/orders/42");
        assertThat(request.getUri()).isEqualTo("/orders/42");
        assertThat(request.getContextPath()).isEqualTo("");
        assertThat(request.getQueryParamString()).isEqualTo("type=gold,id=7");
        assertThat(request.getVersion()).isEqualTo("HTTP/1.1");
        assertThat(request.getHeader("X-Trace")).isEqualTo("abc");
    }

    @Test
    public void shouldDecodeEncodedPath() {
        HttpMessage request = mapper.map(snapshot("GET", "/hello%20world", MultiMap.caseInsensitiveMultiMap(), null));

        assertThat(request.getPath()).isEqualTo("/hello world");
        assertThat(request.getUri()).isEqualTo("/hello world");
    }

    @Test
    public void shouldRemoveMatrixParameters() {
        HttpMessage request = mapper.map(snapshot("GET", "/orders;version=2/42", MultiMap.caseInsensitiveMultiMap(), null));

        assertThat(request.getPath()).isEqualTo("/orders/42");
    }

    @Test
    public void shouldKeepMatrixParametersWhenDisabled() {
        configuration.setRemoveSemicolonPathContent(false);

        HttpMessage request = mapper.map(snapshot("GET", "/orders;version=2/42", MultiMap.caseInsensitiveMultiMap(), null));

        assertThat(request.getPath()).isEqualTo("/orders;version=2/42");
    }

    @Test
    public void shouldCollapseDuplicateSlashes() {
        HttpMessage request = mapper.map(snapshot("GET", "/orders//42", MultiMap.caseInsensitiveMultiMap(), null));

        assertThat(request.getPath()).isEqualTo("/orders/42");
    }

    @Test
    public void shouldKeepContextPathInRequestPath() {
        mapper = new VertxRequestMapper(configuration, new DelegatingHttpEntityMessageConverter(), "/api");

        HttpMessage request = mapper.map(snapshot("GET", "/api/orders/42", MultiMap.caseInsensitiveMultiMap(), null));

        assertThat(request.getPath()).isEqualTo("/api/orders/42");
        assertThat(request.getContextPath()).isEqualTo("/api");
    }

    @Test
    public void shouldCopyHeaderWithoutValueAsEmptyString() {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap().add("X-Empty", "");

        HttpMessage request = mapper.map(snapshot("GET", "/orders/42", headers, null));

        assertThat(request.getHeader("X-Empty")).isEqualTo("");
    }

    @Test
    public void shouldEncodeQueryParameters() {
        HttpMessage request = mapper.map(snapshot("GET", "/orders/42?key=a%20b&key=c&flag", MultiMap.caseInsensitiveMultiMap(), null));

        assertThat(request.getQueryParamString()).isEqualTo("key=a%20b,key=c,flag");
    }

    @Test
    public void shouldMapBinaryBody() {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap().add("Content-Type", "application/octet-stream");
        byte[] body = new byte[] { 0, (byte) 255, 13, 10 };

        HttpMessage request = mapper.map(snapshot("POST", "/orders/42", headers, body));

        assertThat(request.getPayload()).isInstanceOf(byte[].class);
        assertThat((byte[]) request.getPayload()).isEqualTo(body);
    }

    @Test
    public void shouldMapTextBodyWithCharset() {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap().add("Content-Type", "text/plain;charset=UTF-8");
        byte[] body = "Grüße 世界".getBytes(StandardCharsets.UTF_8);

        HttpMessage request = mapper.map(snapshot("POST", "/orders/42", headers, body));

        assertThat(request.getPayload()).isInstanceOf(String.class);
        assertThat(request.getPayload()).isEqualTo("Grüße 世界");
    }

    @Test
    public void shouldMapEmptyBodyToEmptyPayload() {
        HttpMessage request = mapper.map(snapshot("GET", "/orders/42", MultiMap.caseInsensitiveMultiMap(), new byte[0]));

        assertThat(request.getPayload()).isEqualTo("");
    }

    @Test
    public void shouldMapCookiesWhenEnabled() {
        configuration.setHandleCookies(true);
        MultiMap headers = MultiMap.caseInsensitiveMultiMap().add("Cookie", "token=value");

        HttpMessage request = mapper.map(new VertxRequestSnapshot("GET", "/orders/42", "/orders/42", null, "HTTP/1.1",
                headers, List.of(new Cookie("token", "value")), null));

        assertThat(request.getCookies()).hasSize(1);
        assertThat(request.getCookies().get(0).getName()).isEqualTo("token");
        assertThat(request.getCookies().get(0).getValue()).isEqualTo("value");
    }

    @Test
    public void shouldNotMapCookiesWhenDisabled() {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap().add("Cookie", "token=value");

        HttpMessage request = mapper.map(new VertxRequestSnapshot("GET", "/orders/42", "/orders/42", null, "HTTP/1.1",
                headers, List.of(new Cookie("token", "value")), null));

        assertThat(request.getCookies()).isNullOrEmpty();
    }

    private VertxRequestSnapshot snapshot(String method, String target, MultiMap headers, byte[] body) {
        String path = target;
        String query = null;

        int queryIndex = target.indexOf('?');
        if (queryIndex >= 0) {
            path = target.substring(0, queryIndex);
            query = target.substring(queryIndex + 1);
        }

        return new VertxRequestSnapshot(method, target, path, query, "HTTP/1.1", headers, List.of(), body);
    }
}
