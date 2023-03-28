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
import org.citrusframework.kafka.embedded.EmbeddedKafkaServer;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for embedded Kafka server component.
 *
 * @author Christoph Deppisch
 * @since 2.8
 */
public class KafkaEmbeddedServerParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(EmbeddedKafkaServer.class);

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("log-dir-path"), "logDirPath");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("auto-delete-logs"), "autoDeleteLogs");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("zookeeper-port"), "zookeeperPort");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("kafka-server-port"), "kafkaServerPort");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("topics"), "topics");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("partitions"), "partitions");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("broker-properties"), "brokerProperties");

        return builder.getBeanDefinition();
    }
}
