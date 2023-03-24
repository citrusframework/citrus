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

package org.citrusframework.ssh.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.ssh.client.SshClient;
import org.citrusframework.ssh.client.SshEndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parse for SSH-client configuration
 *
 * @author Roland Huss, Christoph Deppisch
 * @since 1.3
 */
public class SshClientParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("host"), "host");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("port"), "port");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("private-key-path"), "privateKeyPath");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("private-key-password"), "privateKeyPassword");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("strict-host-checking"), "strictHostChecking");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("known-hosts-path"), "knownHosts");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("command-timeout"), "commandTimeout");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("connection-timeout"), "connectionTimeout");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("user"), "user");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("password"), "password");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-correlator"), "correlator");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("polling-interval"), "pollingInterval");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return SshClient.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return SshEndpointConfiguration.class;
    }
}
