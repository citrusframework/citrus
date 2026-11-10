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
import org.citrusframework.context.TestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CookieEnricherTest {

    private final CookieEnricher cookieEnricher = new CookieEnricher();
    private TestContext testContextMock;

    private Cookie cookie;

    @BeforeEach
    public void setUp() {
        testContextMock = new TestContext();
        cookie = new Cookie("foo", "bar");
    }

    @Test
    public void testCookiesArePreserved() {
        // GIVEN
        cookie.setMaxAge(42);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        final List<Cookie> cookies = Collections.singletonList(cookie);

        // WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        // THEN
        assertEquals(1, enrichedCookies.size());

        final Cookie enrichedCookie = enrichedCookies.get(0);
        assertEquals("foo", enrichedCookie.getName());
        assertEquals("bar", enrichedCookie.getValue());
        assertEquals(42, enrichedCookie.getMaxAge());
        assertTrue(enrichedCookie.isHttpOnly());
        assertTrue(enrichedCookie.getSecure());
    }

    @Test
    public void testTwoCookiesArePreserved() {
        // GIVEN
        final List<Cookie> cookies = Arrays.asList(cookie, cookie);

        // WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        // THEN
        assertEquals(2, enrichedCookies.size());
    }

    @Test
    public void testValueVariablesAreReplaced() {
        // GIVEN
        Cookie cookie = new Cookie("foo", "${foobar}");
        final List<Cookie> cookies = Collections.singletonList(cookie);

        testContextMock.setVariable("foobar", "bar");

        // WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        // THEN
        assertEquals("foo", enrichedCookies.get(0).getName());
        assertEquals("bar", enrichedCookies.get(0).getValue());
    }

    @Test
    public void testPathVariablesAreReplaced() {
        // GIVEN
        cookie.setPath("/path/to/${variable}");
        final List<Cookie> cookies = Collections.singletonList(cookie);

        testContextMock.setVariable("variable", "foobar");

        // WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        // THEN
        assertEquals("foo", enrichedCookies.get(0).getName());
        assertEquals("/path/to/foobar", enrichedCookies.get(0).getPath());
    }

    @Test
    public void testDomainVariablesAreReplaced() {
        // GIVEN
        cookie.setDomain("${variable}");
        final List<Cookie> cookies = Collections.singletonList(cookie);

        testContextMock.setVariable("variable", "localhost");

        // WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        // THEN
        assertEquals("foo", enrichedCookies.get(0).getName());
        assertEquals("localhost", enrichedCookies.get(0).getDomain());
    }
}
