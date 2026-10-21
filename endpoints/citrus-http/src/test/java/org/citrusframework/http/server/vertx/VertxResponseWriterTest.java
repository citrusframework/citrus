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
import org.springframework.http.HttpStatus;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for the Vert.x response writer. The expectations are the Jetty outcomes recorded by the
 * shared Http server contract suite.
 */
public class VertxResponseWriterTest extends AbstractTestNGUnitTest {

    private HttpEndpointConfiguration configuration;
    private VertxResponseWriter writer;

    @BeforeMethod
    public void setup() {
        configuration = new HttpEndpointConfiguration();
        configuration.setHeaderMapper(DefaultHttpHeaderMapper.inboundMapper());
        writer = new VertxResponseWriter(configuration, new DelegatingHttpEntityMessageConverter(), true);
    }

    @Test
    public void shouldAnswerWithDefaultStatusWhenThereIsNoReply() {
        VertxResponseSnapshot response = writer.write(null, request());

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).isEmpty();
        assertThat(response.headers()).isEmpty();
    }

    @Test
    public void shouldUseDefaultStatusWhenReplyHasNoStatus() {
        configuration.setDefaultStatusCode(202);

        VertxResponseSnapshot response = writer.write(new HttpMessage("ok"), request());

        assertThat(response.status()).isEqualTo(202);
    }

    @Test
    public void shouldWriteStatusHeadersAndBody() {
        HttpMessage reply = new HttpMessage("reply body")
                .status(HttpStatus.CREATED)
                .header("X-Custom", "value");

        VertxResponseSnapshot response = writer.write(reply, request());

        assertThat(response.status()).isEqualTo(201);
        assertThat(response.getHeader("X-Custom")).isEqualTo("value");
        assertThat(new String(response.body(), StandardCharsets.UTF_8)).isEqualTo("reply body");
    }

    @Test
    public void shouldSetContentLengthOfTheBody() {
        VertxResponseSnapshot response = writer.write(new HttpMessage("reply body").status(HttpStatus.OK), request());

        assertThat(response.getHeader("Content-Length")).isEqualTo("10");
    }

    @Test
    public void shouldNotWriteBodyForNoContentReply() {
        VertxResponseSnapshot response = writer.write(new HttpMessage("").status(HttpStatus.NO_CONTENT), request());

        assertThat(response.status()).isEqualTo(204);
        assertThat(response.body()).isEmpty();
        assertThat(response.getHeader("Content-Length")).isNull();
    }

    @Test
    public void shouldWriteSetCookieForReplyCookies() {
        configuration.setHandleCookies(true);

        HttpMessage reply = new HttpMessage("reply body")
                .status(HttpStatus.OK)
                .cookie(new Cookie("token", "value"));

        VertxResponseSnapshot response = writer.write(reply, request());

        assertThat(response.getHeader("Set-Cookie")).isEqualTo("token=value");
    }

    @Test
    public void shouldNotWriteSetCookieWhenCookiesAreDisabled() {
        HttpMessage reply = new HttpMessage("reply body")
                .status(HttpStatus.OK)
                .cookie(new Cookie("token", "value"));

        VertxResponseSnapshot response = writer.write(reply, request());

        assertThat(response.getHeader("Set-Cookie")).isNull();
    }

    @Test
    public void shouldPreferExplicitContentType() {
        HttpMessage reply = new HttpMessage("{}")
                .status(HttpStatus.OK)
                .contentType("application/json");

        VertxResponseSnapshot response = writer.write(reply, request());

        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
    }

    @Test
    public void shouldUseDefaultContentTypeWhenReplyHasNone() {
        HttpMessage reply = new HttpMessage("reply body").status(HttpStatus.OK);

        VertxResponseSnapshot response = writer.write(reply, request());

        assertThat(response.getHeader("Content-Type")).contains("text/plain");
    }

    @Test
    public void shouldSelectContentTypeAcceptedByTheClient() {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap().add("Accept", "application/pdf");

        VertxResponseSnapshot response = writer.write(new HttpMessage("reply body").status(HttpStatus.OK), request(headers));

        assertThat(response.getHeader("Content-Type")).contains("application/pdf");
    }

    private VertxRequestSnapshot request() {
        return request(MultiMap.caseInsensitiveMultiMap());
    }

    private VertxRequestSnapshot request(MultiMap headers) {
        return new VertxRequestSnapshot("GET", "/orders/42", "/orders/42", null, "HTTP/1.1", headers, List.of(), null);
    }
}
