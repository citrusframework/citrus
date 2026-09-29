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

package org.citrusframework.playwright.config.annotation;

import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightBrowserConfiguration;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import java.util.List;

import com.microsoft.playwright.options.HttpCredentialsSend;

class PlaywrightBrowserConfigParserTest {

    @PlaywrightBrowserConfig(
            browserType = "firefox",
            headless = false,
            slowMo = 250D,
            channel = "chrome-canary",
            baseUrl = "http://localhost:8080",
            startPageUrl = "http://localhost:8080/login",
            defaultTimeout = 5_000L,
            defaultNavigationTimeout = 7_500L,
            tracingEnabled = true)
    private PlaywrightBrowser browser;

    @PlaywrightBrowserConfig(httpCredentials = {
            @HttpCredential(origin = "https://api.example.com", username = "u", password = "p", send = "always"),
            @HttpCredential(username = "fallback", password = "p") })
    private PlaywrightBrowser credentialBrowser;

    @PlaywrightBrowserConfig(httpCredentials = {
            @HttpCredential(username = "u", password = "p", send = "sometimes") })
    private PlaywrightBrowser brokenCredentialBrowser;

    @Test
    void shouldLookupParserByQualifier() {
        assertTrue(AnnotationConfigParser.lookup().containsKey("playwright.browser"));
        assertEquals(PlaywrightBrowserConfigParser.class,
                AnnotationConfigParser.lookup("playwright.browser").orElseThrow().getClass());
    }

    @Test
    void shouldParseAnnotationToEndpointConfiguration() throws Exception {
        PlaywrightBrowserConfig annotation = getClass()
                .getDeclaredField("browser")
                .getAnnotation(PlaywrightBrowserConfig.class);

        TestContext context = TestContextFactory.newInstance().getObject();
        PlaywrightBrowser endpoint =
                new PlaywrightBrowserConfigParser().parse(annotation, new SimpleReferenceResolver(), context);

        PlaywrightBrowserConfiguration configuration = endpoint.getEndpointConfiguration();
        assertEquals("firefox", configuration.getBrowserType());
        assertEquals(Boolean.FALSE, configuration.getHeadless());
        assertEquals(250D, configuration.getSlowMo());
        assertEquals("chrome-canary", configuration.getChannel());
        assertEquals("http://localhost:8080", configuration.getBaseUrl());
        assertEquals("http://localhost:8080/login", configuration.getStartPageUrl());
        assertEquals(5_000L, configuration.getDefaultTimeout());
        assertEquals(7_500L, configuration.getDefaultNavigationTimeout());
        assertTrue(configuration.isTracingEnabled());
    }

    @Test
    void shouldParseCredentialSendMode() throws Exception {
        PlaywrightBrowserConfig annotation = getClass()
                .getDeclaredField("credentialBrowser")
                .getAnnotation(PlaywrightBrowserConfig.class);

        TestContext context = TestContextFactory.newInstance().getObject();
        PlaywrightBrowser endpoint =
                new PlaywrightBrowserConfigParser().parse(annotation, new SimpleReferenceResolver(), context);

        List<com.microsoft.playwright.options.HttpCredentials> credentials =
                endpoint.getEndpointConfiguration().getHttpCredentials();
        assertEquals(2, credentials.size());
        assertEquals(HttpCredentialsSend.ALWAYS, credentials.get(0).send);
        assertEquals("https://api.example.com", credentials.get(0).origin);
        assertNull(credentials.get(1).send);
    }

    @Test
    void shouldRejectUnknownCredentialSendMode() throws Exception {
        PlaywrightBrowserConfig annotation = getClass()
                .getDeclaredField("brokenCredentialBrowser")
                .getAnnotation(PlaywrightBrowserConfig.class);

        TestContext context = TestContextFactory.newInstance().getObject();

        CitrusRuntimeException exception = expectThrows(
                CitrusRuntimeException.class,
                () -> new PlaywrightBrowserConfigParser().parse(annotation, new SimpleReferenceResolver(), context));
        assertTrue(exception.getMessage().contains("sometimes"));
    }
}
