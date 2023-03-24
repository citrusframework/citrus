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

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.RequestDispatchingEndpointAdapter;
import org.citrusframework.endpoint.adapter.mapping.EndpointAdapterMappingStrategy;
import org.citrusframework.endpoint.adapter.mapping.MappingKeyExtractor;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parses dispatching endpoint adapter components.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class RequestDispatchingEndpointAdapterParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RequestDispatchingEndpointAdapterFactory.class);

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("mapping-key-extractor"), "mappingKeyExtractor");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("mapping-strategy"), "mappingStrategy");

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("fallback-adapter"), "fallbackEndpointAdapter");

        return builder.getBeanDefinition();
    }

    /**
     * Factory bean for endpoint adapter.
     */
    public static class RequestDispatchingEndpointAdapterFactory implements FactoryBean<RequestDispatchingEndpointAdapter>, BeanNameAware {

        @Autowired(required = false)
        private TestContextFactoryBean testContextFactory;

        private String name;
        private EndpointAdapter fallbackEndpointAdapter;
        private MappingKeyExtractor mappingKeyExtractor;
        private EndpointAdapterMappingStrategy mappingStrategy;

        /**
         * Specifies the fallbackEndpointAdapter.
         * @param fallbackEndpointAdapter
         */
        public void setFallbackEndpointAdapter(EndpointAdapter fallbackEndpointAdapter) {
            this.fallbackEndpointAdapter = fallbackEndpointAdapter;
        }

        /**
         * Specifies the mappingKeyExtractor.
         * @param mappingKeyExtractor
         */
        public void setMappingKeyExtractor(MappingKeyExtractor mappingKeyExtractor) {
            this.mappingKeyExtractor = mappingKeyExtractor;
        }

        /**
         * Specifies the mappingStrategy.
         * @param mappingStrategy
         */
        public void setMappingStrategy(EndpointAdapterMappingStrategy mappingStrategy) {
            this.mappingStrategy = mappingStrategy;
        }

        @Override
        public RequestDispatchingEndpointAdapter getObject() throws Exception {
            RequestDispatchingEndpointAdapter endpointAdapter = new RequestDispatchingEndpointAdapter();

            if (mappingKeyExtractor != null) {
                endpointAdapter.setMappingKeyExtractor(mappingKeyExtractor);
            }

            if (mappingStrategy != null) {
                endpointAdapter.setMappingStrategy(mappingStrategy);
            }

            endpointAdapter.setTestContextFactory(testContextFactory);
            endpointAdapter.setName(name);

            if (fallbackEndpointAdapter != null) {
                endpointAdapter.setFallbackEndpointAdapter(fallbackEndpointAdapter);
            }

            return endpointAdapter;
        }

        @Override
        public Class<?> getObjectType() {
            return RequestDispatchingEndpointAdapter.class;
        }

        @Override
        public void setBeanName(String name) {
            this.name = name;
        }
    }
}
