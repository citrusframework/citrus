/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.condition;

import org.citrusframework.context.TestContext;

/**
 * Tests whether a condition is satisfied.
 *
 * @author Martin Maher
 * @since 2.4
 */
public interface Condition {

    /**
     * Gets the condition name.
     * @return
     */
    String getName();

    /**
     * Tests the condition returning true if it is satisfied.
     *
     * @param context the citrus test context
     * @return true when satisfied otherwise false
     */
    boolean isSatisfied(TestContext context);

    /**
     * Construct proper success message for this condition.
     * @param context the citrus test context
     * @return
     */
    String getSuccessMessage(TestContext context);

    /**
     * Construct proper error message for this condition.
     * @param context the citrus test context
     * @return
     */
    String getErrorMessage(TestContext context);
}
