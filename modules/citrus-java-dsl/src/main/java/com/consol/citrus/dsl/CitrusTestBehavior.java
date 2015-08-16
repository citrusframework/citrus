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

package com.consol.citrus.dsl;

import com.consol.citrus.TestAction;
import com.consol.citrus.dsl.util.PositionHandle;

/**
 * Abstract Citrus test behavior provides interface method implementations for
 * behavior access and defines abstract apply method for subclasses to implement.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 * @deprecated since 2.3 in favour of using {@link com.consol.citrus.dsl.design.AbstractTestBehavior}
 */
public abstract class CitrusTestBehavior extends CitrusTestBuilder implements TestBehavior {

    /** Target test builder to add actions and variables on */
    private TestBuilder target;

    /**
     * Subclasses must overwrite this apply building method in order
     * to add test action logic.
     */
    public abstract void apply();

    @Override
    public void apply(TestBuilder target) {
        this.target = target;
        apply();
    }

    @Override
    public void variable(String name, Object value) {
        target.variable(name, value);
    }

    @Override
    public void action(TestAction testAction) {
        target.action(testAction);
    }

    @Override
    public void doFinally(TestAction... actions) {
        target.doFinally(actions);
    }

    @Override
    public PositionHandle positionHandle() {
        return target.positionHandle();
    }
}
