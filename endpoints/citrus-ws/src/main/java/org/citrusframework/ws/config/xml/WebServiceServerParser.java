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

package org.citrusframework.ws.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractServerParser;
import org.citrusframework.server.AbstractServer;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.server.WebServiceServer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for jetty-server component in Citrus ws namespace.
 *
 * @author Christoph Deppisch
 */
public class WebServiceServerParser extends AbstractServerParser {

    @Override
    protected void parseServer(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("port"), "port");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("auto-start"), "autoStart");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("resource-base"), "resourceBase");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("context-config-location"), "contextConfigLocation");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("connectors"), "connectors");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("connector"), "connector");

        String useRootContext = element.getAttribute("root-parent-context");
        if (StringUtils.hasText(useRootContext)) {
            builder.addPropertyValue("useRootContextAsParent", Boolean.valueOf(useRootContext));
        }

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("servlet-name"), "servletName");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("servlet-mapping-path"), "servletMappingPath");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("context-path"), "contextPath");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("servlet-handler"), "servletHandler");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("security-handler"), "securityHandler");

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("handle-mime-headers"), "handleMimeHeaders");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("handle-header-attributes"), "handleAttributeHeaders");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("keep-soap-envelope"), "keepSoapEnvelope");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("soap-header-namespace"), "soapHeaderNamespace");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("soap-header-prefix"), "soapHeaderPrefix");

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("message-factory"), "messageFactoryName");
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return WebServiceServer.class;
    }


}
