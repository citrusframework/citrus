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

package org.citrusframework.http.message;

import jakarta.servlet.http.Cookie;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.message.MessageHeaderUtils;
import org.citrusframework.message.MessageHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpMessageTest {

    private HttpMessage httpMessage;

    @BeforeEach
    public void setUp() {
        httpMessage = new HttpMessage();
    }

    @Test
    public void testSetCookies() {
        //GIVEN
        final Cookie cookie = mock(Cookie.class);
        final Cookie[] cookies = new Cookie[]{cookie};

        //WHEN
        httpMessage.setCookies(cookies);

        //THEN
        assertTrue(httpMessage.getCookies().contains(cookie));
    }

    /**
     * Required by <a href="https://tools.ietf.org/html/rfc6265#section-4.1.1">rfc6265-section-4.1.1</a>
     */
    @Test
    public void testCookiesWithSameNamesAreOverwritten() {
        //GIVEN
        final Cookie cookie = new Cookie("foo", "bar");
        httpMessage.cookie(cookie);

        final Cookie expectedCookie = new Cookie("foo", "foobar");

        //WHEN
        httpMessage.cookie(expectedCookie);

        //THEN
        assertEquals( 1,httpMessage.getCookies().size());
        assertEquals( "foobar",httpMessage.getCookies().get(0).getValue());
    }

    @Test
    public void testSetCookiesOverwritesOldCookies() {
        //GIVEN
        httpMessage.setCookies(new Cookie[]{
                mock(Cookie.class),
                mock(Cookie.class)});

        final Cookie expectedCookie = mock(Cookie.class);
        final Cookie[] cookies = new Cookie[]{expectedCookie};

        //WHEN
        httpMessage.setCookies(cookies);

        //THEN
        assertTrue(httpMessage.getCookies().contains(expectedCookie));
        assertEquals( 1,httpMessage.getCookies().size());
    }

    @Test
    public void testCopyConstructorPreservesCookies() {
        //GIVEN
        final Cookie expectedCookie = mock(Cookie.class);
        final HttpMessage originalMessage = new HttpMessage();
        originalMessage.cookie(expectedCookie);

        //WHEN
        final HttpMessage messageCopy = new HttpMessage(originalMessage);

        //THEN
        assertEquals( originalMessage.getCookies(),messageCopy.getCookies());
    }

    @Test
    public void testParseQueryParamsAreParsedCorrectly() {
        //GIVEN
        final String queryParamString = "foo=foobar,bar=barbar";

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParams(queryParamString);

        //THEN
        final Map<String, Collection<String>> queryParams = resultMessage.getQueryParams();
        assertTrue(queryParams.get("foo").contains("foobar"));
        assertTrue(queryParams.get("bar").contains("barbar"));
    }

    @Test
    public void testParseQueryParamsSetsQueryParamsHeader() {
        //GIVEN
        final String queryParamString = "foo=foobar,bar=barbar";

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParams(queryParamString);

        //THEN
        assertEquals( queryParamString,resultMessage.getHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS));
    }

    @Test
    public void testParseQueryParamsSetsQueryParamHeaderName() {
        //GIVEN
        final String queryParamString = "foo=foobar,bar=barbar";

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParams(queryParamString);

        //THEN
        assertEquals( queryParamString,resultMessage.getHeader(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
    }

    @Test
    public void testQueryParamWithoutValueContainsNull() {
        //GIVEN
        final String queryParam = "foo";

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParam(queryParam);

        //THEN
        assertTrue(resultMessage.getQueryParams().get("foo").contains(null));
    }

    @Test
    public void testQueryParamWithValueIsSetCorrectly() {
        //GIVEN
        final String key = "foo";
        final String value = "foo";

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParam(key, value);

        //THEN
        assertTrue(resultMessage.getQueryParams().get(key).contains(value));
    }

    @Test
    public void testNewQueryParamIsAddedToExistingParams() {
        //GIVEN
        final String existingKey = "foo";
        final String existingValue = "foobar";
        httpMessage.queryParam(existingKey, existingValue);

        final String newKey = "bar";
        final String newValue = "barbar";

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParam(newKey, newValue);

        //THEN
        assertTrue(resultMessage.getQueryParams().get(existingKey).contains(existingValue));
        assertTrue(resultMessage.getQueryParams().get(newKey).contains(newValue));
    }

    @Test
    public void testNewQueryParamIsAddedQueryParamsHeader() {
        //GIVEN
        httpMessage.queryParam("foo", "foobar");

        final String expectedHeaderValue = "bar=barbar,foo=foobar";

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParam("bar", "barbar");

        //THEN
        assertEquals( expectedHeaderValue,resultMessage.getHeader(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
    }

    @Test
    public void testNewQueryParamIsAddedQueryParamHeaderName() {
        //GIVEN
        httpMessage.queryParam("foo", "foobar");

        final String expectedHeaderValue = "bar=barbar,foo=foobar";

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParam("bar", "barbar");

        //THEN
        assertEquals( expectedHeaderValue,resultMessage.getHeader(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
    }

    @Test
    public void testDefaultStatusCodeIsNull() {
        //GIVEN

        //WHEN
        final HttpStatusCode statusCode = httpMessage.getStatusCode();

        //THEN
        assertNull(statusCode);
    }

    @Test
    public void testStringStatusCodeIsParsed() {
        //GIVEN
        httpMessage.header(HttpMessageHeaders.HTTP_STATUS_CODE, "404");

        //WHEN
        final HttpStatusCode statusCode = httpMessage.getStatusCode();

        //THEN
        assertEquals( HttpStatus.NOT_FOUND,statusCode);
    }

    @Test
    public void testIntegerStatusCodeIsParsed() {
        //GIVEN
        httpMessage.header(HttpMessageHeaders.HTTP_STATUS_CODE, 403);

        //WHEN
        final HttpStatusCode statusCode = httpMessage.getStatusCode();

        //THEN
        assertEquals( HttpStatus.FORBIDDEN,statusCode);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testStatusCodeObjectIsPreserved() {
        //GIVEN
        httpMessage.header(HttpMessageHeaders.HTTP_STATUS_CODE, HttpStatus.I_AM_A_TEAPOT);

        //WHEN
        final HttpStatusCode statusCode = httpMessage.getStatusCode();

        //THEN
        assertEquals( HttpStatus.I_AM_A_TEAPOT,statusCode);
    }

    @Test
    public void testCanHandleCustomStatusCode() {
        //GIVEN
        httpMessage.header(HttpMessageHeaders.HTTP_STATUS_CODE, 555);

        //WHEN
        final HttpStatusCode statusCode = httpMessage.getStatusCode();

        //THEN
        assertEquals( HttpStatusCode.valueOf(555),statusCode);
    }

    @Test
    public void testQueryParamWithMultipleParams() {
        //GIVEN
        httpMessage.queryParam("foo", "bar");

        final String expectedHeaderValue = "foo=bar,foo=foobar";

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParam("foo", "foobar");

        //THEN
        assertEquals( expectedHeaderValue,resultMessage.getHeader(EndpointUriResolver.QUERY_PARAM_HEADER_NAME));
    }

    @Test
    public void testQueryParamsWithEncodedCommaInValue() {
        //GIVEN
        final String queryParamString = "$select=Name%2CValue,$top=10";

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParams(queryParamString);

        //THEN
        final Map<String, Collection<String>> queryParams = resultMessage.getQueryParams();
        assertTrue(queryParams.get("$select").contains("Name,Value"));
        assertTrue(queryParams.get("$top").contains("10"));
    }

    @Test
    public void testQueryParamWithCommaInValueStoresCorrectly() {
        //GIVEN

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParam("$select", "Name,Value");

        //THEN
        assertTrue(resultMessage.getQueryParams().get("$select").contains("Name,Value"));
    }

    @Test
    public void testServerSideIngestionRoundTrip() {
        //GIVEN - simulate HttpMessageController encoding: encode commas, then replace & with ,
        final String rawQueryString = "$select=Name,Value&$top=10";
        final String encodedQueryString = MessageHeaderUtils.encodeQueryParam(rawQueryString).replace("&", ",");

        //WHEN
        final HttpMessage resultMessage = httpMessage.queryParams(encodedQueryString);

        //THEN
        assertTrue(resultMessage.getQueryParams().get("$select").contains("Name,Value"));
        assertTrue(resultMessage.getQueryParams().get("$top").contains("10"));
    }

    @Test
    public void testCopyConstructorPreservesIdAndTimestamp() {
        // Given
        httpMessage.setPayload("myPayload");
        httpMessage.setHeader("k1", "v1");

        // When
        HttpMessage copiedMessage = new HttpMessage(httpMessage);

        // Then
        assertEquals( copiedMessage.getHeader(MessageHeaders.ID),httpMessage.getHeader(MessageHeaders.ID));
        assertEquals( copiedMessage.getHeader(MessageHeaders.TIMESTAMP),httpMessage.getHeader(MessageHeaders.TIMESTAMP));
        assertEquals( copiedMessage.getHeader("k1"),httpMessage.getHeader("k1"));
        assertEquals( copiedMessage.getPayload(),httpMessage.getPayload());

    }

    @Test
    public void testCopyConstructorWithCitrusOverwriteDoesNotPreserveIdAndTimestamp() {
        // Given
        httpMessage.setPayload("myPayload");
        httpMessage.setHeader("k1", "v1");
        httpMessage.setHeader(MessageHeaders.TIMESTAMP, System.currentTimeMillis() - 1);

        // When
        HttpMessage copiedMessage = new HttpMessage(httpMessage, true);

        // Then
        assertNotEquals( copiedMessage.getHeader(MessageHeaders.ID),httpMessage.getHeader(MessageHeaders.ID));
        assertNotEquals( copiedMessage.getHeader(MessageHeaders.TIMESTAMP),httpMessage.getHeader(MessageHeaders.TIMESTAMP));
        assertEquals( copiedMessage.getHeader("k1"),httpMessage.getHeader("k1"));
        assertEquals( copiedMessage.getPayload(),httpMessage.getPayload());
    }
}
