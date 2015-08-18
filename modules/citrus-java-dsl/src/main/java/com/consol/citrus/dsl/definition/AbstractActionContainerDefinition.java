/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.TestAction;
import com.consol.citrus.container.TestActionContainer;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.AbstractTestContainerBuilder}
 */
public class AbstractActionContainerDefinition<T extends TestActionContainer> extends AbstractActionDefinition<T> {

    /**
     * Default constructor with test action.
     * @param action
     */
    public AbstractActionContainerDefinition(T action) {
        super(action);
    }

    /**
     * Gets the test action container nested actions.
     * @return
     */
    public List<TestAction> getActions() {
        return super.getAction().getActions();
    }
}
