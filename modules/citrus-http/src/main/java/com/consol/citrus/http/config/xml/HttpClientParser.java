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

package com.consol.citrus.http.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.AbstractEndpointParser;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.message.ErrorHandlingStrategy;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.http.HttpMethod;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.util.StringUtils;
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

        if (element.hasAttribute("rest-template")){
            BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("rest-template"), "restTemplate");
        } else {
            BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("request-factory"), "requestFactory");
        }

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("request-url"), "requestUrl");

        String requestMethod = element.getAttribute("request-method");
        if (StringUtils.hasText(requestMethod)) {
            endpointConfiguration.addPropertyValue("requestMethod", new TypedStringValue(requestMethod, HttpMethod.class));
        }

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-correlator"), "correlator");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("endpoint-resolver"), "endpointUriResolver");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("charset"), "charset");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("content-type"), "contentType");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("polling-interval"), "pollingInterval");

        if (element.hasAttribute("error-strategy")) {
            endpointConfiguration.addPropertyValue("errorHandlingStrategy",
                    ErrorHandlingStrategy.fromName(element.getAttribute("error-strategy")));
        }

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("interceptors"), "clientInterceptors");

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
