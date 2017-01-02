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

package com.consol.citrus.selenium.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.support.ui.Select;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class DropDownSelectAction extends FindElementAction {

    private String option;

    private List<String> options;

    /**
     * Default constructor.
     */
    public DropDownSelectAction() {
        super("dropdown-select");
    }

    @Override
    protected void execute(WebElement webElement, SeleniumBrowser browser, TestContext context) {
        super.execute(webElement, browser, context);

        Select dropdown = new Select(webElement);

        if (StringUtils.hasText(option)) {
            dropdown.selectByValue(context.replaceDynamicContentInString(option));
        }

        if (!CollectionUtils.isEmpty(options)) {
            if (BrowserType.IE.equals(browser.getEndpointConfiguration().getBrowserType())) {
                for (String option : options) {
                    dropdown.selectByValue(option);
                }
            } else {
                List<WebElement> optionElements = dropdown.getOptions();
                Actions builder = new Actions(browser.getWebDriver());
                builder.keyDown(Keys.CONTROL);
                for (String optionValue : options) {
                    for (WebElement option : optionElements) {
                        if (!option.isSelected() && isSameValue(option, optionValue)) {
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

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getOption() {
        return option;
    }
}
