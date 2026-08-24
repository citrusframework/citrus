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

package org.citrusframework.playwright.xml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.playwright.actions.ExtractAction;
import org.citrusframework.playwright.actions.InputAction;
import org.citrusframework.playwright.actions.JavaScriptAction;
import org.citrusframework.playwright.actions.MouseAction;
import org.citrusframework.playwright.actions.NavigateAction;
import org.citrusframework.playwright.actions.OpenAction;
import org.citrusframework.playwright.actions.ScreenshotAction;
import org.citrusframework.playwright.actions.StartBrowserAction;
import org.citrusframework.playwright.actions.StopBrowserAction;
import org.citrusframework.playwright.actions.VerifyAction;
import org.citrusframework.playwright.actions.WaitForAction;
import org.citrusframework.playwright.dsl.AbstractDslLoaderTest;
import org.citrusframework.playwright.support.StubStartedBrowser;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.XmlTestActionBuilder;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

class PlaywrightXmlDslTest extends AbstractDslLoaderTest {

    @Test
    void shouldLookupPlaywrightActionBuilder() {
        assertTrue(XmlTestActionBuilder.lookup("playwright").isPresent());
        assertEquals(Playwright.class, XmlTestActionBuilder.lookup("playwright").orElseThrow().getClass());
    }

    @Test
    void shouldLoadTestCaseMetaData() {
        bindStubBrowser();

        TestCase result = loadPlaywrightTestCase();

        assertEquals("PlaywrightDslTest", result.getName());
        assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        assertEquals(12L, result.getActionCount());
    }

    @Test
    void shouldLoadBrowserLifecycleActions() {
        StubStartedBrowser browser = bindStubBrowser();

        TestCase result = loadPlaywrightTestCase();

        StartBrowserAction startAction = (StartBrowserAction) result.getTestAction(0);
        assertEquals("playwright:start", startAction.getName());
        assertNotNull(startAction.getBrowser());

        OpenAction openAction = (OpenAction) result.getTestAction(1);
        assertEquals("playwright:open", openAction.getName());
        assertEquals("index.html", openAction.getUrl());

        NavigateAction navigateAction = (NavigateAction) result.getTestAction(8);
        assertEquals(NavigateAction.Command.RELOAD, navigateAction.getCommand());

        StopBrowserAction stopAction = (StopBrowserAction) result.getTestAction(11);
        assertEquals("playwright:stop", stopAction.getName());
        assertEquals(1, browser.stopCount());
    }

    @Test
    void shouldLoadElementInteractionActions() {
        bindStubBrowser();

        TestCase result = loadPlaywrightTestCase();

        MouseAction doubleClickAction = (MouseAction) result.getTestAction(2);
        assertEquals(MouseAction.Command.DOUBLE_CLICK, doubleClickAction.getCommand());
        assertEquals("playwright:double-click", doubleClickAction.getName());

        InputAction fillAction = (InputAction) result.getTestAction(3);
        assertEquals(InputAction.Command.FILL, fillAction.getCommand());
        assertEquals("${username}", fillAction.getValue());

        InputAction uploadAction = (InputAction) result.getTestAction(4);
        assertEquals(InputAction.Command.UPLOAD, uploadAction.getCommand());
        assertEquals("uploads/avatar.png", uploadAction.getValue());
    }

    @Test
    void shouldLoadVerificationAndCaptureActions() {
        bindStubBrowser();

        TestCase result = loadPlaywrightTestCase();

        WaitForAction waitAction = (WaitForAction) result.getTestAction(5);
        assertEquals(WaitForAction.Condition.HIDDEN, waitAction.getCondition());

        VerifyAction verifyAction = (VerifyAction) result.getTestAction(6);
        assertEquals(VerifyAction.Check.ABSENT, verifyAction.getCheck());

        ExtractAction extractAction = (ExtractAction) result.getTestAction(7);
        assertEquals(ExtractAction.Value.URL, extractAction.getValue());
        assertEquals("pageUrl", extractAction.getVariable());

        ScreenshotAction screenshotAction = (ScreenshotAction) result.getTestAction(9);
        assertEquals("target/playwright/screen.png", screenshotAction.getPath());

        JavaScriptAction javaScriptAction = (JavaScriptAction) result.getTestAction(10);
        assertEquals("window.title", javaScriptAction.getScript());
        assertEquals("jsResult", javaScriptAction.getVariable());
    }

    @Test
    void shouldApplyStartEndpointSettings() {
        bindStubBrowser();

        TestCase result = loadXmlTestCase("playwright-start.citrus.it");
        assertEquals(1L, result.getActionCount());

        StartBrowserAction startAction = (StartBrowserAction) result.getTestAction(0);
        assertFalse(startAction.isAllowAlreadyStarted());
        assertNotNull(startAction.getBrowser());
    }

    /**
     * Loads and runs the shared Playwright XML test case covering every action.
     */
    private TestCase loadPlaywrightTestCase() {
        context.setVariable("username", "foo_user");
        return loadXmlTestCase("playwright.citrus.it");
    }

    private TestCase loadXmlTestCase(String name) {
        XmlTestLoader testLoader = createXmlTestLoader(name);
        testLoader.load();
        return testLoader.getTestCase();
    }
}
