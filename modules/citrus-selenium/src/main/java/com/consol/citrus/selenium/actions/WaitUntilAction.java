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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class WaitUntilAction extends FindElementAction {

    private Long timeout = 5000L;

    private String condition;

    /**
     * Default constructor.
     */
    public WaitUntilAction() {
        super("wait");
    }

    @Override
    protected void execute(WebElement webElement, SeleniumBrowser browser, TestContext context) {
        WebDriverWait q = new WebDriverWait(browser.getWebDriver(), timeout);

        if (condition.equals("hidden")) {
            q.until(ExpectedConditions.invisibilityOfElementLocated(createBy(context)));
        } else if (condition.equals("visible")) {
            q.until(ExpectedConditions.visibilityOf(webElement));
        } else {
            throw new CitrusRuntimeException("Unknown wait condition");
        }
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
    }
}
