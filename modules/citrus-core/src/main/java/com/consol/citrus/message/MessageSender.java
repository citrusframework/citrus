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
import org.springframework.integration.Message;

import com.consol.citrus.TestActor;

/**
 * Basic message sender interface. Message senders are capable of publishing messages to a 
 * specific message endpoint. Each message transport may have dedicated message sender implementations.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public interface MessageSender extends Endpoint {
    /**
     * Sends the message.
     * @param message the message object to send.
     */
    void send(Message<?> message);
    
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
