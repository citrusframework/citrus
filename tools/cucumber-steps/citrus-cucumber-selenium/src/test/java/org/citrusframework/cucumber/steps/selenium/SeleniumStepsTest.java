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

package org.citrusframework.cucumber.steps.selenium;

import java.net.URL;

import org.citrusframework.Citrus;
import org.citrusframework.DefaultCitrusContextProvider;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.context.TestContext;
import org.citrusframework.selenium.actions.CheckInputAction;
import org.citrusframework.selenium.actions.ClickAction;
import org.citrusframework.selenium.actions.FindElementAction;
import org.citrusframework.selenium.actions.NavigateAction;
import org.citrusframework.selenium.actions.SeleniumAction;
import org.citrusframework.selenium.actions.SetInputAction;
import org.citrusframework.selenium.actions.StartBrowserAction;
import org.citrusframework.selenium.actions.StopBrowserAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumBrowserConfiguration;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SeleniumStepsTest {

    private SeleniumSteps steps;

    private TestCaseRunner runner;

    private final SeleniumBrowser seleniumBrowser = Mockito.mock(SeleniumBrowser.class);

    private final ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);

    private final Citrus citrus = Citrus.newInstance(new DefaultCitrusContextProvider());

    @BeforeMethod
    public void injectResources() {
        TestContext context = citrus.getCitrusContext().createTestContext();
        steps = new SeleniumSteps();
        runner = new DefaultTestCaseRunner(context);

        CitrusAnnotations.injectAll(steps, citrus, context);
        CitrusAnnotations.injectTestRunner(steps, runner);

        citrus.getCitrusContext().bind("seleniumBrowser", seleniumBrowser);
    }

    @Test
    public void testStart() {
        SeleniumBrowserConfiguration endpointConfiguration = new SeleniumBrowserConfiguration();
        when(seleniumBrowser.getName()).thenReturn("seleniumBrowser");
        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);
        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        steps.setBrowser("seleniumBrowser");
        steps.start();

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof StartBrowserAction);

        verify(seleniumBrowser).start();
    }

    @Test
    public void testStop() {
        SeleniumBrowserConfiguration endpointConfiguration = new SeleniumBrowserConfiguration();
        when(seleniumBrowser.getName()).thenReturn("seleniumBrowser");
        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);
        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        steps.setBrowser("seleniumBrowser");
        steps.stop();

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof StopBrowserAction);

        verify(seleniumBrowser).stop();
    }

    @Test
    public void testNavigate() {
        SeleniumBrowserConfiguration endpointConfiguration = new SeleniumBrowserConfiguration();
        when(seleniumBrowser.getName()).thenReturn("seleniumBrowser");
        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);
        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        WebDriver.Navigation navigation = Mockito.mock(WebDriver.Navigation.class);
        when(webDriver.navigate()).thenReturn(navigation);

        steps.setBrowser("seleniumBrowser");
        steps.navigate("http://localhost:8080/test");

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof NavigateAction);
        Assert.assertEquals(((NavigateAction)action).getPage(), "http://localhost:8080/test");

        verify(navigation).to(any(URL.class));
    }

    @Test
    public void testClick() {
        SeleniumBrowserConfiguration endpointConfiguration = new SeleniumBrowserConfiguration();
        when(seleniumBrowser.getName()).thenReturn("seleniumBrowser");
        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);
        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        WebElement element = Mockito.mock(WebElement.class);
        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("button");

        when(webDriver.findElement(any(By.class))).thenAnswer(invocation -> {
            By select = (By) invocation.getArguments()[0];

            Assert.assertEquals(select.getClass(), By.ById.class);
            Assert.assertEquals(select.toString(), "By.id: foo");
            return element;
        });

        steps.setBrowser("seleniumBrowser");
        steps.click("id", "foo");

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof ClickAction);
        Assert.assertEquals(((ClickAction)action).getProperty(), "id");
        Assert.assertEquals(((ClickAction)action).getPropertyValue(), "foo");

        verify(element).click();
    }

    @Test
    public void testSetInput() {
        SeleniumBrowserConfiguration endpointConfiguration = new SeleniumBrowserConfiguration();
        when(seleniumBrowser.getName()).thenReturn("seleniumBrowser");
        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);
        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        WebElement element = Mockito.mock(WebElement.class);
        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("input");

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        steps.setBrowser("seleniumBrowser");
        steps.setInput("Hello","id", "foo");

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof SetInputAction);
        Assert.assertEquals(((SetInputAction)action).getValue(), "Hello");
        Assert.assertEquals(((SetInputAction)action).getProperty(), "id");
        Assert.assertEquals(((SetInputAction)action).getPropertyValue(), "foo");

        verify(element).clear();
        verify(element).sendKeys("Hello");
    }

    @Test
    public void testCheckInput() {
        SeleniumBrowserConfiguration endpointConfiguration = new SeleniumBrowserConfiguration();
        when(seleniumBrowser.getName()).thenReturn("seleniumBrowser");
        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);
        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        WebElement element = Mockito.mock(WebElement.class);
        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("input");

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        steps.setBrowser("seleniumBrowser");
        steps.checkInput("checks","id", "foo");

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof CheckInputAction);
        Assert.assertTrue(((CheckInputAction) action).isChecked());
        Assert.assertEquals(((CheckInputAction)action).getProperty(), "id");
        Assert.assertEquals(((CheckInputAction)action).getPropertyValue(), "foo");

        verify(element).click();
    }

    @Test
    public void testShouldDisplay() {
        SeleniumBrowserConfiguration endpointConfiguration = new SeleniumBrowserConfiguration();
        when(seleniumBrowser.getName()).thenReturn("seleniumBrowser");
        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);
        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        WebElement element = Mockito.mock(WebElement.class);
        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("button");

        when(webDriver.findElement(any(By.class))).thenAnswer(invocation -> {
            By select = (By) invocation.getArguments()[0];

            Assert.assertEquals(select.getClass(), By.ByName.class);
            Assert.assertEquals(select.toString(), "By.name: foo");
            return element;
        });

        steps.setBrowser("seleniumBrowser");
        steps.shouldDisplay("name", "foo");

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof FindElementAction);
        Assert.assertEquals(((FindElementAction)action).getProperty(), "name");
        Assert.assertEquals(((FindElementAction)action).getPropertyValue(), "foo");
    }

    @Test
    public void testDefaultBrowserInitialization() {
        Assert.assertNull(steps.browser);
        steps.before();
        Assert.assertNotNull(steps.browser);
    }

    @Test
    public void testBrowserInitialization() {
        Assert.assertNull(steps.browser);
        steps.setBrowser("seleniumBrowser");
        steps.before();
        Assert.assertNotNull(steps.browser);
    }

}
