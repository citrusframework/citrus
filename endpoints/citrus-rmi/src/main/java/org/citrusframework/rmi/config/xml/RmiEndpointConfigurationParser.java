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

package org.citrusframework.rmi.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiEndpointConfigurationParser {

    /**
     * Parse endpoint configuration.
     * @param endpointConfiguration
     * @param element
     */
    public void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element) {
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("timeout"), "timeout");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("host"), "host");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("port"), "port");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("binding"), "binding");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("polling-interval"), "pollingInterval");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("message-converter"), "messageConverter");
    }
}
