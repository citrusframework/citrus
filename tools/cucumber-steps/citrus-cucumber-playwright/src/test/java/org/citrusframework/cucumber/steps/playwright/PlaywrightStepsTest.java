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
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

    @BeforeMethod
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
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof PlaywrightAction);
        PlaywrightAction action = (PlaywrightAction) testCase.getTestAction(0);

        Assert.assertEquals(((StartBrowserAction) action).getBrowser(), playwrightBrowser);
        Assert.assertTrue(action instanceof StartBrowserAction);

        verify(playwrightBrowser).start();
    }

    @Test
    public void testStop() {
        steps.setBrowser("playwrightBrowser");
        steps.stop();

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof PlaywrightAction);
        PlaywrightAction action = (PlaywrightAction) testCase.getTestAction(0);

        Assert.assertEquals(((StopBrowserAction) action).getBrowser(), playwrightBrowser);
        Assert.assertTrue(action instanceof StopBrowserAction);

        verify(playwrightBrowser).stop();
    }

    @Test
    public void testSetUnknownBrowserFails() {
        try {
            steps.setBrowser("unknownBrowser");
            Assert.fail("Expected CitrusRuntimeException for unknown browser id");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("unknownBrowser"));
        }
    }

    @Test
    public void testBeforeResolvesDefaultByName() {
        org.citrusframework.playwright.endpoint.PlaywrightBrowser second =
                Mockito.mock(org.citrusframework.playwright.endpoint.PlaywrightBrowser.class);
        when(second.getName()).thenReturn("secondBrowser");
        citrus.getCitrusContext().bind("secondBrowser", second);

        steps.before();

        Assert.assertEquals(steps.browser, playwrightBrowser,
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

        Assert.assertEquals(beforeBindings, afterBindings,
                "@Before must not auto-create a default browser when any PlaywrightBrowser bean is configured");
        Assert.assertNotNull(steps.browser,
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

        Assert.assertNotNull(freshSteps.browser, "@Before must build a default browser when none is configured");
        Assert.assertEquals(
                fresh.getCitrusContext().getReferenceResolver()
                        .resolve(PlaywrightSettings.getBrowserName(), PlaywrightBrowser.class),
                freshSteps.browser,
                "the auto-created default must be bound under the configured browser name");
    }

    @Test
    public void testOpen() {
        steps.setBrowser("playwrightBrowser");
        steps.open("https://example.com/login");

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof OpenAction);
        OpenAction action = (OpenAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getBrowser(), playwrightBrowser);
        Assert.assertEquals(action.getUrl(), "https://example.com/login");

        verify(page).navigate("https://example.com/login");
    }

    @Test
    public void testClick() {
        steps.setBrowser("playwrightBrowser");
        steps.click("#login");

        TestCase testCase = runner.getTestCase();
        Assert.assertTrue(testCase.getTestAction(0) instanceof MouseAction);
        MouseAction action = (MouseAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getBrowser(), playwrightBrowser);
        Assert.assertEquals(action.getCommand(), MouseAction.Command.CLICK);

        verify(page).locator("#login");
        verify(locator).click();
    }

    @Test
    public void testFill() {
        steps.setBrowser("playwrightBrowser");
        steps.fill("alice", "#username");

        TestCase testCase = runner.getTestCase();
        Assert.assertTrue(testCase.getTestAction(0) instanceof InputAction);
        InputAction action = (InputAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getBrowser(), playwrightBrowser);
        Assert.assertEquals(action.getCommand(), InputAction.Command.FILL);
        Assert.assertEquals(action.getValue(), "alice");

        verify(locator).fill("alice");
    }

    @Test
    public void testClear() {
        steps.setBrowser("playwrightBrowser");
        steps.clear("#username");

        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof InputAction);
        Assert.assertEquals(((InputAction) runner.getTestCase().getTestAction(0)).getCommand(), InputAction.Command.CLEAR);
        verify(locator).clear();
    }

    @Test
    public void testPress() {
        steps.setBrowser("playwrightBrowser");
        steps.press("Enter", "#username");

        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof InputAction);
        InputAction action = (InputAction) runner.getTestCase().getTestAction(0);
        Assert.assertEquals(action.getCommand(), InputAction.Command.PRESS);
        Assert.assertEquals(action.getValue(), "Enter");

        verify(locator).press("Enter");
    }

    @Test
    public void testCheckAndUncheck() {
        steps.setBrowser("playwrightBrowser");
        steps.checkInput("checks", "#remember");

        Assert.assertEquals(((InputAction) runner.getTestCase().getTestAction(0)).getCommand(), InputAction.Command.CHECK);
        verify(locator).check();

        steps.checkInput("unchecks", "#remember");
        Assert.assertEquals(((InputAction) runner.getTestCase().getTestAction(1)).getCommand(), InputAction.Command.UNCHECK);
        verify(locator).uncheck();
    }

    @Test
    public void testSelectAndHover() {
        steps.setBrowser("playwrightBrowser");
        steps.select("blue", "#color");

        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof InputAction);
        Assert.assertEquals(((InputAction) runner.getTestCase().getTestAction(0)).getCommand(), InputAction.Command.SELECT);
        verify(locator).selectOption(new String[]{"blue"});

        steps.hover("#menu");
        Assert.assertTrue(runner.getTestCase().getTestAction(1) instanceof MouseAction);
        Assert.assertEquals(((MouseAction) runner.getTestCase().getTestAction(1)).getCommand(), MouseAction.Command.HOVER);
        verify(locator).hover();
    }

    @Test
    public void testShouldDisplay() {
        when(locator.isVisible()).thenReturn(true);

        steps.setBrowser("playwrightBrowser");
        steps.shouldDisplay("#welcome");

        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof VerifyAction);
        VerifyAction action = (VerifyAction) runner.getTestCase().getTestAction(0);
        Assert.assertEquals(action.getCheck(), VerifyAction.Check.VISIBLE);

        verify(locator).isVisible();
    }

    @Test
    public void testShouldHaveText() {
        when(locator.textContent()).thenReturn("Hello");

        steps.setBrowser("playwrightBrowser");
        steps.shouldHaveText("#welcome", "Hello");

        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof VerifyAction);
        Assert.assertEquals(((VerifyAction) runner.getTestCase().getTestAction(0)).getCheck(), VerifyAction.Check.TEXT);

        verify(locator).textContent();
    }

    @Test
    public void testPageTitleAndUrl() {
        when(page.title()).thenReturn("Shop");
        when(page.url()).thenReturn("https://example.com/shop");

        steps.setBrowser("playwrightBrowser");
        steps.pageTitleShouldBe("Shop");
        steps.pageUrlShouldBe("https://example.com/shop");

        Assert.assertEquals(((VerifyAction) runner.getTestCase().getTestAction(0)).getCheck(), VerifyAction.Check.TITLE);
        Assert.assertEquals(((VerifyAction) runner.getTestCase().getTestAction(1)).getCheck(), VerifyAction.Check.URL);

        verify(page).title();
        verify(page).url();
    }

    @Test
    public void testDialogAndScreenshot() {
        steps.setBrowser("playwrightBrowser");
        steps.handleDialog("accepts");
        steps.handleDialog("dismisses");
        steps.screenshot("target/shot.png");

        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof DialogAction);
        Assert.assertEquals(((DialogAction) runner.getTestCase().getTestAction(0)).getCommand(), DialogAction.Command.ACCEPT);
        Assert.assertTrue(runner.getTestCase().getTestAction(1) instanceof DialogAction);
        Assert.assertEquals(((DialogAction) runner.getTestCase().getTestAction(1)).getCommand(), DialogAction.Command.DISMISS);
        Assert.assertTrue(runner.getTestCase().getTestAction(2) instanceof ScreenshotAction);
        Assert.assertEquals(((ScreenshotAction) runner.getTestCase().getTestAction(2)).getPath(), "target/shot.png");

        verify(page, Mockito.times(2)).onceDialog(any());
        verify(page).screenshot(any(Page.ScreenshotOptions.class));
    }

    @Test
    public void testClickTyped() {
        steps.setBrowser("playwrightBrowser");
        steps.clickTyped("xpath", "//button[@id='go']");

        TestCase testCase = runner.getTestCase();
        Assert.assertTrue(testCase.getTestAction(0) instanceof MouseAction);
        Assert.assertEquals(((MouseAction) testCase.getTestAction(0)).getCommand(), MouseAction.Command.CLICK);

        verify(page).locator("xpath=//button[@id='go']");
        verify(locator).click();
    }

    @Test
    public void testClickRoleNamed() {
        steps.setBrowser("playwrightBrowser");
        steps.clickRoleNamed("button", "Login");

        TestCase testCase = runner.getTestCase();
        Assert.assertTrue(testCase.getTestAction(0) instanceof MouseAction);
        Assert.assertEquals(((MouseAction) testCase.getTestAction(0)).getCommand(), MouseAction.Command.CLICK);

        verify(page).getByRole(org.mockito.ArgumentMatchers.eq(AriaRole.BUTTON), any(Page.GetByRoleOptions.class));
        verify(locator).click();
    }

    @Test
    public void testFillTyped() {
        steps.setBrowser("playwrightBrowser");
        steps.fillTyped("alice", "text", "Username");

        TestCase testCase = runner.getTestCase();
        Assert.assertTrue(testCase.getTestAction(0) instanceof InputAction);
        InputAction action = (InputAction) testCase.getTestAction(0);
        Assert.assertEquals(action.getCommand(), InputAction.Command.FILL);
        Assert.assertEquals(action.getValue(), "alice");

        verify(page).getByText("Username");
        verify(locator).fill("alice");
    }

    @Test
    public void testShouldDisplayTyped() {
        when(locator.isVisible()).thenReturn(true);

        steps.setBrowser("playwrightBrowser");
        steps.shouldDisplayTyped("test-id", "welcome");

        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof VerifyAction);
        Assert.assertEquals(((VerifyAction) runner.getTestCase().getTestAction(0)).getCheck(), VerifyAction.Check.VISIBLE);

        verify(page).getByTestId("welcome");
        verify(locator).isVisible();
    }

    @Test
    public void testShouldHaveTextTyped() {
        when(locator.textContent()).thenReturn("Hello");

        steps.setBrowser("playwrightBrowser");
        steps.shouldHaveTextTyped("xpath", "//h1", "Hello");

        Assert.assertTrue(runner.getTestCase().getTestAction(0) instanceof VerifyAction);
        Assert.assertEquals(((VerifyAction) runner.getTestCase().getTestAction(0)).getCheck(), VerifyAction.Check.TEXT);

        verify(page).locator("xpath=//h1");
        verify(locator).textContent();
    }

    @Test
    public void testClearTypedPressTypedCheckTypedSelectTypedHoverTyped() {
        steps.setBrowser("playwrightBrowser");

        steps.clearTyped("xpath", "//input[@id='name']");
        Assert.assertEquals(((InputAction) runner.getTestCase().getTestAction(0)).getCommand(), InputAction.Command.CLEAR);
        verify(page).locator("xpath=//input[@id='name']");
        verify(locator).clear();

        steps.pressTyped("Enter", "test-id", "go");
        Assert.assertEquals(((InputAction) runner.getTestCase().getTestAction(1)).getCommand(), InputAction.Command.PRESS);
        verify(page).getByTestId("go");
        verify(locator).press("Enter");

        steps.checkInputTyped("checks", "css", "#remember");
        Assert.assertEquals(((InputAction) runner.getTestCase().getTestAction(2)).getCommand(), InputAction.Command.CHECK);
        verify(locator).check();

        steps.checkInputTyped("unchecks", "css", "#remember");
        Assert.assertEquals(((InputAction) runner.getTestCase().getTestAction(3)).getCommand(), InputAction.Command.UNCHECK);
        verify(locator).uncheck();

        steps.selectTyped("blue", "text", "Colors");
        Assert.assertEquals(((InputAction) runner.getTestCase().getTestAction(4)).getCommand(), InputAction.Command.SELECT);
        verify(page).getByText("Colors");

        steps.hoverTyped("role", "menu");
        Assert.assertEquals(((MouseAction) runner.getTestCase().getTestAction(5)).getCommand(), MouseAction.Command.HOVER);
    }

    @Test
    public void testFillRoleNamedDisplayRoleNamedHaveTextRoleNamed() {
        steps.setBrowser("playwrightBrowser");

        steps.fillRoleNamed("alice", "textbox", "Username");
        Assert.assertEquals(((InputAction) runner.getTestCase().getTestAction(0)).getCommand(), InputAction.Command.FILL);
        verify(page).getByRole(org.mockito.ArgumentMatchers.eq(AriaRole.TEXTBOX), any(Page.GetByRoleOptions.class));
        verify(locator).fill("alice");

        when(locator.isVisible()).thenReturn(true);
        steps.shouldDisplayRoleNamed("button", "Login");
        Assert.assertEquals(((VerifyAction) runner.getTestCase().getTestAction(1)).getCheck(), VerifyAction.Check.VISIBLE);

        when(locator.textContent()).thenReturn("Hello");
        steps.shouldHaveTextRoleNamed("heading", "Welcome", "Hello");
        Assert.assertEquals(((VerifyAction) runner.getTestCase().getTestAction(2)).getCheck(), VerifyAction.Check.TEXT);
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
        Assert.assertNull(freshSteps.browser);
        try {
            freshSteps.click("#login");
            Assert.fail("Expected CitrusRuntimeException naming the available browsers");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("shopBrowser"), "must name available ids: " + e.getMessage());
            Assert.assertTrue(e.getMessage().contains("adminBrowser"), "must name available ids: " + e.getMessage());
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
            Assert.fail("Expected CitrusRuntimeException when no browser is configured");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("No Playwright browser configured"), e.getMessage());
        }
    }

    @Test
    public void testPageAction() {
        steps.setBrowser("playwrightBrowser");
        steps.pageAction(LoginPageStub.class.getName(), "submit");

        TestCase testCase = runner.getTestCase();
        Assert.assertTrue(testCase.getTestAction(0) instanceof PageObjectAction);
        PageObjectAction action = (PageObjectAction) testCase.getTestAction(0);
        Assert.assertEquals(action.getPageType(), LoginPageStub.class);
        Assert.assertEquals(action.getMethod(), "submit");

        Assert.assertEquals(LoginPageStub.invocations, List.of("submit"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPageActionWithArguments() {
        DataTable dataTable = Mockito.mock(DataTable.class);
        when(dataTable.asList(String.class)).thenReturn(List.of("alice"));

        steps.setBrowser("playwrightBrowser");
        steps.pageActionWithArguments(LoginPageStub.class.getName(), "setUserName", dataTable);

        TestCase testCase = runner.getTestCase();
        Assert.assertTrue(testCase.getTestAction(0) instanceof PageObjectAction);
        PageObjectAction action = (PageObjectAction) testCase.getTestAction(0);
        Assert.assertEquals(action.getPageType(), LoginPageStub.class);
        Assert.assertEquals(action.getMethod(), "setUserName");
        Assert.assertEquals(action.getArguments(), new String[]{"alice"});

        Assert.assertEquals(LoginPageStub.lastUserName, "alice");
    }

    @Test
    public void testPageShouldValidate() {
        steps.setBrowser("playwrightBrowser");
        steps.pageShouldValidate(LoginPageStub.class.getName(), LoginPageValidatorStub.class.getName());

        TestCase testCase = runner.getTestCase();
        Assert.assertTrue(testCase.getTestAction(0) instanceof PageObjectAction);
        PageObjectAction action = (PageObjectAction) testCase.getTestAction(0);
        Assert.assertEquals(action.getPageType(), LoginPageStub.class);
        Assert.assertEquals(action.getValidatorType(), LoginPageValidatorStub.class);

        Assert.assertTrue(LoginPageValidatorStub.validated);
    }

    @Test
    public void testPageActionUnknownTypeFails() {
        steps.setBrowser("playwrightBrowser");
        try {
            steps.pageAction("com.example.MissingPage", "submit");
            Assert.fail("Expected CitrusRuntimeException for unknown page type");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("com.example.MissingPage"));
        }
    }

    @Test
    public void testPageValidateInvalidValidatorFails() {
        steps.setBrowser("playwrightBrowser");
        try {
            steps.pageShouldValidate(LoginPageStub.class.getName(), LoginPageStub.class.getName());
            Assert.fail("Expected CitrusRuntimeException for invalid validator type");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains(LoginPageStub.class.getName()));
        }
    }
}
