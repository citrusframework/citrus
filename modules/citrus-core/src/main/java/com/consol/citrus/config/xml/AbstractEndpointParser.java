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

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Basic endpoint parser adds test actor reference if present and combines endpoint with respective configuration
 * bean definition. Registers endpoint configuration as bean definition in parser context and adds reference to endpoint
 * bean definition.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractEndpointParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder endpointBuilder = parseEndpoint(element, parserContext);

        BeanDefinitionBuilder configurationBuilder = parseEndpointConfiguration(element, parserContext);
        enrichEndpointConfiguration(configurationBuilder, element, parserContext);

        String endpointConfigurationId = element.getAttribute(ID_ATTRIBUTE) + "Configuration";
        BeanDefinitionHolder configurationHolder = new BeanDefinitionHolder(configurationBuilder.getBeanDefinition(), endpointConfigurationId);
        registerBeanDefinition(configurationHolder, parserContext.getRegistry());
        if (shouldFireEvents()) {
            BeanComponentDefinition componentDefinition = new BeanComponentDefinition(configurationHolder);
            postProcessComponentDefinition(componentDefinition);
            parserContext.registerComponent(componentDefinition);
        }

        endpointBuilder.addConstructorArgReference(endpointConfigurationId);
        BeanDefinitionParserUtils.setPropertyReference(endpointBuilder, element.getAttribute("actor"), "actor");

        return endpointBuilder.getBeanDefinition();
    }

    /**
     * Add endpoint configuration properties on bean definition builder.
     * @param endpointConfiguration
     * @param element
     * @param parserContext
     */
    protected void enrichEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("timeout"), "timeout");
    }

    /**
     * Subclasses must implement this parsing method in order to provide proper endpoint configuration bean definition.
     * @param element
     * @param parserContext
     * @return
     */
    protected abstract BeanDefinitionBuilder parseEndpointConfiguration(Element element, ParserContext parserContext);

    /**
     * Subclasses must implement this parsing method in order to provide detailed endpoint bean definition.
     * @param element
     * @param parserContext
     * @return
     */
    protected abstract BeanDefinitionBuilder parseEndpoint(Element element, ParserContext parserContext);
}
