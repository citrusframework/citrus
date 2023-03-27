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

package org.citrusframework.citrus.generate.provider.soap;

import org.citrusframework.citrus.generate.provider.MessageActionProvider;
import org.citrusframework.citrus.message.MessageHeaders;
import org.citrusframework.citrus.model.testcase.ws.SendModel;
import org.citrusframework.citrus.ws.message.SoapMessage;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SendSoapResponseActionProvider implements MessageActionProvider<SendModel, SoapMessage> {

    @Override
    public SendModel getAction(String endpoint, SoapMessage message) {
        SendModel response = new SendModel();

        response.setEndpoint(endpoint);

        org.citrusframework.citrus.model.testcase.core.SendModel.Message sendMessage = new org.citrusframework.citrus.model.testcase.core.SendModel.Message();
        sendMessage.setData(message.getPayload(String.class));
        response.setMessage(sendMessage);

        if (!CollectionUtils.isEmpty(message.getHeaders())) {
            org.citrusframework.citrus.model.testcase.core.SendModel.Header header = new org.citrusframework.citrus.model.testcase.core.SendModel.Header();

            message.getHeaders().entrySet().stream()
                    .filter(entry -> !entry.getKey().startsWith(MessageHeaders.PREFIX))
                    .forEach(entry -> {
                        org.citrusframework.citrus.model.testcase.core.SendModel.Header.Element element = new org.citrusframework.citrus.model.testcase.core.SendModel.Header.Element();
                        element.setName(entry.getKey());
                        element.setValue(Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(""));

                        if (!element.getValue().getClass().equals(String.class)) {
                            element.setType(element.getValue().getClass().getSimpleName().toLowerCase());
                        }

                        header.getElements().add(element);
                    });

            response.setHeader(header);
        }

        return response;
    }
}
