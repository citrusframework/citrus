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
public class WaitActionConditionBuilder extends WaitConditionBuilder<ActionCondition> {


    /** Parent wait action builder */
    private final WaitBuilder builder;

    /**
     * Default constructor using fields.
     * @param action
     * @param condition
     */
    public WaitActionConditionBuilder(Wait action, ActionCondition condition, WaitBuilder builder) {
        super(action, condition);
        this.builder = builder;
    }

    /**
     * Sets the test action to execute and wait for.
     * @param action
     * @return
     */
    public WaitActionConditionBuilder action(TestAction action) {
        if (action instanceof TestActionBuilder) {
            getCondition().setAction(((TestActionBuilder) action).build());
            this.action.setAction(((TestActionBuilder) action).build());
            builder.actions(((TestActionBuilder) action).build());
        } else {
            getCondition().setAction(action);
            this.action.setAction(action);
            builder.actions(action);
        }
        return this;
    }

    @Override
    public WaitActionConditionBuilder ms(Long milliseconds) {
        return (WaitActionConditionBuilder) super.ms(milliseconds);
    }

    @Override
    public WaitActionConditionBuilder ms(String milliseconds) {
        return (WaitActionConditionBuilder) super.ms(milliseconds);
    }

    @Override
    public WaitActionConditionBuilder seconds(Long seconds) {
        return (WaitActionConditionBuilder) super.seconds(seconds);
    }

    @Override
    public WaitActionConditionBuilder seconds(String seconds) {
        return (WaitActionConditionBuilder) super.seconds(seconds);
    }

    @Override
    public WaitActionConditionBuilder interval(Long interval) {
        return (WaitActionConditionBuilder) super.interval(interval);
    }

    @Override
    public WaitActionConditionBuilder interval(String interval) {
        return (WaitActionConditionBuilder) super.interval(interval);
    }
}
