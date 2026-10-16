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
 * Builder for the Selenium verify action.
 */
public interface SeleniumVerifyActionBuilder<T extends TestAction, B extends SeleniumVerifyActionBuilder<T, B>>
        extends SeleniumElementActionBuilderBase<T, B> {

    /**
     * Sets the check type.
     * @param check
     * @return
     */
    B check(String check);

    /**
     * Sets the expected value.
     * @param expected
     * @return
     */
    B expected(String expected);

    /**
     * Verify current page url.
     * @param expected
     * @return
     */
    B url(String expected);

    /**
     * Verify current page title.
     * @param expected
     * @return
     */
    B title(String expected);

    /**
     * Verify element is present on the page.
     * @return
     */
    B present();

    /**
     * Verify element is not present on the page.
     * @return
     */
    B absent();

    /**
     * Verify element is displayed.
     * @return
     */
    B displayed();

    /**
     * Verify element is hidden.
     * @return
     */
    B hidden();

    /**
     * Verify element is enabled.
     * @return
     */
    B enabled();

    /**
     * Verify element is disabled.
     * @return
     */
    B disabled();

    /**
     * Verify element is selected.
     * @return
     */
    B selected();

    /**
     * Verify element is not selected.
     * @return
     */
    B notSelected();

    /**
     * Verify element text.
     * @param expected
     * @return
     */
    B text(String expected);

    /**
     * Verify element value.
     * @param expected
     * @return
     */
    B value(String expected);

    /**
     * Verify element attribute value.
     * @param name
     * @param expected
     * @return
     */
    B attribute(String name, String expected);

    /**
     * Verify element css style value.
     * @param property
     * @param expected
     * @return
     */
    B cssValue(String property, String expected);

    /**
     * Verify number of elements that match the element selector.
     * @param expected
     * @return
     */
    B count(String expected);
}
