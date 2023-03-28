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

package org.citrusframework.kafka.embedded;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

/**
 * Standalone application provides a main cli entry for running an embedded Kafka server with embedded Zookeeper.
 * Kafka server properties can be set via property file or system properties.
 *
 * @author Christoph Deppisch
 * @since 2.8
 */
@PropertySource(value = "classpath:${citrus.kafka.server.properties.file:citrus-kafka-server.properties}", ignoreResourceNotFound = true)
public class EmbeddedKafkaServerApp {

    @Value("${citrus.kafka.server.topics:default.topic}")
    private String topics;

    @Value("${citrus.kafka.server.port:9092}")
    private int port;

    @Bean
    public EmbeddedKafkaServer kafkaServer() {
        return new EmbeddedKafkaServerBuilder()
                .kafkaServerPort(port)
                .topics(topics)
                .build();
    }

    /**
     * Main cli method.
     * @param args
     */
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(EmbeddedKafkaServerApp.class).start();
    }
}
