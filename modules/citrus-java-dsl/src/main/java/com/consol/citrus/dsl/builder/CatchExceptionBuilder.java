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

import com.consol.citrus.container.Catch;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class CatchExceptionBuilder extends AbstractExceptionContainerBuilder<Catch> {

    /**
     * Constructor using action field.
     * @param designer
     * @param action
     */
    public CatchExceptionBuilder(TestDesigner designer, Catch action) {
        super(designer, action);
    }

    /**
     * Default constructor.
     * @param designer
     */
    public CatchExceptionBuilder(TestDesigner designer) {
        this(designer, new Catch());
    }

    /**
     * Default constructor using runner and action container.
     * @param runner
     * @param action
     */
    public CatchExceptionBuilder(TestRunner runner, Catch action) {
        super(runner, action);
    }

    /**
     * Default constructor using test runner.
     * @param runner
     */
    public CatchExceptionBuilder(TestRunner runner) {
        this(runner, new Catch());
    }

    /**
     * Catch exception type during execution.
     * @param exception
     * @return
     */
    public CatchExceptionBuilder exception(Class<? extends Throwable> exception) {
        action.setException(exception.getName());
        return this;
    }

    /**
     * Catch exception type during execution.
     * @param type
     */
    public CatchExceptionBuilder exception(String type) {
        action.setException(type);
        return this;
    }
}
