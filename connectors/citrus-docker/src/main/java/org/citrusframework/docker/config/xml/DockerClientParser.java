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

package org.citrusframework.docker.config.xml;

import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.client.DockerEndpointConfiguration;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for docker client instance.
 * 
 * @author Christoph Deppisch
 * @since 2.4
 */
public class DockerClientParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        DefaultDockerClientConfig.Builder config = DefaultDockerClientConfig.createDefaultConfigBuilder();

        if (element.hasAttribute("url")) {
            config.withDockerHost(element.getAttribute("url"));
        }

        if (element.hasAttribute("version")) {
            config.withApiVersion(element.getAttribute("version"));
        }

        if (element.hasAttribute("username")) {
            config.withRegistryUsername(element.getAttribute("username"));
        }

        if (element.hasAttribute("password")) {
            config.withRegistryPassword(element.getAttribute("password"));
        }

        if (element.hasAttribute("email")) {
            config.withRegistryEmail(element.getAttribute("email"));
        }

        if (element.hasAttribute("registry")) {
            config.withRegistryUrl(element.getAttribute("registry"));
        }

        if (element.hasAttribute("verify-tls")) {
            config.withDockerTlsVerify(element.getAttribute("verify-tls"));
        }

        if (element.hasAttribute("cert-path")) {
            config.withDockerCertPath(element.getAttribute("cert-path"));
        }

        if (element.hasAttribute("config-path")) {
            config.withDockerConfig(element.getAttribute("config-path"));
        }

        endpointConfiguration.addPropertyValue("dockerClientConfig", config.build());
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return DockerClient.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return DockerEndpointConfiguration.class;
    }
}
