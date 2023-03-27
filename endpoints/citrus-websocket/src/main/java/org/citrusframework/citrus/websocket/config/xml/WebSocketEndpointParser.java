/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.citrus.websocket.config.xml;

import org.citrusframework.citrus.config.util.BeanDefinitionParserUtils;
import org.citrusframework.citrus.config.xml.AbstractEndpointParser;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.endpoint.EndpointConfiguration;
import org.citrusframework.citrus.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.citrus.websocket.server.WebSocketServerEndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Used for parsing server WebSocket endpoints and respective configurations.
 *
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketEndpointParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("path"), "endpointUri");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return WebSocketEndpoint.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return WebSocketServerEndpointConfiguration.class;
    }

}
