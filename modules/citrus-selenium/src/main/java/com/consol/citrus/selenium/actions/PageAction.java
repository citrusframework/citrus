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

package com.consol.citrus.selenium.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.model.PageValidator;
import com.consol.citrus.selenium.model.WebPage;
import org.openqa.selenium.support.PageFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Initialize new page object and run optional validation. Page action is a method on page object that is called via reflection.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class PageAction extends AbstractSeleniumAction {

    /** Web page instance */
    private WebPage page;

    /** Web page class type information */
    private String type;

    /** Optional page action that should be executed */
    private String action;

    /** Page action arguments */
    private List<String> arguments = new ArrayList<>();

    /** Web page validator */
    private PageValidator validator;

    /**
     * Default constructor.
     */
    public PageAction() {
        super("page");
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        if (StringUtils.hasText(type)) {
            try {
                page = (WebPage) Class.forName(context.replaceDynamicContentInString(type)).newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new CitrusRuntimeException(String.format("Failed to access page type '%s'", context.replaceDynamicContentInString(type)), e);
            }
        }

        PageFactory.initElements(browser.getWebDriver(), page);

        if (StringUtils.hasText(action)) {
            if (action.equals("validate") && (validator != null || page instanceof PageValidator)) {
                if (validator != null) {
                    validator.validate(page, browser, context);
                }

                if (page instanceof PageValidator) {
                    ((PageValidator) page).validate(page, browser, context);
                }
            } else {
                ReflectionUtils.doWithMethods(page.getClass(), method -> {
                    if (method.getName().equals(action)) {
                        if (method.getParameterCount() == 0 && arguments.size() == 0) {
                            ReflectionUtils.invokeMethod(method, page);
                        } else if (method.getParameterCount() == 1 && method.getParameters()[0].getParameterizedType().getTypeName().equals(TestContext.class.getName())) {
                            ReflectionUtils.invokeMethod(method, page, context);
                        } else if (method.getParameterCount() == arguments.size()) {
                            ReflectionUtils.invokeMethod(method, page, context.resolveDynamicValuesInList(arguments).toArray());
                        } else if (method.getParameterCount() == arguments.size() + 1) {
                            Object[] args = Arrays.copyOf(arguments.toArray(), arguments.size() + 1);
                            args[arguments.size()] = context;
                            ReflectionUtils.invokeMethod(method, page, context.resolveDynamicValuesInArray(args));
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
     * Sets the page.
     *
     * @param page
     */
    public void setPage(WebPage page) {
        this.page = page;
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
     * Sets the action.
     *
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
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
     * Sets the validator.
     *
     * @param validator
     */
    public void setValidator(PageValidator validator) {
        this.validator = validator;
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
     * Sets the type.
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
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
     * Sets the arguments.
     *
     * @param arguments
     */
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }
}
