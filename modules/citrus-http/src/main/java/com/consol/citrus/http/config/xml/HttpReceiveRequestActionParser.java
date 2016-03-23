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

package com.consol.citrus.http.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.DescriptionElementParser;
import com.consol.citrus.config.xml.ReceiveMessageActionParser;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpReceiveRequestActionParser extends ReceiveMessageActionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = parseComponent(element, parserContext);
        builder.addPropertyValue("name", "http:" + element.getLocalName());

        DescriptionElementParser.doParse(element, builder);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");

        String receiveTimeout = element.getAttribute("timeout");
        if (StringUtils.hasText(receiveTimeout)) {
            builder.addPropertyValue("receiveTimeout", Long.valueOf(receiveTimeout));
        }

        if (element.hasAttribute("server")) {
            builder.addPropertyReference("endpoint", element.getAttribute("server"));
        }

        HttpMessage httpMessage = new HttpMessage();
        Element requestElement = DomUtils.getChildElements(element).get(0);
        httpMessage.method(HttpMethod.valueOf(requestElement.getLocalName().toUpperCase()));
        if (requestElement.hasAttribute("path")) {
            httpMessage.path(requestElement.getAttribute("path"));
        }

        if (requestElement.hasAttribute("context-path")) {
            httpMessage.contextPath(requestElement.getAttribute("context-path"));
        }

        List<?> params = DomUtils.getChildElementsByTagName(requestElement, "param");
        for (Iterator<?> iter = params.iterator(); iter.hasNext();) {
            Element param = (Element) iter.next();
            httpMessage.queryParam(param.getAttribute("name"), param.getAttribute("value"));
        }

        Element headers = DomUtils.getChildElementByTagName(requestElement, "headers");
        if (headers != null) {
            List<?> headerElements = DomUtils.getChildElementsByTagName(headers, "header");
            for (Iterator<?> iter = headerElements.iterator(); iter.hasNext();) {
                Element header = (Element) iter.next();
                httpMessage.setHeader(header.getAttribute("name"), header.getAttribute("value"));
            }

            String contentType = headers.getAttribute("content-type");
            if (StringUtils.hasText(contentType)) {
                httpMessage.contentType(contentType);
            }

            String accept = headers.getAttribute("accept");
            if (StringUtils.hasText(accept)) {
                httpMessage.accept(accept);
            }

            String version = headers.getAttribute("version");
            if (StringUtils.hasText(version)) {
                httpMessage.version(version);
            }
        }

        parseMessageSelector(element, builder);

        Element body = DomUtils.getChildElementByTagName(requestElement, "body");
        List<ValidationContext> validationContexts = parseValidationContexts(body, builder);

        AbstractMessageContentBuilder messageBuilder = constructMessageBuilder(body);
        Map<String, Object> messageHeaders = httpMessage.getHeaders();
        messageHeaders.remove(MessageHeaders.ID);
        messageHeaders.remove(MessageHeaders.TIMESTAMP);
        messageBuilder.setMessageHeaders(messageHeaders);

        builder.addPropertyValue("messageBuilder", messageBuilder);
        builder.addPropertyValue("validationContexts", validationContexts);
        builder.addPropertyValue("variableExtractors", getVariableExtractors(element));

        return builder.getBeanDefinition();
    }
}
