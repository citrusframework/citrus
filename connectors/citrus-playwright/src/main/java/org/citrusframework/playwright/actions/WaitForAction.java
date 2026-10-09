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

package org.citrusframework.playwright.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.util.LocatorResolver;
import org.citrusframework.util.StringUtils;

public class WaitForAction extends AbstractPlaywrightAction {

    public enum Condition {
        VISIBLE,
        HIDDEN,
        ATTACHED,
        DETACHED,
        LOAD,
        DOM_CONTENT_LOADED,
        NETWORK_IDLE,
        FUNCTION
    }

    private final Condition condition;
    private final org.citrusframework.playwright.model.LocatorSpec locator;
    private final String expression;

    public WaitForAction(Builder builder) {
        super("wait", builder);
        this.condition = builder.condition;
        this.locator = builder.locator;
        this.expression = builder.expression;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        if (condition == Condition.LOAD || condition == Condition.DOM_CONTENT_LOADED || condition == Condition.NETWORK_IDLE) {
            browser.getCurrentPage().waitForLoadState(resolveLoadState(condition));
            return;
        }

        Locator element = LocatorResolver.resolve(browser.getCurrentPage(), locator, context);

        if (condition == Condition.FUNCTION) {
            element.waitForFunction(LocatorResolver.resolve(expression, context));
            return;
        }

        element.waitFor(new Locator.WaitForOptions().setState(resolveSelectorState(condition)));
    }

    private WaitForSelectorState resolveSelectorState(Condition condition) {
        return switch (condition) {
            case VISIBLE -> WaitForSelectorState.VISIBLE;
            case HIDDEN -> WaitForSelectorState.HIDDEN;
            case ATTACHED -> WaitForSelectorState.ATTACHED;
            case DETACHED -> WaitForSelectorState.DETACHED;
            default -> throw new IllegalArgumentException("Condition is not a selector state: " + condition);
        };
    }

    private LoadState resolveLoadState(Condition condition) {
        return switch (condition) {
            case DOM_CONTENT_LOADED -> LoadState.DOMCONTENTLOADED;
            case NETWORK_IDLE -> LoadState.NETWORKIDLE;
            default -> LoadState.LOAD;
        };
    }

    public Condition getCondition() {
        return condition;
    }

    public String getExpression() {
        return expression;
    }

    public static class Builder extends ElementActionBuilder<WaitForAction, Builder> {
        private Condition condition = Condition.VISIBLE;
        private String expression;

        public Builder visible() {
            this.condition = Condition.VISIBLE;
            return this;
        }

        public Builder hidden() {
            this.condition = Condition.HIDDEN;
            return this;
        }

        public Builder attached() {
            this.condition = Condition.ATTACHED;
            return this;
        }

        public Builder detached() {
            this.condition = Condition.DETACHED;
            return this;
        }

        public Builder load() {
            this.condition = Condition.LOAD;
            return this;
        }

        public Builder domContentLoaded() {
            this.condition = Condition.DOM_CONTENT_LOADED;
            return this;
        }

        public Builder networkIdle() {
            this.condition = Condition.NETWORK_IDLE;
            return this;
        }

        /**
         * Waits until the given JavaScript predicate, called with the matched element, returns a
         * truthy value. Requires a locator and supports Citrus variables inside the expression.
         *
         * @param expression JavaScript expression receiving the matched element
         * @return this builder
         */
        public Builder function(String expression) {
            this.condition = Condition.FUNCTION;
            this.expression = expression;
            return this;
        }

        @Override
        public WaitForAction build() {
            if (condition != Condition.LOAD && condition != Condition.DOM_CONTENT_LOADED && condition != Condition.NETWORK_IDLE) {
                requireLocator();
            }
            if (condition == Condition.FUNCTION && !StringUtils.hasText(expression)) {
                throw new CitrusRuntimeException("Missing Playwright wait predicate - call function(...) with a JavaScript expression");
            }
            return new WaitForAction(this);
        }
    }
}
