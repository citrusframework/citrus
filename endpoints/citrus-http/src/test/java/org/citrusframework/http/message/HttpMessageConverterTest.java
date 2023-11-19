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

package org.citrusframework.http.message;

import jakarta.servlet.http.Cookie;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.messaging.MessageHeaders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

public class HttpMessageConverterTest {
    private CookieConverter cookieConverterMock;
    private HttpMessageConverter messageConverter;

    private HttpEndpointConfiguration endpointConfiguration;
    private TestContext testContext = new TestContext();
    private HttpMessage message;

    private final String payload = "Hello World!";

    @BeforeMethod
    public void setUp() {
        cookieConverterMock = mock(CookieConverter.class);
        messageConverter = new HttpMessageConverter(cookieConverterMock);

        endpointConfiguration = new HttpEndpointConfiguration();
        testContext = new TestContext();
        message = new HttpMessage();
    }

    @Test
    public void testDefaultMessageIsConvertedOnOutbound() {
        // GIVEN
        Message message = new DefaultMessage(payload);

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        assertEquals(payload, httpEntity.getBody());
    }

    @Test
    public void testHttpMessageCookiesArePreservedOnOutbound() {
        // GIVEN
        Cookie cookie = new Cookie("foo", "bar");
        message.cookie(cookie);

        String expectedCookie = "foo=bar";

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        final List<String> cookies = httpEntity.getHeaders().get("Cookie");
        assert cookies != null;
        assertEquals(1, cookies.size());
        assertEquals(expectedCookie, cookies.get(0));
    }

    @Test
    public void testHttpMessageCookiesValuesAreReplacedOnOutbound() {
        // GIVEN
        Cookie cookie = new Cookie("foo", "${foobar}");
        message.cookie(cookie);

        testContext.setVariable("foobar", "bar");

        String expectedCookie = "foo=bar";

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        final List<String> cookies = httpEntity.getHeaders().get("Cookie");
        assert cookies != null;
        assertEquals(1, cookies.size());
        assertEquals(expectedCookie, cookies.get(0));
    }

    @Test
    public void testHttpMessageHeadersAreReplacedOnOutbound() {
        // GIVEN
        message.header("foo", "bar");

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        final List<String> fooHeader = httpEntity.getHeaders().get("foo");
        assert fooHeader != null;
        assertEquals(1, fooHeader.size());
        assertEquals("bar", fooHeader.get(0));
    }

    @Test
    public void testHttpContentTypeIsPresent() {
        // GIVEN
        endpointConfiguration.setContentType("foobar");

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        final List<String> contentTypeHeader = httpEntity.getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE);
        assert contentTypeHeader != null;
        assertEquals(1, contentTypeHeader.size());
        assertEquals("foobar;charset=UTF-8", contentTypeHeader.get(0));
    }

    @Test
    public void testHttpContentTypeContainsAlteredCharsetIsPresent() {
        // GIVEN
        endpointConfiguration.setContentType("foobar");
        endpointConfiguration.setCharset("whatever");

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        final List<String> contentTypeHeader = httpEntity.getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE);
        assert contentTypeHeader != null;
        assertEquals(1, contentTypeHeader.size());
        assertEquals("foobar;charset=whatever", contentTypeHeader.get(0));
    }

    @Test
    public void testHttpContentTypeCharsetIsMissingWhenEmptyIsPresent() {
        // GIVEN
        endpointConfiguration.setContentType("foobar");
        endpointConfiguration.setCharset("");

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        final List<String> contentTypeHeader = httpEntity.getHeaders().get(HttpMessageHeaders.HTTP_CONTENT_TYPE);
        assert contentTypeHeader != null;
        assertEquals(1, contentTypeHeader.size());
        assertEquals("foobar", contentTypeHeader.get(0));
    }

    @Test
    public void testHttpMethodBodyIsSetForPostOnOutbound() {
        // GIVEN
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.POST);
        message.setPayload(payload);

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        assertEquals(payload, httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsSetForPutOnOutbound() {
        // GIVEN
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.PUT);
        message.setPayload(payload);

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        assertEquals(payload, httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsSetForDeleteOnOutbound() {
        // GIVEN
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.DELETE);
        message.setPayload(payload);

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        assertEquals(payload, httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsSetForPatchOnOutbound() {
        // GIVEN
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.PATCH);
        message.setPayload(payload);

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        assertEquals(payload, httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsNotSetForGetOnOutbound() {
        // GIVEN
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.GET);
        message.setPayload(payload);

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        assertNull(httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsNotSetForHeadOnOutbound() {
        // GIVEN
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.HEAD);
        message.setPayload(payload);

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        assertNull(httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsNotSetForOptionsOnOutbound() {
        // GIVEN
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.OPTIONS);
        message.setPayload(payload);

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        assertNull(httpEntity.getBody());
    }

    @Test
    public void testHttpMethodBodyIsNotSetForTraceOnOutbound() {
        // GIVEN
        message.setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, HttpMethod.TRACE);
        message.setPayload(payload);

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        assertNull(httpEntity.getBody());
    }


    @Test
    public void testHttpMessageWithStatusCodeContainsCookiesOnOutbound() {
        // GIVEN
        message.setHeader(HttpMessageHeaders.HTTP_STATUS_CODE, "200");

        Cookie cookie = new Cookie("foo", "bar");
        message.cookie(cookie);

        String expectedCookie = "foo=bar";

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        final List<String> cookies = httpEntity.getHeaders().get("Cookie");
        assert cookies != null;
        assertEquals(1, cookies.size());
        assertEquals(expectedCookie, cookies.get(0));
    }

    @Test
    public void testCustomStatusCodeIsSetOnOutbound() {
        // GIVEN
        message.setHeader(HttpMessageHeaders.HTTP_STATUS_CODE, "555");

        // WHEN
        final HttpEntity<?> httpEntity = messageConverter.convertOutbound(message, endpointConfiguration, testContext);

        // THEN
        assertEquals(HttpStatusCode.valueOf(555), ((ResponseEntity<?>) httpEntity).getStatusCode());
    }

    @Test
    public void testSpringIntegrationHeaderMapperIsUsedOnOutbound() {
        // GIVEN
        @SuppressWarnings("unchecked")
        HeaderMapper<HttpHeaders> headersHeaderMapperMock = (HeaderMapper<HttpHeaders>) mock(HeaderMapper.class);
        endpointConfiguration.setHeaderMapper(headersHeaderMapperMock);

        // WHEN
        messageConverter.convertOutbound(new HttpMessage(), endpointConfiguration, testContext);

        // THEN
        verify(headersHeaderMapperMock).fromHeaders(any(MessageHeaders.class), any(HttpHeaders.class));
    }

    @Test
    public void testSpringIntegrationHeaderMapperResultIsSetOnInbound() {
        // GIVEN
        final Map<String, Object> expectedHeaderMap = new HashMap<>();
        expectedHeaderMap.put("foo", "bar");

        @SuppressWarnings("unchecked")
        HeaderMapper<HttpHeaders> headersHeaderMapperMock = (HeaderMapper<HttpHeaders>) mock(HeaderMapper.class);
        when(headersHeaderMapperMock.toHeaders(any(HttpHeaders.class))).thenReturn(expectedHeaderMap);

        endpointConfiguration.setHeaderMapper(headersHeaderMapperMock);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(HttpEntity.EMPTY, endpointConfiguration, testContext);

        // THEN
        assertEquals("bar", httpMessage.getHeaders().get("foo"));
    }

    @Test
    public void testCitrusDefaultHeaderAreSetOnInbound() {
        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(HttpEntity.EMPTY, endpointConfiguration, testContext);

        // THEN
        assertTrue(httpMessage.getHeaders().containsKey("citrus_message_id"));
        assertTrue(httpMessage.getHeaders().containsKey("citrus_message_timestamp"));
    }

    @Test
    public void testHttpEntityMessageBodyIsPreservedOnInbound() {
        // GIVEN
        final HttpEntity<String> httpEntity = new HttpEntity<>(payload);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(httpEntity, endpointConfiguration, testContext);

        // THEN
        assertEquals(httpMessage.getPayload(String.class), payload);
    }

    @Test
    public void testHttpEntityDefaultMessageBodyIsSetOnInbound() {
        // GIVEN
        final HttpEntity<String> httpEntity = new HttpEntity<>(null);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(httpEntity, endpointConfiguration, testContext);

        // THEN
        assertEquals(httpMessage.getPayload(String.class), "");
    }

    @Test
    public void testCustomHeadersAreSetOnInbound() {
        // GIVEN
        final HttpHeaders expectedHttpHeader = new HttpHeaders();
        expectedHttpHeader.put("foo", Collections.singletonList("bar"));
        final HttpEntity<?> httpEntity = new HttpEntity<>(null, expectedHttpHeader);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(httpEntity, endpointConfiguration, testContext);

        // THEN
        assertEquals("bar", httpMessage.getHeaders().get("foo"));
    }

    @Test
    public void testCustomHeadersListsAreConvertedToStringOnInbound() {
        // GIVEN
        final HttpHeaders expectedHttpHeader = new HttpHeaders();
        expectedHttpHeader.put("foo", Arrays.asList("bar", "foobar", "foo"));
        final HttpEntity<?> httpEntity = new HttpEntity<>(null, expectedHttpHeader);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(httpEntity, endpointConfiguration, testContext);

        // THEN
        assertEquals("bar,foobar,foo", httpMessage.getHeaders().get("foo"));
    }

    @Test
    public void testStatusCodeIsSetOnInbound() {
        // GIVEN
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(responseEntity, endpointConfiguration, testContext);

        // THEN
        assertEquals(HttpStatus.FORBIDDEN, httpMessage.getStatusCode());
    }

    @Test
    public void testCustomStatusCodeIsSetOnInbound() {
        // GIVEN
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(null, null, 555);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(responseEntity, endpointConfiguration, testContext);

        // THEN
        assertEquals(HttpStatusCode.valueOf(555), httpMessage.getStatusCode());
    }

    @Test
    public void testHttpVersionIsSetOnInbound() {
        // GIVEN
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(responseEntity, endpointConfiguration, testContext);

        // THEN
        assertEquals("HTTP/1.1", httpMessage.getVersion());
    }

    @Test
    public void testNoCookiesPreservedByDefaultOnInbound() {
        // GIVEN

        // WHEN
        messageConverter.convertInbound(ResponseEntity.EMPTY, endpointConfiguration, testContext);

        // THEN
        verify(cookieConverterMock, never()).convertCookies(any());
    }

    @Test
    public void testCookiesPreservedOnConfigurationOnInbound() {
        // GIVEN
        Cookie cookieMock = mock(Cookie.class);
        final ResponseEntity<?> responseEntity = ResponseEntity.ok("foobar");
        when(cookieConverterMock.convertCookies(responseEntity)).thenReturn(new Cookie[]{cookieMock});

        endpointConfiguration.setHandleCookies(true);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(responseEntity, endpointConfiguration, testContext);

        // THEN
        assertEquals(1, httpMessage.getCookies().size());
        assertEquals(cookieMock, httpMessage.getCookies().get(0));
    }


    @Test
    public void testSpringIntegrationHeaderMapperListResultIsConvertedOnInbound() {
        // GIVEN
        final Map<String, Object> mockedHeaderMap = new HashMap<>();
        mockedHeaderMap.put("foo", Arrays.asList("bar", "foobar"));

        @SuppressWarnings("unchecked")
        HeaderMapper<HttpHeaders> headersHeaderMapperMock = (HeaderMapper<HttpHeaders>) mock(HeaderMapper.class);
        when(headersHeaderMapperMock.toHeaders(any(HttpHeaders.class))).thenReturn(mockedHeaderMap);

        endpointConfiguration.setHeaderMapper(headersHeaderMapperMock);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(HttpEntity.EMPTY, endpointConfiguration, testContext);

        // THEN
        assertEquals("bar,foobar", httpMessage.getHeaders().get("foo"));
    }

    @Test
    public void testSpringIntegrationHeaderMapperMediaTypeResultIsConvertedOnInbound() {
        // GIVEN
        final Map<String, Object> mockedHeaderMap = new HashMap<>();
        mockedHeaderMap.put("foo", MediaType.APPLICATION_JSON);

        @SuppressWarnings("unchecked")
        HeaderMapper<HttpHeaders> headersHeaderMapperMock = (HeaderMapper<HttpHeaders>) mock(HeaderMapper.class);
        when(headersHeaderMapperMock.toHeaders(any(HttpHeaders.class))).thenReturn(mockedHeaderMap);

        endpointConfiguration.setHeaderMapper(headersHeaderMapperMock);

        // WHEN
        final HttpMessage httpMessage = messageConverter.convertInbound(HttpEntity.EMPTY, endpointConfiguration, testContext);

        // THEN
        assertEquals("application/json", httpMessage.getHeaders().get("foo"));
    }
}
