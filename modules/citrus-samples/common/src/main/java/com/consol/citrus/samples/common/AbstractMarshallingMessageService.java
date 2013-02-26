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

package com.consol.citrus.samples.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.xml.transformer.MarshallingTransformer;
import org.springframework.integration.xml.transformer.UnmarshallingTransformer;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractMarshallingMessageService<T, K> {
    @Autowired
    private UnmarshallingTransformer unmarshallingTransformer;
    
    @Autowired
    private MarshallingTransformer marshallingTransformer;
    
    @ServiceActivator
    public Message<?> processMessageInternal(Message<?> message) {
        Message<K> result = processMessage(unmarshalMessage(message));
        
        return marshalMessage(result);
    }
    
    public abstract Message<K> processMessage(Message<T> request);

    /**
     * Unmarshal message payload.
     * 
     * @param message
     * @return
     */
    @SuppressWarnings("unchecked") private Message<T> unmarshalMessage(Message<?> message) {
        T payload = (T) unmarshallingTransformer.transformPayload(message.getPayload());
        MessageBuilder<T> builder = MessageBuilder.withPayload(payload).copyHeaders(message.getHeaders());
        
        return builder.build();
    }
    
    /**
     * Marshal message payload. 
     * 
     * @param message
     * @return
     */
    private Message<?> marshalMessage(Message<K> message) {
        return marshallingTransformer.transform(message);
    }
}
