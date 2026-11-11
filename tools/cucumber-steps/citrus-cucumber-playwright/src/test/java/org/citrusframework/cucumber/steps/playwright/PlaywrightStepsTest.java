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

package org.citrusframework.cucumber.steps.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.cucumber.datatable.DataTable;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceStrategy;
import org.citrusframework.DefaultCitrusContextProvider;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.DialogAction;
import org.citrusframework.playwright.actions.InputAction;
import org.citrusframework.playwright.actions.MouseAction;
import org.citrusframework.playwright.actions.OpenAction;
import org.citrusframework.playwright.actions.PageObjectAction;
import org.citrusframework.playwright.actions.PlaywrightAction;
import org.citrusframework.playwright.actions.ScreenshotAction;
import org.citrusframework.playwright.actions.StartBrowserAction;
import org.citrusframework.playwright.actions.StopBrowserAction;
import org.citrusframework.playwright.actions.VerifyAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlaywrightStepsTest {

    private PlaywrightSteps steps;

    private TestCaseRunner runner;

    private final PlaywrightBrowser playwrightBrowser = Mockito.mock(PlaywrightBrowser.class);

    private final Page page = Mockito.mock(Page.class);

    private final Locator locator = Mockito.mock(Locator.class);

    private Citrus citrus;

    @BeforeEach
    public void injectResources() {
        Mockito.reset(playwrightBrowser, page, locator);

        citrus = Citrus.newInstance(new DefaultCitrusContextProvider(CitrusInstanceStrategy.NEW));
        TestContext context = citrus.getCitrusContext().createTestContext();
        steps = new PlaywrightSteps();
        runner = new DefaultTestCaseRunner(context);

        CitrusAnnotations.injectAll(steps, citrus, context);
        CitrusAnnotations.injectTestRunner(steps, runner);

        citrus.getCitrusContext().bind("playwrightBrowser", playwrightBrowser);

        when(playwrightBrowser.getName()).thenReturn("playwrightBrowser");
        when(playwrightBrowser.getCurrentPage()).thenReturn(page);
        when(playwrightBrowser.createRedactor()).thenReturn(new SecretPatternRedactor());
        when(page.locator(anyString())).thenReturn(locator);
        when(page.getByText(anyString())).thenReturn(locator);
        when(page.getByTestId(anyString())).thenReturn(locator);
        when(page.getByRole(any(AriaRole.class), any(Page.GetByRoleOptions.class))).thenReturn(locator);

        LoginPageStub.reset();
        LoginPageValidatorStub.reset();
    }

    @Test
    public void testStart() {
        when(playwrightBrowser.isStarted()).thenReturn(false);

        steps.setBrowser("playwrightBrowser");
        steps.start();

        TestCase testCase = runner.getTestCase();
        Assertions.assertEquals(1L, testCase.getActionCount());
        Assertions.assertTrue(testCase.getTestAction(0) instanceof PlaywrightAction);
        PlaywrightAction action = (PlaywrightAction) testCase.getTestAction(0);

        Assertions.assertEquals(playwrightBrowser, ((StartBrowserAction) action).getBrowser());
        Assertions.assertTrue(action instanceof StartBrowserAction);

        verify(playwrightBrowser).start();
    }

    @Test
    public void testStop() {
        steps.setBrowser("playwrightBrowser");
        steps.stop();

        TestCase testCase = runner.getTestCase();
        Assertions.assertEquals(1L, testCase.getActionCount());
        Assertions.assertTrue(testCase.getTestAction(0) instanceof PlaywrightAction);
        PlaywrightAction action = (PlaywrightAction) testCase.getTestAction(0);

        Assertions.assertEquals(playwrightBrowser, ((StopBrowserAction) action).getBrowser());
        Assertions.assertTrue(action instanceof StopBrowserAction);

        verify(playwrightBrowser).stop();
    }

    @Test
    public void testSetUnknownBrowserFails() {
        try {
            steps.setBrowser("unknownBrowser");
            Assertions.fail("Expected CitrusRuntimeException for unknown browser id");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().contains("unknownBrowser"));
        }
    }

    @Test
    public void testBeforeResolvesDefaultByName() {
        org.citrusframework.playwright.endpoint.PlaywrightBrowser second =
                Mockito.mock(org.citrusframework.playwright.endpoint.PlaywrightBrowser.class);
        when(second.getName()).thenReturn("secondBrowser");
        citrus.getCitrusContext().bind("secondBrowser", second);

        steps.before();

        Assertions.assertEquals(playwrightBrowser, steps.browser,
                "with several beans present @Before must resolve the configured default browser name");
    }

    @Test
    public void testBeforeSkipsFallbackWhenBeanPresent() {
        org.citrusframework.playwright.endpoint.PlaywrightBrowser second =
                Mockito.mock(org.citrusframework.playwright.endpoint.PlaywrightBrowser.class);
        when(second.getName()).thenReturn("secondBrowser");
        citrus.getCitrusContext().bind("secondBrowser", second);

        int beforeBindings = citrus.getCitrusContext().getReferenceResolver().resolveAll(
                org.citrusframework.playwright.endpoint.PlaywrightBrowser.class).size();

        steps.before();

        int afterBindings = citrus.getCitrusContext().getReferenceResolver().resolveAll(
                org.citrusframework.playwright.endpoint.PlaywrightBrowser.class).size();

        Assertions.assertEquals(beforeBindings, afterBindings,
                "@Before must not auto-create a default browser when any PlaywrightBrowser bean is configured");
        Assertions.assertNotNull(steps.browser,
                "@Before must bind an existing PlaywrightBrowser bean instead of creating a default");
    }

    @Test
    public void testBeforeBuildsDefaultWhenNoBeanPresent() {
        Citrus fresh = Citrus.newInstance(new DefaultCitrusContextProvider(CitrusInstanceStrategy.NEW));
        TestContext freshContext = fresh.getCitrusContext().createTestContext();
        PlaywrightSteps freshSteps = new PlaywrightSteps();
        CitrusAnnotations.injectAll(freshSteps, fresh, freshContext);
        CitrusAnnotations.injectTestRunner(freshSteps, new DefaultTestCaseRunner(freshContext));

        freshSteps.before();

        Assertions.assertNotNull(freshSteps.browser, "@Before must build a default browser when none is configured");
        Assertions.assertEquals(
                freshSteps.browser,
                fresh.getCitrusContext().getReferenceResolver()
                        .resolve(PlaywrightSettings.getBrowserName(), PlaywrightBrowser.class),
                "the auto-created default must be bound under the configured browser name");
    }

    @Test
    public void testOpen() {
        steps.setBrowser("playwrightBrowser");
        steps.open("https://example.com/login");

        TestCase testCase = runner.getTestCase();
        Assertions.assertEquals(1L, testCase.getActionCount());
        Assertions.assertTrue(testCase.getTestAction(0) instanceof OpenAction);
        OpenAction action = (OpenAction) testCase.getTestAction(0);

        Assertions.assertEquals(playwrightBrowser, action.getBrowser());
        Assertions.assertEquals("https://example.com/login", action.getUrl());

        verify(page).navigate("https://example.com/login");
    }

    @Test
    public void testClick() {
        steps.setBrowser("playwrightBrowser");
        steps.click("#login");

        TestCase testCase = runner.getTestCase();
        Assertions.assertTrue(testCase.getTestAction(0) instanceof MouseAction);
        MouseAction action = (MouseAction) testCase.getTestAction(0);

        Assertions.assertEquals(playwrightBrowser, action.getBrowser());
        Assertions.assertEquals(MouseAction.Command.CLICK, action.getCommand());

        verify(page).locator("#login");
        verify(locator).click();
    }

    @Test
    public void testFill() {
        steps.setBrowser("playwrightBrowser");
        steps.fill("alice", "#username");

        TestCase testCase = runner.getTestCase();
        Assertions.assertTrue(testCase.getTestAction(0) instanceof InputAction);
        InputAction action = (InputAction) testCase.getTestAction(0);

        Assertions.assertEquals(playwrightBrowser, action.getBrowser());
        Assertions.assertEquals(InputAction.Command.FILL, action.getCommand());
        Assertions.assertEquals("alice", action.getValue());

        verify(locator).fill("alice");
    }

    @Test
    public void testClear() {
        steps.setBrowser("playwrightBrowser");
        steps.clear("#username");

        Assertions.assertTrue(runner.getTestCase().getTestAction(0) instanceof InputAction);
        Assertions.assertEquals(InputAction.Command.CLEAR, ((InputAction) runner.getTestCase().getTestAction(0)).getCommand());
        verify(locator).clear();
    }

    @Test
    public void testPress() {
        steps.setBrowser("playwrightBrowser");
        steps.press("Enter", "#username");

        Assertions.assertTrue(runner.getTestCase().getTestAction(0) instanceof InputAction);
        InputAction action = (InputAction) runner.getTestCase().getTestAction(0);
        Assertions.assertEquals(InputAction.Command.PRESS, action.getCommand());
        Assertions.assertEquals("Enter", action.getValue());

        verify(locator).press("Enter");
    }

    @Test
    public void testCheckAndUncheck() {
        steps.setBrowser("playwrightBrowser");
        steps.checkInput("checks", "#remember");

        Assertions.assertEquals(InputAction.Command.CHECK, ((InputAction) runner.getTestCase().getTestAction(0)).getCommand());
        verify(locator).check();

        steps.checkInput("unchecks", "#remember");
        Assertions.assertEquals(InputAction.Command.UNCHECK, ((InputAction) runner.getTestCase().getTestAction(1)).getCommand());
        verify(locator).uncheck();
    }

    @Test
    public void testSelectAndHover() {
        steps.setBrowser("playwrightBrowser");
        steps.select("blue", "#color");

        Assertions.assertTrue(runner.getTestCase().getTestAction(0) instanceof InputAction);
        Assertions.assertEquals(InputAction.Command.SELECT, ((InputAction) runner.getTestCase().getTestAction(0)).getCommand());
        verify(locator).selectOption(new String[]{"blue"});

        steps.hover("#menu");
        Assertions.assertTrue(runner.getTestCase().getTestAction(1) instanceof MouseAction);
        Assertions.assertEquals(MouseAction.Command.HOVER, ((MouseAction) runner.getTestCase().getTestAction(1)).getCommand());
        verify(locator).hover();
    }

    @Test
    public void testShouldDisplay() {
        when(locator.isVisible()).thenReturn(true);

        steps.setBrowser("playwrightBrowser");
        steps.shouldDisplay("#welcome");

        Assertions.assertTrue(runner.getTestCase().getTestAction(0) instanceof VerifyAction);
        VerifyAction action = (VerifyAction) runner.getTestCase().getTestAction(0);
        Assertions.assertEquals(VerifyAction.Check.VISIBLE, action.getCheck());

        verify(locator).isVisible();
    }

    @Test
    public void testShouldHaveText() {
        when(locator.textContent()).thenReturn("Hello");

        steps.setBrowser("playwrightBrowser");
        steps.shouldHaveText("#welcome", "Hello");

        Assertions.assertTrue(runner.getTestCase().getTestAction(0) instanceof VerifyAction);
        Assertions.assertEquals(VerifyAction.Check.TEXT, ((VerifyAction) runner.getTestCase().getTestAction(0)).getCheck());

        verify(locator).textContent();
    }

    @Test
    public void testPageTitleAndUrl() {
        when(page.title()).thenReturn("Shop");
        when(page.url()).thenReturn("https://example.com/shop");

        steps.setBrowser("playwrightBrowser");
        steps.pageTitleShouldBe("Shop");
        steps.pageUrlShouldBe("https://example.com/shop");

        Assertions.assertEquals(VerifyAction.Check.TITLE, ((VerifyAction) runner.getTestCase().getTestAction(0)).getCheck());
        Assertions.assertEquals(VerifyAction.Check.URL, ((VerifyAction) runner.getTestCase().getTestAction(1)).getCheck());

        verify(page).title();
        verify(page).url();
    }

    @Test
    public void testDialogAndScreenshot() {
        steps.setBrowser("playwrightBrowser");
        steps.handleDialog("accepts");
        steps.handleDialog("dismisses");
        steps.screenshot("target/shot.png");

        Assertions.assertTrue(runner.getTestCase().getTestAction(0) instanceof DialogAction);
        Assertions.assertEquals(DialogAction.Command.ACCEPT, ((DialogAction) runner.getTestCase().getTestAction(0)).getCommand());
        Assertions.assertTrue(runner.getTestCase().getTestAction(1) instanceof DialogAction);
        Assertions.assertEquals(DialogAction.Command.DISMISS, ((DialogAction) runner.getTestCase().getTestAction(1)).getCommand());
        Assertions.assertTrue(runner.getTestCase().getTestAction(2) instanceof ScreenshotAction);
        Assertions.assertEquals("target/shot.png", ((ScreenshotAction) runner.getTestCase().getTestAction(2)).getPath());

        verify(page, Mockito.times(2)).onceDialog(any());
        verify(page).screenshot(any(Page.ScreenshotOptions.class));
    }

    @Test
    public void testClickTyped() {
        steps.setBrowser("playwrightBrowser");
        steps.clickTyped("xpath", "//button[@id='go']");

        TestCase testCase = runner.getTestCase();
        Assertions.assertTrue(testCase.getTestAction(0) instanceof MouseAction);
        Assertions.assertEquals(MouseAction.Command.CLICK, ((MouseAction) testCase.getTestAction(0)).getCommand());

        verify(page).locator("xpath=//button[@id='go']");
        verify(locator).click();
    }

    @Test
    public void testClickRoleNamed() {
        steps.setBrowser("playwrightBrowser");
        steps.clickRoleNamed("button", "Login");

        TestCase testCase = runner.getTestCase();
        Assertions.assertTrue(testCase.getTestAction(0) instanceof MouseAction);
        Assertions.assertEquals(MouseAction.Command.CLICK, ((MouseAction) testCase.getTestAction(0)).getCommand());

        verify(page).getByRole(org.mockito.ArgumentMatchers.eq(AriaRole.BUTTON), any(Page.GetByRoleOptions.class));
        verify(locator).click();
    }

    @Test
    public void testFillTyped() {
        steps.setBrowser("playwrightBrowser");
        steps.fillTyped("alice", "text", "Username");

        TestCase testCase = runner.getTestCase();
        Assertions.assertTrue(testCase.getTestAction(0) instanceof InputAction);
        InputAction action = (InputAction) testCase.getTestAction(0);
        Assertions.assertEquals(InputAction.Command.FILL, action.getCommand());
        Assertions.assertEquals("alice", action.getValue());

        verify(page).getByText("Username");
        verify(locator).fill("alice");
    }

    @Test
    public void testShouldDisplayTyped() {
        when(locator.isVisible()).thenReturn(true);

        steps.setBrowser("playwrightBrowser");
        steps.shouldDisplayTyped("test-id", "welcome");

        Assertions.assertTrue(runner.getTestCase().getTestAction(0) instanceof VerifyAction);
        Assertions.assertEquals(VerifyAction.Check.VISIBLE, ((VerifyAction) runner.getTestCase().getTestAction(0)).getCheck());

        verify(page).getByTestId("welcome");
        verify(locator).isVisible();
    }

    @Test
    public void testShouldHaveTextTyped() {
        when(locator.textContent()).thenReturn("Hello");

        steps.setBrowser("playwrightBrowser");
        steps.shouldHaveTextTyped("xpath", "//h1", "Hello");

        Assertions.assertTrue(runner.getTestCase().getTestAction(0) instanceof VerifyAction);
        Assertions.assertEquals(VerifyAction.Check.TEXT, ((VerifyAction) runner.getTestCase().getTestAction(0)).getCheck());

        verify(page).locator("xpath=//h1");
        verify(locator).textContent();
    }

    @Test
    public void testClearTypedPressTypedCheckTypedSelectTypedHoverTyped() {
        steps.setBrowser("playwrightBrowser");

        steps.clearTyped("xpath", "//input[@id='name']");
        Assertions.assertEquals(InputAction.Command.CLEAR, ((InputAction) runner.getTestCase().getTestAction(0)).getCommand());
        verify(page).locator("xpath=//input[@id='name']");
        verify(locator).clear();

        steps.pressTyped("Enter", "test-id", "go");
        Assertions.assertEquals(InputAction.Command.PRESS, ((InputAction) runner.getTestCase().getTestAction(1)).getCommand());
        verify(page).getByTestId("go");
        verify(locator).press("Enter");

        steps.checkInputTyped("checks", "css", "#remember");
        Assertions.assertEquals(InputAction.Command.CHECK, ((InputAction) runner.getTestCase().getTestAction(2)).getCommand());
        verify(locator).check();

        steps.checkInputTyped("unchecks", "css", "#remember");
        Assertions.assertEquals(InputAction.Command.UNCHECK, ((InputAction) runner.getTestCase().getTestAction(3)).getCommand());
        verify(locator).uncheck();

        steps.selectTyped("blue", "text", "Colors");
        Assertions.assertEquals(InputAction.Command.SELECT, ((InputAction) runner.getTestCase().getTestAction(4)).getCommand());
        verify(page).getByText("Colors");

        steps.hoverTyped("role", "menu");
        Assertions.assertEquals(MouseAction.Command.HOVER, ((MouseAction) runner.getTestCase().getTestAction(5)).getCommand());
    }

    @Test
    public void testFillRoleNamedDisplayRoleNamedHaveTextRoleNamed() {
        steps.setBrowser("playwrightBrowser");

        steps.fillRoleNamed("alice", "textbox", "Username");
        Assertions.assertEquals(InputAction.Command.FILL, ((InputAction) runner.getTestCase().getTestAction(0)).getCommand());
        verify(page).getByRole(org.mockito.ArgumentMatchers.eq(AriaRole.TEXTBOX), any(Page.GetByRoleOptions.class));
        verify(locator).fill("alice");

        when(locator.isVisible()).thenReturn(true);
        steps.shouldDisplayRoleNamed("button", "Login");
        Assertions.assertEquals(VerifyAction.Check.VISIBLE, ((VerifyAction) runner.getTestCase().getTestAction(1)).getCheck());

        when(locator.textContent()).thenReturn("Hello");
        steps.shouldHaveTextRoleNamed("heading", "Welcome", "Hello");
        Assertions.assertEquals(VerifyAction.Check.TEXT, ((VerifyAction) runner.getTestCase().getTestAction(2)).getCheck());
    }

    @Test
    public void testStepWithoutResolvedBrowserFailsFast() {
        Citrus fresh = Citrus.newInstance(new DefaultCitrusContextProvider(CitrusInstanceStrategy.NEW));
        TestContext freshContext = fresh.getCitrusContext().createTestContext();
        PlaywrightSteps freshSteps = new PlaywrightSteps();
        CitrusAnnotations.injectAll(freshSteps, fresh, freshContext);
        CitrusAnnotations.injectTestRunner(freshSteps, new DefaultTestCaseRunner(freshContext));

        org.citrusframework.playwright.endpoint.PlaywrightBrowser shop =
                Mockito.mock(org.citrusframework.playwright.endpoint.PlaywrightBrowser.class);
        org.citrusframework.playwright.endpoint.PlaywrightBrowser admin =
                Mockito.mock(org.citrusframework.playwright.endpoint.PlaywrightBrowser.class);
        fresh.getCitrusContext().bind("shopBrowser", shop);
        fresh.getCitrusContext().bind("adminBrowser", admin);
        freshSteps.before();

        // Two custom beans, no default: before() leaves browser unresolved.
        Assertions.assertNull(freshSteps.browser);
        try {
            freshSteps.click("#login");
            Assertions.fail("Expected CitrusRuntimeException naming the available browsers");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().contains("shopBrowser"), "must name available ids: " + e.getMessage());
            Assertions.assertTrue(e.getMessage().contains("adminBrowser"), "must name available ids: " + e.getMessage());
        }
    }

    @Test
    public void testStepWithoutAnyBrowserConfiguredFailsFast() {
        Citrus fresh = Citrus.newInstance(new DefaultCitrusContextProvider(CitrusInstanceStrategy.NEW));
        TestContext freshContext = fresh.getCitrusContext().createTestContext();
        PlaywrightSteps freshSteps = new PlaywrightSteps();
        CitrusAnnotations.injectAll(freshSteps, fresh, freshContext);
        CitrusAnnotations.injectTestRunner(freshSteps, new DefaultTestCaseRunner(freshContext));

        try {
            freshSteps.click("#login");
            Assertions.fail("Expected CitrusRuntimeException when no browser is configured");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().contains("No Playwright browser configured"), e.getMessage());
        }
    }

    @Test
    public void testPageAction() {
        steps.setBrowser("playwrightBrowser");
        steps.pageAction(LoginPageStub.class.getName(), "submit");

        TestCase testCase = runner.getTestCase();
        Assertions.assertTrue(testCase.getTestAction(0) instanceof PageObjectAction);
        PageObjectAction action = (PageObjectAction) testCase.getTestAction(0);
        Assertions.assertEquals(LoginPageStub.class, action.getPageType());
        Assertions.assertEquals("submit", action.getMethod());

        Assertions.assertEquals(List.of("submit"), LoginPageStub.invocations);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPageActionWithArguments() {
        DataTable dataTable = Mockito.mock(DataTable.class);
        when(dataTable.asList(String.class)).thenReturn(List.of("alice"));

        steps.setBrowser("playwrightBrowser");
        steps.pageActionWithArguments(LoginPageStub.class.getName(), "setUserName", dataTable);

        TestCase testCase = runner.getTestCase();
        Assertions.assertTrue(testCase.getTestAction(0) instanceof PageObjectAction);
        PageObjectAction action = (PageObjectAction) testCase.getTestAction(0);
        Assertions.assertEquals(LoginPageStub.class, action.getPageType());
        Assertions.assertEquals("setUserName", action.getMethod());
        Assertions.assertArrayEquals(new String[]{"alice"}, action.getArguments());

        Assertions.assertEquals("alice", LoginPageStub.lastUserName);
    }

    @Test
    public void testPageShouldValidate() {
        steps.setBrowser("playwrightBrowser");
        steps.pageShouldValidate(LoginPageStub.class.getName(), LoginPageValidatorStub.class.getName());

        TestCase testCase = runner.getTestCase();
        Assertions.assertTrue(testCase.getTestAction(0) instanceof PageObjectAction);
        PageObjectAction action = (PageObjectAction) testCase.getTestAction(0);
        Assertions.assertEquals(LoginPageStub.class, action.getPageType());
        Assertions.assertEquals(LoginPageValidatorStub.class, action.getValidatorType());

        Assertions.assertTrue(LoginPageValidatorStub.validated);
    }

    @Test
    public void testPageActionUnknownTypeFails() {
        steps.setBrowser("playwrightBrowser");
        try {
            steps.pageAction("com.example.MissingPage", "submit");
            Assertions.fail("Expected CitrusRuntimeException for unknown page type");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().contains("com.example.MissingPage"));
        }
    }

    @Test
    public void testPageValidateInvalidValidatorFails() {
        steps.setBrowser("playwrightBrowser");
        try {
            steps.pageShouldValidate(LoginPageStub.class.getName(), LoginPageStub.class.getName());
            Assertions.fail("Expected CitrusRuntimeException for invalid validator type");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().contains(LoginPageStub.class.getName()));
        }
    }
}
