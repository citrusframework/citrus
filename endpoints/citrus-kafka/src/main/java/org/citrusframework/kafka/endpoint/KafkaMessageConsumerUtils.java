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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

final class KafkaMessageConsumerUtils {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageConsumerUtils.class);

    static String resolveTopic(
            KafkaEndpointConfiguration kafkaEndpointConfiguration,
            TestContext testContext
    ) {
        return testContext.replaceDynamicContentInString(
                Optional.ofNullable(kafkaEndpointConfiguration.getTopic())
                        .orElseThrow(() -> new CitrusRuntimeException(
                                "Missing Kafka topic to receive messages from - add topic to endpoint configuration")));
    }

    static Message parseConsumerRecordsToMessage(
            List<ConsumerRecord<Object, Object>> consumerRecords,
            KafkaEndpointConfiguration endpointConfiguration,
            TestContext testContext
    ) {
         if (consumerRecords.size() > 1) {
            throw new CitrusRuntimeException("More than one matching record found in topic " + resolveTopic(endpointConfiguration, testContext));
        }

        if (logger.isDebugEnabled()) {
            consumerRecords.forEach(
                    record -> logger.debug("Received message: ({}, {}) at offset {}", record.key(), record.value(), record.offset()));
        }

        Message received = endpointConfiguration.getMessageConverter()
                .convertInbound(
                        consumerRecords.iterator().next(),
                        endpointConfiguration,
                        testContext);
        testContext.onInboundMessage(received);

        return received;
    }

    private KafkaMessageConsumerUtils() {
        // Static utility class
    }
}
