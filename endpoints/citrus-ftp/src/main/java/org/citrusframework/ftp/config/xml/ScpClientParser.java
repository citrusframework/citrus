/*
 * Copyright the original author or authors.
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
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.ftp.client.ScpClient;
import org.citrusframework.ftp.client.ScpEndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @since 2.7.6
 */
public class ScpClientParser extends SftpClientParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("port-option"), "portOption");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return ScpClient.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return ScpEndpointConfiguration.class;
    }
}
