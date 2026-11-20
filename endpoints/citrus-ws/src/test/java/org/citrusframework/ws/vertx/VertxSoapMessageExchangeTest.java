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

package org.citrusframework.ws.vertx;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.vertx.VertxPrototypeAdapters;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxSoapMessageExchangeTest {
    public static String envelope(boolean soap12, String body) {
        return "<s:Envelope xmlns:s=\"" + (soap12 ? "http://www.w3.org/2003/05/soap-envelope" : "http://schemas.xmlsoap.org/soap/envelope/")
                + "\"><s:Header><h:trace xmlns:h=\"urn:test\">abc</h:trace></s:Header><s:Body>" + body + "</s:Body></s:Envelope>";
    }
    static HttpMessage request(boolean soap12, String xml) {
        return new HttpMessage(xml.getBytes(StandardCharsets.UTF_8)).method(org.springframework.http.HttpMethod.POST)
                .path("/soap").contentType(soap12 ? "application/soap+xml;charset=UTF-8;action=\"urn:hello\"" : "text/xml;charset=UTF-8")
                .header("SOAPAction", "\"urn:hello\"");
    }
    @Test public void convertsBothVersionsAndPreservesCitrusMetadata() throws Exception {
        var bridge = new VertxSoapMessageExchange();
        for (boolean soap12 : new boolean[]{false, true}) {
            var request = request(soap12, envelope(soap12, "<hello xmlns=\"urn:test\">request</hello>"));
            request.setHeader("citrus_direct_replyQueue", "queue-marker");
            var response = (HttpMessage) bridge.exchange(request, VertxPrototypeAdapters.delegate(message -> {
                assertTrue(message instanceof SoapMessage);
                assertTrue(message.getPayload(String.class).contains("request"));
                assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "urn:hello");
                assertEquals(message.getHeader("trace"), "abc");
                assertEquals(message.getHeader("citrus_direct_replyQueue"), "queue-marker");
                return new SoapMessage("<reply xmlns=\"urn:test\">ok</reply>").header("{urn:test}result", "yes");
            }));
            assertEquals(response.getStatusCode().value(), 200);
            String body = new String((byte[]) response.getPayload(), StandardCharsets.UTF_8);
            assertTrue(body.contains("reply")); assertTrue(body.contains("yes"));
            assertTrue(body.contains(soap12 ? "http://www.w3.org/2003/05/soap-envelope" : "http://schemas.xmlsoap.org/soap/envelope/"));
        }
    }
    @Test public void rejectsMalformedUnsupportedAndEntityInputBeforeCitrus() throws Exception {
        var bridge = new VertxSoapMessageExchange(); var calls = new AtomicInteger();
        var adapter = VertxPrototypeAdapters.delegate(message -> { calls.incrementAndGet(); return new SoapMessage("<ok/>"); });
        for (String xml : new String[]{"<broken>", "<!DOCTYPE x [<!ENTITY e SYSTEM 'file:///does-not-exist'>]>" + envelope(false, "<x>&e;</x>"),
                "<x/>", envelope(true, "<x/>"), envelope(false, "<x/><y/>")}) {
            assertEquals(((HttpMessage) bridge.exchange(request(false, xml), adapter)).getStatusCode().value(), 400);
        }
        assertEquals(((HttpMessage) bridge.exchange(request(false, "x").contentType("multipart/related;boundary=x"), adapter)).getStatusCode().value(), 415);
        assertEquals(calls.get(), 0);
    }
}
