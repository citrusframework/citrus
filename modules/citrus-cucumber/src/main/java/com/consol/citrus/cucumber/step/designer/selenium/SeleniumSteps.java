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
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.builder.SeleniumActionBuilder;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.model.PageValidator;
import com.consol.citrus.selenium.model.WebPage;
import com.consol.citrus.variable.VariableUtils;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.*;
import io.cucumber.datatable.DataTable;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumSteps {

    @CitrusResource
    protected TestDesigner designer;

    @CitrusFramework
    protected Citrus citrus;

    /** Page objects defined by id */
    private Map<String, WebPage> pages;

    /** Page validators defined by id */
    private Map<String, PageValidator> validators;

    /** Selenium browser */
    protected SeleniumBrowser browser;

    @Before
    public void before(Scenario scenario) {
        if (browser == null && citrus.getApplicationContext().getBeansOfType(SeleniumBrowser.class).size() == 1L) {
            browser = citrus.getApplicationContext().getBean(SeleniumBrowser.class);
        }

        pages = new HashMap<>();
        validators = new HashMap<>();
    }

    @Given("^(?:selenium )?browser \"([^\"]+)\"$")
    public void setBrowser(String id) {
        if (!citrus.getApplicationContext().containsBean(id)) {
            throw new CitrusRuntimeException("Unable to find selenium browser for id: " + id);
        }

        browser = citrus.getApplicationContext().getBean(id, SeleniumBrowser.class);
    }

    @Given("^pages$")
    public void pages(DataTable dataTable) {
        Map<String, String> variables = dataTable.asMap(String.class, String.class);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            page(entry.getKey(), entry.getValue());
        }
    }

    @Given("^page \"([^\"]+)\" ([^\\s]+)$")
    public void page(String id, String type) {
        try {
            pages.put(id, (WebPage) Class.forName(type).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to laod page object", e);
        }
    }

    @Given("^page validators")
    public void page_validators(DataTable dataTable) {
        Map<String, String> variables = dataTable.asMap(String.class, String.class);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            page_validator(entry.getKey(), entry.getValue());
        }
    }

    @Given("^page validator ([^\\s]+) ([^\\s]+)$")
    public void page_validator(String id, String type) {
        try {
            validators.put(id, (PageValidator) Class.forName(type).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to laod page object", e);
        }
    }

    @When("^(?:user )?starts? browser$")
    public void start() {
        designer.selenium().browser(browser)
                .start();
    }

    @When("^(?:user )?stops? browser$")
    public void stop() {
        designer.selenium().browser(browser)
                .stop();
    }

    @When("^(?:user )?navigates? to \"([^\"]+)\"$")
    public void navigate(String url) {
        designer.selenium().browser(browser)
                .navigate(url);
    }

    @When("^(?:user )?clicks? (?:element|button|link) with ([^\"]+)=\"([^\"]+)\"$")
    public void click(String property, String value) {
        designer.selenium().browser(browser)
                .click()
                .element(property, value);
    }

    @When("^(?:user )?(?:sets?|puts?) text \"([^\"]+)\" to (?:element|input|textfield) with ([^\"]+)=\"([^\"]+)\"$")
    public void setInput(String text, String property, String value) {
        designer.selenium().browser(browser)
                .setInput(text)
                .element(property, value);
    }

    @When("^(?:user )?(checks?|unchecks?) checkbox with ([^\"]+)=\"([^\"]+)\"$")
    public void checkInput(String type, String property, String value) {
        designer.selenium().browser(browser)
                .checkInput(type.equals("check") || type.equals("checks"))
                .element(property, value);
    }

    @Then("^(?:page )?should (?:display|have) (?:element|button|link|input|textfield|form|heading) with (id|name|class-name|link-text|css-selector|tag-name|xpath)=\"([^\"]+)\"$")
    public void should_display(String property, String value) {
        designer.selenium().browser(browser)
                .find()
                .enabled(true)
                .displayed(true)
                .element(property, value);
    }

    @Then("^(?:page )?should (?:display|have) (?:element|button|link|input|textfield|form|heading) with (id|name|class-name|link-text|css-selector|tag-name|xpath)=\"([^\"]+)\" having$")
    public void should_display(String property, String value, DataTable dataTable) {
        Map<String, String> elementProperties = dataTable.asMap(String.class, String.class);

        SeleniumActionBuilder.FindElementActionBuilder elementBuilder = designer.selenium().browser(browser)
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
                elementBuilder.enabled(Boolean.valueOf(propertyEntry.getValue()));
            }

            if (propertyEntry.getKey().equals("displayed")) {
                elementBuilder.displayed(Boolean.valueOf(propertyEntry.getValue()));
            }

            if (propertyEntry.getKey().equals("styles") || propertyEntry.getKey().equals("style")) {
                String[] propertyExpressions = StringUtils.delimitedListToStringArray(propertyEntry.getValue(), ";");
                for (String propertyExpression : propertyExpressions) {
                    String[] keyValue = propertyExpression.split("=");
                    elementBuilder.style(keyValue[0].trim(), VariableUtils.cutOffDoubleQuotes(keyValue[1].trim()));
                }
            }

            if (propertyEntry.getKey().equals("attributes") || propertyEntry.getKey().equals("attribute")) {
                String[] propertyExpressions = StringUtils.commaDelimitedListToStringArray(propertyEntry.getValue());
                for (String propertyExpression : propertyExpressions) {
                    String[] keyValue = propertyExpression.split("=");
                    elementBuilder.attribute(keyValue[0].trim(), VariableUtils.cutOffDoubleQuotes(keyValue[1].trim()));
                }
            }
        }
    }

    @When("^(?:page )?([^\\s]+) performs ([^\\s]+)$")
    public void page_action(String pageId, String method) {
        page_action_with_arguments(pageId, method, null);
    }

    @When("^(?:page )?([^\\s]+) performs ([^\\s]+) with arguments$")
    public void page_action_with_arguments(String pageId, String method, DataTable dataTable) {
        verifyPage(pageId);

        List<String> arguments = new ArrayList<>();
        if (dataTable != null) {
            arguments = dataTable.asList(String.class);
        }

        designer.selenium().browser(browser)
                .page(pages.get(pageId))
                .execute(method)
                .arguments(arguments);
    }

    @Then("^(?:page )?([^\\s]+) should validate$")
    public void page_should_validate(String pageId) {
        page_should_validate_with_validator(pageId, null);
    }

    @Then("^(?:page )?([^\\s]+) should validate with ([^\\s]+)$")
    public void page_should_validate_with_validator(String pageId, String validatorId) {
        verifyPage(pageId);

        PageValidator pageValidator = null;
        if (validators.containsKey(validatorId)) {
            pageValidator = validators.get(validatorId);
        }

        designer.selenium().browser(browser)
                .page(pages.get(pageId))
                .validator(pageValidator)
                .validate();
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
