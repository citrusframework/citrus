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

import org.citrusframework.context.TestContext;

/**
 * Interface used when iterating test action containers evaluate
 * the iteration breaking condition.
 *
 * @since 2.3
 */
@FunctionalInterface
public interface IteratingConditionExpression {

    /**
     * Evaluates iteration breaking condition with given index.
     * @param index
     * @param context
     * @return
     */
    boolean evaluate(int index, TestContext context);
}
