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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.util.BrowserUtils;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.remote.Browser;

/**
 * Navigates to new page either by using new absolute page URL or relative page path.
 * Also supports history forward and back navigation as well as page refresh.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class NavigateAction extends AbstractSeleniumAction {

    /** Page URL to navigate to */
    private final String page;

    /**
     * Default constructor.
     */
    public NavigateAction(Builder builder) {
        super("navigate", builder);

        this.page = builder.page;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        if (page.equals("back")) {
            browser.getWebDriver().navigate().back();
        } else if (page.equals("forward")) {
            browser.getWebDriver().navigate().forward();
        } else if (page.equals("refresh")) {
            browser.getWebDriver().navigate().refresh();
        } else {
            try {
                if (Browser.IE.is(browser.getEndpointConfiguration().getBrowserType())) {
                    String cachingSafeUrl = BrowserUtils.makeIECachingSafeUrl(context.replaceDynamicContentInString(page), new Date().getTime());
                    browser.getWebDriver().navigate().to(new URL(cachingSafeUrl));
                } else {
                    browser.getWebDriver().navigate().to(new URL(context.replaceDynamicContentInString(page)));
                }
            } catch (MalformedURLException ex) {
                String baseUrl = browser.getWebDriver().getCurrentUrl();
                try {
                    new URL(baseUrl);
                } catch (MalformedURLException e) {
                    if (StringUtils.hasText(browser.getEndpointConfiguration().getStartPageUrl())) {
                        baseUrl = browser.getEndpointConfiguration().getStartPageUrl();
                    } else {
                        throw new CitrusRuntimeException("Failed to create relative page URL - must set start page on browser", ex);
                    }
                }
                String lastChar = baseUrl.substring(baseUrl.length() - 1);
                if (!lastChar.equals("/")) {
                    baseUrl = baseUrl + "/";
                }

                browser.getWebDriver().navigate().to(baseUrl + context.replaceDynamicContentInString(page));
            }
        }
    }

    /**
     * Gets the page url.
     * @return
     */
    public String getPage() {
        return page;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<NavigateAction, NavigateAction.Builder> {

        private String page;

        public Builder page(String page) {
            this.page = page;
            return this;
        }

        @Override
        public NavigateAction build() {
            return new NavigateAction(this);
        }
    }
}
