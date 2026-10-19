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

package org.citrusframework.selenium.actions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.api.actions.selenium.SeleniumJavaScriptActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumHeaders;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes javascript code on current page and validates errors.
 *
 * <p>Before the script gets executed an error collector is installed on the page that records
 * JavaScript errors in the
 * {@code window._selenide_jsErrors} list. This way errors raised by page scripts are available
 * for validation with the expected error messages.</p>
 *
 * @since 2.7
 */
public class JavaScriptAction extends AbstractSeleniumAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JavaScriptAction.class);

    /** Installs the JavaScript error collector on the current page */
    static final String ERROR_COLLECTOR_SCRIPT = """
            if (!window._selenide_collectorInstalled) {
                window._selenide_collectorInstalled = true;
                if (!Array.isArray(window._selenide_jsErrors)) {
                    window._selenide_jsErrors = [];
                }
                window.addEventListener('error', function(event) {
                    window._selenide_jsErrors.push(event.message || String(event));
                });
            }
            """;

    /** JavaScript code */
    private final String script;

    /** Optional arguments */
    private final List<Object> arguments;

    /** JavaScript errors to validate */
    private final List<String> expectedErrors;

    /** Optional variable to store the script return value */
    private final String variable;

    /**
     * Default constructor.
     */
    public JavaScriptAction(Builder builder) {
        super("javascript", builder);

        this.script = builder.script;
        this.arguments = builder.arguments;
        this.expectedErrors = builder.expectedErrors;
        this.variable = builder.variable;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void execute(SeleniumBrowser browser, TestContext context) {
        try {
            if (browser.getWebDriver() instanceof JavascriptExecutor jsEngine) {
                jsEngine.executeScript(ERROR_COLLECTOR_SCRIPT, new Object[]{});
                Object result = jsEngine.executeScript(context.replaceDynamicContentInString(script), context.resolveDynamicValuesInArray(arguments.toArray()));

                if (variable != null) {
                    context.setVariable(variable, result != null ? String.valueOf(result) : "");
                }

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
     * Gets the variable.
     *
     * @return
     */
    public String getVariable() {
        return variable;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<JavaScriptAction, Builder>
            implements SeleniumJavaScriptActionBuilder<JavaScriptAction, Builder> {

        private String script;
        private final List<Object> arguments = new ArrayList<>();
        private final List<String> expectedErrors = new ArrayList<>();
        private String variable;

        @Override
        public Builder script(String script) {
            this.script = script;
            return this;
        }

        @Override
        public Builder script(Resource resource) {
            return script(resource, FileUtils.getDefaultCharset());
        }

        @Override
        public Builder script(Resource resource, Charset charset) {
            try {
                return script(FileUtils.readToString(resource, charset));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read script resource", e);
            }
        }

        @Override
        public Builder arguments(Object... args) {
            return arguments(Arrays.asList(args));
        }

        @Override
        public Builder arguments(List<Object> args) {
            this.arguments.addAll(args);
            return this;
        }

        @Override
        public Builder argument(Object arg) {
            this.arguments.add(arg);
            return this;
        }

        @Override
        public Builder errors(String... errors) {
            return errors(Arrays.asList(errors));
        }

        @Override
        public Builder errors(List<String> errors) {
            this.expectedErrors.addAll(errors);
            return this;
        }

        @Override
        public Builder error(String error) {
            this.expectedErrors.add(error);
            return this;
        }

        @Override
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public JavaScriptAction build() {
            return new JavaScriptAction(this);
        }
    }
}
