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

package com.consol.citrus.endpoint;

import com.consol.citrus.TestActor;
import com.consol.citrus.report.MessageListeners;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract message endpoint handles send/receive timeout setting and test actors.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractEndpoint implements Endpoint, BeanNameAware {

    @Autowired(required = false)
    private MessageListeners messageListener;

    /** Test actor linked to this endpoint */
    private TestActor actor;

    /** Endpoint configuration */
    private final EndpointConfiguration endpointConfiguration;

    protected AbstractEndpoint(EndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return actor;
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        this.actor = actor;
    }

    /**
     * Gets the timeout for sending and receiving messages.
     * @return
     */
    public long getTimeout() {
        return endpointConfiguration.getTimeout();
    }

    /**
     * Sets the timeout for sending and receiving messages..
     * @param timeout
     */
    public void setTimeout(long timeout) {
        endpointConfiguration.setTimeout(timeout);
    }

    /**
     * Gets the endpoint's name - usually the Spring bean name.
     * @return
     */
    public String getName() {
        return endpointConfiguration.getEndpointName();
    }

    @Override
    public void setBeanName(String name) {
        endpointConfiguration.setEndpointName(name);
    }

    /**
     * Sets the message listeners.
     * @param messageListener
     */
    public void setMessageListener(MessageListeners messageListener) {
        endpointConfiguration.setMessageListener(messageListener);
    }


}
