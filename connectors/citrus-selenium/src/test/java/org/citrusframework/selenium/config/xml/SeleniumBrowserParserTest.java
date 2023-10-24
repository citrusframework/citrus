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

package org.citrusframework.selenium.config.xml;

import java.util.Map;

import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.openqa.selenium.remote.Browser;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumBrowserParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void parseBrowserConfig_multipleBrowsersConfigured_shouldDetectAllBrowsers() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        Assert.assertEquals(browsers.size(), 4);
    }

    @Test
    public void parseBrowserConfig_htmlUnitBrowserConfigured_shouldParseConfigurationSuccessfully() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        SeleniumBrowser browser = browsers.get("defaultBrowser");
        Assert.assertEquals(browser.getEndpointConfiguration().getBrowserType(), Browser.HTMLUNIT.browserName());
        Assert.assertNull(browser.getEndpointConfiguration().getStartPageUrl());
        Assert.assertTrue(browser.getEndpointConfiguration().getEventListeners().isEmpty());
        Assert.assertEquals(browser.getEndpointConfiguration().isJavaScript(), true);
        Assert.assertNull(browser.getEndpointConfiguration().getWebDriver());
        Assert.assertNotNull(browser.getEndpointConfiguration().getFirefoxProfile());
        Assert.assertNull(browser.getEndpointConfiguration().getRemoteServerUrl());
        Assert.assertEquals(browser.getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void parseBrowserConfig_firefoxBrowserConfigured_shouldParseConfigurationSuccessfully() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        SeleniumBrowser browser = browsers.get("firefoxBrowser");
        Assert.assertEquals(browser.getEndpointConfiguration().getBrowserType(), Browser.FIREFOX.browserName());
        Assert.assertEquals(browser.getEndpointConfiguration().getStartPageUrl(), "http://citrusframework.org");
        Assert.assertEquals(browser.getEndpointConfiguration().getEventListeners().size(), 1L);
        Assert.assertEquals(browser.getEndpointConfiguration().getEventListeners().get(0), beanDefinitionContext.getBean("eventListener"));
        Assert.assertEquals(browser.getEndpointConfiguration().getWebDriver(), beanDefinitionContext.getBean("webDriver"));
        Assert.assertEquals(browser.getEndpointConfiguration().getFirefoxProfile(), beanDefinitionContext.getBean("firefoxProfile"));
        Assert.assertEquals(browser.getEndpointConfiguration().isJavaScript(), false);
        Assert.assertNull(browser.getEndpointConfiguration().getRemoteServerUrl());
        Assert.assertEquals(browser.getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void parseBrowserConfig_remoteBrowserConfigured_shouldParseConfigurationSuccessfully() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        SeleniumBrowser browser = browsers.get("remoteBrowser");
        Assert.assertEquals(browser.getEndpointConfiguration().getBrowserType(), Browser.IE.browserName());
        Assert.assertEquals(browser.getEndpointConfiguration().getRemoteServerUrl(), "http://localhost:9090/selenium");
    }

    @Test
    public void parseBrowserConfig_browserUsingDeprecatedConfig_shouldParseConfigurationSuccessfully() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        SeleniumBrowser browser = browsers.get("htmlUnitBrowser");
        Assert.assertEquals(browser.getEndpointConfiguration().getBrowserType(), Browser.HTMLUNIT.browserName());
    }
}
