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

package com.consol.citrus.selenium.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumEndpointComponentTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private WebDriver chromeDriver = Mockito.mock(WebDriver.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateBrowserEndpoint() throws Exception {
        SeleniumEndpointComponent component = new SeleniumEndpointComponent();

        Endpoint endpoint = component.createEndpoint("selenium:browser", context);

        Assert.assertEquals(endpoint.getClass(), SeleniumBrowser.class);

        Assert.assertEquals(((SeleniumBrowser)endpoint).getEndpointConfiguration().getBrowserType(), BrowserType.HTMLUNIT);

        endpoint = component.createEndpoint("selenium:firefox", context);

        Assert.assertEquals(endpoint.getClass(), SeleniumBrowser.class);

        Assert.assertEquals(((SeleniumBrowser)endpoint).getEndpointConfiguration().getBrowserType(), BrowserType.FIREFOX);
        Assert.assertEquals(((SeleniumBrowser) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateBrowserEndpointWithParameters() throws Exception {
        SeleniumEndpointComponent component = new SeleniumEndpointComponent();

        reset(applicationContext);
        when(applicationContext.containsBean("chromeDriver")).thenReturn(true);
        when(applicationContext.getBean("chromeDriver")).thenReturn(chromeDriver);
        Endpoint endpoint = component.createEndpoint("selenium:chrome?start-page=https://localhost:8080&remote-server=https://localhost:8081&webDriver=chromeDriver&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), SeleniumBrowser.class);

        Assert.assertEquals(((SeleniumBrowser)endpoint).getEndpointConfiguration().getWebDriver(), chromeDriver);
        Assert.assertEquals(((SeleniumBrowser)endpoint).getEndpointConfiguration().getStartPageUrl(), "https://localhost:8080");
        Assert.assertEquals(((SeleniumBrowser)endpoint).getEndpointConfiguration().getRemoteServerUrl(), "https://localhost:8081");
        Assert.assertEquals(((SeleniumBrowser) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

}