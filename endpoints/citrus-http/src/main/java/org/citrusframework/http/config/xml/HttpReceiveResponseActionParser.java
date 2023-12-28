/*
 * Copyright 2006-2024 the original author or authors.
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
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.config.xml.ReceiveMessageActionParser;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.List;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static org.citrusframework.config.xml.MessageSelectorParser.doParse;
import static org.springframework.util.xml.DomUtils.getChildElementByTagName;
import static org.springframework.util.xml.DomUtils.getChildElementsByTagName;

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

        Element body = getChildElementByTagName(element, "body");
        List<ValidationContext> validationContexts = parseValidationContexts(body, builder);

        Element headers = getChildElementByTagName(element, "headers");
        if (headers != null) {
            List<?> headerElements = getChildElementsByTagName(headers, "header");
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

            List<?> cookieElements = getChildElementsByTagName(headers, "cookie");
            for (Object item : cookieElements) {
                Element cookieElement = (Element) item;
                Cookie cookie = new Cookie(cookieElement.getAttribute("name"), cookieElement.getAttribute("value"));

                if (cookieElement.hasAttribute("path")) {
                    cookie.setPath(cookieElement.getAttribute("path"));
                }

                if (cookieElement.hasAttribute("domain")) {
                    cookie.setDomain(cookieElement.getAttribute("domain"));
                }

                if (cookieElement.hasAttribute("max-age")) {
                    cookie.setMaxAge(parseInt(cookieElement.getAttribute("max-age")));
                }

                if (cookieElement.hasAttribute("secure")) {
                    cookie.setSecure(parseBoolean(cookieElement.getAttribute("secure")));
                }

                httpMessage.cookie(cookie);
            }

            boolean ignoreCase = !headers.hasAttribute("ignore-case") || parseBoolean(headers.getAttribute("ignore-case"));
            validationContexts.stream()
                    .filter(context -> context instanceof HeaderValidationContext)
                    .map(context -> (HeaderValidationContext) context)
                    .forEach(context -> context.setHeaderNameIgnoreCase(ignoreCase));
        }

        doParse(element, builder);

        HttpMessageBuilder httpMessageBuilder = new HttpMessageBuilder(httpMessage);
        DefaultMessageBuilder messageContentBuilder = constructMessageBuilder(body, builder);

        httpMessageBuilder.setName(messageContentBuilder.getName());
        httpMessageBuilder.setPayloadBuilder(messageContentBuilder.getPayloadBuilder());
        messageContentBuilder.getHeaderBuilders().forEach(httpMessageBuilder::addHeaderBuilder);

        builder.addPropertyValue("messageBuilder", httpMessageBuilder);
        builder.addPropertyValue("validationContexts", validationContexts);
        builder.addPropertyValue("variableExtractors", getVariableExtractors(element));

        return builder.getBeanDefinition();
    }
}
