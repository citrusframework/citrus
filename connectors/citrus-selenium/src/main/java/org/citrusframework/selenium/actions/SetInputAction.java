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

import org.citrusframework.context.TestContext;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Sets new text value for form input element.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class SetInputAction extends FindElementAction {

    /** Value to set on input */
    private final String value;

    /**
     * Default constructor.
     */
    public SetInputAction(Builder builder) {
        super("set-input", builder);

        this.value = builder.value;
    }

    @Override
    protected void execute(WebElement webElement, SeleniumBrowser browser, TestContext context) {
        super.execute(webElement, browser, context);

        String tagName = webElement.getTagName();
        if (!"select".equalsIgnoreCase(tagName)) {
            webElement.clear();
            webElement.sendKeys(context.replaceDynamicContentInString(value));
        } else {
            new Select(webElement).selectByValue(context.replaceDynamicContentInString(value));
        }
    }

    /**
     * Gets the value.
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * Action builder.
     */
    public static class Builder extends ElementActionBuilder<SetInputAction, SetInputAction.Builder> {

        private String value;

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        @Override
        public SetInputAction build() {
            return new SetInputAction(this);
        }
    }
}
