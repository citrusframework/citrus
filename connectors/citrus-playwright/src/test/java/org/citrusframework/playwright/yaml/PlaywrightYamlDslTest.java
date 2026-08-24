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

package org.citrusframework.playwright.yaml;

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
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

class PlaywrightYamlDslTest extends AbstractDslLoaderTest {

    @Test
    void shouldLookupPlaywrightActionBuilder() {
        assertTrue(YamlTestActionBuilder.lookup().containsKey("playwright"));
        assertEquals(Playwright.class, YamlTestActionBuilder.lookup("playwright").orElseThrow().getClass());
    }

    @Test
    void shouldLoadTestCaseMetaData() {
        bindStubBrowser();

        TestCase result = loadPlaywrightTestCase();

        assertEquals("PlaywrightDslTest", result.getName());
        assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        assertEquals(13L, result.getActionCount());
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

        NavigateAction navigateAction = (NavigateAction) result.getTestAction(9);
        assertEquals(NavigateAction.Command.BACK, navigateAction.getCommand());

        StopBrowserAction stopAction = (StopBrowserAction) result.getTestAction(12);
        assertEquals("playwright:stop", stopAction.getName());
        assertEquals(1, browser.stopCount());
    }

    @Test
    void shouldLoadElementInteractionActions() {
        bindStubBrowser();

        TestCase result = loadPlaywrightTestCase();

        MouseAction clickAction = (MouseAction) result.getTestAction(2);
        assertEquals(MouseAction.Command.CLICK, clickAction.getCommand());
        assertEquals("playwright:click", clickAction.getName());

        InputAction fillAction = (InputAction) result.getTestAction(3);
        assertEquals(InputAction.Command.FILL, fillAction.getCommand());
        assertEquals("playwright:fill", fillAction.getName());
        assertEquals("${username}", fillAction.getValue());

        InputAction pressAction = (InputAction) result.getTestAction(4);
        assertEquals(InputAction.Command.PRESS, pressAction.getCommand());
        assertEquals("Enter", pressAction.getValue());

        InputAction selectAction = (InputAction) result.getTestAction(5);
        assertEquals(InputAction.Command.SELECT, selectAction.getCommand());
        assertEquals(1, selectAction.getValues().size());
        assertEquals("male", selectAction.getValues().get(0));
    }

    @Test
    void shouldLoadVerificationAndCaptureActions() {
        bindStubBrowser();

        TestCase result = loadPlaywrightTestCase();

        WaitForAction waitAction = (WaitForAction) result.getTestAction(6);
        assertEquals(WaitForAction.Condition.NETWORK_IDLE, waitAction.getCondition());

        VerifyAction verifyAction = (VerifyAction) result.getTestAction(7);
        assertEquals(VerifyAction.Check.HIDDEN, verifyAction.getCheck());
        assertEquals("playwright:verify", verifyAction.getName());

        ExtractAction extractAction = (ExtractAction) result.getTestAction(8);
        assertEquals(ExtractAction.Value.COUNT, extractAction.getValue());
        assertEquals("titleCount", extractAction.getVariable());

        ScreenshotAction screenshotAction = (ScreenshotAction) result.getTestAction(10);
        assertEquals("target/playwright/screen.png", screenshotAction.getPath());
        assertEquals("screenshotPath", screenshotAction.getVariable());

        JavaScriptAction javaScriptAction = (JavaScriptAction) result.getTestAction(11);
        assertEquals("window.title", javaScriptAction.getScript());
        assertEquals("jsResult", javaScriptAction.getVariable());
    }

    @Test
    void shouldApplyStartEndpointSettings() {
        bindStubBrowser();

        TestCase result = loadYamlTestCase("playwright-start.citrus.it");
        assertEquals(1L, result.getActionCount());

        StartBrowserAction startAction = (StartBrowserAction) result.getTestAction(0);
        assertEquals("playwright:start", startAction.getName());
        assertFalse(startAction.isAllowAlreadyStarted());
        assertNotNull(startAction.getBrowser());
    }

    /**
     * Loads and runs the shared Playwright YAML test case covering every action.
     */
    private TestCase loadPlaywrightTestCase() {
        context.setVariable("username", "foo_user");
        return loadYamlTestCase("playwright.citrus.it");
    }

    private TestCase loadYamlTestCase(String name) {
        YamlTestLoader testLoader = createYamlTestLoader(name);
        testLoader.load();
        return testLoader.getTestCase();
    }
}
