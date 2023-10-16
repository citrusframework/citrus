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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumHeaders;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import org.openqa.selenium.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access current alert dialog. In case no alert is opened action fails.
 * Action supports optional alert text validation.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class AlertAction extends AbstractSeleniumAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AlertAction.class);

    /** Accept or dismiss dialog */
    private final boolean accept;

    /** Optional dialog text validation */
    private final String text;

    /**
     * Default constructor.
     */
    public AlertAction(Builder builder) {
        super("alert", builder);

        this.accept = builder.accept;
        this.text = builder.text;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        Alert alert = browser.getWebDriver().switchTo().alert();
        if (alert == null) {
            throw new CitrusRuntimeException("Failed to access alert dialog - not found");
        }

        if (StringUtils.hasText(text)) {
            logger.info("Validating alert text");

            String alertText = context.replaceDynamicContentInString(text);

            if (ValidationMatcherUtils.isValidationMatcherExpression(alertText)) {
                ValidationMatcherUtils.resolveValidationMatcher("alertText", alert.getText(), alertText, context);
            } else {
                if (!alertText.equals(alert.getText())) {
                    throw new ValidationException(String.format("Failed to validate alert dialog text, " +
                            "expected '%s', but was '%s'", alertText, alert.getText()));
                }

            }
            logger.info("Alert text validation successful - All values Ok");
        }

        context.setVariable(SeleniumHeaders.SELENIUM_ALERT_TEXT, alert.getText());

        if (accept) {
            alert.accept();
        } else {
            alert.dismiss();
        }
    }

    /**
     * Gets the accept.
     *
     * @return
     */
    public boolean isAccept() {
        return accept;
    }

    /**
     * Gets the text.
     *
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<AlertAction, Builder> {

        private boolean accept = true;
        private String text;

        /**
         * Add alert text validation.
         * @param text
         * @return
         */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Accept alert dialog.
         * @return
         */
        public Builder accept() {
            this.accept = true;
            return this;
        }

        /**
         * Dismiss alert dialog.
         * @return
         */
        public Builder dismiss() {
            this.accept = false;
            return this;
        }

        @Override
        public AlertAction build() {
            return new AlertAction(this);
        }
    }
}
