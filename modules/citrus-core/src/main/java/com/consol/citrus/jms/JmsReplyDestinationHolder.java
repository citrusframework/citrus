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

package com.consol.citrus.jms;

import javax.jms.Destination;

import com.consol.citrus.message.MessageReceiver;

/**
 * Reply destination holder interface for getting reply destinations in synchronous
 * JMS communication.
 * 
 * {@link MessageReceiver} implementation receives synchronous messages and saves the 
 * reply destination. Reply message senders may ask for the saved reply destination in order to
 * provide proper reply message.
 * 
 * @author Christoph Deppisch
 */
public interface JmsReplyDestinationHolder {
    /**
     * Get the reply destination with a correlation key. Usually used in multi threaded and parallel
     * testing where destination holder has to manage several destinations at a time.
     * 
     * Destinations are saves and loaded using a correlation key.  
     * @param correlationKey
     * @return
     */
    Destination getReplyDestination(String correlationKey);
    
    /**
     * Get the next reply destination. Used in single threaded testing where only one
     * destination is managed by the destination holder at a time.
     * @return
     */
    Destination getReplyDestination();
}
