/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.kubernetes.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.AbstractEndpointParser;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.consol.citrus.kubernetes.endpoint.KubernetesEndpointConfiguration;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for kubernetes client instance.
 * 
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesClientParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        ConfigBuilder config = new ConfigBuilder();

        if (element.hasAttribute("url")) {
            config.withMasterUrl(element.getAttribute("url"));
        }

        if (element.hasAttribute("version")) {
            config.withApiVersion(element.getAttribute("version"));
        }

        if (element.hasAttribute("username")) {
            config.withUsername(element.getAttribute("username"));
        }

        if (element.hasAttribute("password")) {
            config.withPassword(element.getAttribute("password"));
        }

        if (element.hasAttribute("namespace")) {
            config.withNamespace(element.getAttribute("namespace"));
        }

        if (element.hasAttribute("cert-file")) {
            config.withCaCertFile(element.getAttribute("cert-file"));
        }

        endpointConfiguration.addPropertyValue("kubernetesClientConfig", config.build());

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("result-mapper"), "resultMapper");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return KubernetesClient.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return KubernetesEndpointConfiguration.class;
    }
}
