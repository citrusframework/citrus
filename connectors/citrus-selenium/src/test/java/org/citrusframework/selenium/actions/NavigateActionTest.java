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

package org.citrusframework.selenium.actions;

import java.net.URL;

import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @since 2.7
 */
public class NavigateActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser;
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    private WebDriver.Navigation navigation = Mockito.mock(WebDriver.Navigation.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, navigation);

        seleniumBrowser = new SeleniumBrowser();
        seleniumBrowser.setWebDriver(webDriver);

        when(webDriver.navigate()).thenReturn(navigation);
    }

    @Test
    public void testNavigatePageUrl() {
        seleniumBrowser.getEndpointConfiguration().setBrowserType(Browser.CHROME.browserName());
        doAnswer((Answer<Object>) invocation -> {
            Assert.assertEquals(invocation.getArguments()[0].toString(), "http://localhost:8080");
            return null;
        }).when(navigation).to(any(URL.class));

        NavigateAction action =  new NavigateAction.Builder()
                .browser(seleniumBrowser)
                .page("http://localhost:8080")
                .build();
        action.execute(context);

        verify(navigation).to(any(URL.class));
    }

    @Test
    public void testNavigatePageUrlInternetExplorer() {
        seleniumBrowser.getEndpointConfiguration().setBrowserType(Browser.IE.browserName());
        doAnswer((Answer<Object>) invocation -> {
            Assert.assertTrue(invocation.getArguments()[0].toString().startsWith("http://localhost:8080?timestamp="));
            return null;
        }).when(navigation).to(any(URL.class));

        NavigateAction action =  new NavigateAction.Builder()
                .browser(seleniumBrowser)
                .page("http://localhost:8080")
                .build();
        action.execute(context);

        verify(navigation).to(any(URL.class));
    }

    @Test
    public void testNavigateRelativePageUrl(){
        seleniumBrowser.getEndpointConfiguration().setBrowserType(Browser.IE.browserName());
        seleniumBrowser.getEndpointConfiguration().setStartPageUrl("http://localhost:8080");

        NavigateAction action =  new NavigateAction.Builder()
                .browser(seleniumBrowser)
                .page("info")
                .build();
        action.execute(context);

        verify(navigation).to("http://localhost:8080/info");
    }

    @Test
    public void testExecuteBack(){
        NavigateAction action =  new NavigateAction.Builder()
                .browser(seleniumBrowser)
                .page("back")
                .build();
        action.execute(context);

        verify(navigation).back();
    }

    @Test
    public void testExecuteForward(){
        NavigateAction action =  new NavigateAction.Builder()
                .browser(seleniumBrowser)
                .page("forward")
                .build();
        action.execute(context);

        verify(navigation).forward();
    }

    @Test
    public void testExecuteRefresh(){
        NavigateAction action =  new NavigateAction.Builder()
                .browser(seleniumBrowser)
                .page("refresh")
                .build();
        action.execute(context);

        verify(navigation).refresh();
    }
}
