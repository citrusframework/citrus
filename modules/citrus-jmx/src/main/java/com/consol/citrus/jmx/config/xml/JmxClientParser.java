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

package com.consol.citrus.jmx.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.AbstractEndpointParser;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.jmx.client.JmxClient;
import com.consol.citrus.jmx.endpoint.JmxEndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for jmx client instance.
 * 
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxClientParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("server-url"), "serverUrl");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("username"), "username");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("password"), "password");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("auto-reconnect"), "autoReconnect");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("delay-on-reconnect"), "delayOnReconnect");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-correlator"), "correlator");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("notification-filter"), "notificationFilter");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("polling-interval"), "pollingInterval");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return JmxClient.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return JmxEndpointConfiguration.class;
    }

}
