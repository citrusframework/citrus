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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class StopBrowserActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private WebDriver webDriver = Mockito.mock(WebDriver.class);

    private StopBrowserAction action;

    @BeforeMethod
    public void setup() {
        reset(webDriver);

        seleniumBrowser.setWebDriver(webDriver);
        seleniumBrowser.getEndpointConfiguration().setBrowserType(BrowserType.CHROME);

        action =  new StopBrowserAction();
        action.setBrowser(seleniumBrowser);
    }

    @Test
    public void testStop() throws Exception {
        context.setVariable(SeleniumHeaders.SELENIUM_BROWSER, "ChromeBrowser");

        action.execute(context);

        Assert.assertFalse(context.getVariables().containsKey(SeleniumHeaders.SELENIUM_BROWSER));
        Assert.assertNull(seleniumBrowser.getWebDriver());

        verify(webDriver).quit();
    }
}