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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.vertx.VertxHttpServer;
import org.citrusframework.http.vertx.VertxPrototypeAdapters;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.TestNGCitrusSupport;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.springframework.http.MediaType;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * End-to-end SOAP exchanges through the test-only Vert.x listener. The Citrus SOAP client sends
 * real HTTP requests; the prototype converts them to Citrus SOAP messages, invokes the endpoint
 * adapter and serializes the reply. Explicitly rejected inputs never reach the adapter.
 */
@Test
public class VertxSoapIT extends TestNGCitrusSupport implements TestActionSupport {

    private static final String REQUEST = "<greet xmlns=\"urn:test\">hello</greet>";
    private static final String REPLY = "<greetResponse xmlns=\"urn:test\">world</greetResponse>";

    private final AtomicReference<Function<Message, Message>> handler = new AtomicReference<>();

    private VertxHttpServer server;
    private WebServiceClient soap11;
    private WebServiceClient soap12;

    @BeforeClass
    public void startPrototype() {
        server = new VertxHttpServer();
        server.setName("vertxSoap");
        server.setPort(0);
        server.getHttpEndpointConfiguration().setBinaryMediaTypes(List.of(MediaType.APPLICATION_OCTET_STREAM,
                MediaType.valueOf("text/xml"), MediaType.valueOf("application/soap+xml")));
        server.setMessageExchange(new VertxSoapMessageExchange());
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message ->
                handler.get().apply(message)));
        server.initialize();
        server.start();

        soap11 = soapClient(false);
        soap12 = soapClient(true);
    }

    private WebServiceClient soapClient(boolean useSoap12) {
        var configuration = new WebServiceEndpointConfiguration();
        configuration.setDefaultUri("http://127.0.0.1:" + server.getActualPort() + "/soap");
        configuration.setTimeout(5000L);
        if (useSoap12) {
            var factory = new SaajSoapMessageFactory();
            factory.setSoapVersion(SoapVersion.SOAP_12);
            factory.afterPropertiesSet();
            configuration.setMessageFactory(factory);
        }
        return new WebServiceClient(configuration);
    }

    @AfterClass(alwaysRun = true)
    public void stopPrototype() {
        if (server != null) {
            server.stop();
            server.destroy();
        }
    }

    @CitrusTest
    public void soap11RoundtripUsesCitrusActions() {
        handler.set(message -> {
            Assert.assertTrue(message instanceof SoapMessage);
            Assert.assertTrue(message.getPayload(String.class).contains("hello"));
            Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "urn:greet");
            Assert.assertEquals(message.getHeader(SoapMessageHeaders.HTTP_REQUEST_METHOD), "POST");
            return new SoapMessage(REPLY);
        });

        given(soap().client(soap11)
                .send()
                .message()
                .body(REQUEST)
                .soapAction("urn:greet"));

        then(soap().client(soap11)
                .receive()
                .message()
                .body(REPLY)
                .validate(validation().xml().schemaValidation(false)));
    }

    @CitrusTest
    public void soap12RoundtripUsesCitrusActions() {
        handler.set(message -> {
            Assert.assertTrue(message instanceof SoapMessage);
            Assert.assertTrue(message.getPayload(String.class).contains("hello"));
            Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "urn:greet");
            return new SoapMessage(REPLY);
        });

        given(soap().client(soap12)
                .send()
                .message()
                .body(REQUEST)
                .soapAction("urn:greet"));

        then(soap().client(soap12)
                .receive()
                .message()
                .body(REPLY)
                .validate(validation().xml().schemaValidation(false)));
    }

    @Test
    public void explicitStatusEndsExchangeWithoutSoapBody() throws Exception {
        handler.set(message -> new DefaultMessage("")
                .setHeader(SoapMessageHeaders.HTTP_STATUS_CODE, 202));
        var response = post("text/xml;charset=UTF-8", VertxSoapMessageExchangeTest.envelope(false, REQUEST));
        Assert.assertEquals(response.statusCode(), 202);
    }

    @Test
    public void unsupportedInputsAreRejectedBeforeCitrus() throws Exception {
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        handler.set(message -> {
            calls.incrementAndGet();
            return new SoapMessage(REPLY);
        });

        Assert.assertEquals(post("text/xml;charset=UTF-8", "<broken>").statusCode(), 400);
        Assert.assertEquals(post("application/json", "{}").statusCode(), 415);
        Assert.assertEquals(get("/soap").statusCode(), 405);
        Assert.assertEquals(calls.get(), 0);
    }

    @Test
    public void requiredHeaderReturnsSoapFault() throws Exception {
        handler.set(message -> new SoapMessage(REPLY));
        String xml = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<s:Header><h:trace xmlns:h=\"urn:test\" s:mustUnderstand=\"1\">abc</h:trace></s:Header>"
                + "<s:Body>" + REQUEST + "</s:Body></s:Envelope>";
        var response = post("text/xml;charset=UTF-8", xml);
        Assert.assertEquals(response.statusCode(), 500);
        Assert.assertTrue(response.body().contains("MustUnderstand"));
    }

    @Test
    public void attachmentsAreRejected() throws Exception {
        handler.set(message -> new SoapMessage(REPLY)
                .addAttachment(new SoapAttachment("file", "text/plain", "bytes")));
        var response = post("text/xml;charset=UTF-8", VertxSoapMessageExchangeTest.envelope(false, REQUEST));
        Assert.assertEquals(response.statusCode(), 500);
    }

    private HttpResponse<String> get(String path) throws Exception {
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build().send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + path))
                        .timeout(Duration.ofSeconds(5)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> post(String contentType, String xml) throws Exception {
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build().send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + "/soap"))
                        .timeout(Duration.ofSeconds(5))
                        .header("Content-Type", contentType)
                        .header("SOAPAction", "\"urn:greet\"")
                        .POST(HttpRequest.BodyPublishers.ofString(xml, StandardCharsets.UTF_8)).build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
