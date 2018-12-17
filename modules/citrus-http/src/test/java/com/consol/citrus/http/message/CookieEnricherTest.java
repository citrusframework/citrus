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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CookieEnricherTest {

    private final CookieEnricher cookieEnricher = new CookieEnricher();
    private TestContext testContextMock;

    @BeforeMethod
    public void setUp(){
        testContextMock = new TestContext();
    }

    @Test
    public void testCookiesArePreserved(){

        //GIVEN
        Cookie cookie = new Cookie("foo", "bar");
        cookie.setMaxAge(42);
        cookie.setVersion(24);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        final List<Cookie> cookies = Collections.singletonList(cookie);

        //WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        //THEN
        assertEquals(enrichedCookies.size(), 1);

        final Cookie enrichedCookie = enrichedCookies.get(0);
        assertEquals(enrichedCookie.getName(), "foo");
        assertEquals(enrichedCookie.getMaxAge(), 42);
        assertEquals(enrichedCookie.getVersion(), 24);
        assertTrue(enrichedCookie.isHttpOnly());
        assertTrue(enrichedCookie.getSecure());
    }

    @Test
    public void testTwoCookiesArePreserved(){

        //GIVEN
        Cookie cookie1 = new Cookie("foo", "bar");
        Cookie cookie2 = new Cookie("foo", "bar");
        final List<Cookie> cookies = Arrays.asList(cookie1, cookie2);

        //WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        //THEN
        assertEquals(enrichedCookies.size(), 2);
    }

    @Test
    public void testValueVariablesAreReplaced(){

        //GIVEN
        Cookie cookie = new Cookie("foo", "${foobar}");
        final List<Cookie> cookies = Collections.singletonList(cookie);

        testContextMock.setVariable("foobar", "bar");

        //WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        //THEN
        assertEquals(enrichedCookies.size(), 1);
        assertEquals(enrichedCookies.get(0).getName(), "foo");
        assertEquals(enrichedCookies.get(0).getValue(), "bar");
    }

    @Test
    public void testCommentVariablesAreReplaced(){

        //GIVEN
        Cookie cookie = new Cookie("foo", "bar");
        cookie.setComment("${variable}");
        final List<Cookie> cookies = Collections.singletonList(cookie);

        testContextMock.setVariable("variable", "foobar");

        //WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        //THEN
        assertEquals(enrichedCookies.size(), 1);
        assertEquals(enrichedCookies.get(0).getName(), "foo");
        assertEquals(enrichedCookies.get(0).getComment(), "foobar");
    }

    @Test
    public void testPathVariablesAreReplaced(){

        //GIVEN
        Cookie cookie = new Cookie("foo", "bar");
        cookie.setPath("/path/to/${variable}");
        final List<Cookie> cookies = Collections.singletonList(cookie);

        testContextMock.setVariable("variable", "foobar");

        //WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        //THEN
        assertEquals(enrichedCookies.size(), 1);
        assertEquals(enrichedCookies.get(0).getName(), "foo");
        assertEquals(enrichedCookies.get(0).getPath(), "/path/to/foobar");
    }

    @Test
    public void testDomainVariablesAreReplaced(){

        //GIVEN
        Cookie cookie = new Cookie("foo", "bar");
        cookie.setDomain("${variable}");
        final List<Cookie> cookies = Collections.singletonList(cookie);

        testContextMock.setVariable("variable", "localhost");

        //WHEN
        final List<Cookie> enrichedCookies = cookieEnricher.enrich(cookies, testContextMock);

        //THEN
        assertEquals(enrichedCookies.size(), 1);
        assertEquals(enrichedCookies.get(0).getName(), "foo");
        assertEquals(enrichedCookies.get(0).getDomain(), "localhost");
    }
}