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

package org.citrusframework.api.common;

import org.citrusframework.context.TestContext;

@FunctionalInterface
public interface InitializingPhase {

    /**
     * Initialize component.
     */
    void initialize();

    /**
     * Initialize component with given test context.
     * The given test context is the context where the component is being created with.
     * Test variables and other dynamic components may be resolved during initialization using this context.
     * The initialization is only called once when the component is created.
     * The default implementation delegates to the initialize logic without the test context but subclasses may add their own logic.
     * @param context the context when the component is created.
     */
    default void initialize(TestContext context) {
        initialize();
    }
}
