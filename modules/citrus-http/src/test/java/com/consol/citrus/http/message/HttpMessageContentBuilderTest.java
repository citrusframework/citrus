/*
 * Copyright 2006-2017 the original author or authors.
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


package com.consol.citrus.http.message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class HttpMessageContentBuilderTest {

    private HttpMessage message;

    @BeforeMethod
    public void setUp(){
        message = new HttpMessage("");
    }

    @Test
    public void testDefaultMessageHeader(){

        //GIVEN
        final HttpMessageContentBuilder builder = getBuilder();

        //WHEN
        final Message builtMessage = builder.buildMessageContent(new TestContext(), String.valueOf(MessageType.XML));

        //THEN
        assertEquals(builtMessage.getHeaders().entrySet().size(), 3);
        assertEquals(message.getHeader(MessageHeaders.ID), builtMessage.getHeader(MessageHeaders.ID));
        assertEquals(message.getHeader(MessageHeaders.TIMESTAMP), builtMessage.getHeader(MessageHeaders.TIMESTAMP));
        assertEquals(MessageType.XML.toString(), builtMessage.getHeader(MessageHeaders.MESSAGE_TYPE));
    }

    @Test
    public void testHeaderVariableSubstitution() {

        //GIVEN
        final HttpMessageContentBuilder builder = getBuilder();

        final TestContext testContext = new TestContext();
        testContext.setVariable("testHeader", "foo");
        testContext.setVariable("testValue", "bar");

        message.setHeader("${testHeader}", "${testValue}");

        //WHEN
        final Message builtMessage = builder.buildMessageContent(testContext, String.valueOf(MessageType.XML));

        //THEN
        assertEquals(builtMessage.getHeader("foo"), "bar");
    }

    @Test
    public void testTemplateHeadersArePreserved(){

        //GIVEN
        final HttpMessageContentBuilder builder = getBuilder();
        message.setHeader("foo", "bar");

        //WHEN
        final HttpMessage builtMessage = (HttpMessage) builder.buildMessageContent(
                new TestContext(),
                String.valueOf(MessageType.XML));

        //THEN
        assertEquals(builtMessage.getHeader("foo"), "bar");
    }

    @Test
    public void testCookieEnricherIsCalledForTemplateCookies(){

        //GIVEN
        final CookieEnricher cookieEnricherMock = mock(CookieEnricher.class);
        final TestContext testContextMock = mock(TestContext.class);
        final Cookie templateCookie = mock(Cookie.class);
        message.setCookies(new Cookie[]{templateCookie});

        final Cookie enrichedCookie = mock(Cookie.class);

        when(cookieEnricherMock.enrich(
                eq(Collections.singletonList(templateCookie)),
                eq(testContextMock)))
                .thenReturn(Collections.singletonList(enrichedCookie));

        final HttpMessageContentBuilder builder = new HttpMessageContentBuilder(
                message,
                new StaticMessageContentBuilder(message),
                cookieEnricherMock);

        //WHEN
        final HttpMessage message = (HttpMessage) builder.buildMessageContent(
                testContextMock, String.valueOf(MessageType.XML));

        //THEN
        assertEquals(message.getCookies().size(), 1);
        assertTrue(message.getCookies().contains(enrichedCookie));
    }

    private HttpMessageContentBuilder getBuilder() {
        return new HttpMessageContentBuilder(
                message,
                new StaticMessageContentBuilder(message),
                mock(CookieEnricher.class));
    }
}
