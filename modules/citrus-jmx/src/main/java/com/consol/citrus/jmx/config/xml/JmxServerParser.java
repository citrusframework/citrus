/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.jmx.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.AbstractServerParser;
import com.consol.citrus.jmx.endpoint.JmxEndpointConfiguration;
import com.consol.citrus.jmx.server.JmxServer;
import com.consol.citrus.server.AbstractServer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxServerParser extends AbstractServerParser {
    @Override
    protected void parseServer(BeanDefinitionBuilder serverBuilder, Element element, ParserContext parserContext) {
        BeanDefinitionBuilder configurationBuilder = BeanDefinitionBuilder.genericBeanDefinition(JmxEndpointConfiguration.class);
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("server-url"), "serverUrl");
        BeanDefinitionParserUtils.setPropertyReference(configurationBuilder, element.getAttribute("environment-properties"), "environmentProperties");
        BeanDefinitionParserUtils.setPropertyReference(configurationBuilder, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("timeout"), "timeout");


        BeanDefinitionParserUtils.setPropertyValue(serverBuilder, element.getAttribute("mbean-interface"), "mbeanInterfaces");

        String endpointConfigurationId = element.getAttribute(ID_ATTRIBUTE) + "Configuration";
        BeanDefinitionParserUtils.registerBean(endpointConfigurationId, configurationBuilder.getBeanDefinition(), parserContext, shouldFireEvents());

        serverBuilder.addConstructorArgReference(endpointConfigurationId);
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return JmxServer.class;
    }
}
