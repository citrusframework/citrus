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

package org.citrusframework.http.config.xml;

import jakarta.servlet.http.Cookie;
import java.util.List;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.config.xml.SendMessageActionParser;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpSendResponseActionParser extends SendMessageActionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = parseComponent(element, parserContext);
        builder.addPropertyValue("name", "http:" + element.getLocalName());

        DescriptionElementParser.doParse(element, builder);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");

        HttpMessage httpMessage = new HttpMessage();
        if (element.hasAttribute("server")) {
            builder.addPropertyReference("endpoint", element.getAttribute("server"));
        }

        Element headers = DomUtils.getChildElementByTagName(element, "headers");
        if (headers != null) {
            List<?> headerElements = DomUtils.getChildElementsByTagName(headers, "header");
            for (Object headerElement : headerElements) {
                Element header = (Element) headerElement;
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

            List<?> cookieElements = DomUtils.getChildElementsByTagName(headers, "cookie");
            for (Object item : cookieElements) {
                Element cookieElement = (Element) item;
                Cookie cookie = new Cookie(cookieElement.getAttribute("name"), cookieElement.getAttribute("value"));

                if (cookieElement.hasAttribute("comment")) {
                    cookie.setComment(cookieElement.getAttribute("comment"));
                }

                if (cookieElement.hasAttribute("path")) {
                    cookie.setPath(cookieElement.getAttribute("path"));
                }

                if (cookieElement.hasAttribute("domain")) {
                    cookie.setDomain(cookieElement.getAttribute("domain"));
                }

                if (cookieElement.hasAttribute("max-age")) {
                    cookie.setMaxAge(Integer.valueOf(cookieElement.getAttribute("max-age")));
                }

                if (cookieElement.hasAttribute("secure")) {
                    cookie.setSecure(Boolean.valueOf(cookieElement.getAttribute("secure")));
                }

                if (cookieElement.hasAttribute("version")) {
                    cookie.setVersion(Integer.valueOf(cookieElement.getAttribute("version")));
                }

                httpMessage.cookie(cookie);
            }
        }

        Element body = DomUtils.getChildElementByTagName(element, "body");
        if (body != null) {
            String messageType = body.getAttribute("type");
            if (StringUtils.hasText(messageType)) {
                builder.addPropertyValue("messageType", messageType);
            }

            String dataDictionary = body.getAttribute("data-dictionary");
            if (StringUtils.hasText(dataDictionary)) {
                builder.addPropertyReference("dataDictionary", dataDictionary);
            }
        }

        HttpMessageBuilder httpMessageBuilder = new HttpMessageBuilder(httpMessage);
        DefaultMessageBuilder messageContentBuilder = constructMessageBuilder(body, builder);

        httpMessageBuilder.setName(messageContentBuilder.getName());
        httpMessageBuilder.setPayloadBuilder(messageContentBuilder.getPayloadBuilder());
        messageContentBuilder.getHeaderBuilders().forEach(httpMessageBuilder::addHeaderBuilder);

        builder.addPropertyValue("messageBuilder", httpMessageBuilder);

        return builder.getBeanDefinition();
    }
}
