/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.selenium.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumHeaders;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes javascript code on current page and validates errors.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class JavaScriptAction extends AbstractSeleniumAction {

    /** JavaScript code */
    private String script;

    /** Optional arguments */
    private List<Object> arguments = new ArrayList<>();

    /** JavaScript errors to validate */
    List<String> expectedErrors = new ArrayList<>();

    /**
     * Default constructor.
     */
    public JavaScriptAction() {
        super("javascript");
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        try {
            if (browser.getWebDriver() instanceof JavascriptExecutor) {
                JavascriptExecutor jsEngine = ((JavascriptExecutor) browser.getWebDriver());
                jsEngine.executeScript(context.replaceDynamicContentInString(script), context.resolveDynamicValuesInArray(arguments.toArray()));

                List<String> errors = new ArrayList<>();
                List<Object> errorObjects = (List<Object>) jsEngine.executeScript("return window._selenide_jsErrors", new Object[]{});
                if (errorObjects != null) {
                    for (Object error : errorObjects) {
                        errors.add(error.toString());
                    }
                }

                context.setVariable(SeleniumHeaders.SELENIUM_JS_ERRORS, errors);

                for (String expected : expectedErrors) {
                    if (!errors.contains(expected)) {
                        throw new ValidationException("Missing JavaScript error " + expected);
                    }
                }
            } else {
                log.warn("Skip javascript action because web driver is missing javascript features");
            }
        } catch (WebDriverException e) {
            throw new CitrusRuntimeException("Failed to execute JavaScript code", e);
        }
    }

    /**
     * Gets the script.
     *
     * @return
     */
    public String getScript() {
        return script;
    }

    /**
     * Sets the script.
     *
     * @param script
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * Gets the arguments.
     *
     * @return
     */
    public List<Object> getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments.
     *
     * @param arguments
     */
    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    /**
     * Gets the expectedErrors.
     *
     * @return
     */
    public List<String> getExpectedErrors() {
        return expectedErrors;
    }

    /**
     * Sets the expectedErrors.
     *
     * @param expectedErrors
     */
    public void setExpectedErrors(List<String> expectedErrors) {
        this.expectedErrors = expectedErrors;
    }
}
