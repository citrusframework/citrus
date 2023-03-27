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

package org.citrusframework.citrus.websocket.config.xml;

import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.http.config.xml.HttpServerParser;
import org.citrusframework.citrus.server.AbstractServer;
import org.citrusframework.citrus.websocket.server.WebSocketServer;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class WebSocketServerParser extends HttpServerParser {

    @Override
    protected void parseServer(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        super.parseServer(builder, element, parserContext);

        ManagedList<RuntimeBeanReference> webSocketReferences = new ManagedList<>();
        Element socketsElement = DomUtils.getChildElementByTagName(element, "endpoints");
        if (socketsElement != null) {
            List<Element> socketElements = DomUtils.getChildElements(socketsElement);

            if (CollectionUtils.isEmpty(socketElements)) {
                throw new CitrusRuntimeException("Invalid '<endpoints>..</endpoints>' configuration - at least one '<endpoint ref=\"..\" />' must be defined");
            }

            for (Element socketElement : socketElements) {
                if (socketElement.hasAttribute("ref")) {
                    webSocketReferences.add(new RuntimeBeanReference(socketElement.getAttribute("ref")));
                }
            }

            if (webSocketReferences.size() > 0) {
                builder.addPropertyValue("webSockets", webSocketReferences);
            }

        }
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return WebSocketServer.class;
    }
}
