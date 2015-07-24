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

package com.consol.citrus.http.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.AbstractServerParser;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.server.AbstractServer;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("servlet-name"), "servletName");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("servlet-mapping-path"), "servletMappingPath");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("context-path"), "contextPath");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("servlet-handler"), "servletHandler");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("security-handler"), "securityHandler");

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("message-converter"), "messageConverter");

        ManagedList<RuntimeBeanReference> webSocketReferences = new ManagedList<>();

        Element socketsElement = DomUtils.getChildElementByTagName(element, "websockets");
        if (socketsElement != null) {
            List<Element> socketElements = DomUtils.getChildElements(socketsElement);
            for (Element socketElement : socketElements) {
                if (socketElement.hasAttribute("websocket")){
                    webSocketReferences.add(new RuntimeBeanReference(socketElement.getAttribute("websocket")));
                } else {
                    throw new CitrusRuntimeException("Invalid '<websockets>..</websockets>' configuration - at least one '<websocket ref=\"..\" />' must be defined");
                }
            }
            if (webSocketReferences.size() > 0) {
                builder.addPropertyValue("webSockets", webSocketReferences);
            }

        }
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return HttpServer.class;
    }
}
