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

package org.citrusframework.selenium.config.annotation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.http.config.annotation.HttpClientConfigParser;
import org.citrusframework.http.config.annotation.HttpServerConfigParser;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.events.WebDriverListener;

import static org.mockito.Mockito.lenient;


/**
 * @since 2.7
 */
@ExtendWith(MockitoExtension.class)
public class SeleniumBrowserConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "browser1")
    @SeleniumBrowserConfig()
    private SeleniumBrowser browser1;

    @CitrusEndpoint
    @SeleniumBrowserConfig(type="firefox",
            version="1.0",
            eventListeners="eventListener",
            javaScript=false,
            webDriver="webDriver",
            firefoxProfile="firefoxProfile",
            startPage="http://citrusframework.org",
            timeout=10000L)
    private SeleniumBrowser browser2;

    @CitrusEndpoint
    @SeleniumBrowserConfig(type="internet explorer",
            remoteServer="http://localhost:9090/selenium")
    private SeleniumBrowser browser3;

    @CitrusEndpoint
    @SeleniumBrowserConfig(type="htmlunit")
    private SeleniumBrowser browser4;

    @CitrusEndpoint
    @SeleniumBrowserConfig(type="chrome",
            headless=true,
            arguments={"--lang=de", "--no-sandbox"},
            windowSize="1280x800",
            pageLoadTimeout=10000L,
            scriptTimeout=5000L)
    private SeleniumBrowser browser5;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private WebDriverListener eventListener;
    @Mock
    private WebDriver webDriver;
    @Mock
    private FirefoxProfile firefoxProfile;

    @BeforeEach
    public void setup() {
        lenient().when(referenceResolver.resolve("webDriver", WebDriver.class)).thenReturn(webDriver);
        lenient().when(referenceResolver.resolve("firefoxProfile", FirefoxProfile.class)).thenReturn(firefoxProfile);
        lenient().when(referenceResolver.resolve("eventListener", WebDriverListener.class)).thenReturn(eventListener);
        lenient().when(referenceResolver.resolve(new String[] { "eventListener" }, WebDriverListener.class)).thenReturn(Collections.singletonList(eventListener));
    }

    @BeforeEach
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void parseBrowserConfig_browserUsingMinimalConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assertions.assertNotNull(browser1);
        Assertions.assertEquals(browser1.getEndpointConfiguration().getBrowserType(), Browser.HTMLUNIT.browserName());
        Assertions.assertNull(browser1.getEndpointConfiguration().getStartPageUrl());
        Assertions.assertTrue(browser1.getEndpointConfiguration().getEventListeners().isEmpty());
        Assertions.assertTrue(browser1.getEndpointConfiguration().isJavaScript());
        Assertions.assertNull(browser1.getEndpointConfiguration().getWebDriver());
        Assertions.assertNotNull(browser1.getEndpointConfiguration().getFirefoxProfile());
        Assertions.assertNull(browser1.getEndpointConfiguration().getRemoteServerUrl());
        Assertions.assertEquals(browser1.getEndpointConfiguration().getTimeout(), 5000L);
        Assertions.assertFalse(browser1.getEndpointConfiguration().isHeadless());
        Assertions.assertTrue(browser1.getEndpointConfiguration().getArguments().isEmpty());
        Assertions.assertNull(browser1.getEndpointConfiguration().getWindowSize());
        Assertions.assertNull(browser1.getEndpointConfiguration().getPageLoadTimeout());
        Assertions.assertNull(browser1.getEndpointConfiguration().getScriptTimeout());
    }

    @Test
    public void parseBrowserConfig_firefoxBrowserUsingFullConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assertions.assertNotNull(browser2);
        Assertions.assertEquals(browser2.getEndpointConfiguration().getBrowserType(), Browser.FIREFOX.browserName());
        Assertions.assertEquals(browser2.getEndpointConfiguration().getStartPageUrl(), "http://citrusframework.org");
        Assertions.assertEquals(browser2.getEndpointConfiguration().getEventListeners().size(), 1L);
        Assertions.assertEquals(browser2.getEndpointConfiguration().getEventListeners().get(0), eventListener);
        Assertions.assertEquals(browser2.getEndpointConfiguration().getWebDriver(), webDriver);
        Assertions.assertEquals(browser2.getEndpointConfiguration().getFirefoxProfile(), firefoxProfile);
        Assertions.assertFalse(browser2.getEndpointConfiguration().isJavaScript());
        Assertions.assertNull(browser2.getEndpointConfiguration().getRemoteServerUrl());
        Assertions.assertEquals(browser2.getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void parseBrowserConfig_remoteBrowserConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assertions.assertNotNull(browser3);
        Assertions.assertEquals(browser3.getEndpointConfiguration().getBrowserType(), Browser.IE.browserName());
        Assertions.assertEquals(browser3.getEndpointConfiguration().getRemoteServerUrl(), "http://localhost:9090/selenium");
    }

    @Test
    public void parseBrowserConfig_htmlUnitBrowserConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assertions.assertNotNull(browser4);
        Assertions.assertEquals(browser4.getEndpointConfiguration().getBrowserType(), Browser.HTMLUNIT.browserName());
    }

    @Test
    public void parseBrowserConfig_browserWithOptions_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assertions.assertNotNull(browser5);
        Assertions.assertEquals(browser5.getEndpointConfiguration().getBrowserType(), Browser.CHROME.browserName());
        Assertions.assertTrue(browser5.getEndpointConfiguration().isHeadless());
        Assertions.assertEquals(browser5.getEndpointConfiguration().getArguments(), Arrays.asList("--lang=de", "--no-sandbox"));
        Assertions.assertEquals(browser5.getEndpointConfiguration().getWindowSize(), "1280x800");
        Assertions.assertEquals(browser5.getEndpointConfiguration().getPageLoadTimeout(), Long.valueOf(10000L));
        Assertions.assertEquals(browser5.getEndpointConfiguration().getScriptTimeout(), Long.valueOf(5000L));
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(validators.size(), 5L);
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assertions.assertNotNull(validators.get("http.client"));
        Assertions.assertEquals(validators.get("http.client").getClass(), HttpClientConfigParser.class);
        Assertions.assertNotNull(validators.get("http.server"));
        Assertions.assertEquals(validators.get("http.server").getClass(), HttpServerConfigParser.class);
        Assertions.assertNotNull(validators.get("selenium.browser"));
        Assertions.assertEquals(validators.get("selenium.browser").getClass(), SeleniumBrowserConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("selenium.browser").isPresent());
    }
}
