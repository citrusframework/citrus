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

package org.citrusframework.actions.selenium;

import java.util.List;

import org.citrusframework.TestAction;

public interface SeleniumPageActionBuilder<T extends TestAction, B extends SeleniumPageActionBuilder<T, B>>
        extends SeleniumActionBuilderBase<T, B> {

    /**
     * Sets the web page.
     */
    B page(WebPage o);

    /**
     * Sets the web page type.
     */
    B type(String pageType);

    /**
     * Sets the web page type.
     */
    B type(Class<? extends WebPage> pageType);

    /**
     * Sets the web page action.
     */
    B action(String action);

    /**
     * Perform page validation.
     */
    B validate();

    /**
     * Set page validator.
     */
    B validator(PageValidator<?, ?> validator);

    /**
     * Set page action method to execute.
     */
    B execute(String method);

    /**
     * Set page action argument.
     */
    B argument(String arg);

    /**
     * Set page action arguments.
     */
    B arguments(String... args);

    /**
     * Set page action arguments.
     */
    B arguments(List<String> args);
}
