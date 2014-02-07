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
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.server.AbstractServer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for Http server implementation in Citrus http namespace.
 * 
 * @author Christoph Deppisch
 */
public class HttpServerParser extends AbstractServerParser {

    @Override
    protected void parseServer(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        super.parseServer(builder, element, parserContext);

        String port = element.getAttribute("port");
        if (StringUtils.hasText(port)) {
            builder.addPropertyValue("port", port);
        }
        
        String contextConfigLocation = element.getAttribute("context-config-location");
        if (StringUtils.hasText(contextConfigLocation)) {
            builder.addPropertyValue("contextConfigLocation", contextConfigLocation);
        }
        
        String resourceBase = element.getAttribute("resource-base");
        if (StringUtils.hasText(resourceBase)) {
            builder.addPropertyValue("resourceBase", resourceBase);
        }
        
        String useRootContext = element.getAttribute("root-parent-context");
        if (StringUtils.hasText(useRootContext)) {
            builder.addPropertyValue("useRootContextAsParent", Boolean.valueOf(useRootContext));
        }
        
        String connectors = element.getAttribute("connectors");
        if (StringUtils.hasText(connectors)) {
            builder.addPropertyReference("connectors", connectors);
        }
        
        String connector = element.getAttribute("connector");
        if (StringUtils.hasText(connector)) {
            builder.addPropertyReference("connector", connector);
        }
        
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("servlet-name"), "servletName");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("servlet-mapping-path"), "servletMappingPath");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("context-path"), "contextPath");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("servlet-handler"), "servletHandler");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("security-handler"), "securityHandler");
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return HttpServer.class;
    }
}
