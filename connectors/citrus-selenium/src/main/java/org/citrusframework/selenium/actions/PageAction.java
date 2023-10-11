/*
 * Copyright 2006-2017 the original author or authors.
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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.model.PageValidator;
import org.citrusframework.selenium.model.WebPage;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.support.PageFactory;

/**
 * Initialize new page object and run optional validation. Page action is a method on page object that is called via reflection.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class PageAction extends AbstractSeleniumAction {

    /** Web page instance */
    private final WebPage page;

    /** Web page class type information */
    private final String type;

    /** Optional page action that should be executed */
    private final String action;

    /** Page action arguments */
    private final List<String> arguments;

    /** Web page validator */
    private final PageValidator validator;

    /**
     * Default constructor.
     */
    public PageAction(Builder builder) {
        super("page", builder);

        this.page = builder.page;
        this.type = builder.type;
        this.action = builder.action;
        this.arguments = builder.arguments;
        this.validator = builder.validator;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        final WebPage pageToUse;

        if (StringUtils.hasText(type)) {
            try {
                pageToUse = (WebPage) Class.forName(context.replaceDynamicContentInString(type)).newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new CitrusRuntimeException(String.format("Failed to access page type '%s'", context.replaceDynamicContentInString(type)), e);
            }
        } else {
            pageToUse = page;
        }

        PageFactory.initElements(browser.getWebDriver(), pageToUse);

        if (StringUtils.hasText(action)) {
            if (action.equals("validate") && (validator != null || pageToUse instanceof PageValidator)) {
                if (validator != null) {
                    validator.validate(pageToUse, browser, context);
                }

                if (pageToUse instanceof PageValidator) {
                    ((PageValidator) pageToUse).validate(pageToUse, browser, context);
                }
            } else {
                ReflectionHelper.doWithMethods(pageToUse.getClass(), method -> {
                    if (method.getName().equals(action)) {
                        if (method.getParameterCount() == 0 && arguments.size() == 0) {
                            ReflectionHelper.invokeMethod(method, pageToUse);
                        } else if (method.getParameterCount() == 1 && method.getParameters()[0].getParameterizedType().getTypeName().equals(TestContext.class.getName())) {
                            ReflectionHelper.invokeMethod(method, pageToUse, context);
                        } else if (method.getParameterCount() == arguments.size()) {
                            ReflectionHelper.invokeMethod(method, pageToUse, context.resolveDynamicValuesInList(arguments).toArray());
                        } else if (method.getParameterCount() == arguments.size() + 1) {
                            Object[] args = Arrays.copyOf(arguments.toArray(), arguments.size() + 1);
                            args[arguments.size()] = context;
                            ReflectionHelper.invokeMethod(method, pageToUse, context.resolveDynamicValuesInArray(args));
                        } else {
                            throw new CitrusRuntimeException("Unsupported method signature for page action - not matching given arguments");
                        }
                    }
                });
            }
        }
    }

    /**
     * Gets the page.
     *
     * @return
     */
    public WebPage getPage() {
        return page;
    }

    /**
     * Gets the action.
     *
     * @return
     */
    public String getAction() {
        return action;
    }

    /**
     * Gets the validator.
     *
     * @return
     */
    public PageValidator getValidator() {
        return validator;
    }

    /**
     * Gets the type.
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the arguments.
     *
     * @return
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<PageAction, Builder> {

        private WebPage page;
        private String type;
        private String action;
        private final List<String> arguments = new ArrayList<>();
        private PageValidator validator;

        /**
         * Sets the web page.
         * @param page
         * @return
         */
        public Builder page(WebPage page) {
            this.page = page;
            return this;
        }

        /**
         * Sets the web page type.
         * @param pageType
         * @return
         */
        public Builder type(String pageType) {
            this.type = pageType;
            return this;
        }

        /**
         * Sets the web page type.
         * @param pageType
         * @return
         */
        public Builder type(Class<? extends WebPage> pageType) {
            this.type = pageType.getName();
            return this;
        }

        /**
         * Sets the web page action.
         * @param action
         * @return
         */
        public Builder action(String action) {
            this.action = action;
            return this;
        }

        /**
         * Perform page validation.
         * @return
         */
        public Builder validate() {
            this.action = "validate";
            return this;
        }

        /**
         * Set page validator.
         * @param validator
         * @return
         */
        public Builder validator(PageValidator validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Set page action method to execute.
         * @param method
         * @return
         */
        public Builder execute(String method) {
            this.action = method;
            return this;
        }

        /**
         * Set page action argument.
         * @param arg
         * @return
         */
        public Builder argument(String arg) {
            this.arguments.add(arg);
            return this;
        }

        /**
         * Set page action arguments.
         * @param args
         * @return
         */
        public Builder arguments(String ... args) {
            return arguments(Arrays.asList(args));
        }

        /**
         * Set page action arguments.
         * @param args
         * @return
         */
        public Builder arguments(List<String> args) {
            this.arguments.addAll(args);
            return this;
        }

        @Override
        public PageAction build() {
            return new PageAction(this);
        }
    }
}
