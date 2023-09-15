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

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public abstract class AbstractSeleniumAction extends AbstractTestAction implements SeleniumAction {

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Selenium browser instance  */
    private final SeleniumBrowser browser;

    public AbstractSeleniumAction(String name, Builder<?, ?> builder) {
        super("selenium:" + name, builder);

        this.browser = builder.browser;
    }

    @Override
    public void doExecute(TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing Selenium browser command '%s'", getName()));
        }

        SeleniumBrowser browserToUse = browser;
        if (browserToUse == null) {
            if (context.getVariables().containsKey(SeleniumHeaders.SELENIUM_BROWSER)) {
                browserToUse = context.getReferenceResolver().resolve(context.getVariable(SeleniumHeaders.SELENIUM_BROWSER), SeleniumBrowser.class);
            } else {
                throw new CitrusRuntimeException("Failed to get active browser instance, " +
                        "either set explicit browser for action or start browser instance");
            }
        }

        execute(browserToUse, context);

        logger.info(String.format("Selenium browser command execution successful: '%s'", getName()));
    }

    protected abstract void execute(SeleniumBrowser browser, TestContext context);

    /**
     * Gets the Selenium browser.
     * @return
     */
    public SeleniumBrowser getBrowser() {
        return browser;
    }

    /**
     * Action builder.
     */
    public static abstract class Builder<T extends SeleniumAction, B extends Builder<T, B>> extends AbstractTestActionBuilder<T, B> {

        private SeleniumBrowser browser;

        /**
         * Use a custom selenium browser.
         */
        public B browser(SeleniumBrowser seleniumBrowser) {
            this.browser = seleniumBrowser;
            return self;
        }

    }

}
