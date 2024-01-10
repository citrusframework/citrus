/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.kubernetes.config.xml;

import io.fabric8.kubernetes.client.Config;
import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.endpoint.KubernetesEndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static org.citrusframework.config.util.BeanDefinitionParserUtils.registerBean;
import static org.citrusframework.config.util.BeanDefinitionParserUtils.setPropertyReference;
import static org.citrusframework.config.util.BeanDefinitionParserUtils.setPropertyValue;
import static org.citrusframework.kubernetes.config.CredentialValidator.isValid;

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

        String username = element.getAttribute("username");
        String password = element.getAttribute("password");
        String oauthToken = element.getAttribute("oauthToken");

        if (!isValid(username, password, oauthToken)) {
            throw new IllegalArgumentException("Parameters not set correctly - check if either an oauthToke or password and username is set");
        }

        BeanDefinitionBuilder configBuilder = BeanDefinitionBuilder.genericBeanDefinition(Config.class);
        setPropertyValue(configBuilder, element.getAttribute("url"), "masterUrl");
        setPropertyValue(configBuilder, element.getAttribute("version"), "apiVersion");
        setPropertyValue(configBuilder, username, "username");
        setPropertyValue(configBuilder, password, "password");
        setPropertyValue(configBuilder, oauthToken, "oauthToken");
        setPropertyValue(configBuilder, element.getAttribute("namespace"), "namespace");
        setPropertyValue(configBuilder, element.getAttribute("cert-file"), "caCertFile");

        String clientConfigId = element.getAttribute(ID_ATTRIBUTE) + "Config";
        registerBean(clientConfigId, configBuilder.getBeanDefinition(), parserContext, shouldFireEvents());

        endpointConfiguration.addPropertyReference("kubernetesClientConfig", clientConfigId);

        setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
        setPropertyReference(endpointConfiguration, element.getAttribute("object-mapper"), "objectMapper");
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
