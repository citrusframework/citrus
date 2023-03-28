/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.http.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractServerParser;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.server.AbstractServer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for Http server implementation in Citrus http namespace.
 *
 * @author Christoph Deppisch
 */
public class HttpServerParser extends AbstractServerParser {

    @Override
    protected void parseServer(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("port"), "port");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("context-config-location"), "contextConfigLocation");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("resource-base"), "resourceBase");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("root-parent-context"), "useRootContextAsParent");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("connectors"), "connectors");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("connector"), "connector");

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("filters"), "filters");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("filter-mappings"), "filterMappings");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("binary-media-types"), "binaryMediaTypes");

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("servlet-name"), "servletName");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("servlet-mapping-path"), "servletMappingPath");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("context-path"), "contextPath");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("servlet-handler"), "servletHandler");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("security-handler"), "securityHandler");

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("handle-header-attributes"), "handleAttributeHeaders");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("handle-cookies"), "handleCookies");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("default-status-code"), "defaultStatusCode");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("response-cache-size"), "responseCacheSize");
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return HttpServer.class;
    }
}
