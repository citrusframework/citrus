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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;

import static org.mockito.Mockito.mock;

public class HttpMessageTest {

    private HttpMessage httpMessage;

    @BeforeMethod
    public void setUp(){
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
        Assert.assertTrue(httpMessage.getCookies().contains(cookie));
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
        Assert.assertTrue(httpMessage.getCookies().contains(expectedCookie));
        Assert.assertEquals(httpMessage.getCookies().size(), 1);
    }
}