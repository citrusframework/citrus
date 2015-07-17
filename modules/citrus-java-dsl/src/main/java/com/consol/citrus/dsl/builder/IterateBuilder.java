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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.container.Iterate;

/**
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class IterateBuilder extends AbstractTestActionContainerBuilder<Iterate> {

    /**
     * Default constructor using action container.
     * @param action
     */
    public IterateBuilder(Iterate action) {
        super(action);
    }

    /**
     * Default constructor.
     */
    public IterateBuilder() {
        super(new Iterate());
    }

    /**
     * Adds a condition to this iterate container.
     * @param condition
     * @return
     */
    public IterateBuilder condition(String condition) {
        action.setCondition(condition);
        return this;
    }
    
    /**
     * Sets the index variable name.
     * @param name
     * @return
     */
    public IterateBuilder index(String name) {
        action.setIndexName(name);
        return this;
    }
    
    /**
     * Sets the index start value.
     * @param index
     * @return
     */
    public IterateBuilder startsWith(int index) {
        action.setStart(index);
        return this;
    }
    
    /**
     * Sets the step for each iteration.
     * @param step
     * @return
     */
    public IterateBuilder step(int step) {
        action.setStep(step);
        return this;
    }

}
