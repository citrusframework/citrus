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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class FindElementAction extends AbstractSeleniumAction {

    private String selectorType;
    private String select;

    /**
     * Default constructor.
     */
    public FindElementAction() {
        super("find");
    }

    /**
     * Constructor with name.
     * @param name
     */
    public FindElementAction(String name) {
        super(name);
    }

    @Override
    protected final void execute(SeleniumBrowser browser, TestContext context) {
        WebElement element = browser.getWebDriver().findElement(createBy(context));

        if (element == null) {
            throw new CitrusRuntimeException(String.format("Failed to find element '%s' on page", selectorType + "=" + select));
        }

        execute(element, browser, context);
    }

    /**
     * Subclasses may override this method in order to add element actions.
     * @param element
     * @param browser
     * @param context
     */
    protected void execute(WebElement element, SeleniumBrowser browser, TestContext context) {
        context.setVariable(element.getTagName(), element);
    }

    /**
     * Create by selector from type information.
     * @return
     */
    protected By createBy(TestContext context) {
        switch (selectorType) {
            case "id":
                return By.id(context.replaceDynamicContentInString(select));
            case "class-name":
                return By.className(context.replaceDynamicContentInString(select));
            case "link-text":
                return By.linkText(context.replaceDynamicContentInString(select));
            case "css-selector":
                return By.cssSelector(context.replaceDynamicContentInString(select));
            case "name":
                return By.name(context.replaceDynamicContentInString(select));
            case "tag-name":
                return By.tagName(context.replaceDynamicContentInString(select));
            case "xpath":
                return By.xpath(context.replaceDynamicContentInString(select));
        }

        throw new CitrusRuntimeException("Unknown selector type: " + selectorType);
    }

    public void setSelectorType(String selectorType) {
        this.selectorType = selectorType;
    }

    public String getSelectorType() {
        return selectorType;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getSelect() {
        return select;
    }
}
