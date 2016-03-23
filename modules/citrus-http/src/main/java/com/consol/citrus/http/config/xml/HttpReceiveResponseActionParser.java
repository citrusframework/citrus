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
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpReceiveResponseActionParser extends ReceiveMessageActionParser {

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

        if (!element.hasAttribute("uri") && !element.hasAttribute("client")) {
            throw new BeanCreationException("Neither http request uri nor http client endpoint reference is given - invalid test action definition");
        }

        if (element.hasAttribute("client")) {
            builder.addPropertyReference("endpoint", element.getAttribute("client"));
        } else if (element.hasAttribute("uri")) {
            builder.addPropertyValue("endpointUri", element.getAttribute("uri"));
        }

        HttpMessage httpMessage = new HttpMessage();
        Element headers = DomUtils.getChildElementByTagName(element, "headers");
        if (headers != null) {
            List<?> headerElements = DomUtils.getChildElementsByTagName(headers, "header");
            for (Iterator<?> iter = headerElements.iterator(); iter.hasNext();) {
                Element header = (Element) iter.next();
                httpMessage.setHeader(header.getAttribute("name"), header.getAttribute("value"));
            }

            String statusCode = headers.getAttribute("status");
            if (StringUtils.hasText(statusCode)) {
                httpMessage.setHeader(HttpMessageHeaders.HTTP_STATUS_CODE, statusCode);
            }

            String reasonPhrase = headers.getAttribute("reason-phrase");
            if (StringUtils.hasText(reasonPhrase)) {
                httpMessage.reasonPhrase(reasonPhrase);
            }

            String version = headers.getAttribute("version");
            if (StringUtils.hasText(version)) {
                httpMessage.version(version);
            }
        }

        parseMessageSelector(element, builder);

        Element body = DomUtils.getChildElementByTagName(element, "body");
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
