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

import com.consol.citrus.dsl.design.TestDesigner;

/**
 * Test builder interface defines builder pattern methods for creating a new
 * Citrus test case.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 * @deprecated since 2.2.1 in favor of {@link com.consol.citrus.dsl.design.TestDesigner}
 */
public interface TestBuilder extends TestDesigner {

    /**
     * Apply test apply with all test actions, finally actions and test
     * variables defined in given apply.
     *
     * @param behavior
     */
    void applyBehavior(TestBehavior behavior);
}
