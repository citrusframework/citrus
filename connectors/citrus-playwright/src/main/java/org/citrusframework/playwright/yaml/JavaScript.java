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

package org.citrusframework.playwright.yaml;

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.JavaScriptAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Evaluates JavaScript in the current page.
 */
public class JavaScript extends AbstractPlaywrightAction.Builder<JavaScriptAction, JavaScript> {

    private final JavaScriptAction.Builder delegate = new JavaScriptAction.Builder();

    @SchemaProperty
    public void setScript(String script) {
        delegate.script(script);
    }

    @SchemaProperty
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    @Override
    public JavaScript description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public JavaScript actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public JavaScript browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public JavaScriptAction build() {
        return delegate.build();
    }
}
