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

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightEndpointBuilder;
import org.citrusframework.playwright.model.LocatorSpec;
import org.citrusframework.playwright.page.PlaywrightPageValidator;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.citrusframework.playwright.actions.PlaywrightActionBuilder.playwright;

public class PlaywrightSteps {

    @CitrusResource
    protected TestCaseRunner runner;

    @CitrusFramework
    protected Citrus citrus;

    /** Playwright browser */
    protected PlaywrightBrowser browser;

    /**
     * Resolves the browser for the scenario before any step runs.
     *
     * <p>Resolution order: the single configured {@link PlaywrightBrowser} bean
     * wins, else the bean named by {@code citrus.playwright.browser.name}, else
     * a locally built default when nothing is configured. No default is
     * auto-created while any bean exists — with several named browsers and no
     * default, select one explicitly with {@code Playwright browser "<id>"};
     * any other step then fails fast via {@link #requireBrowser()} instead of
     * running against an arbitrary bean.</p>
     *
     * <p>Step phrases are namespaced with {@code playwright} where the
     * Selenium module defines an identical expression, so both glues can share
     * a classpath (e.g. via {@code citrus-cucumber-all}) without duplicate
     * step definitions.</p>
     */
    @Before
    public void before() {
        // Settings are read live so properties set by earlier hooks still apply.
        String browserType = PlaywrightSettings.getBrowserType();
        boolean headless = PlaywrightSettings.isHeadless();
        String browserName = PlaywrightSettings.getBrowserName();

        browser = null;
        Map<String, PlaywrightBrowser> browsers =
                citrus.getCitrusContext().getReferenceResolver().resolveAll(PlaywrightBrowser.class);
        if (browsers.size() == 1) {
            browser = citrus.getCitrusContext().getReferenceResolver().resolve(PlaywrightBrowser.class);
        } else if (citrus.getCitrusContext().getReferenceResolver().isResolvable(browserName, PlaywrightBrowser.class)) {
            browser = citrus.getCitrusContext().getReferenceResolver().resolve(browserName, PlaywrightBrowser.class);
        } else if (browsers.isEmpty()
                && !citrus.getCitrusContext().getReferenceResolver().isResolvable(browserName)) {
            PlaywrightEndpointBuilder builder = new PlaywrightEndpointBuilder()
                    .browserType(browserType)
                    .headless(headless)
                    .defaultTimeout(PlaywrightSettings.getDefaultTimeoutMs());
            String baseUrl = PlaywrightSettings.getBaseUrl();
            if (StringUtils.hasText(baseUrl)) {
                builder.baseUrl(baseUrl);
            }

            browser = builder.build();
            citrus.getCitrusContext().bind(browserName, browser);
        }
    }

    @Given("^(?:Playwright|playwright) browser \"([^\"]+)\"$")
    public void setBrowser(String id) {
        if (!citrus.getCitrusContext().getReferenceResolver().isResolvable(id, PlaywrightBrowser.class)) {
            throw new CitrusRuntimeException("Unable to find playwright browser for id: " + id);
        }

        browser = citrus.getCitrusContext().getReferenceResolver().resolve(id, PlaywrightBrowser.class);
    }

    @When("^start playwright browser$")
    public void start() {
        runner.run(playwright().browser(requireBrowser())
                .start());
    }

    @When("^stop playwright browser$")
    public void stop() {
        runner.run(playwright().browser(requireBrowser())
                .stop());
    }

    @When("^(?:User|user) opens \"([^\"]+)\"$")
    public void open(String url) {
        runner.run(playwright().browser(requireBrowser())
                .open()
                .url(url));
    }

    @When("^(?:User|user) clicks \"([^\"]+)\"$")
    public void click(String selector) {
        runner.run(playwright().browser(requireBrowser())
                .click()
                .locator(selector));
    }

    @When("^(?:User|user) clicks (css|xpath|text|role|test-id) \"([^\"]+)\"$")
    public void clickTyped(String type, String selector) {
        runner.run(playwright().browser(requireBrowser())
                .click()
                .locator(toLocatorSpec(type, selector)));
    }

    @When("^(?:User|user) clicks role \"([^\"]+)\" named \"([^\"]+)\"$")
    public void clickRoleNamed(String role, String name) {
        runner.run(playwright().browser(requireBrowser())
                .click()
                .locator(LocatorSpec.role(role).name(name)));
    }

    @When("^(?:User|user) (?:fills|enters text) \"([^\"]+)\" to \"([^\"]+)\"$")
    public void fill(String text, String selector) {
        runner.run(playwright().browser(requireBrowser())
                .fill()
                .locator(selector)
                .value(text));
    }

    @When("^(?:User|user) (?:fills|enters text) \"([^\"]+)\" to (css|xpath|text|role|test-id) \"([^\"]+)\"$")
    public void fillTyped(String text, String type, String selector) {
        runner.run(playwright().browser(requireBrowser())
                .fill()
                .locator(toLocatorSpec(type, selector))
                .value(text));
    }

    @When("^(?:User|user) (?:fills|enters text) \"([^\"]+)\" to role \"([^\"]+)\" named \"([^\"]+)\"$")
    public void fillRoleNamed(String text, String role, String name) {
        runner.run(playwright().browser(requireBrowser())
                .fill()
                .locator(LocatorSpec.role(role).name(name))
                .value(text));
    }

    @When("^(?:User|user) clears \"([^\"]+)\"$")
    public void clear(String selector) {
        runner.run(playwright().browser(requireBrowser())
                .clear()
                .locator(selector));
    }

    @When("^(?:User|user) clears (css|xpath|text|role|test-id) \"([^\"]+)\"$")
    public void clearTyped(String type, String selector) {
        runner.run(playwright().browser(requireBrowser())
                .clear()
                .locator(toLocatorSpec(type, selector)));
    }

    @When("^(?:User|user) presses key \"([^\"]+)\" on \"([^\"]+)\"$")
    public void press(String key, String selector) {
        runner.run(playwright().browser(requireBrowser())
                .press()
                .locator(selector)
                .value(key));
    }

    @When("^(?:User|user) presses key \"([^\"]+)\" on (css|xpath|text|role|test-id) \"([^\"]+)\"$")
    public void pressTyped(String key, String type, String selector) {
        runner.run(playwright().browser(requireBrowser())
                .press()
                .locator(toLocatorSpec(type, selector))
                .value(key));
    }

    @When("^(?:User|user) (checks|unchecks) \"([^\"]+)\"$")
    public void checkInput(String check, String selector) {
        if ("checks".equals(check)) {
            runner.run(playwright().browser(requireBrowser())
                    .check()
                    .locator(selector));
        } else {
            runner.run(playwright().browser(requireBrowser())
                    .uncheck()
                    .locator(selector));
        }
    }

    @When("^(?:User|user) (checks|unchecks) (css|xpath|text|role|test-id) \"([^\"]+)\"$")
    public void checkInputTyped(String check, String type, String selector) {
        if ("checks".equals(check)) {
            runner.run(playwright().browser(requireBrowser())
                    .check()
                    .locator(toLocatorSpec(type, selector)));
        } else {
            runner.run(playwright().browser(requireBrowser())
                    .uncheck()
                    .locator(toLocatorSpec(type, selector)));
        }
    }

    @When("^(?:User|user) selects option \"([^\"]+)\" on \"([^\"]+)\"$")
    public void select(String option, String selector) {
        runner.run(playwright().browser(requireBrowser())
                .select()
                .locator(selector)
                .value(option));
    }

    @When("^(?:User|user) selects option \"([^\"]+)\" on (css|xpath|text|role|test-id) \"([^\"]+)\"$")
    public void selectTyped(String option, String type, String selector) {
        runner.run(playwright().browser(requireBrowser())
                .select()
                .locator(toLocatorSpec(type, selector))
                .value(option));
    }

    @When("^(?:User|user) hovers over \"([^\"]+)\"$")
    public void hover(String selector) {
        runner.run(playwright().browser(requireBrowser())
                .hover()
                .locator(selector));
    }

    @When("^(?:User|user) hovers over (css|xpath|text|role|test-id) \"([^\"]+)\"$")
    public void hoverTyped(String type, String selector) {
        runner.run(playwright().browser(requireBrowser())
                .hover()
                .locator(toLocatorSpec(type, selector)));
    }

    @Then("^(?:Browser|browser) should display element \"([^\"]+)\"$")
    public void shouldDisplay(String selector) {
        runner.run(playwright().browser(requireBrowser())
                .verify()
                .locator(selector)
                .visible());
    }

    @Then("^(?:Browser|browser) should display element (css|xpath|text|role|test-id) \"([^\"]+)\"$")
    public void shouldDisplayTyped(String type, String selector) {
        runner.run(playwright().browser(requireBrowser())
                .verify()
                .locator(toLocatorSpec(type, selector))
                .visible());
    }

    @Then("^(?:Browser|browser) should display element role \"([^\"]+)\" named \"([^\"]+)\"$")
    public void shouldDisplayRoleNamed(String role, String name) {
        runner.run(playwright().browser(requireBrowser())
                .verify()
                .locator(LocatorSpec.role(role).name(name))
                .visible());
    }

    @Then("^(?:Element|element) \"([^\"]+)\" should have text \"([^\"]+)\"$")
    public void shouldHaveText(String selector, String text) {
        runner.run(playwright().browser(requireBrowser())
                .verify()
                .locator(selector)
                .text(text));
    }

    @Then("^(?:Element|element) (css|xpath|text|role|test-id) \"([^\"]+)\" should have text \"([^\"]+)\"$")
    public void shouldHaveTextTyped(String type, String selector, String text) {
        runner.run(playwright().browser(requireBrowser())
                .verify()
                .locator(toLocatorSpec(type, selector))
                .text(text));
    }

    @Then("^(?:Element|element) role \"([^\"]+)\" named \"([^\"]+)\" should have text \"([^\"]+)\"$")
    public void shouldHaveTextRoleNamed(String role, String name, String text) {
        runner.run(playwright().browser(requireBrowser())
                .verify()
                .locator(LocatorSpec.role(role).name(name))
                .text(text));
    }

    @Then("^(?:Page|page) title should be \"([^\"]+)\"$")
    public void pageTitleShouldBe(String title) {
        runner.run(playwright().browser(requireBrowser())
                .verify()
                .title(title));
    }

    @Then("^(?:Page|page) url should be \"([^\"]+)\"$")
    public void pageUrlShouldBe(String url) {
        runner.run(playwright().browser(requireBrowser())
                .verify()
                .url(url));
    }

    @When("^(?:User|user) (accepts|dismisses) dialog$")
    public void handleDialog(String type) {
        if ("accepts".equals(type)) {
            runner.run(playwright().browser(requireBrowser())
                    .dialog()
                    .accept());
        } else {
            runner.run(playwright().browser(requireBrowser())
                    .dialog()
                    .dismiss());
        }
    }

    @When("^(?:Take|take) screenshot \"([^\"]+)\"$")
    public void screenshot(String path) {
        runner.run(playwright().browser(requireBrowser())
                .screenshot()
                .path(path));
    }

    /**
     * Invokes a method on a page-object class, instantiated per action.
     * Any class with a no-arg (or injectable {@code Page}/{@code BrowserContext}/
     * {@code PlaywrightBrowser}/{@code TestContext}) constructor works; methods
     * may take {@code String} arguments plus injectable parameters.
     */
    @When("^playwright page ([^\\s]+) performs ([^\\s]+)$")
    public void pageAction(String pageType, String method) {
        pageActionWithArguments(pageType, method, null);
    }

    @When("^playwright page ([^\\s]+) performs ([^\\s]+) with arguments$")
    public void pageActionWithArguments(String pageType, String method, DataTable dataTable) {
        List<String> arguments = List.of();
        if (dataTable != null) {
            arguments = dataTable.asList(String.class);
        }

        runner.run(playwright().browser(requireBrowser())
                .pageObject()
                .type(loadPageType(pageType, "page object"))
                .execute(method)
                .arguments(arguments.toArray(new String[0])));
    }

    /**
     * Validates a page object with a {@link PlaywrightPageValidator}. The page
     * type should implement {@code PlaywrightPage}; the validator must
     * implement {@link PlaywrightPageValidator} and provide a no-arg constructor.
     */
    @Then("^playwright page ([^\\s]+) should validate with ([^\\s]+)$")
    public void pageShouldValidate(String pageType, String validatorType) {
        runner.run(playwright().browser(requireBrowser())
                .pageObject()
                .type(loadPageType(pageType, "page object"))
                .validate(loadValidatorType(validatorType)));
    }

    /**
     * Returns the scenario browser, failing fast when none was resolved.
     *
     * <p>This happens only with several named browsers and no default: running
     * against an arbitrary bean would be non-deterministic, so the step names
     * the available ids instead.</p>
     *
     * @return active browser endpoint, never null
     */
    private PlaywrightBrowser requireBrowser() {
        if (browser != null) {
            return browser;
        }

        String browserName = PlaywrightSettings.getBrowserName();
        Map<String, PlaywrightBrowser> candidates =
                new TreeMap<>(citrus.getCitrusContext().getReferenceResolver().resolveAll(PlaywrightBrowser.class));
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(browserName)) {
            throw new CitrusRuntimeException(
                    "Bean '%s' is configured but is not a PlaywrightBrowser - check citrus.playwright.browser.name".formatted(browserName));
        }
        if (candidates.isEmpty()) {
            throw new CitrusRuntimeException(
                    "No Playwright browser configured - declare a PlaywrightBrowser bean or rely on the @Before hook to build the default");
        }

        throw new CitrusRuntimeException(
                "No Playwright browser selected - use Playwright browser \"<id>\" to select one of %s".formatted(candidates.keySet()));
    }

    /**
     * Maps a Gherkin locator type to its {@link LocatorSpec}.
     * @param type one of css, xpath, text, role, test-id
     * @param selector selector value
     * @return locator specification
     */
    private LocatorSpec toLocatorSpec(String type, String selector) {
        return switch (type.toLowerCase()) {
            case "css" -> LocatorSpec.css(selector);
            case "xpath" -> LocatorSpec.xpath(selector);
            case "text" -> LocatorSpec.text(selector);
            case "role" -> LocatorSpec.role(selector);
            case "test-id" -> LocatorSpec.testId(selector);
            default -> throw new CitrusRuntimeException("Unsupported locator type: " + type);
        };
    }

    /**
     * Loads a page-object class by name.
     * @param type fully qualified class name
     * @param kind human-readable kind used in error messages (page object, page validator)
     * @return page-object class
     */
    private Class<?> loadPageType(String type, String kind) {
        try {
            return ClassLoaderHelper.getClassLoader(PlaywrightSteps.class).loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to load %s type: %s".formatted(kind, type), e);
        }
    }

    /**
     * Loads a page validator class by name.
     * @param type fully qualified class name
     * @return validator class
     */
    @SuppressWarnings("unchecked")
    private Class<? extends PlaywrightPageValidator<?>> loadValidatorType(String type) {
        Class<?> validatorClass = loadPageType(type, "page validator");
        if (!PlaywrightPageValidator.class.isAssignableFrom(validatorClass)) {
            throw new CitrusRuntimeException("Invalid page validator type, expected a PlaywrightPageValidator, but got: " + type);
        }
        return (Class<? extends PlaywrightPageValidator<?>>) validatorClass;
    }
}
