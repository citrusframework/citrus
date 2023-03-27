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

package org.citrusframework.citrus.kafka.config.annotation;

import java.util.Map;

import org.citrusframework.citrus.TestActor;
import org.citrusframework.citrus.config.annotation.AnnotationConfigParser;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.citrus.kafka.endpoint.KafkaEndpointBuilder;
import org.citrusframework.citrus.kafka.message.KafkaMessageConverter;
import org.citrusframework.citrus.kafka.message.KafkaMessageHeaderMapper;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.8
 */
public class KafkaEndpointConfigParser implements AnnotationConfigParser<KafkaEndpointConfig, KafkaEndpoint> {

    @Override
    public KafkaEndpoint parse(KafkaEndpointConfig annotation, ReferenceResolver referenceResolver) {
        KafkaEndpointBuilder builder = new KafkaEndpointBuilder();

        String server = annotation.server();

        if (!StringUtils.hasText(server)) {
            throw new CitrusRuntimeException("Required server is missing for kafka configuration");
        }

        builder.server(server);
        builder.topic(annotation.topic());
        builder.partition(annotation.partition());

        builder.autoCommit(annotation.autoCommit());
        builder.autoCommitInterval(annotation.autoCommitInterval());
        builder.offsetReset(annotation.offsetReset());

        if (StringUtils.hasText(annotation.clientId())) {
            builder.clientId(annotation.clientId());
        }

        builder.consumerGroup(annotation.consumerGroup());

        if (StringUtils.hasText(annotation.producerProperties())) {
            builder.producerProperties(referenceResolver.resolve(annotation.producerProperties(), Map.class));
        }

        if (StringUtils.hasText(annotation.consumerProperties())) {
            builder.consumerProperties(referenceResolver.resolve(annotation.consumerProperties(), Map.class));
        }

        builder.keySerializer(annotation.keySerializer());
        builder.keyDeserializer(annotation.keyDeserializer());

        builder.valueSerializer(annotation.valueSerializer());
        builder.valueDeserializer(annotation.valueDeserializer());

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), KafkaMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.headerMapper())) {
            builder.headerMapper(referenceResolver.resolve(annotation.headerMapper(), KafkaMessageHeaderMapper.class));
        }

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
