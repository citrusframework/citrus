/*
 * Copyright 2006-2010 the original author or authors.
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

import java.util.Map;

import org.citrusframework.actions.ReceiveTimeoutAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for receive-timeout action in test case.
 *
 * @author Christoph Deppisch
 */
public class ReceiveTimeoutActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ReceiveTimeoutActionFactoryBean.class);

        String endpointUri = element.getAttribute("endpoint");
        if (!StringUtils.hasText(endpointUri)) {
            throw new BeanCreationException("Missing proper message endpoint reference for expect timeout action - 'endpoint' attribute is required and should not be empty");
        }

        if (endpointUri.contains(":")) {
            beanDefinition.addPropertyValue("endpointUri", endpointUri);
        } else {
            beanDefinition.addPropertyReference("endpoint", endpointUri);
        }

        beanDefinition.addPropertyValue("name", element.getLocalName()+ ":" + endpointUri);

        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("wait"), "timeout");

        Element messageSelectorElement = DomUtils.getChildElementByTagName(element, "select");
        if (messageSelectorElement != null) {
            beanDefinition.addPropertyValue("messageSelector", DomUtils.getTextValue(messageSelectorElement));
        }

        MessageSelectorParser.doParse(element, beanDefinition);

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class ReceiveTimeoutActionFactoryBean extends AbstractTestActionFactoryBean<ReceiveTimeoutAction, ReceiveTimeoutAction.Builder> {

        private final ReceiveTimeoutAction.Builder builder = new ReceiveTimeoutAction.Builder();

        /**
         * Setter for receive timeout.
         * @param timeout
         */
        public void setTimeout(long timeout) {
            builder.timeout(timeout);
        }

        /**
         * Set message selector string.
         * @param messageSelector
         */
        public void setMessageSelector(String messageSelector) {
            builder.selector(messageSelector);
        }

        /**
         * Set message selector map.
         * @param messageSelector
         */
        public void setMessageSelectorMap(Map<String, Object> messageSelector) {
            builder.selector(messageSelector);
        }

        /**
         * Set message endpoint instance.
         * @param endpoint the message endpoint
         */
        public void setEndpoint(Endpoint endpoint) {
            builder.endpoint(endpoint);
        }

        /**
         * Sets the endpoint uri.
         * @param endpointUri
         */
        public void setEndpointUri(String endpointUri) {
            builder.endpoint(endpointUri);
        }

        @Override
        public ReceiveTimeoutAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return ReceiveTimeoutAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public ReceiveTimeoutAction.Builder getBuilder() {
            return builder;
        }
    }
}
