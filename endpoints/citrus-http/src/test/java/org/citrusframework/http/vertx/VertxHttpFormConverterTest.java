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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpFormConverterTest {
    private static HttpMessage read(String type, byte[] body, HttpEndpointConfiguration config) {
        return VertxHttpMessageMapper.toHttpMessage(new VertxHttpMessageMapper.RequestSnapshot(
                "POST", "/forms", "/forms", null, "HTTP/1.1", Map.of("Content-Type", List.of(type)), body, "id", 1), config);
    }

    @Test
    public void explicitCharsetAndRepeatedFields() {
        var message = read("application/x-www-form-urlencoded;charset=ISO-8859-1",
                "name=Gr%FC%DFe&name=a%2Bb&empty=&flag".getBytes(StandardCharsets.US_ASCII), new HttpEndpointConfiguration());
        var form = (MultiValueMap<?, ?>) message.getPayload();
        assertEquals(form.get("name"), List.of("Grüße", "a+b"));
        assertEquals(form.get("empty"), List.of(""));
        assertNull(form.get("flag").get(0));
    }

    @Test
    public void invalidEscapeFailsInboundConversion() {
        expectThrows(RuntimeException.class, () -> read("application/x-www-form-urlencoded",
                "name=%GG".getBytes(StandardCharsets.US_ASCII), new HttpEndpointConfiguration()));
    }

    @Test
    public void multipartCanBePreservedAsBinary() {
        HttpEndpointConfiguration config = new HttpEndpointConfiguration();
        config.setBinaryMediaTypes(List.of(MediaType.MULTIPART_FORM_DATA));
        byte[] body = {0, (byte) 255, 1};
        assertEquals((byte[]) read("multipart/form-data;boundary=fixture", body, config).getPayload(), body);
    }

    @Test
    public void formResponseCharsetAndHead() {
        var fields = new LinkedMultiValueMap<String, Object>(); fields.add("name", "Grüße");
        var reply = new HttpMessage(fields).contentType("application/x-www-form-urlencoded;charset=ISO-8859-1");
        var config = new HttpEndpointConfiguration();
        var full = VertxHttpMessageMapper.toResponsePlan(reply, config, false);
        assertEquals(new String(full.body(), StandardCharsets.US_ASCII), "name=Gr%FC%DFe");
        var head = VertxHttpMessageMapper.toResponsePlan(reply, config, true);
        assertEquals(head.body().length, 0);
        assertEquals(head.headers().get("Content-Length"), List.of(Integer.toString(full.body().length)));
    }

    @Test
    public void rawFormResponsesAreNotEncodedAgain() {
        var plan = VertxHttpMessageMapper.toResponsePlan(new HttpMessage("a=b%2Bc")
                .contentType("application/x-www-form-urlencoded"), new HttpEndpointConfiguration(), false);
        assertEquals(new String(plan.body(), StandardCharsets.UTF_8), "a=b%2Bc");
    }

    @Test
    public void unsupportedMultipartPartFailsInsteadOfStringifying() {
        var parts = new LinkedMultiValueMap<String, Object>(); parts.add("bad", new Object());
        expectThrows(RuntimeException.class, () -> VertxHttpMessageMapper.toResponsePlan(
                new HttpMessage(parts).contentType("multipart/form-data"), new HttpEndpointConfiguration(), false));
    }
}
