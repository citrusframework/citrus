/*
 *    Copyright 2018 the original author or authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.consol.citrus.http.message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class HttpMessageConverterTest {

    private HttpMessageConverter messageConverter = new HttpMessageConverter();

    private HttpEndpointConfiguration endpointConfiguration;
    private TestContext testContext = new TestContext();

    @BeforeMethod
    public void setUp(){
        endpointConfiguration = new HttpEndpointConfiguration();
        testContext = new TestContext();
    }

    @Test
    public void testDefaultMessageIsConvertedOnOutbound(){

        //GIVEN
        final String payload = "Hello World!";
        Message message = new DefaultMessage(payload);

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        assertEquals(payload, httpEntity.getBody());
    }

    @Test
    public void testHttpMessageCookiesArePreservedOnOutbound(){

        //GIVEN
        Cookie cookie = new Cookie("foo","bar");
        HttpMessage message = new HttpMessage();
        message.cookie(cookie);

        String expectedCookie = "foo=bar";

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        final List<String> cookies = httpEntity.getHeaders().get("Cookie");
        assert cookies != null;
        assertEquals(1, cookies.size());
        assertEquals(expectedCookie, cookies.get(0));
    }

    @Test
    public void testHttpMessageCookiesValuesAreReplacedOnOutbound(){

        //GIVEN
        Cookie cookie = new Cookie("foo","${foobar}");
        HttpMessage message = new HttpMessage();
        message.cookie(cookie);

        testContext.setVariable("foobar", "bar");

        String expectedCookie = "foo=bar";

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        final List<String> cookies = httpEntity.getHeaders().get("Cookie");
        assert cookies != null;
        assertEquals(1, cookies.size());
        assertEquals(expectedCookie, cookies.get(0));
    }

    @Test
    public void testHttpMessageHeadersAreReplacedOnOutbound(){

        //GIVEN
        HttpMessage message = new HttpMessage();
        message.header("foo","bar");

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        final List<String> fooHeader = httpEntity.getHeaders().get("foo");
        assert fooHeader != null;
        assertEquals(1, fooHeader.size());
        assertEquals("bar", fooHeader.get(0));
    }

    @Test
    public void testHttpContentTypeIsPresent(){

        //GIVEN
        HttpMessage message = new HttpMessage();
        endpointConfiguration.setContentType("foobar");

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        final List<String> contentTypeHeader = httpEntity.getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE);
        assert contentTypeHeader != null;
        assertEquals(1, contentTypeHeader.size());
        assertEquals("foobar;charset=UTF-8", contentTypeHeader.get(0));
    }

    @Test
    public void testHttpContentTypeContainsAlteredCharsetIsPresent(){

        //GIVEN
        HttpMessage message = new HttpMessage();
        endpointConfiguration.setContentType("foobar");
        endpointConfiguration.setCharset("whatever");

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        final List<String> contentTypeHeader = httpEntity.getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE);
        assert contentTypeHeader != null;
        assertEquals(1, contentTypeHeader.size());
        assertEquals("foobar;charset=whatever", contentTypeHeader.get(0));
    }

    @Test
    public void testHttpContentTypeCharsetIsMissingWhenEmptyIsPresent(){

        //GIVEN
        HttpMessage message = new HttpMessage();
        endpointConfiguration.setContentType("foobar");
        endpointConfiguration.setCharset("");

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        final List<String> contentTypeHeader = httpEntity.getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE);
        assert contentTypeHeader != null;
        assertEquals(1, contentTypeHeader.size());
        assertEquals("foobar", contentTypeHeader.get(0));
    }

    @Test
    public void testHttpMethodBodyIsSetForPostOnOutbound(){

        //GIVEN
        final String payload = "Hello World";
        HttpMessage message = new HttpMessage();
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.POST);
        message.setPayload(payload);

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        assertEquals(payload, httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsSetForPutOnOutbound(){

        //GIVEN
        final String payload = "Hello World";
        HttpMessage message = new HttpMessage();
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.PUT);
        message.setPayload(payload);

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        assertEquals(payload, httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsSetForDeleteOnOutbound(){

        //GIVEN
        final String payload = "Hello World";
        HttpMessage message = new HttpMessage();
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.DELETE);
        message.setPayload(payload);

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        assertEquals(payload, httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsSetForPatchOnOutbound(){

        //GIVEN
        final String payload = "Hello World";
        HttpMessage message = new HttpMessage();
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.PATCH);
        message.setPayload(payload);

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        assertEquals(payload, httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsNotSetForGetOnOutbound(){

        //GIVEN
        final String payload = "Hello World";
        HttpMessage message = new HttpMessage();
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.GET);
        message.setPayload(payload);

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        assertNull(httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsNotSetForHeadOnOutbound(){

        //GIVEN
        final String payload = "Hello World";
        HttpMessage message = new HttpMessage();
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.HEAD);
        message.setPayload(payload);

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        assertNull(httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsNotSetForOptionsOnOutbound(){

        //GIVEN
        final String payload = "Hello World";
        HttpMessage message = new HttpMessage();
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.OPTIONS);
        message.setPayload(payload);

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        assertNull(httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsNotSetForTraceOnOutbound(){

        //GIVEN
        final String payload = "Hello World";
        HttpMessage message = new HttpMessage();
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.TRACE);
        message.setPayload(payload);

        //WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        //THEN
        assertNull(httpEntity.getBody());
    }
}