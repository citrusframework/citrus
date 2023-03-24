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
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parses endpoint adapter bean definition for direct endpoint adapter.
 * @author Christoph Deppisch
 * @since 3.0
 */
public class DirectEndpointAdapterParser extends AbstractBeanDefinitionParser {

    @Override
    public AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DirectEndpointAdapterFactory.class);

        BeanDefinitionBuilder endpointConfiguration = BeanDefinitionBuilder.genericBeanDefinition(DirectSyncEndpointConfiguration.class);
        new DirectSyncEndpointParser().parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        String endpointConfigurationId = element.getAttribute(ID_ATTRIBUTE) + "EndpointAdapterConfiguration";
        BeanDefinitionParserUtils.registerBean(endpointConfigurationId, endpointConfiguration.getBeanDefinition(), parserContext, shouldFireEvents());

        builder.addPropertyReference("endpointConfiguration", endpointConfigurationId);

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("fallback-adapter"), "fallbackEndpointAdapter");

        return builder.getBeanDefinition();
    }

    /**
     * Factory bean for endpoint adapter.
     */
    public static class DirectEndpointAdapterFactory implements FactoryBean<DirectEndpointAdapter>, BeanNameAware {

        @Autowired(required = false)
        private TestContextFactoryBean testContextFactory;

        private String name;
        private DirectSyncEndpointConfiguration endpointConfiguration;
        private EndpointAdapter fallbackEndpointAdapter;

        /**
         * Specifies the endpointConfiguration.
         * @param endpointConfiguration
         */
        public void setEndpointConfiguration(DirectSyncEndpointConfiguration endpointConfiguration) {
            this.endpointConfiguration = endpointConfiguration;
        }

        /**
         * Specifies the fallbackEndpointAdapter.
         * @param fallbackEndpointAdapter
         */
        public void setFallbackEndpointAdapter(EndpointAdapter fallbackEndpointAdapter) {
            this.fallbackEndpointAdapter = fallbackEndpointAdapter;
        }

        @Override
        public DirectEndpointAdapter getObject() throws Exception {
            DirectEndpointAdapter endpointAdapter = new DirectEndpointAdapter(endpointConfiguration);

            endpointAdapter.setTestContextFactory(testContextFactory);
            endpointAdapter.setName(name);

            if (fallbackEndpointAdapter != null) {
                endpointAdapter.setFallbackEndpointAdapter(fallbackEndpointAdapter);
            }

            return endpointAdapter;
        }

        @Override
        public Class<?> getObjectType() {
            return DirectEndpointAdapter.class;
        }

        @Override
        public void setBeanName(String name) {
            this.name = name;
        }
    }
}
