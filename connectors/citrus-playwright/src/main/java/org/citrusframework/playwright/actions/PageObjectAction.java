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

package org.citrusframework.playwright.actions;

import java.lang.reflect.InvocationTargetException;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.page.PlaywrightPage;
import org.citrusframework.playwright.page.PlaywrightPageFactory;
import org.citrusframework.playwright.page.PlaywrightPageValidator;

/**
 * Citrus action for instantiating, invoking, and validating page-object classes.
 *
 * <p>The action delegates construction and method invocation to
 * {@link PlaywrightPageFactory}. Page objects can expose constructors or methods
 * that accept Playwright/Citrus runtime objects for dependency injection.</p>
 */
public class PageObjectAction extends AbstractPlaywrightAction {

    private final Class<?> pageType;
    private final String method;
    private final String[] arguments;
    private final Class<? extends PlaywrightPageValidator<?>> validatorType;

    public PageObjectAction(Builder builder) {
        super("page-object", builder);
        this.pageType = builder.pageType;
        this.method = builder.method;
        this.arguments = builder.arguments;
        this.validatorType = builder.validatorType;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        PlaywrightPageFactory factory = new PlaywrightPageFactory();
        Object page = factory.create(pageType, browser, context);
        if (method != null) {
            factory.invoke(page, method, arguments, browser, context);
        }
        if (validatorType != null) {
            invokeValidator(page, browser, context);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void invokeValidator(Object page, PlaywrightBrowser browser, TestContext context) {
        if (!(page instanceof PlaywrightPage playwrightPage)) {
            String typeName = page == null ? "null" : page.getClass().getName();
            throw new CitrusRuntimeException("Playwright page-object validators require page objects to implement "
                    + PlaywrightPage.class.getName() + ": " + typeName);
        }
        try {
            PlaywrightPageValidator validator = validatorType.getDeclaredConstructor().newInstance();
            validator.validate(playwrightPage, browser, context);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new CitrusRuntimeException("Failed to invoke Playwright page-object validator: " + validatorType.getName(), e);
        }
    }

    /**
     * Fluent builder for page-object execution and validation.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<PageObjectAction, Builder> {
        private Class<?> pageType;
        private String method;
        private String[] arguments = new String[0];
        private Class<? extends PlaywrightPageValidator<?>> validatorType;

        /**
         * Sets the page-object class to instantiate.
         *
         * @param pageType page-object class
         * @return this builder
         */
        public Builder type(Class<?> pageType) {
            this.pageType = pageType;
            return this;
        }

        /**
         * Invokes a method on the page object after instantiation.
         *
         * @param method method name
         * @return this builder
         */
        public Builder execute(String method) {
            this.method = method;
            return this;
        }

        /**
         * Supplies string arguments for the invoked method.
         *
         * @param arguments method arguments resolved through Citrus variables
         * @return this builder
         */
        public Builder arguments(String... arguments) {
            this.arguments = arguments == null ? new String[0] : arguments;
            return this;
        }

        /**
         * Runs a validator against the page object after optional method execution.
         *
         * @param validatorType validator class with a no-argument constructor
         * @return this builder
         */
        public Builder validate(Class<? extends PlaywrightPageValidator<?>> validatorType) {
            this.validatorType = validatorType;
            return this;
        }

        @Override
        public PageObjectAction build() {
            if (pageType == null) {
                throw new CitrusRuntimeException("Missing Playwright page-object type");
            }
            if (method == null && validatorType == null) {
                throw new CitrusRuntimeException("Missing Playwright page-object method or validator");
            }
            return new PageObjectAction(this);
        }
    }
}
