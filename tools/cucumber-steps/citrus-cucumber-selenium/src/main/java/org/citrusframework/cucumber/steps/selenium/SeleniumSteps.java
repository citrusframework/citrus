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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.cucumber.steps.CucumberStepsSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.actions.FindElementAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumBrowserBuilder;
import org.citrusframework.selenium.model.PageValidator;
import org.citrusframework.selenium.model.WebPage;
import org.citrusframework.variable.VariableUtils;

import static org.citrusframework.selenium.actions.SeleniumActionBuilder.selenium;

public class SeleniumSteps {

    @CitrusResource
    protected TestCaseRunner runner;

    @CitrusFramework
    protected Citrus citrus;

    /** Page objects defined by id */
    private Map<String, WebPage> pages;

    /** Page validators defined by id */
    private Map<String, PageValidator<?>> validators;

    private final String browserType = SeleniumSettings.getBrowserType();
    private final String browserName = SeleniumSettings.getBrowserName();
    private final String browserRemoteServerUrl = SeleniumSettings.getBrowserRemoteServerUrl();

    /** Selenium browser */
    protected SeleniumBrowser browser;

    @Before
    public void before() {
        if (browser == null && citrus.getCitrusContext().getReferenceResolver().resolveAll(SeleniumBrowser.class).size() == 1L) {
            browser = citrus.getCitrusContext().getReferenceResolver().resolve(SeleniumBrowser.class);
        } else if (citrus.getCitrusContext().getReferenceResolver().isResolvable(browserName)) {
            browser = citrus.getCitrusContext().getReferenceResolver().resolve(browserName, SeleniumBrowser.class);
        } else {
            if (CucumberStepsSettings.isLocal()) {
                browser = new SeleniumBrowserBuilder()
                        .type(browserType)
                        .build();
            } else {
                browser = new SeleniumBrowserBuilder()
                        .type(browserType)
                        .remoteServer(browserRemoteServerUrl)
                        .build();
            }

            citrus.getCitrusContext().bind(browserName, browser);
        }

        pages = new HashMap<>();
        validators = new HashMap<>();
    }

    @Given("^(?:Browser|browser) \"([^\"]+)\"$")
    public void setBrowser(String id) {
        if (!citrus.getCitrusContext().getReferenceResolver().isResolvable(id)) {
            throw new CitrusRuntimeException("Unable to find selenium browser for id: " + id);
        }

        browser = citrus.getCitrusContext().getReferenceResolver().resolve(id, SeleniumBrowser.class);
    }

    @When("^start browser$")
    public void start() {
        runner.run(selenium().browser(browser)
                .start());
    }

    @When("^stop browser$")
    public void stop() {
        runner.run(selenium().browser(browser)
                .stop());
    }

    @When("^(?:User|user) navigates to \"([^\"]+)\"$")
    public void navigate(String url) {
        runner.run(selenium().browser(browser)
                .navigate(url));
    }

    @When("^(?:User|user) clicks (?:element|button|link) with ([^\"]+)=\"([^\"]+)\"$")
    public void click(String property, String value) {
        runner.run(selenium().browser(browser)
                .click()
                .element(property, value));
    }

    @When("^(?:User|user) enters text \"([^\"]+)\" to (?:element|input|textfield) with ([^\"]+)=\"([^\"]+)\"$")
    public void setInput(String text, String property, String value) {
        runner.run(selenium().browser(browser)
                .setInput(text)
                .element(property, value));
    }

    @When("^(?:User|user) (checks|unchecks) checkbox with ([^\"]+)=\"([^\"]+)\"$")
    public void checkInput(String type, String property, String value) {
        runner.run(selenium().browser(browser)
                .checkInput(type.equals("checks"))
                .element(property, value));
    }

    @When("^(?:User|user) selects option \"([^\"]+)\" on (?:element|dropdown) with ([^\"]+)=\"([^\"]+)\"$")
    public void select(String option, String property, String value) {
        runner.run(selenium().browser(browser)
                .select(option)
                .element(property, value));
    }

    @When("^(?:User|user) (accepts|dismisses) alert$")
    public void acceptAlert(String type) {
        if (type.equals("accepts")) {
            runner.run(selenium().browser(browser)
                    .alert()
                    .accept());
        } else {
            runner.run(selenium().browser(browser)
                    .alert()
                    .dismiss());
        }
    }

    @When("^(?:Browser|browser) page should display alert with text \"([^\"]+)\"$")
    public void shouldDisplayAlert(String text) {
        runner.run(selenium().browser(browser)
                .alert()
                .text(text));
    }

    @Then("^(?:Browser|browser) page should display (?:element|button|link|input|textfield|form|heading) with (id|name|class-name|link-text|css-selector|tag-name|xpath)=\"([^\"]+)\"$")
    public void shouldDisplay(String property, String value) {
        runner.run(selenium().browser(browser)
                .find()
                .enabled(true)
                .displayed(true)
                .element(property, value));
    }

    @Then("^(?:Browser|browser) page should display (?:element|button|link|input|textfield|form|heading) with (id|name|class-name|link-text|css-selector|tag-name|xpath)=\"([^\"]+)\" having$")
    public void shouldDisplay(String property, String value, DataTable dataTable) {
        Map<String, String> elementProperties = dataTable.asMap(String.class, String.class);

        FindElementAction.Builder elementBuilder = selenium().browser(browser)
                .find()
                .element(property, value);

        for (Map.Entry<String, String> propertyEntry : elementProperties.entrySet()) {
            if (propertyEntry.getKey().equals("tag-name")) {
                elementBuilder.tagName(propertyEntry.getValue());
            }

            if (propertyEntry.getKey().equals("text")) {
                elementBuilder.text(propertyEntry.getValue());
            }

            if (propertyEntry.getKey().equals("enabled")) {
                elementBuilder.enabled(Boolean.parseBoolean(propertyEntry.getValue()));
            }

            if (propertyEntry.getKey().equals("displayed")) {
                elementBuilder.displayed(Boolean.parseBoolean(propertyEntry.getValue()));
            }

            if (propertyEntry.getKey().equals("styles") || propertyEntry.getKey().equals("style")) {
                String[] propertyExpressions = propertyEntry.getValue().split(";");
                for (String propertyExpression : propertyExpressions) {
                    String[] keyValue = propertyExpression.split("=");
                    elementBuilder.style(keyValue[0].trim(), VariableUtils.cutOffDoubleQuotes(keyValue[1].trim()));
                }
            }

            if (propertyEntry.getKey().equals("attributes") || propertyEntry.getKey().equals("attribute")) {
                String[] propertyExpressions = propertyEntry.getValue().split(",");
                for (String propertyExpression : propertyExpressions) {
                    String[] keyValue = propertyExpression.split("=");
                    elementBuilder.attribute(keyValue[0].trim(), VariableUtils.cutOffDoubleQuotes(keyValue[1].trim()));
                }
            }
        }

        runner.run(elementBuilder);
    }

    @Given("^(?:Browser|browser) page types$")
    public void pages(DataTable dataTable) {
        Map<String, String> variables = dataTable.asMap(String.class, String.class);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            pageByType(entry.getKey(), entry.getValue());
        }
    }

    @Given("^(?:Browser|browser) page \"([^\"]+)\" of type ([^\\s]+)$")
    public void pageByType(String id, String type) {
        try {
            Object page = Class.forName(type).newInstance();
            pages.put(id, (WebPage) page);

            if (page instanceof PageValidator) {
                validators.put(id, (PageValidator<?>) page);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to load page object", e);
        }
    }

    @Given("^(?:Browser|browser) page \"([^\"]+)\"$")
    public void page(String id) {
        if (!citrus.getCitrusContext().getReferenceResolver().isResolvable(id, WebPage.class)) {
            throw new CitrusRuntimeException("Unable to find page for id: " + id);
        }

        WebPage page = citrus.getCitrusContext().getReferenceResolver().resolve(id, WebPage.class);
        pages.put(id, page);

        if (page instanceof PageValidator) {
            validators.put(id, (PageValidator<?>) page);
        }
    }

    @When("^(?:Browser|browser) page ([^\\s]+) performs ([^\\s]+)$")
    public void pageAction(String pageId, String method) {
        pageActionWithArguments(pageId, method, null);
    }

    @When("^(?:Browser|browser) page ([^\\s]+) performs ([^\\s]+) with arguments$")
    public void pageActionWithArguments(String pageId, String method, DataTable dataTable) {
        verifyPage(pageId);

        List<String> arguments = new ArrayList<>();
        if (dataTable != null) {
            arguments = dataTable.asList(String.class);
        }

        runner.run(selenium().browser(browser)
                .page(pages.get(pageId))
                .execute(method)
                .arguments(arguments));
    }

    @Given("^(?:Browser|browser) page validator \"([^\"]+)\"$")
    public void pageValidator(String id) {
        if (!citrus.getCitrusContext().getReferenceResolver().isResolvable(id, PageValidator.class)) {
            throw new CitrusRuntimeException("Unable to find page validator for id: " + id);
        }

        validators.put(id, citrus.getCitrusContext().getReferenceResolver().resolve(id, PageValidator.class));
    }

    @Given("^(?:Browser|browser) page validator types$")
    public void pageValidators(DataTable dataTable) {
        Map<String, String> variables = dataTable.asMap(String.class, String.class);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            pageValidatorByType(entry.getKey(), entry.getValue());
        }
    }

    @Given("^(?:Browser|browser) page validator ([^\\s]+) of type ([^\\s]+)$")
    public void pageValidatorByType(String id, String type) {
        try {
            validators.put(id, (PageValidator<?>) Class.forName(type).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to load page object", e);
        }
    }

    @Then("^(?:Browser|browser) page ([^\\s]+) should validate$")
    public void pageShouldValidate(String pageId) {
        pageShouldValidateWithValidator(pageId, pageId);
    }

    @Then("^(?:Browser|browser) page ([^\\s]+) should validate with ([^\\s]+)$")
    public void pageShouldValidateWithValidator(String pageId, String validatorId) {
        verifyPage(pageId);

        PageValidator<?> pageValidator = null;
        if (validators.containsKey(validatorId)) {
            pageValidator = validators.get(validatorId);
        }

        runner.run(selenium().browser(browser)
                .page(pages.get(pageId))
                .validator(pageValidator)
                .validate());
    }

    /**
     * Verify that page is known.
     * @param pageId
     */
    private void verifyPage(String pageId) {
        if (!pages.containsKey(pageId)) {
            throw new CitrusRuntimeException(String.format("Unknown page '%s' - please introduce page with type information first", pageId));
        }
    }
}
