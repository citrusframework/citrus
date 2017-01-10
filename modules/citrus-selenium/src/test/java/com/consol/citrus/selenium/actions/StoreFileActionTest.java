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
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class StoreFileActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private WebDriver webDriver = Mockito.mock(WebDriver.class);

    private StoreFileAction action;

    @BeforeMethod
    public void setup() {
        reset(webDriver);

        seleniumBrowser.setWebDriver(webDriver);

        action =  new StoreFileAction();
        action.setBrowser(seleniumBrowser);
    }

    @Test
    public void testExecute() throws Exception {
        action.setFilePath("classpath:download/file.txt");
        action.execute(context);

        Assert.assertNotNull(seleniumBrowser.getStoredFile("file.txt"));
    }

    @Test
    public void testExecuteVariableSupport() throws Exception {
        context.setVariable("file", "classpath:download/file.xml");

        action.setFilePath("${file}");
        action.execute(context);

        Assert.assertNotNull(seleniumBrowser.getStoredFile("file.xml"));
    }

}