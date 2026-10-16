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
 * Drag and drop action builder moves a source element onto a target element. Both elements
 * get located on the current page.
 */
public interface SeleniumDragAndDropActionBuilder<T extends TestAction, B extends SeleniumDragAndDropActionBuilder<T, B>>
        extends SeleniumElementActionBuilderBase<T, B> {

    /**
     * Sets the target element to drop the source element on.
     */
    B target(Object target);

    /**
     * Sets the target element to drop the source element on.
     */
    B target(String property, String propertyValue);
}
