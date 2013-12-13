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
import org.springframework.integration.Message;

/**
 * Endpoint interface defines basic send and receive operations on a message endpoint.
 * @author Christoph Deppisch
 * @since 1.4
 */
public interface Endpoint {

    /**
     * Sends the message.
     * @param message the message object to send.
     */
    void send(Message<?> message);

    /**
     * Receive message with a given timeout.
     * @param timeout
     * @return
     */
    Message<?> receive(long timeout);

    /**
     * Gets the sending actor.
     * @return
     */
    TestActor getActor();

    /**
     * Gets the sender name usually the Spring bean name.
     * @return
     */
    String getName();
}
