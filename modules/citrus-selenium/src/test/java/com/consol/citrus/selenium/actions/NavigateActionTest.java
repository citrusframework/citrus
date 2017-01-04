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

package com.consol.citrus.selenium.actions;

import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumBrowserConfiguration;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class NavigateActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = Mockito.mock(SeleniumBrowser.class);
    private SeleniumBrowserConfiguration seleniumBrowserConfiguration = Mockito.mock(SeleniumBrowserConfiguration.class);
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    private WebDriver.Navigation navigation = Mockito.mock(WebDriver.Navigation.class);

    private NavigateAction action;

    @BeforeMethod
    public void setup() {
        reset(seleniumBrowser, seleniumBrowserConfiguration, webDriver, navigation);

        action =  new NavigateAction();
        action.setBrowser(seleniumBrowser);

        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);
        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(seleniumBrowserConfiguration);
        when(webDriver.navigate()).thenReturn(navigation);
    }

    @Test
    public void testNavigatePageUrl() throws Exception {
        when(seleniumBrowserConfiguration.getBrowserType()).thenReturn(BrowserType.CHROME);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(invocation.getArguments()[0].toString(), "http://localhost:8080");
                return null;
            }
        }).when(navigation).to(any(URL.class));

        action.setPage("http://localhost:8080");
        action.execute(seleniumBrowser, context);

        verify(navigation).to(any(URL.class));
    }

    @Test
    public void testNavigatePageUrlInternetExplorer() throws Exception {
        when(seleniumBrowserConfiguration.getBrowserType()).thenReturn(BrowserType.IE);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertTrue(invocation.getArguments()[0].toString().startsWith("http://localhost:8080?timestamp="));
                return null;
            }
        }).when(navigation).to(any(URL.class));

        action.setPage("http://localhost:8080");
        action.execute(seleniumBrowser, context);

        verify(navigation).to(any(URL.class));
    }

    @Test
    public void testNavigateRelativePageUrl() throws Exception {
        when(seleniumBrowserConfiguration.getBrowserType()).thenReturn(BrowserType.CHROME);
        when(seleniumBrowserConfiguration.getStartPageUrl()).thenReturn("http://localhost:8080");

        action.setPage("info");
        action.execute(seleniumBrowser, context);

        verify(navigation).to("http://localhost:8080/info");
    }

    @Test
    public void testExecuteBack() throws Exception {
        action.setPage("back");
        action.execute(seleniumBrowser, context);

        verify(navigation).back();
    }

    @Test
    public void testExecuteForward() throws Exception {
        action.setPage("forward");
        action.execute(seleniumBrowser, context);

        verify(navigation).forward();
    }

    @Test
    public void testExecuteRefresh() throws Exception {
        action.setPage("refresh");
        action.execute(seleniumBrowser, context);

        verify(navigation).refresh();
    }

}