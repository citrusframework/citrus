/*
 * Copyright the original author or authors.
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

package org.citrusframework.kafka.config.xml;

import org.apache.commons.lang3.RandomStringUtils;
import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.endpoint.KafkaEndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static java.lang.Boolean.parseBoolean;
import static org.citrusframework.config.util.BeanDefinitionParserUtils.setPropertyReference;
import static org.citrusframework.config.util.BeanDefinitionParserUtils.setPropertyValue;
import static org.citrusframework.kafka.message.KafkaMessageHeaders.KAFKA_PREFIX;

/**
 * Bean definition parser for Kafka endpoint component.
 *
 * @since 2.8
 */
public class KafkaEndpointParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        setPropertyValue(endpointConfiguration, element.getAttribute("client-id"), "clientId");
        setPropertyValue(endpointConfiguration, element.getAttribute("server"), "server");
        setPropertyValue(endpointConfiguration, element.getAttribute("topic"), "topic");
        setPropertyValue(endpointConfiguration, element.getAttribute("partition"), "partition");

        setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
        setPropertyReference(endpointConfiguration, element.getAttribute("header-mapper"), "headerMapper");
        setPropertyReference(endpointConfiguration, element.getAttribute("producer-properties"), "producerProperties");
        setPropertyReference(endpointConfiguration, element.getAttribute("consumer-properties"), "consumerProperties");

        setPropertyValue(endpointConfiguration, element.getAttribute("auto-commit"), "autoCommit");
        setPropertyValue(endpointConfiguration, element.getAttribute("auto-commit-interval"), "autoCommitInterval");
        setPropertyValue(endpointConfiguration, element.getAttribute("offset-reset"), "offsetReset");

        if (parseBoolean(element.getAttribute("random-consumer-group"))) {
            setPropertyValue(endpointConfiguration, KAFKA_PREFIX + RandomStringUtils.insecure().nextAlphabetic(10).toLowerCase(), "consumerGroup");
        }else {
            setPropertyValue(endpointConfiguration, element.getAttribute("consumer-group"), "consumerGroup");
        }

        setPropertyValue(endpointConfiguration, element.getAttribute("key-serializer"), "keySerializer");
        setPropertyValue(endpointConfiguration, element.getAttribute("key-deserializer"), "keyDeserializer");
        setPropertyValue(endpointConfiguration, element.getAttribute("value-serializer"), "valueSerializer");
        setPropertyValue(endpointConfiguration, element.getAttribute("value-deserializer"), "valueDeserializer");

        setPropertyValue(endpointConfiguration, element.getAttribute("thread-safe-consumer"), "useThreadSafeConsumer");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return KafkaEndpoint.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass()  {
        return KafkaEndpointConfiguration.class;
    }
}
