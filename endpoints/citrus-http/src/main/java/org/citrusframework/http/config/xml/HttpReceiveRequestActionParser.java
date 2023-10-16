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

import java.util.List;

import jakarta.servlet.http.Cookie;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.config.xml.MessageSelectorParser;
import org.citrusframework.config.xml.ReceiveMessageActionParser;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpQueryParamHeaderValidator;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.http.HttpMethod;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

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
        for (Object item : params) {
            Element param = (Element) item;
            httpMessage.queryParam(param.getAttribute("name"), param.getAttribute("value"));
        }

        Element body = DomUtils.getChildElementByTagName(requestElement, "body");
        List<ValidationContext> validationContexts = parseValidationContexts(body, builder);

        validationContexts.stream().filter(context -> context instanceof HeaderValidationContext)
                .map(context -> (HeaderValidationContext) context)
                .forEach(context -> context.addHeaderValidator(new HttpQueryParamHeaderValidator()));

        Element headers = DomUtils.getChildElementByTagName(requestElement, "headers");
        if (headers != null) {
            List<?> headerElements = DomUtils.getChildElementsByTagName(headers, "header");
            for (Object headerElement : headerElements) {
                Element header = (Element) headerElement;
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

            List<?> cookieElements = DomUtils.getChildElementsByTagName(headers, "cookie");
            for (Object cookieElement : cookieElements) {
                Element cookie = (Element) cookieElement;
                httpMessage.cookie(new Cookie(cookie.getAttribute("name"), cookie.getAttribute("value")));
            }

            boolean ignoreCase = headers.hasAttribute("ignore-case") ? Boolean.valueOf(headers.getAttribute("ignore-case")) : true;
            validationContexts.stream().filter(context -> context instanceof HeaderValidationContext)
                    .map(context -> (HeaderValidationContext) context)
                    .forEach(context -> context.setHeaderNameIgnoreCase(ignoreCase));
        }

        MessageSelectorParser.doParse(element, builder);

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
