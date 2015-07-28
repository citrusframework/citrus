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

package com.consol.citrus.http.socket.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.AbstractEndpointParser;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.http.socket.endpoint.WebSocketClientEndpointConfiguration;
import com.consol.citrus.http.socket.endpoint.WebSocketEndpoint;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Used for parsing Client WebSocket configurations
 *
 * @author Martin Maher
 * @since 2.2.1
 */
public class WebSocketClientEndpointParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfigurationBuilder, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfigurationBuilder, element, parserContext);
        String url = element.getAttribute("url");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfigurationBuilder, url, "endpointUri");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return WebSocketEndpoint.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return WebSocketClientEndpointConfiguration.class;
    }

}
