/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.telnet.message;

import javax.xml.transform.Source;

import org.springframework.xml.transform.StringResult;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageConverter;
import com.consol.citrus.telnet.client.TelnetEndpointConfiguration;
import com.consol.citrus.telnet.model.TelnetMessage;

/**
 * @author Donat Müller
 * @since 2.6
 */
public class TelnetMessageConverter implements MessageConverter<TelnetMessage, TelnetEndpointConfiguration> {

    @Override
    public TelnetMessage convertOutbound(Message internalMessage, TelnetEndpointConfiguration endpointConfiguration) {
        Object payload = internalMessage.getPayload();

        TelnetMessage telnetMessage = null;
        if (payload != null) {
            if (payload instanceof TelnetMessage) {
                telnetMessage = (TelnetMessage) payload;
            } else {
                telnetMessage = (TelnetMessage) endpointConfiguration.getTelnetMarshaller()
                        .unmarshal(internalMessage.getPayload(Source.class));
            }
        }

        if (telnetMessage == null) {
            throw new CitrusRuntimeException("Unable to create proper telnet message from paylaod: " + payload);
        }

        return telnetMessage;
    }

    @Override
    public void convertOutbound(TelnetMessage externalMessage, Message internalMessage, TelnetEndpointConfiguration endpointConfiguration) {
    }

    @Override
    public Message convertInbound(TelnetMessage externalMessage, TelnetEndpointConfiguration endpointConfiguration) {
        StringResult payload = new StringResult();
        endpointConfiguration.getTelnetMarshaller().marshal(externalMessage, payload);

        return new DefaultMessage(payload.toString());
    }

}
