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

package org.citrusframework.api.actions.selenium;

import org.citrusframework.TestAction;

/**
 * Builder for the Selenium extract action.
 */
public interface SeleniumExtractActionBuilder<T extends TestAction, B extends SeleniumExtractActionBuilder<T, B>>
        extends SeleniumElementActionBuilderBase<T, B> {

    /**
     * Sets the value type to extract.
     * @param value
     * @return
     */
    B value(String value);

    /**
     * Sets the variable name to store the extracted value.
     * @param variable
     * @return
     */
    B variable(String variable);

    /**
     * Extract current page url into a variable.
     * @return
     */
    B url();

    /**
     * Extract current page title into a variable.
     * @return
     */
    B title();

    /**
     * Extract current page source into a variable.
     * @return
     */
    B pageSource();

    /**
     * Extract element text into a variable.
     * @return
     */
    B text();

    /**
     * Extract element value into a variable.
     * @return
     */
    B value();

    /**
     * Extract element attribute value into a variable.
     * @param name
     * @return
     */
    B attribute(String name);

    /**
     * Extract element css style value into a variable.
     * @param property
     * @return
     */
    B cssValue(String property);

    /**
     * Extract number of elements that match the element selector into a variable.
     * @return
     */
    B count();
}
