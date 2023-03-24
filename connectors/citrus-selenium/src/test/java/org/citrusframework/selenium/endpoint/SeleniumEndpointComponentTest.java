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

package org.citrusframework.selenium.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.http.client.HttpEndpointComponent;
import org.citrusframework.http.client.HttpsEndpointComponent;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
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

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private WebDriver chromeDriver = Mockito.mock(WebDriver.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateBrowserEndpoint() throws Exception {
        SeleniumEndpointComponent component = new SeleniumEndpointComponent();

        Endpoint endpoint = component.createEndpoint("selenium:browser", context);

        Assert.assertEquals(endpoint.getClass(), SeleniumBrowser.class);

        Assert.assertEquals(((SeleniumBrowser)endpoint).getEndpointConfiguration().getBrowserType(), Browser.HTMLUNIT.browserName());

        endpoint = component.createEndpoint("selenium:firefox", context);

        Assert.assertEquals(endpoint.getClass(), SeleniumBrowser.class);

        Assert.assertEquals(((SeleniumBrowser)endpoint).getEndpointConfiguration().getBrowserType(), Browser.FIREFOX.browserName());
        Assert.assertEquals(((SeleniumBrowser) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateBrowserEndpointWithParameters() throws Exception {
        SeleniumEndpointComponent component = new SeleniumEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("chromeDriver")).thenReturn(true);
        when(referenceResolver.resolve("chromeDriver", WebDriver.class)).thenReturn(chromeDriver);
        Endpoint endpoint = component.createEndpoint("selenium:chrome?start-page=https://localhost:8080&remote-server=https://localhost:8081&webDriver=chromeDriver&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), SeleniumBrowser.class);

        Assert.assertEquals(((SeleniumBrowser)endpoint).getEndpointConfiguration().getWebDriver(), chromeDriver);
        Assert.assertEquals(((SeleniumBrowser)endpoint).getEndpointConfiguration().getStartPageUrl(), "https://localhost:8080");
        Assert.assertEquals(((SeleniumBrowser)endpoint).getEndpointConfiguration().getRemoteServerUrl(), "https://localhost:8081");
        Assert.assertEquals(((SeleniumBrowser) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("http"));
        Assert.assertEquals(validators.get("http").getClass(), HttpEndpointComponent.class);
        Assert.assertNotNull(validators.get("https"));
        Assert.assertEquals(validators.get("https").getClass(), HttpsEndpointComponent.class);
        Assert.assertNotNull(validators.get("selenium"));
        Assert.assertEquals(validators.get("selenium").getClass(), SeleniumEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("selenium").isPresent());
    }

}
