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

import java.time.Duration;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Waits until element is visible or hidden.
 *
 * @author Christoph Deppisch
 * @since 2.7
 */
public class WaitUntilAction extends FindElementAction {

    /** Wait timeout */
    private final Long timeout;

    /** Wait condition on element */
    private final String condition;

    /**
     * Default constructor.
     */
    public WaitUntilAction(Builder builder) {
        super("wait", builder);

        this.timeout = builder.timeout;
        this.condition = builder.condition;
    }

    @Override
    protected void execute(WebElement webElement, SeleniumBrowser browser, TestContext context) {
        WebDriverWait q = new WebDriverWait(browser.getWebDriver(), Duration.ofMillis(timeout));

        if (condition.equals("hidden")) {
            q.until(ExpectedConditions.invisibilityOf(webElement));
        } else if (condition.equals("visible")) {
            q.until(ExpectedConditions.visibilityOf(webElement));
        } else {
            throw new CitrusRuntimeException("Unknown wait condition");
        }
    }

    @Override
    protected void validate(WebElement element, SeleniumBrowser browser, TestContext context) {
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
     * Action builder.
     */
    public static class Builder extends ElementActionBuilder<WaitUntilAction, Builder> {

        private Long timeout = 5000L;
        private String condition;

        /**
         * Add visible condition.
         * @return
         */
        public Builder visible() {
            condition("visible");
            return this;
        }

        /**
         * Add hidden condition.
         * @return
         */
        public Builder hidden() {
            condition("hidden");
            return this;
        }

        /**
         * Add hidden condition.
         * @return
         */
        public Builder condition(String condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Add timeout condition.
         * @return
         */
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
