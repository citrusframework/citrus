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

    private SeleniumBrowser seleniumBrowser;
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    private WebDriver.Navigation navigation = Mockito.mock(WebDriver.Navigation.class);

    private NavigateAction action;

    @BeforeMethod
    public void setup() {
        reset(webDriver, navigation);

        seleniumBrowser = new SeleniumBrowser();
        seleniumBrowser.setWebDriver(webDriver);

        action =  new NavigateAction();
        action.setBrowser(seleniumBrowser);

        when(webDriver.navigate()).thenReturn(navigation);
    }

    @Test
    public void testNavigatePageUrl() throws Exception {
        seleniumBrowser.getEndpointConfiguration().setBrowserType(BrowserType.CHROME);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(invocation.getArguments()[0].toString(), "http://localhost:8080");
                return null;
            }
        }).when(navigation).to(any(URL.class));

        action.setPage("http://localhost:8080");
        action.execute(context);

        verify(navigation).to(any(URL.class));
    }

    @Test
    public void testNavigatePageUrlInternetExplorer() throws Exception {
        seleniumBrowser.getEndpointConfiguration().setBrowserType(BrowserType.IE);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertTrue(invocation.getArguments()[0].toString().startsWith("http://localhost:8080?timestamp="));
                return null;
            }
        }).when(navigation).to(any(URL.class));

        action.setPage("http://localhost:8080");
        action.execute(context);

        verify(navigation).to(any(URL.class));
    }

    @Test
    public void testNavigateRelativePageUrl() throws Exception {
        seleniumBrowser.getEndpointConfiguration().setBrowserType(BrowserType.IE);
        seleniumBrowser.getEndpointConfiguration().setStartPageUrl("http://localhost:8080");

        action.setPage("info");
        action.execute(context);

        verify(navigation).to("http://localhost:8080/info");
    }

    @Test
    public void testExecuteBack() throws Exception {
        action.setPage("back");
        action.execute(context);

        verify(navigation).back();
    }

    @Test
    public void testExecuteForward() throws Exception {
        action.setPage("forward");
        action.execute(context);

        verify(navigation).forward();
    }

    @Test
    public void testExecuteRefresh() throws Exception {
        action.setPage("refresh");
        action.execute(context);

        verify(navigation).refresh();
    }

}