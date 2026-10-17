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
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.springframework.http.HttpInputMessage;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxMessageBodyConverterTest {
    @Test
    public void firstMatchingConverterWinsAndOtherTypesUseDefaults() {
        var first = new VertxMessageBodyConverter<>(VertxHttpCustomConverterIT.Payload.class,
                new VertxHttpCustomConverterIT.Converter() {
                    @Override protected VertxHttpCustomConverterIT.Payload readInternal(
                            Class<? extends VertxHttpCustomConverterIT.Payload> type, HttpInputMessage input) {
                        return new VertxHttpCustomConverterIT.Payload("first");
                    }
                });
        var second = new VertxMessageBodyConverter<>(VertxHttpCustomConverterIT.Payload.class,
                new VertxHttpCustomConverterIT.Converter());
        List<VertxMessageBodyConverter<?>> converters = List.of(first, second);
        var request = new VertxHttpMessageMapper.RequestSnapshot("POST", "/", "/", null, "HTTP/1.1",
                Map.of("Content-Type", List.of("application/x-citrus-test")), new byte[] {65}, "id", 1);
        assertEquals(VertxHttpMessageMapper.toHttpMessage(request, new HttpEndpointConfiguration(), converters).getPayload(),
                new VertxHttpCustomConverterIT.Payload("first"));
        var text = new VertxHttpMessageMapper.RequestSnapshot("POST", "/", "/", null, "HTTP/1.1",
                Map.of("Content-Type", List.of("text/plain")), new byte[] {65}, "id", 1);
        assertEquals(VertxHttpMessageMapper.toHttpMessage(text, new HttpEndpointConfiguration(), converters).getPayload(), "A");
    }

    @Test
    public void converterForObjectReceivesTheRegisteredSpecificType() {
        var converter = new org.springframework.http.converter.AbstractHttpMessageConverter<Object>(
                org.springframework.http.MediaType.APPLICATION_JSON) {
            @Override protected boolean supports(Class<?> type) { return true; }
            @Override protected Object readInternal(Class<?> type, HttpInputMessage input) {
                assertEquals(type, VertxHttpCustomConverterIT.Payload.class);
                return new VertxHttpCustomConverterIT.Payload("typed");
            }
            @Override protected void writeInternal(Object value, org.springframework.http.HttpOutputMessage output) { }
        };
        var registration = new VertxMessageBodyConverter<>(VertxHttpCustomConverterIT.Payload.class, converter);
        var request = new VertxHttpMessageMapper.RequestSnapshot("POST", "/", "/", null, "HTTP/1.1",
                Map.of("Content-Type", List.of("application/json")), new byte[] {65}, "id", 1);
        assertEquals(VertxHttpMessageMapper.toHttpMessage(request, new HttpEndpointConfiguration(), List.of(registration)).getPayload(),
                new VertxHttpCustomConverterIT.Payload("typed"));
    }

    @Test
    public void invalidRegistrationsAreRejected() {
        expectThrows(NullPointerException.class, () -> new VertxMessageBodyConverter<>(String.class, null));
        expectThrows(NullPointerException.class, () -> new VertxHttpServer().setMessageBodyConverters(null));
    }
}
