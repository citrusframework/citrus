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
package com.consol.citrus.admin.converter.container;

import com.consol.citrus.admin.converter.actions.AbstractTestActionConverter;
import com.consol.citrus.admin.model.TestActionData;
import com.consol.citrus.container.RepeatOnErrorUntilTrue;
import com.consol.citrus.model.testcase.core.ObjectFactory;
import com.consol.citrus.model.testcase.core.RepeatOnerrorUntilTrueDefinition;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class RepeatOnErrorContainerConverter extends AbstractTestActionConverter<RepeatOnerrorUntilTrueDefinition, RepeatOnErrorUntilTrue> {

    /**
     * Default constructor using action type reference.
     */
    public RepeatOnErrorContainerConverter() {
        super("repeat-on-error");
    }

    @Override
    public TestActionData convert(RepeatOnerrorUntilTrueDefinition definition) {
        return null;
    }

    @Override
    public RepeatOnerrorUntilTrueDefinition convertModel(RepeatOnErrorUntilTrue definition) {
        RepeatOnerrorUntilTrueDefinition action = new ObjectFactory().createRepeatOnerrorUntilTrueDefinition();

        action.setDescription(definition.getDescription());
        action.setCondition(definition.getCondition());
        action.setAutoSleep(definition.getAutoSleep().toString());

        return action;
    }

    @Override
    public Class<RepeatOnerrorUntilTrueDefinition> getModelClass() {
        return RepeatOnerrorUntilTrueDefinition.class;
    }
}
