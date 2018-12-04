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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;
import java.util.Collections;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class CookieParserTest {

    private CookieParser cookieParser = new CookieParser();

    @Test
    public void testCookiesAreParsedCorrectly(){

        //GIVEN
        final HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        //WHEN
        final Cookie[] cookies = cookieParser.convertCookies(responseEntity);

        //THEN
        assertEquals(1, cookies.length);
        assertEquals("foo", cookies[0].getName());
        assertEquals("bar", cookies[0].getValue());
    }

    @Test
    public void testAdditionalCookieDirectivesAreDiscarded(){

        //GIVEN
        final HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;HttpOnly"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        //WHEN
        final Cookie[] cookies = cookieParser.convertCookies(responseEntity);

        //THEN
        assertEquals(1, cookies.length);
        assertEquals("foo", cookies[0].getName());
        assertEquals("bar", cookies[0].getValue());
    }

    @Test
    public void testCookieCommentIsPreserved(){

        //GIVEN
        final HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Comment=wtf"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        //WHEN
        final Cookie[] cookies = cookieParser.convertCookies(responseEntity);

        //THEN
        assertEquals(1, cookies.length);
        assertEquals("wtf", cookies[0].getComment());
    }

    @Test
    public void testCookieDomainIsPreserved(){

        //GIVEN
        final HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Domain=whatever"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        //WHEN
        final Cookie[] cookies = cookieParser.convertCookies(responseEntity);

        //THEN
        assertEquals(1, cookies.length);
        assertEquals("whatever", cookies[0].getDomain());
    }

    @Test
    public void testCookieEndParameterIsRecognizedAndPreserved(){

        //GIVEN
        final HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Version=1;"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        //WHEN
        final Cookie[] cookies = cookieParser.convertCookies(responseEntity);

        //THEN
        assertEquals(1, cookies.length);
        assertEquals(1, cookies[0].getVersion());
    }

    @Test
    public void testCookieMaxAgeIsPreserved(){

        //GIVEN
        final HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Max-Age=42"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        //WHEN
        final Cookie[] cookies = cookieParser.convertCookies(responseEntity);

        //THEN
        assertEquals(1, cookies.length);
        assertEquals(42, cookies[0].getMaxAge());
    }

    @Test
    public void testCookiePathIsPreserved(){

        //GIVEN
        final HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Path=foobar"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        //WHEN
        final Cookie[] cookies = cookieParser.convertCookies(responseEntity);

        //THEN
        assertEquals(1, cookies.length);
        assertEquals("foobar", cookies[0].getPath());
    }


    @Test
    public void testCookieSecureIsPreserved(){

        //GIVEN
        final HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Secure"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        //WHEN
        final Cookie[] cookies = cookieParser.convertCookies(responseEntity);

        //THEN
        assertEquals(1, cookies.length);
        assertTrue(cookies[0].getSecure());
    }

    @Test
    public void testCookieVersionIsPreserved(){

        //GIVEN
        final HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.put("Set-Cookie", Collections.singletonList("foo=bar;Version=1"));
        final ResponseEntity<?> responseEntity = new ResponseEntity<>(cookieHeaders, HttpStatus.OK);

        //WHEN
        final Cookie[] cookies = cookieParser.convertCookies(responseEntity);

        //THEN
        assertEquals(1, cookies.length);
        assertEquals(1, cookies[0].getVersion());
    }
}