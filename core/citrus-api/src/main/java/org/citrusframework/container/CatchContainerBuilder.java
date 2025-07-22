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

package org.citrusframework.container;

import org.citrusframework.TestActionContainerBuilder;

public interface CatchContainerBuilder<T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>>
        extends ExceptionContainerBuilder<T, B>, TestActionContainerBuilder<T, B> {

    /**
     * Catch exception type during execution.
     * @param exception
     * @return
     */
    CatchContainerBuilder<T, B> exception(Class<? extends Throwable> exception);

    /**
     * Catch exception type during execution.
     * @param type
     * @return
     */
    CatchContainerBuilder<T, B> exception(String type);

    interface BuilderFactory {

        CatchContainerBuilder<?, ?> catchException();

    }
}
