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
import com.consol.citrus.config.xml.SendMessageActionParser;
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.http.HttpMethod;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpSendRequestActionParser extends SendMessageActionParser {

    /** Http request method */
    private final HttpMethod requestMethod;

    /**
     * Default constructor using Http request method.
     * @param requestMethod
     */
    public HttpSendRequestActionParser(HttpMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = parseComponent(element, parserContext);
        builder.addPropertyValue("name", "http:" + element.getLocalName());

        DescriptionElementParser.doParse(element, builder);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("fork"), "forkMode");

        HttpMessage httpMessage = new HttpMessage();
        httpMessage.method(requestMethod);

        if (!element.hasAttribute("uri") && !element.hasAttribute("client")) {
            throw new BeanCreationException("Neither http request uri nor http client endpoint reference is given - invalid test action definition");
        }

        if (element.hasAttribute("client")) {
            builder.addPropertyReference("endpoint", element.getAttribute("client"));
        }

        if (element.hasAttribute("uri")) {
            if (!element.hasAttribute("client")) {
                builder.addPropertyValue("endpointUri", element.getAttribute("uri"));
            }

            httpMessage.setHeader(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME, element.getAttribute("uri"));
        }

        if (element.hasAttribute("path")) {
            httpMessage.path(element.getAttribute("path"));
        }

        List<?> params = DomUtils.getChildElementsByTagName(element, "param");
        for (Iterator<?> iter = params.iterator(); iter.hasNext();) {
            Element param = (Element) iter.next();
            httpMessage.queryParam(param.getAttribute("name"), param.getAttribute("value"));
        }

        Element headers = DomUtils.getChildElementByTagName(element, "headers");
        if (headers != null) {
            List<?> headerElements = DomUtils.getChildElementsByTagName(headers, "header");
            for (Iterator<?> iter = headerElements.iterator(); iter.hasNext();) {
                Element header = (Element) iter.next();
                httpMessage.setHeader(header.getAttribute("name"), header.getAttribute("value"));
            }

            Element contentType = DomUtils.getChildElementByTagName(headers, "contentType");
            if (contentType != null) {
                httpMessage.contentType(DomUtils.getTextValue(contentType));
            }

            Element accept = DomUtils.getChildElementByTagName(headers, "accept");
            if (accept != null) {
                httpMessage.accept(DomUtils.getTextValue(accept));
            }
        }

        Element body = DomUtils.getChildElementByTagName(element, "body");
        if (body != null) {
            httpMessage.setPayload(DomUtils.getTextValue(body).trim());
        }

        builder.addPropertyValue("messageBuilder", new StaticMessageContentBuilder(httpMessage));

        return builder.getBeanDefinition();
    }
}
