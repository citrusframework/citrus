/*
 * Copyright 2006-2012 the original author or authors.
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

import com.consol.citrus.container.Iterate;

/**
 * @author Christoph Deppisch
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.IterateBuilder}
 */
public class IterateDefinition extends AbstractActionContainerDefinition<Iterate> {

    /**
     * Default constructor using action container.
     * @param action
     */
    public IterateDefinition(Iterate action) {
        super(action);
    }

    /**
     * Default constructor.
     */
    public IterateDefinition() {
        super(new Iterate());
    }

    /**
     * Adds a condition to this iterate container.
     * @param condition
     * @return
     */
    public IterateDefinition condition(String condition) {
        action.setCondition(condition);
        return this;
    }

    /**
     * Sets the index variable name.
     * @param name
     * @return
     */
    public IterateDefinition index(String name) {
        action.setIndexName(name);
        return this;
    }

    /**
     * Sets the index start value.
     * @param index
     * @return
     */
    public IterateDefinition startsWith(int index) {
        action.setStart(index);
        return this;
    }

    /**
     * Sets the step for each iteration.
     * @param step
     * @return
     */
    public IterateDefinition step(int step) {
        action.setStep(step);
        return this;
    }

}
