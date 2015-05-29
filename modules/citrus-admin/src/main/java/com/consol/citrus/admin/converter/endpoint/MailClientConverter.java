/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.admin.converter.endpoint;

import com.consol.citrus.admin.model.EndpointData;
import com.consol.citrus.message.MessageConverter;
import com.consol.citrus.model.config.mail.MailClientDefinition;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
@Component
public class MailClientConverter extends AbstractEndpointConverter<MailClientDefinition> {

    @Override
    public EndpointData convert(MailClientDefinition client) {
        EndpointData endpointData = new EndpointData(getEndpointType(), client.getId(), getModelClass());

        endpointData.add(property("host", client));
        endpointData.add(property("port", client, "25"));
        endpointData.add(property("protocol", client, JavaMailSenderImpl.DEFAULT_PROTOCOL));
        endpointData.add(property("username", client));
        endpointData.add(property("password", client));
        endpointData.add(property("properties", client));
        endpointData.add(property("messageConverter", client)
                .optionKey(MessageConverter.class.getName()));

        endpointData.add(property("actor", "TestActor", client));

        return endpointData;
    }

    @Override
    public Class<MailClientDefinition> getModelClass() {
        return MailClientDefinition.class;
    }
}
