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

import com.consol.citrus.Citrus;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class MakeScreenshotActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = Mockito.mock(SeleniumBrowser.class);
    private ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);

    private File screenshot = Mockito.mock(File.class);

    private MakeScreenshotAction action;

    @BeforeMethod
    public void setup() {
        reset(seleniumBrowser, webDriver);

        action =  new MakeScreenshotAction();
        action.setBrowser(seleniumBrowser);

        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);
        when(screenshot.getName()).thenReturn("screenshot123.png");
    }

    @Test
    public void testExecute() throws Exception {
        when(webDriver.getScreenshotAs(OutputType.FILE)).thenReturn(screenshot);

        action.execute(seleniumBrowser, context);

        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_SCREENSHOT), "Test_screenshot123.png");
        verify(seleniumBrowser).storeFile(screenshot);
    }

    @Test
    public void testExecuteOutputDir() throws Exception {
        when(webDriver.getScreenshotAs(OutputType.FILE)).thenReturn(new ClassPathResource("screenshot.png").getFile());

        context.setVariable(Citrus.TEST_NAME_VARIABLE, "MyTest");

        action.setOutputDir("target");
        action.execute(seleniumBrowser, context);
    }

}