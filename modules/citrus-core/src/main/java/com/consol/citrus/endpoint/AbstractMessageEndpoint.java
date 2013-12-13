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
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.integration.Message;

/**
 * Abstract message endpoint handles send/receive timeout setting and test actors.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractMessageEndpoint implements Endpoint, BeanNameAware {

    /** Send/receive timeout setting */
    private long timeout = 5000L;

    /** Test actor linked to this endpoint */
    private TestActor actor;

    /** Endpoint name */
    private String name = getClass().getSimpleName();

    public Message<?> receive() {
        return receive(timeout);
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
        return timeout;
    }

    /**
     * Sets the timeout for sending and receiving messages..
     * @param timeout
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the endpoint's name - usually the Spring bean name.
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }
}
