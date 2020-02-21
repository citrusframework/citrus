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

package com.consol.citrus.camel.config.xml;

import com.consol.citrus.camel.endpoint.CamelEndpoint;
import com.consol.citrus.camel.endpoint.CamelEndpointConfiguration;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.AbstractEndpointParser;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelEndpointParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("camel-context"), "camelContext", "camelContext");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("endpoint-uri"), "endpointUri");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return CamelEndpoint.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return CamelEndpointConfiguration.class;
    }
}
