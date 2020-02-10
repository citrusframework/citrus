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

package com.consol.citrus.cucumber.step.runner.selenium;

import java.net.URL;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSpringContext;
import com.consol.citrus.TestCase;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.dsl.annotations.CitrusDslAnnotations;
import com.consol.citrus.dsl.runner.DefaultTestRunner;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.selenium.actions.CheckInputAction;
import com.consol.citrus.selenium.actions.ClickAction;
import com.consol.citrus.selenium.actions.FindElementAction;
import com.consol.citrus.selenium.actions.NavigateAction;
import com.consol.citrus.selenium.actions.SeleniumAction;
import com.consol.citrus.selenium.actions.SetInputAction;
import com.consol.citrus.selenium.actions.StartBrowserAction;
import com.consol.citrus.selenium.actions.StopBrowserAction;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumBrowserConfiguration;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import cucumber.api.Scenario;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumStepsTest extends AbstractTestNGUnitTest {

    private Citrus citrus;
    private SeleniumSteps steps;

    private TestRunner runner;

    @Autowired
    private SeleniumBrowser seleniumBrowser;

    private ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);

    @BeforeClass
    public void setup() {
        citrus = Citrus.newInstance(CitrusSpringContext.create(applicationContext));
    }

    @BeforeMethod
    public void injectResources() {
        steps = new SeleniumSteps();
        runner = new DefaultTestRunner(context);
        CitrusAnnotations.injectAll(steps, citrus, context);
        CitrusDslAnnotations.injectTestRunner(steps, runner);
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
        steps.checkInput("check","id", "foo");

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
        steps.should_display("name", "foo");

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
        steps.before(Mockito.mock(Scenario.class));
        Assert.assertNotNull(steps.browser);
    }

    @Test
    public void testBrowserInitialization() {
        Assert.assertNull(steps.browser);
        steps.setBrowser("seleniumBrowser");
        steps.before(Mockito.mock(Scenario.class));
        Assert.assertNotNull(steps.browser);
    }

}
