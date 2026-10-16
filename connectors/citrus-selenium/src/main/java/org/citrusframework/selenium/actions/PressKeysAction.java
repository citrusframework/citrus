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

import org.citrusframework.api.actions.selenium.SeleniumPressKeysActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * Presses one or more keys on a web element or on the currently focused element when no
 * element is given. Keys may be combined with "+", e.g. "CONTROL+A". Single characters
 * are sent as typed, all other key names must match a Selenium {@link Keys} name such as
 * ENTER, TAB or ESCAPE (case insensitive).
 *
 * @since 5.1
 */
public class PressKeysAction extends FindElementAction {

    /** Keys to press on the element */
    private final String keys;

    public PressKeysAction(Builder builder) {
        super("press", builder);

        this.keys = builder.keys;
    }

    @Override
    protected boolean requiresElementBeforeExecute() {
        return getBy() != null || getProperty() != null;
    }

    @Override
    protected void execute(WebElement webElement, SeleniumBrowser browser, TestContext context) {
        super.execute(webElement, browser, context);

        webElement.sendKeys(parseKeys(context));
    }

    @Override
    protected void executeWithoutElement(By by, SeleniumBrowser browser, TestContext context) {
        browser.getWebDriver().switchTo().activeElement().sendKeys(parseKeys(context));
    }

    /**
     * Parses the configured keys into a sequence of Selenium keys. Resolves dynamic content
     * in the key expression and supports "+" separated key combinations.
     * @param context the current test context
     * @return the keys to send to the element
     */
    private CharSequence[] parseKeys(TestContext context) {
        if (!StringUtils.hasText(keys)) {
            throw new CitrusRuntimeException("Missing keys to press");
        }

        String[] keyParts = context.replaceDynamicContentInString(keys).split("\\+");

        CharSequence[] result = new CharSequence[keyParts.length];
        for (int i = 0; i < keyParts.length; i++) {
            String keyPart = keyParts[i].trim();

            if (keyPart.length() == 1) {
                result[i] = keyPart;
            } else {
                try {
                    result[i] = Keys.valueOf(keyPart.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new CitrusRuntimeException(String.format("Unknown key '%s' - use a Selenium key name such as ENTER, TAB or ESCAPE or a single character", keyPart), e);
                }
            }
        }

        return result;
    }

    /**
     * Gets the keys to press.
     * @return
     */
    public String getKeys() {
        return keys;
    }

    public static class Builder extends ElementActionBuilder<PressKeysAction, Builder>
            implements SeleniumPressKeysActionBuilder<PressKeysAction, Builder> {

        private String keys;

        @Override
        public Builder keys(String keys) {
            this.keys = keys;
            return this;
        }

        @Override
        public PressKeysAction build() {
            return new PressKeysAction(this);
        }
    }
}
