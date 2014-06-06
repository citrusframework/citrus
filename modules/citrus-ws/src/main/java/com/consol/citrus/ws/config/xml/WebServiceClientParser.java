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

package com.consol.citrus.ws.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.AbstractEndpointParser;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser configures web service client bean definition.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class WebServiceClientParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute(WSParserConstants.REQUEST_URL_ATTRIBUTE), WSParserConstants.REQUEST_URL_PROPERTY);

        if (element.hasAttribute(WSParserConstants.WS_TEMPLATE_ATTRIBUTE) && (element.hasAttribute("message-factory") ||
                element.hasAttribute(WSParserConstants.MESSAGE_SENDER_ATTRIBUTE) ||
                element.hasAttribute(WSParserConstants.MESSAGE_SENDERS_ATTRIBUTE))) {
            parserContext.getReaderContext().error("When providing a '" + WSParserConstants.WS_TEMPLATE_ATTRIBUTE + "' reference, none of " +
                    "'message-factory', '" + WSParserConstants.MESSAGE_SENDER_ATTRIBUTE +
                    "', or '" + WSParserConstants.MESSAGE_SENDERS_ATTRIBUTE + "' should be set.", element);
        }

        if (!element.hasAttribute(WSParserConstants.REQUEST_URL_ATTRIBUTE) && !element.hasAttribute(WSParserConstants.ENDPOINT_RESOLVER_ATTRIBUTE)) {
            parserContext.getReaderContext().error(String.format("One of the properties '%s' or '%s' is required!",
                    WSParserConstants.REQUEST_URL_ATTRIBUTE, WSParserConstants.ENDPOINT_RESOLVER_ATTRIBUTE), element);
        }

        if (element.hasAttribute(WSParserConstants.MESSAGE_SENDER_ATTRIBUTE) && element.hasAttribute(WSParserConstants.MESSAGE_SENDERS_ATTRIBUTE)) {
            parserContext.getReaderContext().error(String.format("When '%s' is set, no '%s' attribute should be provided.",
                    WSParserConstants.MESSAGE_SENDER_ATTRIBUTE, WSParserConstants.MESSAGE_SENDERS_ATTRIBUTE), element);
        }

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute(WSParserConstants.WS_TEMPLATE_ATTRIBUTE), WSParserConstants.WS_TEMPLATE_PROPERTY);

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-factory"), "messageFactory", "messageFactory");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute(WSParserConstants.MESSAGE_SENDER_ATTRIBUTE), WSParserConstants.MESSAGE_SENDER_PROPERTY);
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute(WSParserConstants.MESSAGE_SENDERS_ATTRIBUTE), WSParserConstants.MESSAGE_SENDERS_PROPERTY);

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute(WSParserConstants.INTERCEPTORS_ATTRIBUTE), WSParserConstants.INTERCEPTORS_PROPERTY);
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("interceptor"), "interceptor");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-correlator"), WSParserConstants.REPLY_CORRELATOR_PROPERTY);
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute(WSParserConstants.ENDPOINT_RESOLVER_ATTRIBUTE), WSParserConstants.ENDPOINT_RESOLVER_PROPERTY);
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute(WSParserConstants.ADRESSING_HEADERS_ATTRIBUTE), WSParserConstants.ADRESSING_HEADERS_PROPERTY);

        if (element.hasAttribute("fault-strategy")) {
            endpointConfiguration.addPropertyValue("errorHandlingStrategy",
                    ErrorHandlingStrategy.fromName(element.getAttribute("fault-strategy")));
        }

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("polling-interval"), "pollingInterval");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return WebServiceClient.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return WebServiceEndpointConfiguration.class;
    }
}
