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

package org.citrusframework;

/**
 * Test action builders implementing this interface receive the test action runner that is about to run them,
 * before the action is built. This includes builders nested in test action containers.
 * <p>
 * Injection is opt-in: only builders that declare this interface are touched.
 */
public interface TestActionRunnerAware {

    /**
     * Sets the test action runner that runs the built test action.
     * @param runner the active test action runner.
     */
    void setTestActionRunner(TestActionRunner runner);
}
