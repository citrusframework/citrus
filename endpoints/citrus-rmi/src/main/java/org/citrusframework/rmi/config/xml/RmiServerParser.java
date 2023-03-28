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

package org.citrusframework.rmi.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractServerParser;
import org.citrusframework.rmi.endpoint.RmiEndpointConfiguration;
import org.citrusframework.rmi.server.RmiServer;
import org.citrusframework.server.AbstractServer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiServerParser extends AbstractServerParser {
    @Override
    protected void parseServer(BeanDefinitionBuilder serverBuilder, Element element, ParserContext parserContext) {
        BeanDefinitionBuilder configurationBuilder = BeanDefinitionBuilder.genericBeanDefinition(RmiEndpointConfiguration.class);
        new RmiEndpointConfigurationParser().parseEndpointConfiguration(configurationBuilder, element);

        BeanDefinitionParserUtils.setPropertyValue(serverBuilder, element.getAttribute("interface"), "remoteInterfaces");
        BeanDefinitionParserUtils.setPropertyValue(serverBuilder, element.getAttribute("create-registry"), "createRegistry");

        String endpointConfigurationId = element.getAttribute(ID_ATTRIBUTE) + "Configuration";
        BeanDefinitionParserUtils.registerBean(endpointConfigurationId, configurationBuilder.getBeanDefinition(), parserContext, shouldFireEvents());

        serverBuilder.addConstructorArgReference(endpointConfigurationId);
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return RmiServer.class;
    }
}
