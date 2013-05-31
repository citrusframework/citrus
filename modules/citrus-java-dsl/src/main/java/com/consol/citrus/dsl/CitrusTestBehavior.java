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

package com.consol.citrus.dsl;

import com.consol.citrus.TestAction;

import java.util.List;
import java.util.Map;

/**
 * Abstract Citrus test behavior provides interface method implementations for
 * behavior access and defines abstract apply method for subclasses to implement.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public abstract class CitrusTestBehavior extends CitrusTestBuilder implements TestBehavior {

    /**
     * Subclasses must overwrite this apply building method in order
     * to add test action logic.
     */
    public abstract void apply();

    /**
     * Get this apply's test actions.
     * @return
     */
    public List<TestAction> getTestActions() {
        return getTestCase().getActions();
    }

    /**
     * Get this apply's finally test actions.
     * @return
     */
    public List<TestAction> getFinallyActions() {
        return getTestCase().getFinallyChain();
    }

    /**
     * Get this apply's test variables.
     * @return
     */
    public Map<String, Object> getVariableDefinitions() {
        return getVariables();
    }
}
