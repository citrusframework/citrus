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

import java.time.Duration;

import org.citrusframework.api.actions.selenium.SeleniumWaitUntilActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Waits for the element to meet a condition. The element is looked up while waiting so the wait
 * action is able to wait for elements that appear in the DOM tree later on. Supported conditions
 * are {@code visible}, {@code hidden}, {@code present}, {@code absent}, {@code clickable},
 * {@code text}, {@code url} and {@code title}.
 *
 * @since 2.7
 */
public class WaitUntilAction extends FindElementAction {

    /** Wait timeout */
    private final Long timeout;

    /** Wait condition on element */
    private final String condition;

    /** Expected value used by wait conditions such as text, url and title */
    private final String expected;

    /**
     * Default constructor.
     */
    public WaitUntilAction(Builder builder) {
        super("wait", builder);

        this.timeout = builder.timeout;
        this.condition = builder.condition;
        this.expected = builder.expected;
    }

    @Override
    protected boolean requiresElementBeforeExecute() {
        return false;
    }

    @Override
    protected void executeWithoutElement(By by, SeleniumBrowser browser, TestContext context) {
        WebDriverWait wait = new WebDriverWait(browser.getWebDriver(), Duration.ofMillis(timeout));

        String resolvedExpected = context.replaceDynamicContentInString(expected);

        switch (condition != null ? condition : "") {
            case "visible" -> wait.until(ExpectedConditions.visibilityOfElementLocated(requireElement(by, condition)));
            case "hidden" -> wait.until(ExpectedConditions.invisibilityOfElementLocated(requireElement(by, condition)));
            case "present" -> wait.until(ExpectedConditions.presenceOfElementLocated(requireElement(by, condition)));
            case "absent" -> wait.until(ExpectedConditions.numberOfElementsToBe(requireElement(by, condition), 0));
            case "clickable" -> wait.until(ExpectedConditions.elementToBeClickable(requireElement(by, condition)));
            case "text" -> wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    requireElement(by, condition), requireExpected(resolvedExpected, condition)));
            case "url" -> wait.until(ExpectedConditions.urlContains(requireExpected(resolvedExpected, condition)));
            case "title" -> wait.until(ExpectedConditions.titleContains(requireExpected(resolvedExpected, condition)));
            default -> throw new CitrusRuntimeException(String.format(
                    "Unsupported wait condition '%s' - use one of visible, hidden, present, absent, clickable, text, url, title",
                    condition));
        }
    }

    /**
     * Checks that the selector is set on this action for wait conditions that require an element.
     * @param by
     * @param condition
     * @return
     */
    private By requireElement(By by, String condition) {
        if (by == null) {
            throw new CitrusRuntimeException(String.format("Missing element selector for wait condition '%s'", condition));
        }

        return by;
    }

    /**
     * Checks that the expected value is set for wait conditions that compare against an expected value.
     * @param resolvedExpected
     * @param condition
     * @return
     */
    private String requireExpected(String resolvedExpected, String condition) {
        if (!StringUtils.hasText(resolvedExpected)) {
            throw new CitrusRuntimeException(String.format("Missing expected value for wait condition '%s'", condition));
        }

        return resolvedExpected;
    }

    /**
     * Gets the timeout.
     *
     * @return
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * Gets the condition.
     *
     * @return
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Gets the expected value.
     *
     * @return
     */
    public String getExpected() {
        return expected;
    }

    /**
     * Action builder.
     */
    public static class Builder extends ElementActionBuilder<WaitUntilAction, Builder>
            implements SeleniumWaitUntilActionBuilder<WaitUntilAction, Builder> {

        private Long timeout = 5000L;
        private String condition;
        private String expected;

        @Override
        public Builder visible() {
            condition("visible");
            return this;
        }

        @Override
        public Builder hidden() {
            condition("hidden");
            return this;
        }

        @Override
        public Builder condition(String condition) {
            this.condition = condition;
            return this;
        }

        @Override
        public Builder present() {
            condition("present");
            return this;
        }

        @Override
        public Builder absent() {
            condition("absent");
            return this;
        }

        @Override
        public Builder clickable() {
            condition("clickable");
            return this;
        }

        @Override
        public Builder text(String expected) {
            this.condition = "text";
            this.expected = expected;
            return this;
        }

        @Override
        public Builder url(String expected) {
            this.condition = "url";
            this.expected = expected;
            return this;
        }

        @Override
        public Builder title(String expected) {
            this.condition = "title";
            this.expected = expected;
            return this;
        }

        @Override
        public Builder expected(String expected) {
            this.expected = expected;
            return this;
        }

        @Override
        public Builder timeout(Long timeout) {
            this.timeout = timeout;
            return this;
        }

        @Override
        public WaitUntilAction build() {
            return new WaitUntilAction(this);
        }
    }
}
