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

package org.citrusframework.kafka.message;

import org.citrusframework.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 * @since 2.8
 */
public final class KafkaMessageHeaders {

    /**
     * Prevent instantiation.
     */
    private KafkaMessageHeaders() {
    }

    public static final String KAFKA_PREFIX = MessageHeaders.PREFIX + "kafka_";

    public static final String TOPIC = KAFKA_PREFIX + "topic";

    public static final String OFFSET = KAFKA_PREFIX + "offset";

    public static final String PARTITION = KAFKA_PREFIX + "partition";

    public static final String MESSAGE_KEY = KAFKA_PREFIX + "messageKey";

    public static final String TIMESTAMP = KAFKA_PREFIX + "timestamp";
}
