/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.samples.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.integration.xml.transformer.XmlPayloadMarshallingTransformer;
import org.springframework.integration.xml.transformer.XmlPayloadUnmarshallingTransformer;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public abstract class AbstractMarshallingMessageService<T, K> {
    @Autowired
    private XmlPayloadUnmarshallingTransformer unmarshallingTransformer;
    
    @Autowired
    private XmlPayloadMarshallingTransformer marshallingTransformer;
    
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
    @SuppressWarnings("unchecked")
    private Message<T> unmarshalMessage(Message<?> message) {
        T payload = (T) unmarshallingTransformer.transformPayload(message.getPayload());
        MessageBuilder<T> builder = MessageBuilder.withPayload(payload).copyHeaders(message.getHeaders());
        
        return builder.build();
    }
    
    /**
     * Marshal message message payload. 
     * 
     * @param message
     * @return
     */
    private Message<?> marshalMessage(Message<K> message) {
        String payload = marshallingTransformer.transformPayload(
                message.getPayload()).toString();

        MessageBuilder<String> builder = MessageBuilder.withPayload(payload)
                .copyHeaders(message.getHeaders());
        
        return builder.build();
    }
}
