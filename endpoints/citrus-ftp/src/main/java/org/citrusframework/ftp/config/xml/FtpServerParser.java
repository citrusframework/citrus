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

package org.citrusframework.ftp.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractServerParser;
import org.citrusframework.ftp.client.FtpEndpointConfiguration;
import org.citrusframework.ftp.server.FtpServer;
import org.citrusframework.server.AbstractServer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpServerParser extends AbstractServerParser {

    @Override
    protected void parseServer(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        BeanDefinitionBuilder configurationBuilder = BeanDefinitionBuilder.genericBeanDefinition(FtpEndpointConfiguration.class);
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("port"), "port");

        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("auto-connect"), "autoConnect");
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("auto-login"), "autoLogin");
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("auto-handle-commands"), "autoHandleCommands");

        String endpointConfigurationId = element.getAttribute(ID_ATTRIBUTE) + "Configuration";
        BeanDefinitionParserUtils.registerBean(endpointConfigurationId, configurationBuilder.getBeanDefinition(), parserContext, shouldFireEvents());

        builder.addConstructorArgReference(endpointConfigurationId);

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("server"), "ftpServer");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("user-manager"), "userManager");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("user-manager-properties"), "userManagerProperties");
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return FtpServer.class;
    }
}
