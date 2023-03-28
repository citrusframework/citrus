/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework;

import org.citrusframework.context.TestContext;

/**
 * Interface for all test actions.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
@FunctionalInterface
public interface TestAction {
    /**
     * Main execution method doing all work
     * @param context
     */
    void execute(TestContext context);

    /**
     * Name of test action injected as Spring bean name
     * @return name as String
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Checks if this action is disabled.
     * @param context the current test context.
     * @return true if action is marked disabled.
     */
    default boolean isDisabled(TestContext context) {
        return false;
    }

    /**
     * Gets the test actor associated with this test action.
     * @return
     */
    default TestActor getActor() {
        return null;
    }

}
