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

package org.citrusframework.kafka.endpoint;

import org.citrusframework.common.ShutdownPhase;
import org.citrusframework.endpoint.AbstractEndpoint;

/**
 * Kafka message endpoint capable of sending/receiving messages from Kafka message destination. Either uses a Kafka connection factory or
 * a Spring Kafka template to connect with Kafka destinations.
 *
 * @author Christoph Deppisch
 * @since 2.8
 */
public class KafkaEndpoint extends AbstractEndpoint implements ShutdownPhase {

    /** Cached producer or consumer */
    private KafkaProducer kafkaProducer;
    private KafkaConsumer kafkaConsumer;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public KafkaEndpoint() {
        super(new KafkaEndpointConfiguration());
    }

    /**
     * Constructor with endpoint configuration.
     * @param endpointConfiguration
     */
    public KafkaEndpoint(KafkaEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public KafkaConsumer createConsumer() {
        if (kafkaConsumer == null) {
            kafkaConsumer = new KafkaConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return kafkaConsumer;
    }

    @Override
    public KafkaProducer createProducer() {
        if (kafkaProducer == null) {
            kafkaProducer = new KafkaProducer(getProducerName(), getEndpointConfiguration());
        }

        return kafkaProducer;
    }

    @Override
    public KafkaEndpointConfiguration getEndpointConfiguration() {
        return (KafkaEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void destroy() {
        if (kafkaConsumer != null) {
            kafkaConsumer.stop();
        }
    }
}
