/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.generate.provider.soap;

import com.consol.citrus.generate.provider.MessageActionProvider;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.model.testcase.ws.SendModel;
import com.consol.citrus.ws.message.SoapMessage;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SendSoapRequestActionProvider implements MessageActionProvider<SendModel, SoapMessage> {

    @Override
    public SendModel getAction(String endpoint, SoapMessage message) {
        SendModel request = new SendModel();

        request.setEndpoint(endpoint);

        request.setSoapAction(message.getSoapAction());

        com.consol.citrus.model.testcase.core.SendModel.Message sendMessage = new com.consol.citrus.model.testcase.core.SendModel.Message();
        sendMessage.setData(message.getPayload(String.class));
        request.setMessage(sendMessage);

        request.setContentType("application/xml");

        if (!CollectionUtils.isEmpty(message.getHeaders())) {
            com.consol.citrus.model.testcase.core.SendModel.Header header = new com.consol.citrus.model.testcase.core.SendModel.Header();

            message.getHeaders().entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith(MessageHeaders.PREFIX))
                .forEach(entry -> {
                    com.consol.citrus.model.testcase.core.SendModel.Header.Element element = new com.consol.citrus.model.testcase.core.SendModel.Header.Element();
                    element.setName(entry.getKey());
                    element.setValue(Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(""));

                    if (!element.getValue().getClass().equals(String.class)) {
                        element.setType(element.getValue().getClass().getSimpleName().toLowerCase());
                    }

                    header.getElements().add(element);
                });

            request.setHeader(header);
        }

        return request;
    }
}
