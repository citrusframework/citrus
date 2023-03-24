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

import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.endpoint.adapter.TimeoutProducingEndpointAdapter;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Timeout producing endpoint adapter bean definition parser.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class TimeoutProducingEndpointAdapterParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.genericBeanDefinition(TimeoutProducingEndpointAdapterFactory.class).getBeanDefinition();
    }

    /**
     * Factory bean for endpoint adapter.
     */
    public static class TimeoutProducingEndpointAdapterFactory implements FactoryBean<TimeoutProducingEndpointAdapter>, BeanNameAware {

        @Autowired(required = false)
        private TestContextFactoryBean testContextFactory;

        private String name;

        @Override
        public TimeoutProducingEndpointAdapter getObject() throws Exception {
            TimeoutProducingEndpointAdapter endpointAdapter = new TimeoutProducingEndpointAdapter();

            endpointAdapter.setTestContextFactory(testContextFactory);
            endpointAdapter.setName(name);

            return endpointAdapter;
        }

        @Override
        public Class<?> getObjectType() {
            return TimeoutProducingEndpointAdapter.class;
        }

        @Override
        public void setBeanName(String name) {
            this.name = name;
        }
    }
}
