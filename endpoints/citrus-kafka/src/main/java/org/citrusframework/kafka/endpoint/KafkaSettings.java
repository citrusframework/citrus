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

package org.citrusframework.kafka.endpoint;

import org.citrusframework.config.CitrusConfigProperties;
import org.citrusframework.config.CitrusConfigProperty;

@CitrusConfigProperties(prefix = "citrus.kafka", description = "Kafka endpoint settings")
public final class KafkaSettings {

    private static final String KAFKA_PROPERTY_PREFIX = "citrus.kafka.";
    private static final String KAFKA_ENV_PREFIX = "CITRUS_KAFKA_";

    @CitrusConfigProperty(description = "When enabled, dynamic Kafka endpoint URIs automatically use a unique consumer group so that each consumer independently receives all messages from its subscribed topic.", type = "java.lang.Boolean", defaultValue = "true")
    private static final String DYNAMIC_CONSUMER_GROUP_PROPERTY = KAFKA_PROPERTY_PREFIX + "dynamic.consumer.group";
    private static final String DYNAMIC_CONSUMER_GROUP_ENV = KAFKA_ENV_PREFIX + "DYNAMIC_CONSUMER_GROUP";
    private static final String DYNAMIC_CONSUMER_GROUP_DEFAULT = "true";

    private KafkaSettings() {
    }

    /**
     * When enabled, dynamic Kafka endpoint URIs automatically use a unique consumer group
     * so that each consumer independently receives all messages from its subscribed topic.
     */
    public static boolean isDynamicConsumerGroup() {
        return Boolean.parseBoolean(System.getProperty(DYNAMIC_CONSUMER_GROUP_PROPERTY,
                System.getenv(DYNAMIC_CONSUMER_GROUP_ENV) != null ? System.getenv(DYNAMIC_CONSUMER_GROUP_ENV) : DYNAMIC_CONSUMER_GROUP_DEFAULT));
    }
}
