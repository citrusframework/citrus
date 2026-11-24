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

package org.citrusframework.junit.jupiter;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionRunner;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.context.TestContext;

/**
 * Test case runner that only builds test actions instead of executing them. Useful for unit
 * testing TestLoader implementations without running the loaded actions.
 */
// Inherits an unchecked generic override from DefaultTestCaseRunner; javac reports the
// unchecked conversion against the subclass, so suppress at class level.
@SuppressWarnings("unchecked")
public class NoopTestCaseRunner extends DefaultTestCaseRunner {

    public NoopTestCaseRunner(TestContext context) {
        super(context);
    }

    @Override
    public <T extends TestAction> TestActionRunner run(TestActionBuilder<T> builder) {
        builder.build();
        return this;
    }
}
