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
package com.consol.citrus.telnet.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.AbstractServerParser;
import com.consol.citrus.server.AbstractServer;
import com.consol.citrus.telnet.server.TelnetServer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Michael Wurmbrand
 * @since 2.6
 */
public class TelnetServerParser extends AbstractServerParser {

    @Override
    protected void parseServer(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("port"), "port");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("user"), "user");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("password"), "password");

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("message-converter"), "messageConverter");
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return TelnetServer.class;
    }

}
