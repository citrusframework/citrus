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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumHeaders;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import org.openqa.selenium.Alert;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Access current alert dialog. In case no alert is opened action fails.
 * Action supports optional alert text validation.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class AlertAction extends AbstractSeleniumAction {

    /** Accept or dismiss dialog */
    private boolean accept = true;

    /** Optional dialog text validation */
    private String text;

    /**
     * Default constructor.
     */
    public AlertAction() {
        super("alert");
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        Alert alert = browser.getWebDriver().switchTo().alert();
        if (alert == null) {
            throw new CitrusRuntimeException("Failed to access alert dialog - not found");
        }

        if (StringUtils.hasText(text)) {
            log.info("Validating alert text");

            String alertText = context.replaceDynamicContentInString(text);

            if (ValidationMatcherUtils.isValidationMatcherExpression(alertText)) {
                ValidationMatcherUtils.resolveValidationMatcher("alertText", alert.getText(), alertText, context);
            } else {
                Assert.isTrue(alertText.equals(alert.getText()),
                        String.format("Failed to validate alert dialog text, " +
                                "expected '%s', but was '%s'", alertText, alert.getText()));

            }
            log.info("Alert text validation successful - All values Ok");
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
     * Sets the accept.
     *
     * @param accept
     */
    public void setAccept(boolean accept) {
        this.accept = accept;
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
     * Sets the text.
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }
}
