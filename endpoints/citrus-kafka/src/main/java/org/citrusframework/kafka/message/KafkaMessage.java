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

package org.citrusframework.kafka.message;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;

import java.util.Map;

import static java.lang.String.format;

/**
 * @since 2.8
 */
public class KafkaMessage extends DefaultMessage {

    /**
     * Empty constructor initializing with empty message payload.
     */
    public KafkaMessage() {
        super();
    }

    /**
     * Default constructor using payload and headers.
     */
    public KafkaMessage(Object payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    /**
     * Default constructor using message payload.
     */
    public KafkaMessage(Object payload) {
        super(payload);
    }

    /**
     * Sets the Kafka partition id header.
     */
    public KafkaMessage partition(int partition) {
        setHeader(KafkaMessageHeaders.PARTITION, partition);
        return this;
    }

    /**
     * Sets the Kafka timestamp header.
     */
    public KafkaMessage timestamp(Long timestamp) {
        setHeader(KafkaMessageHeaders.TIMESTAMP, timestamp);
        return this;
    }

    /**
     * Sets the Kafka offset header.
     */
    public KafkaMessage offset(long offset) {
        setHeader(KafkaMessageHeaders.OFFSET, offset);
        return this;
    }

    /**
     * Sets the Kafka message key header.
     */
    public KafkaMessage messageKey(Object key) {
        setHeader(KafkaMessageHeaders.MESSAGE_KEY, key);
        return this;
    }

    /**
     * Sets the Kafka topic key header.
     */
    public KafkaMessage topic(String topic) {
        setHeader(KafkaMessageHeaders.TOPIC, topic);
        return this;
    }

    /**
     * Gets the Kafka partition header.
     */
    public Integer getPartition() {
        Object partition = getHeader(KafkaMessageHeaders.PARTITION);

        if (partition != null) {
            if (partition instanceof Integer partitionInteger) {
                return partitionInteger;
            } else if (partition instanceof String partitionString) {
                return Integer.parseInt(partitionString);
            }

            throw new CitrusRuntimeException(format("Failed to convert partition header to proper Integer value: %s", partition.getClass()));
        }

        return null;
    }

    /**
     * Gets the Kafka timestamp header.
     */
    public Long getTimestamp() {
        Object timestamp = getHeader(KafkaMessageHeaders.TIMESTAMP);

        if (timestamp != null) {
            return Long.valueOf(timestamp.toString());
        }

        return null;
    }

    /**
     * Gets the Kafka offset header.
     */
    public Long getOffset() {
        Object offset = getHeader(KafkaMessageHeaders.OFFSET);

        if (offset != null) {
            if (offset instanceof Long longOffset) {
                return longOffset;
            } else if (offset instanceof String stringOffset) {
                return Long.parseLong(stringOffset);
            }

            throw new CitrusRuntimeException(format("Failed to convert partition header to proper Long value: %s", offset.getClass()));
        }

        return 0L;
    }

    /**
     * Gets the Kafka message key header.
     */
    public Object getMessageKey() {
        return getHeader(KafkaMessageHeaders.MESSAGE_KEY);
    }

    /**
     * Gets the Kafka topic header.
     */
    public String getTopic() {
        Object topic = getHeader(KafkaMessageHeaders.TOPIC);

        if (topic != null) {
            return topic.toString();
        }

        return null;
    }
}
