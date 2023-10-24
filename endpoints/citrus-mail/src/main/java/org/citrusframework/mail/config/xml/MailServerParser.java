/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.mail.config.xml;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractServerParser;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.server.AbstractServer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailServerParser extends AbstractServerParser {

    @Override
    protected void parseServer(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("port"), "port");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("auth-required"), "authRequired");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("auto-accept"), "autoAccept");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("split-multipart"), "splitMultipart");

        if (element.hasAttribute("known-users")) {
            builder.addPropertyValue("knownUsers", Arrays.stream(element.getAttribute("known-users").split(",")).collect(Collectors.toList()));
        }

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("marshaller"), "marshaller");

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("mail-properties"), "javaMailProperties");
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return MailServer.class;
    }
}
