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

import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriver;

import static org.mockito.Mockito.reset;


/**
 * @since 2.7
 */
public class StoreFileActionTest extends AbstractUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private WebDriver webDriver = Mockito.mock(WebDriver.class);

    @BeforeEach
    public void setup() {
        reset(webDriver);

        seleniumBrowser.setWebDriver(webDriver);
    }

    @Test
    public void testExecute() {
        StoreFileAction action =  new StoreFileAction.Builder()
                .browser(seleniumBrowser)
                .filePath("classpath:download/file.txt")
                .build();
        action.execute(context);

        Assertions.assertNotNull(seleniumBrowser.getStoredFile("file.txt"));
    }

    @Test
    public void testExecuteVariableSupport() {
        context.setVariable("file", "classpath:download/file.xml");

        StoreFileAction action =  new StoreFileAction.Builder()
                .browser(seleniumBrowser)
                .filePath("${file}")
                .build();
        action.execute(context);

        Assertions.assertNotNull(seleniumBrowser.getStoredFile("file.xml"));
    }
}
