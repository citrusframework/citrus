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

import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.http.config.annotation.HttpClientConfigParser;
import org.citrusframework.http.config.annotation.HttpServerConfigParser;
import org.citrusframework.http.config.annotation.VertxHttpServerConfigParser;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.events.WebDriverListener;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @since 2.7
 */
public class SeleniumBrowserConfigParserTest extends AbstractTestNGUnitTest {

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

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("webDriver", WebDriver.class)).thenReturn(webDriver);
        when(referenceResolver.resolve("firefoxProfile", FirefoxProfile.class)).thenReturn(firefoxProfile);
        when(referenceResolver.resolve("eventListener", WebDriverListener.class)).thenReturn(eventListener);
        when(referenceResolver.resolve(new String[] { "eventListener" }, WebDriverListener.class)).thenReturn(Collections.singletonList(eventListener));
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void parseBrowserConfig_browserUsingMinimalConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assert.assertNotNull(browser1);
        Assert.assertEquals(browser1.getEndpointConfiguration().getBrowserType(), Browser.HTMLUNIT.browserName());
        Assert.assertNull(browser1.getEndpointConfiguration().getStartPageUrl());
        Assert.assertTrue(browser1.getEndpointConfiguration().getEventListeners().isEmpty());
        Assert.assertTrue(browser1.getEndpointConfiguration().isJavaScript());
        Assert.assertNull(browser1.getEndpointConfiguration().getWebDriver());
        Assert.assertNotNull(browser1.getEndpointConfiguration().getFirefoxProfile());
        Assert.assertNull(browser1.getEndpointConfiguration().getRemoteServerUrl());
        Assert.assertEquals(browser1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertFalse(browser1.getEndpointConfiguration().isHeadless());
        Assert.assertTrue(browser1.getEndpointConfiguration().getArguments().isEmpty());
        Assert.assertNull(browser1.getEndpointConfiguration().getWindowSize());
        Assert.assertNull(browser1.getEndpointConfiguration().getPageLoadTimeout());
        Assert.assertNull(browser1.getEndpointConfiguration().getScriptTimeout());
    }

    @Test
    public void parseBrowserConfig_firefoxBrowserUsingFullConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assert.assertNotNull(browser2);
        Assert.assertEquals(browser2.getEndpointConfiguration().getBrowserType(), Browser.FIREFOX.browserName());
        Assert.assertEquals(browser2.getEndpointConfiguration().getStartPageUrl(), "http://citrusframework.org");
        Assert.assertEquals(browser2.getEndpointConfiguration().getEventListeners().size(), 1L);
        Assert.assertEquals(browser2.getEndpointConfiguration().getEventListeners().get(0), eventListener);
        Assert.assertEquals(browser2.getEndpointConfiguration().getWebDriver(), webDriver);
        Assert.assertEquals(browser2.getEndpointConfiguration().getFirefoxProfile(), firefoxProfile);
        Assert.assertFalse(browser2.getEndpointConfiguration().isJavaScript());
        Assert.assertNull(browser2.getEndpointConfiguration().getRemoteServerUrl());
        Assert.assertEquals(browser2.getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void parseBrowserConfig_remoteBrowserConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assert.assertNotNull(browser3);
        Assert.assertEquals(browser3.getEndpointConfiguration().getBrowserType(), Browser.IE.browserName());
        Assert.assertEquals(browser3.getEndpointConfiguration().getRemoteServerUrl(), "http://localhost:9090/selenium");
    }

    @Test
    public void parseBrowserConfig_htmlUnitBrowserConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assert.assertNotNull(browser4);
        Assert.assertEquals(browser4.getEndpointConfiguration().getBrowserType(), Browser.HTMLUNIT.browserName());
    }

    @Test
    public void parseBrowserConfig_browserWithOptions_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assert.assertNotNull(browser5);
        Assert.assertEquals(browser5.getEndpointConfiguration().getBrowserType(), Browser.CHROME.browserName());
        Assert.assertTrue(browser5.getEndpointConfiguration().isHeadless());
        Assert.assertEquals(browser5.getEndpointConfiguration().getArguments(), Arrays.asList("--lang=de", "--no-sandbox"));
        Assert.assertEquals(browser5.getEndpointConfiguration().getWindowSize(), "1280x800");
        Assert.assertEquals(browser5.getEndpointConfiguration().getPageLoadTimeout(), Long.valueOf(10000L));
        Assert.assertEquals(browser5.getEndpointConfiguration().getScriptTimeout(), Long.valueOf(5000L));
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 6L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("http.client"));
        Assert.assertEquals(validators.get("http.client").getClass(), HttpClientConfigParser.class);
        Assert.assertNotNull(validators.get("http.server"));
        Assert.assertEquals(validators.get("http.server").getClass(), HttpServerConfigParser.class);
        Assert.assertNotNull(validators.get("selenium.browser"));
        Assert.assertEquals(validators.get("selenium.browser").getClass(), SeleniumBrowserConfigParser.class);
        Assert.assertNotNull(validators.get("http.vertx-server"));
        Assert.assertEquals(validators.get("http.vertx-server").getClass(), VertxHttpServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("selenium.browser").isPresent());
    }
}
