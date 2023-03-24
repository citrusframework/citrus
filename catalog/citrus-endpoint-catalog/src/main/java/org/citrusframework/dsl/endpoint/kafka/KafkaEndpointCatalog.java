/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.dsl.endpoint.kafka;

import org.citrusframework.kafka.endpoint.KafkaEndpointBuilder;
import org.citrusframework.kafka.endpoint.builder.KafkaEndpoints;

/**
 * @author Christoph Deppisch
 */
public class KafkaEndpointCatalog {

    /**
     * Private constructor setting the sync and async builder implementation.
     */
    private KafkaEndpointCatalog() {
        // prevent direct instantiation
    }

    public static KafkaEndpointCatalog kafka() {
        return new KafkaEndpointCatalog();
    }

    /**
     * Gets the async endpoint builder.
     * @return
     */
    public KafkaEndpointBuilder asynchronous() {
        return KafkaEndpoints.kafka().asynchronous();
    }

    /**
     * Gets the sync endpoint builder.
     * @return
     */
    public KafkaEndpointBuilder synchronous() {
        return KafkaEndpoints.kafka().synchronous();
    }
}
