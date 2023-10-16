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

import java.io.IOException;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.endpoint.KafkaEndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;

/**
 * Basic message converter for converting Spring Integration message implementations to Kafka
 * messages and vice versa. Converter combines message converting logic and header mapping.
 * Usually the message's payload is extracted to the Kafka message payload and default Kafka headers are mapped.
 *
 * @author Christoph Deppisch
 * @since 2.8
 */
public class KafkaMessageConverter implements MessageConverter<ConsumerRecord<Object, Object>, ProducerRecord<Object, Object>, KafkaEndpointConfiguration> {

    @Override
    public ProducerRecord<Object, Object> convertOutbound(Message internalMessage, KafkaEndpointConfiguration endpointConfiguration, TestContext context) {
        Object payload;
        if (internalMessage.getPayload() instanceof String) {
            payload = context.replaceDynamicContentInString(internalMessage.getPayload(String.class));
        } else if (internalMessage.getPayload() instanceof Resource) {
            try {
                payload = context.replaceDynamicContentInString(FileUtils.readToString(internalMessage.getPayload(Resource.class)));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read payload resource");
            }
        } else {
            payload = internalMessage.getPayload();
        }

        KafkaMessage kafkaMessage;

        if (internalMessage instanceof KafkaMessage) {
            kafkaMessage = (KafkaMessage) internalMessage;
        } else {
            kafkaMessage = new KafkaMessage(internalMessage.getPayload(), internalMessage.getHeaders());
        }

        return new ProducerRecord<>(Optional.ofNullable(kafkaMessage.getTopic()).map(context::replaceDynamicContentInString).orElseGet(() -> context.replaceDynamicContentInString(endpointConfiguration.getTopic())),
                                    Optional.ofNullable(kafkaMessage.getPartition()).orElseGet(endpointConfiguration::getPartition),
                                    kafkaMessage.getMessageKey(),
                                    payload,
                                    endpointConfiguration.getHeaderMapper().toHeaders(kafkaMessage.getHeaders(), context));
    }

    @Override
    public void convertOutbound(ProducerRecord<Object, Object> externalMessage, Message internalMessage, KafkaEndpointConfiguration endpointConfiguration, TestContext context) {
    }

    @Override
    public Message convertInbound(ConsumerRecord<Object, Object> consumerRecord, KafkaEndpointConfiguration endpointConfiguration, TestContext context) {
        if (consumerRecord == null) {
            return null;
        }
        return new KafkaMessage(consumerRecord.value(), endpointConfiguration.getHeaderMapper().fromHeaders(consumerRecord.headers()))
                                    .topic(consumerRecord.topic())
                                    .timestamp(consumerRecord.timestamp())
                                    .partition(consumerRecord.partition())
                                    .offset(consumerRecord.offset())
                                    .messageKey(consumerRecord.key());
    }
}
