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

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumSteps {

    @CitrusResource
    protected TestRunner runner;

    @CitrusFramework
    protected Citrus citrus;

    /** Selenium browser */
    protected SeleniumBrowser browser;

    @Before
    public void before(Scenario scenario) {
        if (browser == null && citrus.getApplicationContext().getBeansOfType(SeleniumBrowser.class).size() == 1L) {
            browser = citrus.getApplicationContext().getBean(SeleniumBrowser.class);
        }
    }

    @Given("^(?:selenium )?browser \"([^\"]+)\"$")
    public void setBrowser(String id) {
        if (!citrus.getApplicationContext().containsBean(id)) {
            throw new CitrusRuntimeException("Unable to find selenium browser for id: " + id);
        }

        browser = citrus.getApplicationContext().getBean(id, SeleniumBrowser.class);
    }

    @When("^(?:user )?starts? browser$")
    public void start() {
        runner.selenium(builder -> builder.browser(browser)
                .start());
    }

    @When("^(?:user )?stops? browser$")
    public void stop() {
        runner.selenium(builder -> builder.browser(browser)
                .stop());
    }

    @When("^(?:user )?navigates? to \"([^\"]+)\"$")
    public void navigate(String url) {
        runner.selenium(builder -> builder.browser(browser)
                .navigate(url));
    }

    @When("^(?:user )?clicks? (?:element|button|link) with ([^\"]+)=\"([^\"]+)\"$")
    public void click(String property, String value) {
        runner.selenium(builder -> builder.browser(browser)
                .click()
                .element(property, value));
    }

    @When("^(?:user )?(?:sets?|puts?) text \"([^\"]+)\" to (?:element|input|textfield) with ([^\"]+)=\"([^\"]+)\"$")
    public void setInput(String text, String property, String value) {
        runner.selenium(builder -> builder.browser(browser)
                .setInput(text)
                .element(property, value));
    }

    @When("^(?:user )?(checks?|unchecks?) checkbox with ([^\"]+)=\"([^\"]+)\"$")
    public void checkInput(String action, String property, String value) {
        runner.selenium(builder -> builder.browser(browser)
                .checkInput(action.equals("check") || action.equals("checks"))
                .element(property, value));
    }

    @Then("^(?:page )?should (?:display|have) (?:element|button|link|input|textfield|form|heading) with (id|name|class-name|link-text|css-selector|tag-name|xpath)=\"([^\"]+)\"$")
    public void should_display(String property, String value) {
        runner.selenium(builder -> builder.browser(browser)
                .find()
                .enabled(true)
                .displayed(true)
                .element(property, value));
    }
}
