/*
 * Copyright the original author or authors.
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

import org.citrusframework.base.server.AbstractServer;
import org.citrusframework.http.server.vertx.VertxHttpServer;
import org.citrusframework.spring.config.util.BeanDefinitionParserUtils;
import org.citrusframework.spring.config.xml.AbstractServerParser;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for the Vert.x based Http server implementation in the Citrus http namespace.
 *
 * @since 5.2
 */
public class VertxHttpServerParser extends AbstractServerParser {

    @Override
    protected void parseServer(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("port"), "port");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("secure-port"), "securePort");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("context-path"), "contextPath");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("use-default-filters"), "useDefaultFilters");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("binary-media-types"), "binaryMediaTypes");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("handle-cookies"), "handleCookies");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("default-status-code"), "defaultStatusCode");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("authentication"), "authentication");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("secured-path"), "securedPath");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("secured-connection"), "secureConnection");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("vertx"), "vertx");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("worker-pool-size"), "workerPoolSize");
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return VertxHttpServer.class;
    }
}
