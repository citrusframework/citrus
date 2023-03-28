/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.mail.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.mail.client.MailClient;
import org.citrusframework.mail.client.MailEndpointConfiguration;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailClientParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(MailClient.class);

        BeanDefinitionBuilder endpointConfigurationBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(MailEndpointConfiguration.class);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfigurationBuilder, element.getAttribute("host"), "host");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfigurationBuilder, element.getAttribute("port"), "port");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfigurationBuilder, element.getAttribute("protocol"), "protocol");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfigurationBuilder, element.getAttribute("username"), "username");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfigurationBuilder, element.getAttribute("password"), "password");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfigurationBuilder, element.getAttribute("properties"), "javaMailProperties");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfigurationBuilder, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfigurationBuilder, element.getAttribute("marshaller"), "marshaller");

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");

        parserContext.getRegistry().registerBeanDefinition(element.getAttribute("id") + "Configuration", endpointConfigurationBuilder.getBeanDefinition());

        builder.addConstructorArgReference(element.getAttribute("id") + "Configuration");

        return builder.getBeanDefinition();
    }
}
