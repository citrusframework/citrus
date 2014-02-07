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

package com.consol.citrus.config.xml;

import com.consol.citrus.channel.ChannelEndpointAdapter;
import com.consol.citrus.channel.ChannelSyncEndpointConfiguration;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.endpoint.adapter.*;
import com.consol.citrus.jms.JmsEndpointAdapter;
import com.consol.citrus.jms.JmsSyncEndpointConfiguration;
import com.consol.citrus.message.MessageHeaderType;
import com.consol.citrus.server.AbstractServer;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Abstract server parser adds endpoint adapter construction and basic server property parsing.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractServerParser extends AbstractBeanDefinitionParser {

    @Override
    protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder serverBuilder = BeanDefinitionBuilder.genericBeanDefinition(getServerClass());

        parseServer(serverBuilder, element, parserContext);

        if (element.hasChildNodes()) {
            Element endpointAdapterRef = DomUtils.getChildElementByTagName(element, "endpoint-adapter");

            if (endpointAdapterRef != null) {
                BeanDefinitionParserUtils.setPropertyReference(serverBuilder, endpointAdapterRef.getAttribute("ref"), "endpointAdapter");
            } else {
                BeanDefinitionBuilder endpointAdapterBuilder = parseEndpointAdapter(element, parserContext);

                String endpointAdapterId = element.getAttribute(ID_ATTRIBUTE) + "EndpointAdapter";
                BeanDefinitionHolder configurationHolder = new BeanDefinitionHolder(endpointAdapterBuilder.getBeanDefinition(), endpointAdapterId);
                registerBeanDefinition(configurationHolder, parserContext.getRegistry());
                if (shouldFireEvents()) {
                    BeanComponentDefinition componentDefinition = new BeanComponentDefinition(configurationHolder);
                    postProcessComponentDefinition(componentDefinition);
                    parserContext.registerComponent(componentDefinition);
                }

                BeanDefinitionParserUtils.setPropertyReference(serverBuilder, endpointAdapterId, "endpointAdapter");
            }
        }

        return serverBuilder.getBeanDefinition();
    }

    /**
     * Parses optional endpoint adapter definition. Evaluates endpoint adapter choice and returns respective bean definition builder.
     * @param element
     * @param parserContext
     * @return
     */
    private BeanDefinitionBuilder parseEndpointAdapter(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder endpointAdapterBuilder;
        Element channelEndpointAdapter = DomUtils.getChildElementByTagName(element, "channel-endpoint-adapter");
        Element jmsEndpointAdapter = DomUtils.getChildElementByTagName(element, "jms-endpoint-adapter");
        Element emptyResponseEndpointAdapter = DomUtils.getChildElementByTagName(element, "empty-response-adapter");
        Element staticResponseEndpointAdapter = DomUtils.getChildElementByTagName(element, "static-response-adapter");
        Element timeoutProducingEndpointAdapter = DomUtils.getChildElementByTagName(element, "timeout-producing-adapter");

        if (channelEndpointAdapter != null) {
            endpointAdapterBuilder = getChannelEndpointAdapterDefinition(channelEndpointAdapter, element.getAttribute(ID_ATTRIBUTE), parserContext);
        } else if (jmsEndpointAdapter != null) {
            endpointAdapterBuilder = getJmsEndpointAdapterDefinition(jmsEndpointAdapter, element.getAttribute(ID_ATTRIBUTE), parserContext);
        } else if (emptyResponseEndpointAdapter != null) {
            endpointAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(EmptyResponseEndpointAdapter.class);
        } else if (staticResponseEndpointAdapter != null) {
            endpointAdapterBuilder = getStaticEndpointAdapterDefinition(staticResponseEndpointAdapter);
        } else if (timeoutProducingEndpointAdapter != null) {
            endpointAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(TimeoutProducingEndpointAdapter.class);
        } else {
            throw new BeanCreationException("Unsupported endpoint-apapter element: " + DomUtils.getChildElements(element).iterator().next().getTagName());
        }

        return endpointAdapterBuilder;
    }

    private BeanDefinitionBuilder getChannelEndpointAdapterDefinition(Element element, String serverId, ParserContext parserContext) {
        BeanDefinitionBuilder endpointAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(ChannelEndpointAdapter.class);

        BeanDefinitionBuilder endpointConfiguration = BeanDefinitionBuilder.genericBeanDefinition(ChannelSyncEndpointConfiguration.class);
        new ChannelSyncEndpointParser().parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        String endpointConfigurationId = serverId + "EndpointAdapterConfiguration";
        BeanDefinitionHolder configurationHolder = new BeanDefinitionHolder(endpointConfiguration.getBeanDefinition(), endpointConfigurationId);
        registerBeanDefinition(configurationHolder, parserContext.getRegistry());
        if (shouldFireEvents()) {
            BeanComponentDefinition componentDefinition = new BeanComponentDefinition(configurationHolder);
            postProcessComponentDefinition(componentDefinition);
            parserContext.registerComponent(componentDefinition);
        }

        endpointAdapterBuilder.addConstructorArgReference(endpointConfigurationId);

        return endpointAdapterBuilder;
    }

    /**
     * Constructs bean definition builder from element for jms endpoint adapter.
     * @param element
     * @param serverId
     * @param parserContext
     * @return
     */
    private BeanDefinitionBuilder getJmsEndpointAdapterDefinition(Element element, String serverId, ParserContext parserContext) {
        BeanDefinitionBuilder endpointAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(JmsEndpointAdapter.class);

        BeanDefinitionBuilder endpointConfiguration = BeanDefinitionBuilder.genericBeanDefinition(JmsSyncEndpointConfiguration.class);
        new JmsSyncEndpointParser().parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        String endpointConfigurationId = serverId + "EndpointAdapterConfiguration";
        BeanDefinitionHolder configurationHolder = new BeanDefinitionHolder(endpointConfiguration.getBeanDefinition(), endpointConfigurationId);
        registerBeanDefinition(configurationHolder, parserContext.getRegistry());
        if (shouldFireEvents()) {
            BeanComponentDefinition componentDefinition = new BeanComponentDefinition(configurationHolder);
            postProcessComponentDefinition(componentDefinition);
            parserContext.registerComponent(componentDefinition);
        }

        endpointAdapterBuilder.addConstructorArgReference(endpointConfigurationId);

        return endpointAdapterBuilder;
    }

    /**
     * Construct bean definition builder from element for static response endpoint adapter.
     * @param element
     * @return
     */
    private BeanDefinitionBuilder getStaticEndpointAdapterDefinition(Element element) {
        BeanDefinitionBuilder endpointAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(StaticResponseEndpointAdapter.class);

        Element payloadData = DomUtils.getChildElementByTagName(element, "payload");
        if (payloadData != null) {
            endpointAdapterBuilder.addPropertyValue("messagePayload", DomUtils.getTextValue(payloadData));
        }

        Element headerElement = DomUtils.getChildElementByTagName(element, "header");
        if (headerElement != null) {
            Map<String, Object> messageHeaders = new HashMap<String, Object>();

            List<?> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();

                String name = headerValue.getAttribute("name");
                String value = headerValue.getAttribute("value");
                String type = headerValue.getAttribute("type");

                if (StringUtils.hasText(type)) {
                    value = MessageHeaderType.createTypedValue(type, value);
                }

                messageHeaders.put(name, value);
            }

            endpointAdapterBuilder.addPropertyValue("messageHeader", messageHeaders);

        }

        return endpointAdapterBuilder;
    }

    /**
     * Parses element and adds server properties to bean definition via provided builder.
     * Subclasses can override this parsing method in order to add detailed server bean definition properties.
     * @param serverBuilder
     * @param element
     * @param parserContext
     * @return
     */
    protected void parseServer(BeanDefinitionBuilder serverBuilder, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(serverBuilder, element.getAttribute("auto-start"), "autoStart");
    }

    /**
     * Subclasses must provide proper server class implementation.
     * @return
     */
    protected abstract Class<? extends AbstractServer> getServerClass();
}
