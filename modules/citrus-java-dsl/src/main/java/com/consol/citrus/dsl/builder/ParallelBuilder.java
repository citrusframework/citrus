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

import com.consol.citrus.container.*;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ParallelBuilder extends AbstractTestContainerBuilder<Parallel> {

    /**
     * Default constructor using designer and action container.
     * @param designer
     * @param action
     */
    public ParallelBuilder(TestDesigner designer, Parallel action) {
        super(designer, action);
    }

    /**
     * Default constructor using test designer.
     * @param designer
     */
    public ParallelBuilder(TestDesigner designer) {
        this(designer, new Parallel());
    }

    /**
     * Default constructor using runner and action container.
     * @param runner
     * @param action
     */
    public ParallelBuilder(TestRunner runner, Parallel action) {
        super(runner, action);
    }

    /**
     * Default constructor using test runner.
     * @param runner
     */
    public ParallelBuilder(TestRunner runner) {
        this(runner, new Parallel());
    }

}
