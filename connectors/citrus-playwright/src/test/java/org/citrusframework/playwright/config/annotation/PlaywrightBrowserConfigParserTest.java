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
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightBrowserConfiguration;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
}
