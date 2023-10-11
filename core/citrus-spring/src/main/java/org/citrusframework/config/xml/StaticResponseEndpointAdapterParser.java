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

package org.citrusframework.config.xml;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticResponseEndpointAdapter;
import org.citrusframework.message.MessageHeaderType;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Endpoint adapter parser configures bean definition for static response producing component.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class StaticResponseEndpointAdapterParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(StaticResponseEndpointAdapterFactory.class);

        Element payloadData = DomUtils.getChildElementByTagName(element, "payload");
        if (payloadData != null) {
            builder.addPropertyValue("messagePayload", DomUtils.getTextValue(payloadData));
        }

        Element payloadResource = DomUtils.getChildElementByTagName(element, "resource");
        if (payloadResource != null) {
            builder.addPropertyValue("messagePayloadResource", payloadResource.getAttribute("file"));
            if (payloadResource.hasAttribute("charset")) {
                builder.addPropertyValue("messagePayloadResourceCharset", payloadResource.getAttribute("charset"));
            }
        }

        Element headerElement = DomUtils.getChildElementByTagName(element, "header");
        if (headerElement != null) {
            Map<String, Object> messageHeaders = new LinkedHashMap<>();

            List<Element> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Element headerValue : elements) {
                String name = headerValue.getAttribute("name");
                String value = headerValue.getAttribute("value");
                String type = headerValue.getAttribute("type");

                if (StringUtils.hasText(type)) {
                    value = MessageHeaderType.createTypedValue(type, value);
                }

                messageHeaders.put(name, value);
            }

            builder.addPropertyValue("messageHeader", messageHeaders);

        }

        return builder.getBeanDefinition();
    }

    /**
     * Factory bean for endpoint adapter.
     */
    public static class StaticResponseEndpointAdapterFactory implements FactoryBean<StaticResponseEndpointAdapter>, BeanNameAware {

        @Autowired(required = false)
        private TestContextFactoryBean testContextFactory;

        private String name;
        private EndpointAdapter fallbackEndpointAdapter;

        private String messagePayload;
        private String messagePayloadResource;
        private String messagePayloadResourceCharset;
        private Map<String, Object> messageHeader = new HashMap<>();

        /**
         * Specifies the messagePayload.
         * @param messagePayload
         */
        public void setMessagePayload(String messagePayload) {
            this.messagePayload = messagePayload;
        }

        /**
         * Specifies the messagePayloadResource.
         * @param messagePayloadResource
         */
        public void setMessagePayloadResource(String messagePayloadResource) {
            this.messagePayloadResource = messagePayloadResource;
        }

        /**
         * Specifies the messagePayloadResourceCharset.
         * @param messagePayloadResourceCharset
         */
        public void setMessagePayloadResourceCharset(String messagePayloadResourceCharset) {
            this.messagePayloadResourceCharset = messagePayloadResourceCharset;
        }

        /**
         * Specifies the messageHeader.
         * @param messageHeader
         */
        public void setMessageHeader(Map<String, Object> messageHeader) {
            this.messageHeader = messageHeader;
        }

        /**
         * Specifies the fallbackEndpointAdapter.
         * @param fallbackEndpointAdapter
         */
        public void setFallbackEndpointAdapter(EndpointAdapter fallbackEndpointAdapter) {
            this.fallbackEndpointAdapter = fallbackEndpointAdapter;
        }

        @Override
        public StaticResponseEndpointAdapter getObject() throws Exception {
            StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();

            if (messagePayload != null) {
                endpointAdapter.setMessagePayload(messagePayload);
            }

            if (messagePayloadResource != null) {
                endpointAdapter.setMessagePayloadResource(messagePayloadResource);
            }

            if (messagePayloadResourceCharset != null) {
                endpointAdapter.setMessagePayloadResourceCharset(messagePayloadResource);
            }

            endpointAdapter.setMessageHeader(messageHeader);

            endpointAdapter.setTestContextFactory(testContextFactory);
            endpointAdapter.setName(name);

            if (fallbackEndpointAdapter != null) {
                endpointAdapter.setFallbackEndpointAdapter(fallbackEndpointAdapter);
            }

            return endpointAdapter;
        }

        @Override
        public Class<?> getObjectType() {
            return StaticResponseEndpointAdapter.class;
        }

        @Override
        public void setBeanName(String name) {
            this.name = name;
        }
    }
}
