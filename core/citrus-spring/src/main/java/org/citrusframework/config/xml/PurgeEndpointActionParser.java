/*
 * Copyright 2006-2015 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citrusframework.actions.PurgeEndpointAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class PurgeEndpointActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PurgeEndpointActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);
        MessageSelectorParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("timeout"), "receiveTimeout");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("sleep"), "sleepTime");

        List<String> endpointNames = new ArrayList<>();
        ManagedList<BeanDefinition> endpointRefs = new ManagedList<>();
        List<?> endpointElements = DomUtils.getChildElementsByTagName(element, "endpoint");
        for (Object endpointElement : endpointElements) {
            Element endpoint = (Element) endpointElement;
            String endpointName = endpoint.getAttribute("name");
            String endpointRef = endpoint.getAttribute("ref");

            if (StringUtils.hasText(endpointName)) {
                endpointNames.add(endpointName);
            } else if (StringUtils.hasText(endpointRef)) {
                endpointRefs.add(BeanDefinitionBuilder.childBeanDefinition(endpointRef).getBeanDefinition());
            } else {
                throw new BeanCreationException("Element 'endpoint' must set one of the attributes 'name' or 'ref'");
            }
        }

        beanDefinition.addPropertyValue("endpointNames", endpointNames);
        beanDefinition.addPropertyValue("endpoints", endpointRefs);

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class PurgeEndpointActionFactoryBean extends AbstractTestActionFactoryBean<PurgeEndpointAction, PurgeEndpointAction.Builder> implements ApplicationContextAware {

        private final PurgeEndpointAction.Builder builder = new PurgeEndpointAction.Builder();

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            builder.referenceResolver(new SpringBeanReferenceResolver(applicationContext));
        }

        /**
         * Sets the endpointNames.
         * @param endpointNames the endpointNames to set
         */
        public void setEndpointNames(List<String> endpointNames) {
            builder.endpointNames(endpointNames);
        }

        /**
         * Sets the endpoints.
         * @param endpoints the endpoints to set
         */
        public void setEndpoints(List<Endpoint> endpoints) {
            builder.endpoints(endpoints);
        }

        /**
         * Setter for messageSelector.
         * @param messageSelectorMap
         */
        public void setMessageSelectorMap(Map<String, Object> messageSelectorMap) {
            builder.selector(messageSelectorMap);
        }

        /**
         * Set message selector string.
         * @param messageSelector
         */
        public void setMessageSelector(String messageSelector) {
            builder.selector(messageSelector);
        }

        /**
         * Set the receive timeout.
         * @param receiveTimeout the receiveTimeout to set
         */
        public void setReceiveTimeout(long receiveTimeout) {
            builder.timeout(receiveTimeout);
        }

        /**
         * Sets the sleepTime.
         * @param sleepTime the sleepTime to set
         */
        public void setSleepTime(long sleepTime) {
            builder.sleep(sleepTime);
        }

        @Override
        public PurgeEndpointAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return PurgeEndpointAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public PurgeEndpointAction.Builder getBuilder() {
            return builder;
        }
    }
}
