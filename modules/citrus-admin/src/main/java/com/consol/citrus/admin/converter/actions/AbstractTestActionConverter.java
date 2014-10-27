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

import com.consol.citrus.TestAction;
import com.consol.citrus.admin.converter.AbstractObjectConverter;
import com.consol.citrus.admin.model.TestActionData;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractTestActionConverter<S, R extends TestAction> extends AbstractObjectConverter<TestActionData, S> implements TestActionConverter<S, R> {

    private final String actionType;

    /**
     * Default constructor using action type reference.
     * @param actionType
     */
    protected AbstractTestActionConverter(String actionType) {
        this.actionType = actionType;
    }

    /**
     * Adds basic action properties using reflection on definition objects.
     * @param action
     * @param definition
     */
    protected void addActionProperties(TestActionData action, S definition) {
        action.add(property("description", definition));
    }


    @Override
    public String getActionType() {
        return actionType;
    }
}
