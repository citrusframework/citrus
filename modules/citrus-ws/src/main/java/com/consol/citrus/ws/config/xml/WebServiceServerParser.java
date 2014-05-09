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

package com.consol.citrus.ws.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.AbstractServerParser;
import com.consol.citrus.server.AbstractServer;
import com.consol.citrus.ws.server.WebServiceServer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for jetty-server component in Citrus ws namespace.
 * 
 * @author Christoph Deppisch
 */
public class WebServiceServerParser extends AbstractServerParser {

    @Override
    protected void parseServer(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute(WSParserConstants.PORT_ATTRIBUTE), WSParserConstants.PORT_PROPERTY);
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute(WSParserConstants.AUTOSTART_ATTRIBUTE), WSParserConstants.AUTOSTART_PROPERTY);
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute(WSParserConstants.RESOURCE_BASE_ATTRIBUTE), WSParserConstants.RESOURCE_BASE_PROPERTY);
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute(WSParserConstants.CONTEXT_CONFIC_LOCATION_ATTRIBUTE), WSParserConstants.CONTEXT_CONFIC_LOCATION_PROPERTY);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute(WSParserConstants.CONNECTORS_ATTRIBUTE), WSParserConstants.CONNECTORS_PROPERTY);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute(WSParserConstants.CONNECTOR_ATTRIBUTE), WSParserConstants.CONNECTOR_PROPERTY);

        String useRootContext = element.getAttribute(WSParserConstants.USE_ROOT_CONTEXT_ATTRIBUTE);
        if (StringUtils.hasText(useRootContext)) {
            builder.addPropertyValue(WSParserConstants.USE_ROOT_CONTEXT_PROPERTY, Boolean.valueOf(useRootContext));
        }
        
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("servlet-name"), "servletName");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("servlet-mapping-path"), "servletMappingPath");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("context-path"), "contextPath");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("servlet-handler"), "servletHandler");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("security-handler"), "securityHandler");

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("handle-mime-headers"), "handleMimeHeaders");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("soap-header-namespace"), "soapHeaderNamespace");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("soap-header-prefix"), "soapHeaderPrefix");
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return WebServiceServer.class;
    }


}
