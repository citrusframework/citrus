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

package org.citrusframework.kafka.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citrusframework.annotations.CitrusEndpointConfig;
import org.citrusframework.kafka.message.KafkaMessageHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * @author Christoph Deppisch
 * @since 2.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "kafka")
public @interface KafkaEndpointConfig {

    /**
     * Topic name.
     * @return
     */
    String topic() default "";

    /**
     * Server connection url.
     * @return
     */
    String server() default "localhost:9092";

    /**
     * Server offset reset.
     * @return
     */
    String offsetReset() default "earliest";

    /**
     * Server auto commit mode.
     * @return
     */
    boolean autoCommit() default true;

    /**
     * Server auto commit interval.
     * @return
     */
    int autoCommitInterval() default 1000;

    /**
     * Topic partition.
     * @return
     */
    int partition() default 0;

    /**
     * Message converter reference.
     * @return
     */
    String messageConverter() default "";

    /**
     * Header mapper reference.
     * @return
     */
    String headerMapper() default "";

    /**
     * Key serializer reference.
     * @return
     */
    Class<? extends Serializer> keySerializer() default StringSerializer.class;

    /**
     * Key deserializer reference.
     * @return
     */
    Class<? extends Deserializer> keyDeserializer() default StringDeserializer.class;

    /**
     * Value serializer reference.
     * @return
     */
    Class<? extends Serializer> valueSerializer() default StringSerializer.class;

    /**
     * Value deserializer reference.
     * @return
     */
    Class<? extends Deserializer> valueDeserializer() default StringDeserializer.class;

    /**
     * Producer properties reference.
     * @return
     */
    String producerProperties() default "";

    /**
     * Consumer properties reference.
     * @return
     */
    String consumerProperties() default "";

    /**
     * Client id.
     * @return
     */
    String clientId() default "";

    /**
     * Consumer group id.
     * @return
     */
    String consumerGroup() default KafkaMessageHeaders.KAFKA_PREFIX + "group";

    /**
     * Timeout.
     * @return
     */
    long timeout() default 5000L;

    /**
     * Test actor.
     * @return
     */
    String actor() default "";
}
