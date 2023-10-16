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

package org.citrusframework.http.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.web.bind.annotation.RequestMethod;
import org.w3c.dom.Element;

/**
 * Http client parser sets properties on bean definition for client component.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class HttpClientParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        if (element.hasAttribute("rest-template") && element.hasAttribute("request-factory")) {
            parserContext.getReaderContext().error("When providing a 'rest-template' property, " +
                    "no 'request-factory' should be set!", element);
        }

        if (!element.hasAttribute("request-url") && !element.hasAttribute("endpoint-resolver")) {
            parserContext.getReaderContext().error("One of the properties 'request-url' or " +
                    "'endpoint-resolver' is required!", element);
        }

        if (element.hasAttribute("rest-template")) {
            BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("rest-template"), "restTemplate");
        } else {
            BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("request-factory"), "requestFactory");
        }

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("request-url"), "requestUrl");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("default-accept-header"), "defaultAcceptHeader");

        String requestMethod = element.getAttribute("request-method");
        if (StringUtils.hasText(requestMethod)) {
            endpointConfiguration.addPropertyValue("requestMethod", new TypedStringValue(requestMethod, RequestMethod.class));
        }

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-correlator"), "correlator");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("endpoint-resolver"), "endpointUriResolver");

        if (element.hasAttribute("charset")) {
            endpointConfiguration.addPropertyValue("charset", element.getAttribute("charset"));
        }

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("content-type"), "contentType");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("polling-interval"), "pollingInterval");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("handle-cookies"), "handleCookies");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("error-handler"), "errorHandler");
        if (element.hasAttribute("error-strategy")) {
            endpointConfiguration.addPropertyValue("errorHandlingStrategy",
                    ErrorHandlingStrategy.fromName(element.getAttribute("error-strategy")));
        }

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("interceptors"), "clientInterceptors");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("binary-media-types"), "binaryMediaTypes");

        // Set outbound header mapper
        endpointConfiguration.addPropertyValue("headerMapper", DefaultHttpHeaderMapper.outboundMapper());
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return HttpClient.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return HttpEndpointConfiguration.class;
    }
}
