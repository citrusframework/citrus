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

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.Cookie;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.config.xml.SendMessageActionParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.variable.VariableExtractor;
import org.springframework.beans.factory.BeanCreationException;
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
public class HttpSendRequestActionParser extends SendMessageActionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = parseComponent(element, parserContext);
        builder.addPropertyValue("name", "http:" + element.getLocalName());

        DescriptionElementParser.doParse(element, builder);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("fork"), "forkMode");

        HttpMessage httpMessage = new HttpMessage();

        if (!element.hasAttribute("uri") && !element.hasAttribute("client")) {
            throw new BeanCreationException("Neither http request uri nor http client endpoint reference is given - invalid test action definition");
        }

        if (element.hasAttribute("client")) {
            builder.addPropertyReference("endpoint", element.getAttribute("client"));
        }

        if (element.hasAttribute("uri")) {
            if (!element.hasAttribute("client")) {
                builder.addPropertyValue("endpointUri", element.getAttribute("uri"));
            } else {
                httpMessage.setHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME, element.getAttribute("uri"));
            }
        }

        Element requestElement = DomUtils.getChildElements(element).get(0);
        httpMessage.method(HttpMethod.valueOf(requestElement.getLocalName().toUpperCase()));
        if (requestElement.hasAttribute("path")) {
            httpMessage.path(requestElement.getAttribute("path"));
        }

        List<?> params = DomUtils.getChildElementsByTagName(requestElement, "param");
        for (Object aParam : params) {
            Element param = (Element) aParam;
            httpMessage.queryParam(param.getAttribute("name"), param.getAttribute("value"));
        }

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
        }

        Element body = DomUtils.getChildElementByTagName(requestElement, "body");
        if (body != null) {
            String messageType = body.getAttribute("type");
            if (StringUtils.hasText(messageType)) {
                builder.addPropertyValue("messageType", messageType);
            }

            String dataDictionary = body.getAttribute("data-dictionary");
            if (StringUtils.hasText(dataDictionary)) {
                builder.addPropertyReference("dataDictionary", dataDictionary);
            }

            String schemaValidation = body.getAttribute("schema-validation");
            if (StringUtils.hasText(schemaValidation)) {
                builder.addPropertyValue("schemaValidation", Boolean.valueOf(schemaValidation));
            }

            String schema = body.getAttribute("schema");
            if (StringUtils.hasText(schema)) {
                builder.addPropertyValue("schemaValidation", true);
                builder.addPropertyValue("schema", schema);
            }

            String schemaRepository = body.getAttribute("schema-repository");
            if (StringUtils.hasText(schemaRepository)) {
                builder.addPropertyValue("schemaValidation", true);
                builder.addPropertyValue("schemaRepository", schemaRepository);
            }
        }

        HttpMessageBuilder httpMessageBuilder = new HttpMessageBuilder(httpMessage);
        DefaultMessageBuilder messageContentBuilder = constructMessageBuilder(body, builder);

        httpMessageBuilder.setName(messageContentBuilder.getName());
        httpMessageBuilder.setPayloadBuilder(messageContentBuilder.getPayloadBuilder());
        messageContentBuilder.getHeaderBuilders().forEach(httpMessageBuilder::addHeaderBuilder);

        builder.addPropertyValue("messageBuilder", httpMessageBuilder);

        List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
        parseExtractHeaderElements(element, variableExtractors);

        if (!variableExtractors.isEmpty()) {
            builder.addPropertyValue("variableExtractors", variableExtractors);
        }

        return builder.getBeanDefinition();
    }
}
