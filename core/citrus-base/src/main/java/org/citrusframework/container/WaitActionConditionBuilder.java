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

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.condition.ActionCondition;

/**
 * @since 2.4
 */
public class WaitActionConditionBuilder extends WaitConditionBuilder<ActionCondition, WaitActionConditionBuilder>
        implements WaitContainerBuilder.ActionConditionBuilder<Wait, ActionCondition, WaitActionConditionBuilder> {

    /**
     * Default constructor using fields.
     * @param builder
     */
    public WaitActionConditionBuilder(Wait.Builder<ActionCondition> builder) {
        super(builder);
    }

    @Override
    public WaitActionConditionBuilder action(TestAction action) {
        getCondition().setAction(action);
        return this;
    }

    @Override
    public WaitActionConditionBuilder action(TestActionBuilder<?> action) {
        getCondition().setAction(action.build());
        return this;
    }
}
