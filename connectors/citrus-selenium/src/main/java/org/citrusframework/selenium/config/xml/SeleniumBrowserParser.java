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

package org.citrusframework.selenium.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractEndpointParser;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumBrowserConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for selenium browser instance.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class SeleniumBrowserParser extends AbstractEndpointParser {

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("type"), "browserType");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("version"), "version");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("start-page"), "startPageUrl");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("remote-server"), "remoteServerUrl");
        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration, element.getAttribute("javascript"), "javaScript");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("web-driver"), "webDriver");
        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("firefox-profile"), "firefoxProfile");

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration, element.getAttribute("event-listeners"), "eventListeners");
    }

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return SeleniumBrowser.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return SeleniumBrowserConfiguration.class;
    }
}
