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

public interface SeleniumWaitUntilActionBuilder<T extends TestAction, B extends SeleniumWaitUntilActionBuilder<T, B>>
        extends SeleniumElementActionBuilderBase<T, B> {

    /**
     * Add visible condition.
     */
    B visible();

    /**
     * Add hidden condition.
     */
    B hidden();

    /**
     * Add hidden condition.
     */
    B condition(String condition);

    /**
     * Add present condition.
     */
    B present();

    /**
     * Add absent condition.
     */
    B absent();

    /**
     * Add clickable condition.
     */
    B clickable();

    /**
     * Add text condition with expected text.
     */
    B text(String expected);

    /**
     * Add url condition with expected url.
     */
    B url(String expected);

    /**
     * Add title condition with expected title.
     */
    B title(String expected);

    /**
     * Add expected value used by wait conditions such as text, url and title.
     */
    B expected(String expected);

    /**
     * Add timeout condition.
     */
    B timeout(Long timeout);
}
