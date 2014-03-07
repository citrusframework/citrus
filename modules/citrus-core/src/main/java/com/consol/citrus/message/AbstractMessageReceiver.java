/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.message;

import com.consol.citrus.endpoint.Endpoint;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.integration.Message;

import com.consol.citrus.TestActor;

/**
 * Abstract base class for message receiver implementations.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public abstract class AbstractMessageReceiver implements MessageReceiver, BeanNameAware {

    /** Message endpoint */
    private final Endpoint endpoint;

    protected AbstractMessageReceiver(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Message<?> receive() {
        return receive(endpoint.getEndpointConfiguration().getTimeout());
    }

    @Override
    public abstract Message<?> receive(long timeout);

    @Override
    public Message<?> receiveSelected(String selector) {
        return receiveSelected(selector, endpoint.getEndpointConfiguration().getTimeout());
    }

    @Override
    public abstract Message<?> receiveSelected(String selector, long timeout);

    /**
     * Setter for receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        endpoint.getEndpointConfiguration().setTimeout(receiveTimeout);
    }

    /**
     * Gets the receiveTimeout.
     * @return the receiveTimeout
     */
    public long getReceiveTimeout() {
        return endpoint.getEndpointConfiguration().getTimeout();
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return endpoint.getActor();
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        endpoint.setActor(actor);
    }

    @Override
    public void setBeanName(String name) {
        endpoint.setName(name);
    }

    @Override
    public String getName() {
        return endpoint.getName();
    }

    @Override
    public void setName(String name) {
        endpoint.setName(name);
    }
}
