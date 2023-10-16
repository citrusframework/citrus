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

package org.citrusframework.jms.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Abstract base class for JMS endpoint configuration. Parser creates endpoint bean definitions setting
 * properties and property references.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractJmsEndpointParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        String jmsTemplate = element.getAttribute("jms-template");
        String destination = element.getAttribute("destination");
        String destinationName = element.getAttribute("destination-name");

        if (StringUtils.hasText(destination) || StringUtils.hasText(destinationName)) {
            //connectionFactory
            String connectionFactory = "connectionFactory"; //default value

            if (element.hasAttribute("connection-factory")) {
                connectionFactory = element.getAttribute("connection-factory");
            }

            if (!StringUtils.hasText(connectionFactory)) {
                parserContext.getReaderContext().error("Attribute connection-factory must not be empty " +
                        "for jms configuration elements", element);
            }

            endpointConfiguration.addPropertyReference("connectionFactory", connectionFactory);

            //destination
            if (StringUtils.hasText(destination)) {
                endpointConfiguration.addPropertyReference("destination", destination);
            } else {
                endpointConfiguration.addPropertyValue("destinationName", destinationName);
            }
        } else if (StringUtils.hasText(jmsTemplate)) {
            if (element.hasAttribute("connection-factory") ||
                    element.hasAttribute("destination") ||
                    element.hasAttribute("destination-name")) {
                parserContext.getReaderContext().error("When providing a jms-template, none of " +
                        "connection-factory, destination, or destination-name should be provided.", element);
            }

            endpointConfiguration.addPropertyReference("jmsTemplate", jmsTemplate);
        } else {
            parserContext.getReaderContext().error("Either a jms-template reference " +
                    "or one of destination or destination-name must be provided.", element);
        }

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("pub-sub-domain"), "pubSubDomain");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("use-object-messages"), "useObjectMessages");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("filter-internal-headers"), "filterInternalHeaders");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("destination-resolver"), "destinationResolver");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("destination-name-resolver"), "destinationNameResolver");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration,
                element.getAttribute("polling-interval"), "pollingInterval");
    }

}
