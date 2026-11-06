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

package org.citrusframework.selenium.config.xml;

import java.util.Arrays;
import java.util.Map;

import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.Browser;


/**
 * @since 2.7
 */
public class SeleniumBrowserParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void parseBrowserConfig_multipleBrowsersConfigured_shouldDetectAllBrowsers() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        Assertions.assertEquals(browsers.size(), 5);
    }

    @Test
    public void parseBrowserConfig_htmlUnitBrowserConfigured_shouldParseConfigurationSuccessfully() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        SeleniumBrowser browser = browsers.get("defaultBrowser");
        Assertions.assertEquals(browser.getEndpointConfiguration().getBrowserType(), Browser.HTMLUNIT.browserName());
        Assertions.assertNull(browser.getEndpointConfiguration().getStartPageUrl());
        Assertions.assertTrue(browser.getEndpointConfiguration().getEventListeners().isEmpty());
        Assertions.assertEquals(browser.getEndpointConfiguration().isJavaScript(), true);
        Assertions.assertNull(browser.getEndpointConfiguration().getWebDriver());
        Assertions.assertNotNull(browser.getEndpointConfiguration().getFirefoxProfile());
        Assertions.assertNull(browser.getEndpointConfiguration().getRemoteServerUrl());
        Assertions.assertEquals(browser.getEndpointConfiguration().getTimeout(), 5000L);
        Assertions.assertFalse(browser.getEndpointConfiguration().isHeadless());
        Assertions.assertTrue(browser.getEndpointConfiguration().getArguments().isEmpty());
        Assertions.assertNull(browser.getEndpointConfiguration().getWindowSize());
        Assertions.assertNull(browser.getEndpointConfiguration().getPageLoadTimeout());
        Assertions.assertNull(browser.getEndpointConfiguration().getScriptTimeout());
    }

    @Test
    public void parseBrowserConfig_firefoxBrowserConfigured_shouldParseConfigurationSuccessfully() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        SeleniumBrowser browser = browsers.get("firefoxBrowser");
        Assertions.assertEquals(browser.getEndpointConfiguration().getBrowserType(), Browser.FIREFOX.browserName());
        Assertions.assertEquals(browser.getEndpointConfiguration().getStartPageUrl(), "http://citrusframework.org");
        Assertions.assertEquals(browser.getEndpointConfiguration().getEventListeners().size(), 1L);
        Assertions.assertEquals(browser.getEndpointConfiguration().getEventListeners().get(0), beanDefinitionContext.getBean("eventListener"));
        Assertions.assertEquals(browser.getEndpointConfiguration().getWebDriver(), beanDefinitionContext.getBean("webDriver"));
        Assertions.assertEquals(browser.getEndpointConfiguration().getFirefoxProfile(), beanDefinitionContext.getBean("firefoxProfile"));
        Assertions.assertEquals(browser.getEndpointConfiguration().isJavaScript(), false);
        Assertions.assertNull(browser.getEndpointConfiguration().getRemoteServerUrl());
        Assertions.assertEquals(browser.getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void parseBrowserConfig_remoteBrowserConfigured_shouldParseConfigurationSuccessfully() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        SeleniumBrowser browser = browsers.get("remoteBrowser");
        Assertions.assertEquals(browser.getEndpointConfiguration().getBrowserType(), Browser.IE.browserName());
        Assertions.assertEquals(browser.getEndpointConfiguration().getRemoteServerUrl(), "http://localhost:9090/selenium");
    }

    @Test
    public void parseBrowserConfig_browserUsingDeprecatedConfig_shouldParseConfigurationSuccessfully() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        SeleniumBrowser browser = browsers.get("htmlUnitBrowser");
        Assertions.assertEquals(browser.getEndpointConfiguration().getBrowserType(), Browser.HTMLUNIT.browserName());
    }

    @Test
    public void parseBrowserConfig_browserWithOptionsConfigured_shouldParseConfigurationSuccessfully() {
        Map<String, SeleniumBrowser> browsers = beanDefinitionContext.getBeansOfType(SeleniumBrowser.class);

        SeleniumBrowser browser = browsers.get("optionsBrowser");
        Assertions.assertEquals(browser.getEndpointConfiguration().getBrowserType(), Browser.CHROME.browserName());
        Assertions.assertTrue(browser.getEndpointConfiguration().isHeadless());
        Assertions.assertEquals(browser.getEndpointConfiguration().getArguments(), Arrays.asList("--lang=de", "--no-sandbox"));
        Assertions.assertEquals(browser.getEndpointConfiguration().getWindowSize(), "1280x800");
        Assertions.assertEquals(browser.getEndpointConfiguration().getPageLoadTimeout(), Long.valueOf(10000L));
        Assertions.assertEquals(browser.getEndpointConfiguration().getScriptTimeout(), Long.valueOf(5000L));
    }
}
