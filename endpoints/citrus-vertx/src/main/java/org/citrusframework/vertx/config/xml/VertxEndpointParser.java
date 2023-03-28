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

package org.citrusframework.vertx.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.vertx.endpoint.VertxEndpoint;
import org.citrusframework.vertx.endpoint.VertxEndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxEndpointParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpoint(BeanDefinitionBuilder endpointBuilder, Element element, ParserContext parserContext) {
        super.parseEndpoint(endpointBuilder, element, parserContext);
        BeanDefinitionParserUtils.setPropertyReference(endpointBuilder, element.getAttribute("vertx-factory"), "vertxInstanceFactory", "vertxInstanceFactory");
    }

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("host"), "host");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("port"), "port");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("address"), "address");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("pub-sub-domain"), "pubSubDomain");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("polling-interval"), "pollingInterval");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return VertxEndpoint.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return VertxEndpointConfiguration.class;
    }
}
