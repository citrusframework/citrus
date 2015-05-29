/*
 * Copyright 2006-2013 the original author or authors.
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
package com.consol.citrus.admin.converter.actions;

import com.consol.citrus.actions.SleepAction;
import com.consol.citrus.admin.model.TestActionData;
import com.consol.citrus.model.testcase.core.*;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
@Component
public class SleepActionConverter extends AbstractTestActionConverter<SleepDefinition, SleepAction> {

    public SleepActionConverter() {
        super("sleep");
    }

    @Override
    public TestActionData convert(SleepDefinition definition) {
        TestActionData action = new TestActionData(getActionType(), getModelClass());

        addActionProperties(action, definition);

        action.add(property("milliseconds", definition));
        action.add(property("seconds", definition));

        return action;
    }

    @Override
    public SleepDefinition convertModel(SleepAction definition) {
        SleepDefinition action = new ObjectFactory().createSleepDefinition();

        action.setDescription(definition.getDescription());
        action.setMilliseconds(definition.getMilliseconds());
        action.setSeconds(definition.getSeconds());

        return action;
    }

    @Override
    public Class<SleepDefinition> getModelClass() {
        return SleepDefinition.class;
    }
}
