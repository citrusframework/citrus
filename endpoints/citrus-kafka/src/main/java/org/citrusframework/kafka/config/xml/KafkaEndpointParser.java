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

package org.citrusframework.kafka.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.endpoint.KafkaEndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for Kafka endpoint component.
 *
 * @author Christoph Deppisch
 * @since 2.8
 */
public class KafkaEndpointParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("client-id"), "clientId");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("server"), "server");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("topic"), "topic");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("partition"), "partition");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("header-mapper"), "headerMapper");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("producer-properties"), "producerProperties");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("consumer-properties"), "consumerProperties");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("auto-commit"), "autoCommit");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("auto-commit-interval"), "autoCommitInterval");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("offset-reset"), "offsetReset");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("consumer-group"), "consumerGroup");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("key-serializer"), "keySerializer");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("key-deserializer"), "keyDeserializer");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("value-serializer"), "valueSerializer");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("value-deserializer"), "valueDeserializer");
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
