/*
 * Copyright the original author or authors.
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

package org.citrusframework.actions;

import org.citrusframework.TestAction;
import org.citrusframework.context.TestContext;

/**
 * Special test action doing nothing but implementing the test action interface. This is useful during Java dsl fluent API
 * that needs to return a test action but this should not be included or executed during the test run. See test behavior applying
 * test actions.
 *
 */
public class NoopTestAction implements TestAction {

    @Override
    public void execute(TestContext context) {
        // do nothing
    }

    @Override
    public String getName() {
        return "noop";
    }
}
