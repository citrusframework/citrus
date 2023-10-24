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

package org.citrusframework.selenium.actions;

import java.io.File;

import org.citrusframework.CitrusSettings;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumHeaders;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class MakeScreenshotActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver);

        seleniumBrowser.setWebDriver(webDriver);
    }

    @Test
    public void testExecute() throws Exception {
        when(webDriver.getScreenshotAs(OutputType.FILE)).thenReturn(Resources.fromClasspath("screenshot.png").getFile());

        MakeScreenshotAction action =  new MakeScreenshotAction.Builder()
                .browser(seleniumBrowser)
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_SCREENSHOT), "Test_screenshot.png");

        Assert.assertNotNull(seleniumBrowser.getStoredFile("screenshot.png"));
    }

    @Test
    public void testExecuteOutputDir() throws Exception {
        when(webDriver.getScreenshotAs(OutputType.FILE)).thenReturn(Resources.fromClasspath("screenshot.png").getFile());

        context.setVariable(CitrusSettings.TEST_NAME_VARIABLE, "MyTest");

        MakeScreenshotAction action =  new MakeScreenshotAction.Builder()
                .browser(seleniumBrowser)
                .outputDir("target")
                .build();
        action.execute(context);

        File stored = new File("target/MyTest_screenshot.png");
        Assert.assertTrue(stored.exists());
    }

}
