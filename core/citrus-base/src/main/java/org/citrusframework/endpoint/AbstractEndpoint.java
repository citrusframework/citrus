/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.endpoint;

import org.citrusframework.TestActor;

/**
 * Abstract message endpoint handles send/receive timeout setting and test actors.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractEndpoint implements Endpoint {

    /** Endpoint name usually the Spring bean id */
    private String name = getClass().getSimpleName();

    /** Test actor linked to this endpoint */
    private TestActor actor;

    /** Endpoint configuration */
    private final EndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public AbstractEndpoint(EndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    /**
     * Gets the endpoints consumer name.
     * @return
     */
    public String getConsumerName() {
        return name + ":consumer";
    }

    /**
     * Gets the endpoints producer name.
     * @return
     */
    public String getProducerName() {
        return name + ":producer";
    }

    @Override
    public TestActor getActor() {
        return actor;
    }

    @Override
    public void setActor(TestActor actor) {
        this.actor = actor;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
