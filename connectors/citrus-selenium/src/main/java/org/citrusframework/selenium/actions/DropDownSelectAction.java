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
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.ui.Select;

/**
 * Selects dropdown option(s) on form input.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class DropDownSelectAction extends FindElementAction {

    /** Option to select */
    private final String option;

    /** Multiple options to select */
    private final List<String> options;

    /**
     * Default constructor.
     */
    public DropDownSelectAction(Builder builder) {
        super("dropdown-select", builder);

        this.option = builder.option;
        this.options = builder.options;
    }

    @Override
    protected void execute(WebElement webElement, SeleniumBrowser browser, TestContext context) {
        super.execute(webElement, browser, context);

        Select dropdown = new Select(webElement);

        if (StringUtils.hasText(option)) {
            dropdown.selectByValue(context.replaceDynamicContentInString(option));
        }

        if (options != null && !options.isEmpty()) {
            if (Browser.IE.is(browser.getEndpointConfiguration().getBrowserType())) {
                for (String option : options) {
                    dropdown.selectByValue(context.replaceDynamicContentInString(option));
                }
            } else {
                List<WebElement> optionElements = dropdown.getOptions();
                Actions builder = new Actions(browser.getWebDriver());
                builder.keyDown(Keys.CONTROL);
                for (String optionValue : options) {
                    for (WebElement option : optionElements) {
                        if (!option.isSelected() && isSameValue(option, context.replaceDynamicContentInString(optionValue))) {
                            builder.moveToElement(option).click(option);
                        }
                    }
                }
                builder.keyUp(Keys.CONTROL);
                Action multiple = builder.build();
                multiple.perform();
            }
        }
    }

    private boolean isSameValue(WebElement option, String value) {
        if (StringUtils.hasText(option.getText())) {
            return value.equals(option.getText());
        } else {
            return value.equals(option.getAttribute("value"));
        }
    }

    /**
     * Gets the option.
     *
     * @return
     */
    public String getOption() {
        return option;
    }

    /**
     * Gets the options.
     *
     * @return
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     * Action builder.
     */
    public static class Builder extends ElementActionBuilder<DropDownSelectAction, Builder> {

        private String option;
        private final List<String> options = new ArrayList<>();

        public Builder option(String option) {
            this.option = option;
            return this;
        }

        public Builder options(String... options) {
            return options(Arrays.asList(options));
        }

        public Builder options(List<String> options) {
            this.options.addAll(options);
            return this;
        }

        @Override
        public DropDownSelectAction build() {
            return new DropDownSelectAction(this);
        }
    }
}
