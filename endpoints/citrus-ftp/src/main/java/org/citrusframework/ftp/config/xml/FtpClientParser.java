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
import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.ftp.client.FtpClient;
import org.citrusframework.ftp.client.FtpEndpointConfiguration;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpClientParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("host"), "host");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("port"), "port");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("auto-read-files"), "autoReadFiles");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("local-passive-mode"), "localPassiveMode");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("username"), "user");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("password"), "password");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-correlator"), "correlator");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("polling-interval"), "pollingInterval");

        if (element.hasAttribute("error-strategy")) {
            endpointConfiguration.addPropertyValue("errorHandlingStrategy",
                    ErrorHandlingStrategy.fromName(element.getAttribute("error-strategy")));
        }

    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return FtpClient.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return FtpEndpointConfiguration.class;
    }
}
