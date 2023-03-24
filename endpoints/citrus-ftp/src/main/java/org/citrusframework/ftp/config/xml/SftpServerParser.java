/*
 * Copyright 2006-2018 the original author or authors.
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
import org.citrusframework.ftp.client.SftpEndpointConfiguration;
import org.citrusframework.ftp.server.SftpServer;
import org.citrusframework.server.AbstractServer;
import org.citrusframework.ssh.config.xml.SshServerParser;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for the configuration of an SSH server
 * 
 * @author Roland Huss, Christoph Deppisch
 */
public class SftpServerParser extends SshServerParser {

    @Override
    protected void parseServer(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        super.parseServer(builder, element, parserContext);

        BeanDefinitionBuilder configurationBuilder = BeanDefinitionBuilder.genericBeanDefinition(SftpEndpointConfiguration.class);

        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("auto-connect"), "autoConnect");
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("auto-login"), "autoLogin");

        String endpointConfigurationId = element.getAttribute(ID_ATTRIBUTE) + "Configuration";
        BeanDefinitionParserUtils.registerBean(endpointConfigurationId, configurationBuilder.getBeanDefinition(), parserContext, shouldFireEvents());

        builder.addConstructorArgReference(endpointConfigurationId);
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return SftpServer.class;
    }
}
