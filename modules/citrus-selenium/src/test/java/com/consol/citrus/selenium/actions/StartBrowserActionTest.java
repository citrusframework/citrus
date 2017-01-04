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

import com.consol.citrus.selenium.endpoint.*;
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
public class StartBrowserActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = Mockito.mock(SeleniumBrowser.class);
    private SeleniumBrowserConfiguration seleniumBrowserConfiguration = Mockito.mock(SeleniumBrowserConfiguration.class);
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    private WebDriver.Navigation navigation = Mockito.mock(WebDriver.Navigation.class);

    private StartBrowserAction action;

    @BeforeMethod
    public void setup() {
        reset(seleniumBrowser, seleniumBrowserConfiguration, webDriver, navigation);

        action =  new StartBrowserAction();
        action.setBrowser(seleniumBrowser);

        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);
        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(seleniumBrowserConfiguration);
        when(seleniumBrowser.getName()).thenReturn("ChromeBrowser");
        when(seleniumBrowserConfiguration.getBrowserType()).thenReturn(BrowserType.CHROME);
        when(webDriver.navigate()).thenReturn(navigation);
    }

    @Test
    public void testStart() throws Exception {
        when(seleniumBrowser.isStarted()).thenReturn(false);

        action.execute(seleniumBrowser, context);

        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_BROWSER), "ChromeBrowser");

        verify(seleniumBrowser).start();
    }

    @Test
    public void testStartWithStartPage() throws Exception {
        when(seleniumBrowser.isStarted()).thenReturn(false);
        when(seleniumBrowserConfiguration.getStartPageUrl()).thenReturn("http://localhost:8080");

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(invocation.getArguments()[0].toString(), "http://localhost:8080");
                return null;
            }
        }).when(navigation).to(any(URL.class));

        action.execute(seleniumBrowser, context);

        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_BROWSER), "ChromeBrowser");

        verify(seleniumBrowser).start();
        verify(navigation).to(any(URL.class));
    }

    @Test
    public void testStartAlreadyStarted() throws Exception {
        when(seleniumBrowser.isStarted()).thenReturn(true);

        action.execute(seleniumBrowser, context);

        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_BROWSER), "ChromeBrowser");

        verify(seleniumBrowser, times(0)).stop();
        verify(seleniumBrowser, times(0)).start();
    }

    @Test
    public void testStartAlreadyStartedNotAllowed() throws Exception {
        when(seleniumBrowser.isStarted()).thenReturn(true);

        action.setAllowAlreadyStarted(false);
        action.execute(seleniumBrowser, context);

        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_BROWSER), "ChromeBrowser");

        verify(seleniumBrowser).stop();
        verify(seleniumBrowser).start();
    }

}