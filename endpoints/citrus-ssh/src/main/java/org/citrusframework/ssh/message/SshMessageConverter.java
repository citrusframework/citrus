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

package org.citrusframework.ssh.message;

import javax.xml.transform.Source;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.ssh.client.SshEndpointConfiguration;
import org.citrusframework.ssh.model.SshMessage;
import org.citrusframework.xml.StringResult;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
public class SshMessageConverter implements MessageConverter<SshMessage, SshMessage, SshEndpointConfiguration> {

    @Override
    public SshMessage convertOutbound(Message internalMessage, SshEndpointConfiguration endpointConfiguration, TestContext context) {
        Object payload = internalMessage.getPayload();

        SshMessage sshMessage = null;
        if (payload != null) {
            if (payload instanceof SshMessage) {
                sshMessage = (SshMessage) payload;
            } else {
                sshMessage = (SshMessage) endpointConfiguration.getSshMarshaller()
                        .unmarshal(internalMessage.getPayload(Source.class));
            }
        }

        if (sshMessage == null) {
            throw new CitrusRuntimeException("Unable to create proper ssh message from payload: " + payload);
        }

        return sshMessage;
    }

    @Override
    public void convertOutbound(SshMessage externalMessage, Message internalMessage, SshEndpointConfiguration endpointConfiguration, TestContext context) {
    }

    @Override
    public Message convertInbound(SshMessage externalMessage, SshEndpointConfiguration endpointConfiguration, TestContext context) {
        StringResult payload = new StringResult();
        endpointConfiguration.getSshMarshaller().marshal(externalMessage, payload);

        return new DefaultMessage(payload.toString());
    }

}
