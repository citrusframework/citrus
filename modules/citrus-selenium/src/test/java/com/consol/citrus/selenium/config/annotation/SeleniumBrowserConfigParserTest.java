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

package com.consol.citrus.selenium.config.annotation;

import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
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
    @SeleniumBrowserConfig(browserType="htmlunit")
    private SeleniumBrowser browserWithDeprecatedConfig;

    @Mock
    private WebDriverEventListener eventListener = Mockito.mock(WebDriverEventListener.class);
    @Mock
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    @Mock
    private FirefoxProfile firefoxProfile = Mockito.mock(FirefoxProfile.class);
    @Autowired
    private SpringBeanReferenceResolver referenceResolver;
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("webDriver", WebDriver.class)).thenReturn(webDriver);
        when(applicationContext.getBean("firefoxProfile", FirefoxProfile.class)).thenReturn(firefoxProfile);
        when(applicationContext.getBean("eventListener", WebDriverEventListener.class)).thenReturn(eventListener);
    }

    @Test
    public void parseBrowserConfig_browserUsingMinimalConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assert.assertNotNull(browser1);
        Assert.assertEquals(browser1.getEndpointConfiguration().getBrowserType(), BrowserType.HTMLUNIT);
        Assert.assertNull(browser1.getEndpointConfiguration().getStartPageUrl());
        Assert.assertTrue(browser1.getEndpointConfiguration().getEventListeners().isEmpty());
        Assert.assertEquals(browser1.getEndpointConfiguration().isJavaScript(), true);
        Assert.assertNull(browser1.getEndpointConfiguration().getWebDriver());
        Assert.assertNotNull(browser1.getEndpointConfiguration().getFirefoxProfile());
        Assert.assertNull(browser1.getEndpointConfiguration().getRemoteServerUrl());
        Assert.assertEquals(browser1.getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void parseBrowserConfig_firefoxBrowserUsingFullConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assert.assertNotNull(browser2);
        Assert.assertEquals(browser2.getEndpointConfiguration().getBrowserType(), BrowserType.FIREFOX);
        Assert.assertEquals(browser2.getEndpointConfiguration().getStartPageUrl(), "http://citrusframework.org");
        Assert.assertEquals(browser2.getEndpointConfiguration().getEventListeners().size(), 1L);
        Assert.assertEquals(browser2.getEndpointConfiguration().getEventListeners().get(0), eventListener);
        Assert.assertEquals(browser2.getEndpointConfiguration().getWebDriver(), webDriver);
        Assert.assertEquals(browser2.getEndpointConfiguration().getFirefoxProfile(), firefoxProfile);
        Assert.assertEquals(browser2.getEndpointConfiguration().isJavaScript(), false);
        Assert.assertNull(browser2.getEndpointConfiguration().getRemoteServerUrl());
        Assert.assertEquals(browser2.getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void parseBrowserConfig_remoteBrowserConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assert.assertNotNull(browser3);
        Assert.assertEquals(browser3.getEndpointConfiguration().getBrowserType(), BrowserType.IE);
        Assert.assertEquals(browser3.getEndpointConfiguration().getRemoteServerUrl(), "http://localhost:9090/selenium");
    }

    @Test
    public void parseBrowserConfig_browserUsingDeprecatedConfig_shouldParseConfigurationSuccessfully() {
        CitrusAnnotations.injectEndpoints(this, context);

        Assert.assertNotNull(browserWithDeprecatedConfig);
        Assert.assertEquals(browserWithDeprecatedConfig.getEndpointConfiguration().getBrowserType(), BrowserType.HTMLUNIT);
  }

}