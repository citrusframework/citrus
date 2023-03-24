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

package org.citrusframework.ws.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
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

    public static final String MESSAGE_SENDER_ATTRIBUTE = "message-sender";
    public static final String REQUEST_URL_ATTRIBUTE = "request-url";

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute(REQUEST_URL_ATTRIBUTE), "defaultUri");

        if (element.hasAttribute("web-service-template") && (element.hasAttribute("message-factory") ||
                element.hasAttribute(MESSAGE_SENDER_ATTRIBUTE))) {
            parserContext.getReaderContext().error("When providing a 'web-service-template' reference, none of " +
                    "'message-factory', '" + MESSAGE_SENDER_ATTRIBUTE + "' should be set.", element);
        }

        if (!element.hasAttribute(REQUEST_URL_ATTRIBUTE) && !element.hasAttribute("endpoint-resolver")) {
            parserContext.getReaderContext().error(String.format("One of the properties '%s' or '%s' is required!",
                    REQUEST_URL_ATTRIBUTE, "endpoint-resolver"), element);
        }

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("web-service-template"), "webServiceTemplate");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-factory"), "messageFactory", "messageFactory");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute(MESSAGE_SENDER_ATTRIBUTE), "messageSender");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("interceptors"), "interceptors");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("interceptor"), "interceptor");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-correlator"), "correlator");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("endpoint-resolver"), "endpointResolver");

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
