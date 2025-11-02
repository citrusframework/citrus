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

package org.citrusframework.selenium.yaml;

import java.util.List;

import org.citrusframework.TestActor;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.PageAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.actions.selenium.PageValidator;
import org.citrusframework.actions.selenium.WebPage;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

public class Page extends AbstractSeleniumAction.Builder<PageAction, Page> implements ReferenceResolverAware {

    private final PageAction.Builder delegate = new PageAction.Builder();

    private String pageName;
    private String pageValidator;

    private ReferenceResolver referenceResolver;

    /**
     * Sets the web page by name.
     */
    @SchemaProperty
    public void setName(String name) {
        this.pageName = name;
    }

    /**
     * Sets the web page type.
     */
    @SchemaProperty
    public void setType(String pageType) {
        this.delegate.type(pageType);
    }

    /**
     * Sets the web page action.
     */
    @SchemaProperty
    public void setAction(String action) {
        this.delegate.action(action);
    }

    /**
     * Perform page validation.
     */
    @SchemaProperty
    public void setValidate() {
        this.delegate.action("validate");
    }

    /**
     * Set page validator.
     */
    @SchemaProperty
    public void setValidator(String pageValidator) {
        this.pageValidator = pageValidator;
    }

    /**
     * Set page action method to execute.
     */
    @SchemaProperty
    public void setExecute(String method) {
        this.delegate.action(method);
    }

    /**
     * Set page action argument.
     */
    @SchemaProperty
    public void setArgument(String arg) {
        this.delegate.argument(arg);
    }

    /**
     * Set page action arguments.
     */
    @SchemaProperty
    public void setArguments(List<String> args) {
        this.delegate.arguments(args);
    }

    @Override
    public Page description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Page actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Page browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public PageAction build() {
        if (referenceResolver != null) {
            if (pageName != null) {
                this.delegate.page(referenceResolver.resolve(pageName, WebPage.class));
            }

            if (pageValidator != null) {
                this.delegate.validator(referenceResolver.resolve(pageValidator, PageValidator.class));
            }
        }

        return delegate.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
