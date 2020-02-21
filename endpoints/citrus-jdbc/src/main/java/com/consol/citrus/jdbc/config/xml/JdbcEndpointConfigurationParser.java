/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.jdbc.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.db.server.JdbcServerConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcEndpointConfigurationParser {

    /**
     * Parse endpoint configuration.
     * @param endpointConfiguration
     * @param element
     */
    public void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element) {
        JdbcServerConfiguration serverConfiguration = new JdbcServerConfiguration();

        if (element.hasAttribute("host")) {
            serverConfiguration.setHost(element.getAttribute("host"));
        }

        if (element.hasAttribute("port")) {
            serverConfiguration.setPort(Integer.valueOf(element.getAttribute("port")));
        }

        if (element.hasAttribute("database-name")) {
            serverConfiguration.setDatabaseName(element.getAttribute("database-name"));
        }

        if (element.hasAttribute("max-connections")) {
            serverConfiguration.setMaxConnections(Integer.valueOf(element.getAttribute("max-connections")));
        }

        endpointConfiguration.addPropertyValue("serverConfiguration", serverConfiguration);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("timeout"), "timeout");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("auto-connect"), "autoConnect");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("auto-create-statement"), "autoCreateStatement");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("auto-transaction-handling"), "autoTransactionHandling");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("auto-handle-queries"), "autoHandleQueries");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("polling-interval"), "pollingInterval");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-correlator"), "correlator");
    }
}
