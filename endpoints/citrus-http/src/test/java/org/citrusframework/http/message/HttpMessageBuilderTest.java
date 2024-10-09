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
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.setProperty;
import static java.util.Collections.singletonList;
import static org.citrusframework.CitrusSettings.HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_DEFAULT;
import static org.citrusframework.CitrusSettings.HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_PROPERTY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class HttpMessageBuilderTest {

    private HttpMessage message;

    @BeforeMethod
    public void setUp() {
        message = new HttpMessage("");
        message.setHeader(MessageHeaders.TIMESTAMP, currentTimeMillis() - 1);
    }

    @Test
    public void testDefaultMessageHeader() {
        //GIVEN
        final HttpMessageBuilder builder = getBuilder();

        //WHEN
        final Message builtMessage = builder.build(new TestContext(), MessageType.XML.name());

        //THEN
        assertEquals(builtMessage.getHeaders().entrySet().size(), 3);
        assertNotNull(message.getHeader(MessageHeaders.ID));
        assertNotNull(message.getHeader(MessageHeaders.TIMESTAMP));
        assertNotEquals(message.getHeader(MessageHeaders.ID), builtMessage.getHeader(MessageHeaders.ID));
        assertNotEquals(message.getHeader(MessageHeaders.TIMESTAMP), builtMessage.getHeader(MessageHeaders.TIMESTAMP));
        assertEquals(builtMessage.getType(), MessageType.XML.toString());
    }

    @Test
    public void testDefaultMessageHeaderWithNoForceUpdate() {
        try {
            setProperty(HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_PROPERTY, "false");

            //GIVEN
            final HttpMessageBuilder builder = getBuilder();

            //WHEN
            final Message builtMessage = builder.build(new TestContext(), MessageType.XML.name());

            //THEN
            assertEquals(builtMessage.getHeaders().entrySet().size(), 3);
            assertNotNull(message.getHeader(MessageHeaders.ID));
            assertNotNull(message.getHeader(MessageHeaders.TIMESTAMP));
            assertEquals(message.getHeader(MessageHeaders.ID), builtMessage.getHeader(MessageHeaders.ID));
            assertEquals(message.getHeader(MessageHeaders.TIMESTAMP), builtMessage.getHeader(MessageHeaders.TIMESTAMP));
            assertEquals(builtMessage.getType(), MessageType.XML.toString());
        } finally {
            setProperty(HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_PROPERTY, HTTP_MESSAGE_BUILDER_FORCE_CITRUS_HEADER_UPDATE_ENABLED_DEFAULT);
        }
    }

    @Test
    public void testHeaderVariableSubstitution() {
        //GIVEN
        final HttpMessageBuilder builder = getBuilder();

        final TestContext testContext = new TestContext();
        testContext.setVariable("testHeader", "foo");
        testContext.setVariable("testValue", "bar");

        message.setHeader("${testHeader}", "${testValue}");

        //WHEN
        final Message builtMessage = builder.build(testContext, String.valueOf(MessageType.XML));

        //THEN
        assertEquals(builtMessage.getHeader("foo"), "bar");
    }

    @Test
    public void testTemplateHeadersArePreserved() {
        //GIVEN
        final HttpMessageBuilder builder = getBuilder();
        message.setHeader("foo", "bar");

        //WHEN
        final HttpMessage builtMessage = (HttpMessage) builder.build(
                new TestContext(),
                String.valueOf(MessageType.XML));

        //THEN
        assertEquals(builtMessage.getHeader("foo"), "bar");
    }

    @Test
    public void testCookieEnricherIsCalledForTemplateCookies() {
        //GIVEN
        final CookieEnricher cookieEnricherMock = mock(CookieEnricher.class);
        final TestContext testContextMock = mock(TestContext.class);
        final Cookie templateCookie = mock(Cookie.class);
        message.setCookies(new Cookie[]{templateCookie});

        final Cookie enrichedCookie = mock(Cookie.class);

        when(cookieEnricherMock.enrich(
                singletonList(templateCookie),
                testContextMock))
                .thenReturn(singletonList(enrichedCookie));

        final HttpMessageBuilder builder = new HttpMessageBuilder(message, cookieEnricherMock);

        //WHEN
        final HttpMessage message = (HttpMessage) builder.build(testContextMock, String.valueOf(MessageType.XML));

        //THEN
        assertEquals(message.getCookies().size(), 1);
        assertTrue(message.getCookies().contains(enrichedCookie));
    }

    private HttpMessageBuilder getBuilder() {
        return new HttpMessageBuilder(message, mock(CookieEnricher.class));
    }
}
