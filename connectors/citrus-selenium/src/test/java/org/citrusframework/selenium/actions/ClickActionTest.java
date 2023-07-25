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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class ClickActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebElement element = Mockito.mock(WebElement.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, element);

        seleniumBrowser.setWebDriver(webDriver);

        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("button");
    }

    @Test
    public void testExecute() throws Exception {
        when(webDriver.findElement(any(By.class))).thenAnswer(new Answer<WebElement>() {
            @Override
            public WebElement answer(InvocationOnMock invocation) throws Throwable {
                By select = (By) invocation.getArguments()[0];

                Assert.assertEquals(select.getClass(), By.ById.class);
                Assert.assertEquals(select.toString(), "By.id: myButton");
                return element;
            }
        });

        ClickAction action =  new ClickAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "myButton")
                .build();
        action.execute(context);

        verify(element).click();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to find element 'By.id: myButton' on page")
    public void testElementNotFound() {
        when(webDriver.findElement(any(By.class))).thenReturn(null);

        ClickAction action =  new ClickAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "myButton")
                .build();
        action.execute(context);
    }

}
