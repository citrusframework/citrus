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

package org.citrusframework.selenium.yaml;

import java.util.List;

import org.citrusframework.TestActor;
import org.citrusframework.api.actions.selenium.SeleniumElementActionBuilderBase;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.DropDownSelectAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.api.yaml.SchemaProperty;

public class DropDownSelect extends AbstractSeleniumAction.Builder<DropDownSelectAction, DropDownSelect> implements ElementAware {

    private final DropDownSelectAction.Builder delegate = new DropDownSelectAction.Builder();

    @SchemaProperty
    public void setOption(String option) {
        this.delegate.option(option);
    }

    @SchemaProperty
    public void setOptions(List<String> options) {
        this.delegate.options(options);
    }

    @SchemaProperty
    public void setBy(String selectionMode) {
        this.delegate.by(selectionMode);
    }

    @SchemaProperty
    public void setDeselectAll(Boolean deselectAll) {
        if (Boolean.TRUE.equals(deselectAll)) {
            this.delegate.deselectAll();
        }
    }

    @SchemaProperty
    public void setDeselectOptions(List<String> options) {
        this.delegate.deselect(options.toArray(new String[0]));
    }

    @Override
    @SchemaProperty
    public void setElement(Element element) {
        ElementAware.super.setElement(element);
    }

    @Override
    public SeleniumElementActionBuilderBase<?, ?> getElementBuilder() {
        return delegate;
    }

    @Override
    public DropDownSelect description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public DropDownSelect actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public DropDownSelect browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public DropDownSelectAction build() {
        return delegate.build();
    }
}
