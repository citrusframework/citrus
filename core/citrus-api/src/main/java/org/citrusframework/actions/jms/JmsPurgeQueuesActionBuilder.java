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

package org.citrusframework.actions.jms;

import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;

public interface JmsPurgeQueuesActionBuilder<T extends TestAction, B extends JmsPurgeQueuesActionBuilder<T, B>>
        extends ActionBuilder<T, B>, ReferenceResolverAwareBuilder<T, B>, TestActionBuilder<T> {

    /**
     * Sets the Connection factory.
     * @param connectionFactory the queueConnectionFactory to set
     */
    B connectionFactory(Object connectionFactory);

    /**
     * List of queues to purge in this action.
     * @param queues The queues which are to be purged.
     */
    B queues(List<?> queues);

    /**
     * Queues to purge in this action.
     * @param queues The queues which are to be purged.
     */
    B queues(Object... queues);

    /**
     * Adds a new queue to the list of queues to purge in this action.
     */
    B queue(Object queue);

    /**
     * List of queue names to purge in this action.
     * @param names the queueNames to set
     */
    B queueNames(List<String> names);

    /**
     * List of queue names to purge in this action.
     */
    B queueNames(String... names);

    /**
     * Adds a queue name to the list of queues to purge in this action.
     */
    B queue(String name);

    /**
     * Receive timeout for reading message from a destination.
     * @param receiveTimeout the receiveTimeout to set
     */
    B timeout(long receiveTimeout);

    /**
     * Sets the sleepTime.
     * @param millis the sleepTime to set
     */
    B sleep(long millis);

}
