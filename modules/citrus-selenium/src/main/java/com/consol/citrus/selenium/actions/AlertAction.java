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
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.Alert;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class AlertAction extends AbstractSeleniumAction {

    private String action = "accept";

    private String text;

    /**
     * Default constructor.
     */
    public AlertAction() {
        super("alert");
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        try {
            Alert alert = browser.getWebDriver().switchTo().alert();
            if (alert == null) {
                throw new ValidationException("Failed to access alert dialog - not found");
            }

            if (StringUtils.hasText(text)) {
                Assert.isTrue(context.replaceDynamicContentInString(text).equals(alert.getText()),
                        String.format("Failed to validate alert dialog text, " +
                        "expected '%s', but was '%s'", text, alert.getText()));
            }

            context.setVariable("selenium_alert_text", alert.getText());

            if (action.equals("accept")) {
                alert.accept();
            } else if (action.equals("dismiss")) {
                alert.dismiss();
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to access alert box.", e);
        }
    }

    /**
     * Sets the alert action to perform.
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Gets the alert action.
     * @return
     */
    public String getAction() {
        return action;
    }
}
