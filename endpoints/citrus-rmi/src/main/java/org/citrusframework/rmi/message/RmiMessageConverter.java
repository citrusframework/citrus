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

package org.citrusframework.rmi.message;

import javax.xml.transform.Source;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.rmi.endpoint.RmiEndpointConfiguration;
import org.citrusframework.rmi.model.RmiServiceInvocation;
import org.citrusframework.util.StringUtils;
import org.citrusframework.xml.StringResult;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiMessageConverter implements MessageConverter<RmiServiceInvocation, RmiServiceInvocation, RmiEndpointConfiguration> {

    @Override
    public RmiServiceInvocation convertOutbound(Message internalMessage, RmiEndpointConfiguration endpointConfiguration, TestContext context) {
        RmiServiceInvocation serviceInvocation = getServiceInvocation(internalMessage, endpointConfiguration);
        convertOutbound(serviceInvocation, internalMessage, endpointConfiguration, context);
        return serviceInvocation;
    }

    @Override
    public void convertOutbound(RmiServiceInvocation serviceInvocation, Message internalMessage, RmiEndpointConfiguration endpointConfiguration, TestContext context) {
        if (internalMessage.getHeader(RmiMessageHeaders.RMI_METHOD) != null) {
            serviceInvocation.setMethod(internalMessage.getHeader(RmiMessageHeaders.RMI_METHOD).toString());
        } else if (StringUtils.hasText(endpointConfiguration.getMethod())) {
            serviceInvocation.setMethod(endpointConfiguration.getMethod());
        }
    }

    @Override
    public Message convertInbound(RmiServiceInvocation serviceInvocation, RmiEndpointConfiguration endpointConfiguration, TestContext context) {
        StringResult payload = new StringResult();
        endpointConfiguration.getMarshaller().marshal(serviceInvocation, payload);

        return new DefaultMessage(payload.toString())
                .setHeader(RmiMessageHeaders.RMI_INTERFACE, serviceInvocation.getRemote())
                .setHeader(RmiMessageHeaders.RMI_METHOD, serviceInvocation.getMethod());
    }

    /**
     * Reads Citrus internal RMI message model object from message payload. Either payload is actually a service invocation object or
     * XML payload String is unmarshalled to proper object representation.
     *
     * @param message
     * @param endpointConfiguration
     * @return
     */
    private RmiServiceInvocation getServiceInvocation(Message message, RmiEndpointConfiguration endpointConfiguration) {
        Object payload = message.getPayload();

        RmiServiceInvocation serviceInvocation = null;
        if (payload != null) {
            if (payload instanceof RmiServiceInvocation) {
                serviceInvocation = (RmiServiceInvocation) payload;
            } else if (payload != null && StringUtils.hasText(message.getPayload(String.class))) {
                serviceInvocation = (RmiServiceInvocation) endpointConfiguration.getMarshaller()
                        .unmarshal(message.getPayload(Source.class));
            } else {
                serviceInvocation = new RmiServiceInvocation();
            }
        }

        return serviceInvocation;
    }
}
