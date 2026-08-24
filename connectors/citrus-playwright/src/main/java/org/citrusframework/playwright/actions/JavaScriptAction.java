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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;
import org.citrusframework.util.StringUtils;

public class JavaScriptAction extends AbstractPlaywrightAction {

    private final String script;
    private final String variable;

    public JavaScriptAction(Builder builder) {
        super("javascript", builder);
        this.script = builder.script;
        this.variable = builder.variable;
    }

    public String getScript() {
        return script;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Object result = browser.getCurrentPage().evaluate(LocatorResolver.resolve(script, context));
        if (variable != null) {
            context.setVariable(variable, result == null ? "" : result);
        }
    }

    public static class Builder extends AbstractPlaywrightAction.Builder<JavaScriptAction, Builder> {
        private String script;
        private String variable;

        public Builder script(String script) {
            this.script = script;
            return this;
        }

        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public JavaScriptAction build() {
            if (!StringUtils.hasText(script)) {
                throw new CitrusRuntimeException("Missing Playwright JavaScript - call script(...) before building javascript action");
            }
            return new JavaScriptAction(this);
        }
    }
}
