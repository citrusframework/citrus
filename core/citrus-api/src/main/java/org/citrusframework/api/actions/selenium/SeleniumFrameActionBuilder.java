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

public interface SeleniumFrameActionBuilder<T extends TestAction, B extends SeleniumFrameActionBuilder<T, B>>
        extends SeleniumElementActionBuilderBase<T, B> {

    /**
     * Switch into frame by zero-based index.
     */
    B index(int index);

    /**
     * Switch into frame by name or id.
     */
    B nameOrId(String nameOrId);

    /**
     * Switch to the parent frame.
     */
    B parent();

    /**
     * Switch to the top document.
     */
    B defaultContent();
}
