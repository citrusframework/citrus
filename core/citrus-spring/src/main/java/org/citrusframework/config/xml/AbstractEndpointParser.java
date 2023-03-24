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
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
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
        BeanDefinitionBuilder endpointBuilder = BeanDefinitionBuilder.genericBeanDefinition(getEndpointClass());
        parseEndpoint(endpointBuilder, element, parserContext);

        BeanDefinitionBuilder configurationBuilder = BeanDefinitionBuilder.genericBeanDefinition(getEndpointConfigurationClass());
        parseEndpointConfiguration(configurationBuilder, element, parserContext);

        endpointBuilder.addPropertyValue("name", element.getAttribute(ID_ATTRIBUTE));

        String endpointConfigurationId = element.getAttribute(ID_ATTRIBUTE) + "Configuration";
        BeanDefinitionParserUtils.registerBean(endpointConfigurationId, configurationBuilder.getBeanDefinition(), parserContext, shouldFireEvents());

        endpointBuilder.addConstructorArgReference(endpointConfigurationId);
        BeanDefinitionParserUtils.setPropertyReference(endpointBuilder, element.getAttribute("actor"), "actor");

        return endpointBuilder.getBeanDefinition();
    }

    /**
     * Subclasses must provide endpoint class.
     * @return
     */
    protected abstract Class<? extends Endpoint> getEndpointClass();

    /**
     * Subclasses must provide endpoint configuration class.
     * @return
     */
    protected abstract Class<? extends EndpointConfiguration> getEndpointConfigurationClass();

    /**
     * Subclasses can override this parsing method in order to provide proper endpoint configuration bean definition properties.
     * @param endpointConfigurationBuilder
     * @param element
     * @param parserContext
     * @return
     */
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfigurationBuilder, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(endpointConfigurationBuilder, element.getAttribute("timeout"), "timeout");
    }

    /**
     * Subclasses can implement this parsing method in order to provide detailed endpoint bean definition properties.
     * @param endpointBuilder
     * @param element
     * @param parserContext
     * @return
     */
    protected void parseEndpoint(BeanDefinitionBuilder endpointBuilder, Element element, ParserContext parserContext) {
    }
}
