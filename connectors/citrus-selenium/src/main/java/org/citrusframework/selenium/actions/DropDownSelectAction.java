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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.api.actions.selenium.SeleniumDropDownSelectActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.ui.Select;

/**
 * Selects and deselects dropdown option(s) on form input. Options may be identified by value
 * (default), by visible text or by index. Single select actions use the value/text/index
 * selection APIs, multi-select actions add options to the existing selection.
 *
 * @since 2.7
 */
public class DropDownSelectAction extends FindElementAction {

    /** Mode identifying options by their value attribute */
    private static final String MODE_VALUE = "value";

    /** Mode identifying options by their visible text */
    private static final String MODE_TEXT = "text";

    /** Mode identifying options by their index */
    private static final String MODE_INDEX = "index";

    /** Modifier key pressed while adding options to an existing selection */
    private static final Keys MULTI_SELECT_KEY = multiSelectKey(System.getProperty("os.name"));

    /** Option to select */
    private final String option;

    /** Multiple options to select */
    private final List<String> options;

    /** Selection mode identifying options by value, text or index */
    private final String selectionMode;

    /** Deselect all options before selecting */
    private final boolean deselectAll;

    /** Options to deselect */
    private final List<String> deselectOptions;

    /**
     * Default constructor.
     */
    public DropDownSelectAction(Builder builder) {
        super("dropdown-select", builder);

        this.option = builder.option;
        this.options = builder.options;
        this.selectionMode = builder.selectionMode;
        this.deselectAll = builder.deselectAll;
        this.deselectOptions = builder.deselectOptions;
    }

    @Override
    protected void execute(WebElement webElement, SeleniumBrowser browser, TestContext context) {
        super.execute(webElement, browser, context);

        assertSupportedSelectionMode(selectionMode);

        Select dropdown = new Select(webElement);

        if (deselectAll) {
            dropdown.deselectAll();
        }

        if (deselectOptions != null && !deselectOptions.isEmpty()) {
            for (String deselectOption : deselectOptions) {
                deselectOption(dropdown, context.replaceDynamicContentInString(deselectOption));
            }
        }

        if (StringUtils.hasText(option)) {
            selectOption(dropdown, context.replaceDynamicContentInString(option));
        }

        if (options != null && !options.isEmpty()) {
            if (Browser.IE.is(browser.getEndpointConfiguration().getBrowserType())) {
                for (String option : options) {
                    selectOption(dropdown, context.replaceDynamicContentInString(option));
                }
            } else {
                List<WebElement> optionElements = dropdown.getOptions();
                Actions builder = new Actions(browser.getWebDriver());
                builder.keyDown(MULTI_SELECT_KEY);
                for (String optionValue : options) {
                    String resolvedOptionValue = context.replaceDynamicContentInString(optionValue);

                    if (MODE_INDEX.equals(selectionMode)) {
                        int index = parseOptionIndex(resolvedOptionValue);
                        if (index < 0 || index >= optionElements.size()) {
                            throw new CitrusRuntimeException(String.format("Cannot locate option with index: %s", resolvedOptionValue));
                        }

                        WebElement option = optionElements.get(index);
                        if (!option.isSelected()) {
                            builder.moveToElement(option).click(option);
                        }
                    } else {
                        for (WebElement option : optionElements) {
                            if (!option.isSelected() && isSameOption(option, resolvedOptionValue)) {
                                builder.moveToElement(option).click(option);
                            }
                        }
                    }
                }
                builder.keyUp(MULTI_SELECT_KEY);
                Action multiple = builder.build();
                multiple.perform();
            }
        }
    }

    private void selectOption(Select dropdown, String option) {
        switch (selectionMode) {
            case MODE_VALUE -> dropdown.selectByValue(option);
            case MODE_TEXT -> dropdown.selectByVisibleText(option);
            case MODE_INDEX -> dropdown.selectByIndex(parseOptionIndex(option));
            default -> throw unknownSelectionMode(selectionMode);
        }
    }

    private void deselectOption(Select dropdown, String option) {
        switch (selectionMode) {
            case MODE_VALUE -> dropdown.deselectByValue(option);
            case MODE_TEXT -> dropdown.deselectByVisibleText(option);
            case MODE_INDEX -> dropdown.deselectByIndex(parseOptionIndex(option));
            default -> throw unknownSelectionMode(selectionMode);
        }
    }

    private boolean isSameOption(WebElement option, String value) {
        if (MODE_TEXT.equals(selectionMode)) {
            return value.equals(option.getText());
        } else {
            return isSameValue(option, value);
        }
    }

    private boolean isSameValue(WebElement option, String value) {
        if (StringUtils.hasText(option.getText())) {
            return value.equals(option.getText());
        } else {
            return value.equals(option.getAttribute("value"));
        }
    }

    private static int parseOptionIndex(String index) {
        try {
            return Integer.parseInt(index);
        } catch (NumberFormatException e) {
            throw new CitrusRuntimeException(String.format("Invalid option index '%s' - please use a number", index), e);
        }
    }

    private static void assertSupportedSelectionMode(String selectionMode) {
        if (!MODE_VALUE.equals(selectionMode) && !MODE_TEXT.equals(selectionMode) && !MODE_INDEX.equals(selectionMode)) {
            throw unknownSelectionMode(selectionMode);
        }
    }

    private static CitrusRuntimeException unknownSelectionMode(String selectionMode) {
        return new CitrusRuntimeException(String.format("Unknown selection mode '%s' - use one of value, text, index", selectionMode));
    }

    /**
     * Gets the modifier key used to add options to an existing selection. Mac OS uses the
     * command key because the control key does not extend a selection there, all other
     * operating systems use the control key.
     * @param osName
     * @return
     */
    static Keys multiSelectKey(String osName) {
        return osName != null && osName.startsWith("Mac") ? Keys.COMMAND : Keys.CONTROL;
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
     * Gets the selection mode.
     * @return
     */
    public String getSelectionMode() {
        return selectionMode;
    }

    /**
     * Gets the deselect options.
     * @return
     */
    public List<String> getDeselectOptions() {
        return deselectOptions;
    }

    /**
     * Checks if all options get deselected before selecting.
     * @return
     */
    public boolean isDeselectAll() {
        return deselectAll;
    }

    /**
     * Action builder.
     */
    public static class Builder extends ElementActionBuilder<DropDownSelectAction, Builder>
            implements SeleniumDropDownSelectActionBuilder<DropDownSelectAction, Builder> {

        private String option;
        private final List<String> options = new ArrayList<>();
        private String selectionMode = MODE_VALUE;
        private boolean deselectAll = false;
        private final List<String> deselectOptions = new ArrayList<>();

        @Override
        public Builder option(String option) {
            this.option = option;
            return this;
        }

        @Override
        public Builder options(String... options) {
            return options(Arrays.asList(options));
        }

        @Override
        public Builder options(List<String> options) {
            this.options.addAll(options);
            return this;
        }

        @Override
        public Builder by(String selectionMode) {
            this.selectionMode = selectionMode.toLowerCase();
            return this;
        }

        @Override
        public Builder byText() {
            return by(MODE_TEXT);
        }

        @Override
        public Builder byIndex() {
            return by(MODE_INDEX);
        }

        @Override
        public Builder deselectAll() {
            this.deselectAll = true;
            return this;
        }

        @Override
        public Builder deselect(String... options) {
            this.deselectOptions.addAll(Arrays.asList(options));
            return this;
        }

        @Override
        public DropDownSelectAction build() {
            return new DropDownSelectAction(this);
        }
    }
}
