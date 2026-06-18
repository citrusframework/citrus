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

package org.citrusframework.actions.testcontainers;

import org.citrusframework.TestAction;

public interface TestcontainersKafkaStartActionBuilder<C extends AutoCloseable, T extends TestAction, B extends TestcontainersKafkaStartActionBuilder<C, T, B>>
        extends TestcontainersStartActionBuilderBase<C, T, B> {

    /**
     * Kafka implementation variant (usually one of Confluent or Apache).
     */
    B implementation(String implementation);

    /**
     * Sets a fixed port for the Kafka container.
     */
    B port(int port);

    B version(String kafkaVersion);

    B topics(String... topics);
}
