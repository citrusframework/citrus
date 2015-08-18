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

import com.consol.citrus.container.Sequence;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class SequenceBuilder extends AbstractTestContainerBuilder<Sequence> {

    /**
     * Default constructor using designer and action container.
     * @param designer
     * @param action
     */
    public SequenceBuilder(TestDesigner designer, Sequence action) {
        super(designer, action);
    }

    /**
     * Default constructor using test designer.
     * @param designer
     */
    public SequenceBuilder(TestDesigner designer) {
        this(designer, new Sequence());
    }

    /**
     * Default constructor using runner and action container.
     * @param runner
     * @param action
     */
    public SequenceBuilder(TestRunner runner, Sequence action) {
        super(runner, action);
    }

    /**
     * Default constructor using test runner.
     * @param runner
     */
    public SequenceBuilder(TestRunner runner) {
        this(runner, new Sequence());
    }

}
