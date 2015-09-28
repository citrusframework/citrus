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

package com.consol.citrus.docker.config.xml;

import com.consol.citrus.docker.client.DockerClient;
import com.github.dockerjava.core.DockerClientConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for docker client instance.
 * 
 * @author Christoph Deppisch
 * @since 2.4
 */
public class DockerClientParser implements BeanDefinitionParser {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DockerClient.class);

        DockerClientConfig.DockerClientConfigBuilder config = DockerClientConfig.createDefaultConfigBuilder();

        if (element.hasAttribute("url")) {
            config.withUri(element.getAttribute("url"));
        }

        if (element.hasAttribute("version")) {
            config.withVersion(element.getAttribute("version"));
        }

        if (element.hasAttribute("username")) {
            config.withUsername(element.getAttribute("username"));
        }

        if (element.hasAttribute("password")) {
            config.withPassword(element.getAttribute("password"));
        }

        if (element.hasAttribute("email")) {
            config.withEmail(element.getAttribute("email"));
        }

        if (element.hasAttribute("server-address")) {
            config.withServerAddress(element.getAttribute("server-address"));
        }

        if (element.hasAttribute("cert-path")) {
            config.withDockerCertPath(element.getAttribute("cert-path"));
        }

        if (element.hasAttribute("config-path")) {
            config.withDockerCfgPath(element.getAttribute("config-path"));
        }

        builder.addPropertyValue("dockerClientConfig", config.build());
        parserContext.getRegistry().registerBeanDefinition(element.getAttribute("id"), builder.getBeanDefinition());

        return null;
    }
}
