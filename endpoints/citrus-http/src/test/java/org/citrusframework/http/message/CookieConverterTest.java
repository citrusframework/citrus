/*
 *    Copyright 2018-2024 the original author or authors
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

public class CookieConverterTest {

    private CookieConverter cookieConverter = new CookieConverter();

    private Cookie cookie;
    private HttpHeaders cookieHeaders;

    @BeforeMethod
    public void setUp() {
        cookie = new Cookie("foo", "bar");
        cookieHeaders = new HttpHeaders();
    }

    @Test
    public void testCookiesAreParsedCorrectly() {
        // GIVEN
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        // WHEN
        final Cookie[] cookies = cookieConverter.convertCookies(responseEntity);

        // THEN
        assertEquals("foo", cookies[0].getName());
        assertEquals("bar", cookies[0].getValue());
    }

    @Test
    public void testAdditionalCookieDirectivesAreDiscarded() {
        // GIVEN
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;HttpOnly"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        // WHEN
        final Cookie[] cookies = cookieConverter.convertCookies(responseEntity);

        // THEN
        assertEquals("foo", cookies[0].getName());
        assertEquals("bar", cookies[0].getValue());
    }

    @Test
    public void testCookieCommentIsNoLongerPreserved() {
        // GIVEN
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Comment=wtf"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        // WHEN
        final Cookie[] cookies = cookieConverter.convertCookies(responseEntity);

        // THEN
        assertNull(cookies[0].getComment());
    }

    @Test
    public void testCookieDomainIsPreserved() {
        // GIVEN
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Domain=whatever"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        // WHEN
        final Cookie[] cookies = cookieConverter.convertCookies(responseEntity);

        // THEN
        assertEquals("whatever", cookies[0].getDomain());
    }

    @Test
    public void testCookieMaxAgeIsPreserved() {
        // GIVEN
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Max-Age=42"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        // WHEN
        final Cookie[] cookies = cookieConverter.convertCookies(responseEntity);

        // THEN
        assertEquals(42, cookies[0].getMaxAge());
    }

    @Test
    public void testCookiePathIsPreserved() {
        // GIVEN
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Path=foobar"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        // WHEN
        final Cookie[] cookies = cookieConverter.convertCookies(responseEntity);

        // THEN
        assertEquals("foobar", cookies[0].getPath());
    }


    @Test
    public void testCookieSecureIsPreserved() {
        // GIVEN
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Secure"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        // WHEN
        final Cookie[] cookies = cookieConverter.convertCookies(responseEntity);

        // THEN
        assertTrue(cookies[0].getSecure());
    }

    @Test
    public void testCookieVersionIsNoLongerPreserved() {
        // GIVEN
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Version=1"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        // WHEN
        final Cookie[] cookies = cookieConverter.convertCookies(responseEntity);

        // THEN
        assertEquals(0, cookies[0].getVersion());
    }

    @Test
    public void testCookieHttpOnlyIsPreserved() {
        // GIVEN
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;HttpOnly"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        // WHEN
        final Cookie[] cookies = cookieConverter.convertCookies(responseEntity);

        // THEN
        assertTrue(cookies[0].isHttpOnly());
    }

    @Test
    public void testCookieStringDoesNotContainVersion() {
        // GIVEN
        cookie.setVersion(42);
        String expectedCookieString = "foo=bar";

        // WHEN
        final String cookieString = cookieConverter.getCookieString(cookie);

        // THEN
        assertEquals(expectedCookieString, cookieString);
    }

    @Test
    public void testCookieStringContainsPath() {
        // GIVEN
        cookie.setPath("/foo/bar");
        String expectedCookieString = "foo=bar;Path=/foo/bar";

        // WHEN
        final String cookieString = cookieConverter.getCookieString(cookie);

        // THEN
        assertEquals(expectedCookieString, cookieString);
    }

    @Test
    public void testCookieStringContainsDomain() {
        // GIVEN
        cookie.setDomain("localhost");
        String expectedCookieString = "foo=bar;Domain=localhost";

        // WHEN
        final String cookieString = cookieConverter.getCookieString(cookie);

        // THEN
        assertEquals(expectedCookieString, cookieString);
    }

    @Test
    public void testCookieStringContainsMaxAge() {
        // GIVEN
        cookie.setMaxAge(42);
        String expectedCookieString = "foo=bar;Max-Age=42";

        // WHEN
        final String cookieString = cookieConverter.getCookieString(cookie);

        // THEN
        assertEquals(expectedCookieString, cookieString);
    }

    @Test
    public void testCookieStringContainsComment() {
        // GIVEN
        cookie.setComment("whatever");
        String expectedCookieString = "foo=bar";

        // WHEN
        final String cookieString = cookieConverter.getCookieString(cookie);

        // THEN
        assertEquals(expectedCookieString, cookieString);
    }

    @Test
    public void testCookieStringContainsSecure() {
        // GIVEN
        cookie.setSecure(true);
        String expectedCookieString = "foo=bar;Secure";

        // WHEN
        final String cookieString = cookieConverter.getCookieString(cookie);

        // THEN
        assertEquals(expectedCookieString, cookieString);
    }

    @Test
    public void testCookieStringContainsHttpOnly() {
        // GIVEN
        cookie.setHttpOnly(true);
        String expectedCookieString = "foo=bar;HttpOnly";

        // WHEN
        final String cookieString = cookieConverter.getCookieString(cookie);

        // THEN
        assertEquals(expectedCookieString, cookieString);
    }
}
