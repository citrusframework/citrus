/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.condition.ActionCondition;
import com.consol.citrus.container.Wait;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class WaitActionConditionBuilder extends WaitConditionBuilder<ActionCondition, WaitActionConditionBuilder> {

    /** Parent wait action builder */
    private final Wait action;

    /**
     * Default constructor using fields.
     * @param condition
     * @param builder
     */
    public WaitActionConditionBuilder(Wait action, ActionCondition condition, WaitBuilder builder) {
        super(condition, builder);
        this.action = action;
    }

    /**
     * Sets the test action to execute and wait for.
     * @param action
     * @return
     */
    public Wait action(TestAction action) {
        if (action instanceof TestActionBuilder) {
            getCondition().setAction(((TestActionBuilder) action).build());
            this.action.setAction(((TestActionBuilder) action).build());
            getBuilder().actions(((TestActionBuilder) action).build());
        } else {
            getCondition().setAction(action);
            this.action.setAction(action);
            getBuilder().actions(action);
        }

        return getBuilder().build();
    }

    Wait getAction() {
        return action;
    }
}
