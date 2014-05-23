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

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.admin.converter.ObjectConverter;
import com.consol.citrus.model.testcase.core.ObjectFactory;
import com.consol.citrus.model.testcase.core.Receive;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ReceiveMessageActionConverter implements ObjectConverter<Receive, ReceiveMessageAction> {

    @Override
    public Receive convert(ReceiveMessageAction definition) {
        Receive action = new ObjectFactory().createReceive();

        if (definition.getActor() != null) {
            action.setActor(definition.getActor().getName());
        } else if (definition.getEndpoint().getActor() != null) {
            action.setActor(definition.getEndpoint().getActor().getName());
        }

        action.setDescription(definition.getDescription());
        action.setWith(definition.getEndpoint().getName());

        return action;
    }

    /**
     * Gets the model class usually the jaxb model class.
     *
     * @return
     */
    @Override
    public Class<ReceiveMessageAction> getModelClass() {
        return ReceiveMessageAction.class;
    }
}
