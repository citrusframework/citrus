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

package com.consol.citrus.cucumber.step.designer.selenium;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.dsl.annotations.CitrusDslAnnotations;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.selenium.actions.*;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import cucumber.api.Scenario;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumStepsTest extends AbstractTestNGUnitTest {

    private Citrus citrus;
    private SeleniumSteps steps;

    private TestDesigner designer;

    @Autowired
    private SeleniumBrowser seleniumBrowser;

    @BeforeClass
    public void setup() {
        citrus = Citrus.newInstance(applicationContext);
    }

    @BeforeMethod
    public void injectResources() {
        steps = new SeleniumSteps();
        designer = new DefaultTestDesigner(applicationContext, context);
        CitrusAnnotations.injectAll(steps, citrus, context);
        CitrusDslAnnotations.injectTestDesigner(steps, designer);
    }

    @Test
    public void testStart() {
        steps.setBrowser("seleniumBrowser");
        steps.start();

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate() instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) ((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate();

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof StartBrowserAction);
    }

    @Test
    public void testStop() {
        steps.setBrowser("seleniumBrowser");
        steps.stop();

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate() instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) ((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate();

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof StopBrowserAction);
    }

    @Test
    public void testNavigate() {
        steps.setBrowser("seleniumBrowser");
        steps.navigate("http://localhost:8080/test");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate() instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) ((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate();

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof NavigateAction);
        Assert.assertEquals(((NavigateAction)action).getPage(), "http://localhost:8080/test");
    }

    @Test
    public void testClick() {
        steps.setBrowser("seleniumBrowser");
        steps.click("id", "foo");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate() instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) ((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate();

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof ClickAction);
        Assert.assertEquals(((ClickAction)action).getProperty(), "id");
        Assert.assertEquals(((ClickAction)action).getPropertyValue(), "foo");
    }
    
    @Test
    public void testSetInput() {
        steps.setBrowser("seleniumBrowser");
        steps.setInput("Hello","id", "foo");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate() instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) ((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate();

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof SetInputAction);
        Assert.assertEquals(((SetInputAction)action).getValue(), "Hello");
        Assert.assertEquals(((SetInputAction)action).getProperty(), "id");
        Assert.assertEquals(((SetInputAction)action).getPropertyValue(), "foo");
    }

    @Test
    public void testCheckInput() {
        steps.setBrowser("seleniumBrowser");
        steps.checkInput("uncheck","id", "foo");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate() instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) ((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate();

        Assert.assertEquals(action.getBrowser(), seleniumBrowser);
        Assert.assertTrue(action instanceof CheckInputAction);
        Assert.assertEquals(((CheckInputAction)action).isChecked(), false);
        Assert.assertEquals(((CheckInputAction)action).getProperty(), "id");
        Assert.assertEquals(((CheckInputAction)action).getPropertyValue(), "foo");
    }

    @Test
    public void testShouldDisplay() {
        steps.setBrowser("seleniumBrowser");
        steps.should_display("name", "foo");

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate() instanceof SeleniumAction);
        SeleniumAction action = (SeleniumAction) ((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate();

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