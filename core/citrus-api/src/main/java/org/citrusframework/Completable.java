/*
 * Copyright 2006-2018 the original author or authors.
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
 * Interface indicates that test action is aware of its completed state. This is used when test case needs to wait for nested actions to finish
 * working before closing the test case. Asynchronous test action execution may implement this interface in order to publish the completed state of
 * forked processes.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public interface Completable {

    /**
     * Checks for test action to be finished.
     * @return
     */
    boolean isDone(TestContext context);
}
