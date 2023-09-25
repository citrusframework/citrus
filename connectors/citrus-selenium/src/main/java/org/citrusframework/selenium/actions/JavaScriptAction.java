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

package org.citrusframework.selenium.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumHeaders;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes javascript code on current page and validates errors.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class JavaScriptAction extends AbstractSeleniumAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JavaScriptAction.class);

    /** JavaScript code */
    private final String script;

    /** Optional arguments */
    private final List<Object> arguments;

    /** JavaScript errors to validate */
    private final List<String> expectedErrors;

    /**
     * Default constructor.
     */
    public JavaScriptAction(Builder builder) {
        super("javascript", builder);

        this.script = builder.script;
        this.arguments = builder.arguments;
        this.expectedErrors = builder.expectedErrors;
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
                logger.warn("Skip javascript action because web driver is missing javascript features");
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
     * Gets the arguments.
     *
     * @return
     */
    public List<Object> getArguments() {
        return arguments;
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
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<JavaScriptAction, Builder> {

        private String script;
        private final List<Object> arguments = new ArrayList<>();
        private final List<String> expectedErrors = new ArrayList<>();

        /**
         * Add script.
         * @param script
         * @return
         */
        public Builder script(String script) {
            this.script = script;
            return this;
        }

        /**
         * Add script arguments.
         * @param args
         * @return
         */
        public Builder arguments(Object... args) {
            return arguments(Arrays.asList(args));
        }

        /**
         * Add script arguments.
         * @param args
         * @return
         */
        public Builder arguments(List<Object> args) {
            this.arguments.addAll(args);
            return this;
        }

        /**
         * Add script argument.
         * @param arg
         * @return
         */
        public Builder argument(Object arg) {
            this.arguments.add(arg);
            return this;
        }

        /**
         * Add expected error.
         * @param errors
         * @return
         */
        public Builder errors(String... errors) {
            return errors(Arrays.asList(errors));
        }

        /**
         * Add expected error.
         * @param errors
         * @return
         */
        public Builder errors(List<String> errors) {
            this.expectedErrors.addAll(errors);
            return this;
        }

        /**
         * Add expected error.
         * @param error
         * @return
         */
        public Builder error(String error) {
            this.expectedErrors.add(error);
            return this;
        }

        @Override
        public JavaScriptAction build() {
            return new JavaScriptAction(this);
        }
    }
}
