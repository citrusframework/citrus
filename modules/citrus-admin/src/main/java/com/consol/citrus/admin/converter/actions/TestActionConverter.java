/*
 * Copyright 2006-2014 the original author or authors.
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
import com.consol.citrus.admin.converter.ObjectConverter;
import com.consol.citrus.admin.model.TestActionData;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public interface TestActionConverter<S, T extends TestAction> extends ObjectConverter<TestActionData, S> {


    @Override
    TestActionData convert(S definition);

    /**
     * Gets the action type name.
     * @return
     */
    String getActionType();

    /**
     * Converts raw test action Java model to JaxB object model source.
     * @param model
     * @return
     */
    S convertModel(T model);
}
