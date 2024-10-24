/*
 * Copyright the original author or authors.
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
import static org.citrusframework.config.xml.MessageSelectorParser.doParse;
import static org.citrusframework.http.config.xml.CookieUtils.setCookieElement;
import static org.springframework.util.xml.DomUtils.getChildElementByTagName;
import static org.springframework.util.xml.DomUtils.getChildElementsByTagName;

/**
 * @since 2.4
 */
public class HttpReceiveResponseActionParser extends ReceiveMessageActionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = createBeanDefinitionBuilder(
            element, parserContext);
        return builder.getBeanDefinition();
    }

    protected BeanDefinitionBuilder createBeanDefinitionBuilder(Element element,
        ParserContext parserContext) {
        BeanDefinitionBuilder builder = parseComponent(element, parserContext);
        builder.addPropertyValue("name", "http:" + element.getLocalName());

        DescriptionElementParser.doParse(element, builder);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");

        String receiveTimeout = element.getAttribute("timeout");
        if (StringUtils.hasText(receiveTimeout)) {
            builder.addPropertyValue("receiveTimeout", Long.valueOf(receiveTimeout));
        }

        validateEndpointConfiguration(element);

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
            setCookieElement(httpMessage, cookieElements);

            boolean ignoreCase = !headers.hasAttribute("ignore-case") || parseBoolean(headers.getAttribute("ignore-case"));
            validationContexts.stream()
                    .filter(context -> context instanceof HeaderValidationContext)
                    .map(context -> (HeaderValidationContext) context)
                    .forEach(context -> context.setHeaderNameIgnoreCase(ignoreCase));
        }

        doParse(element, builder);

        HttpMessageBuilder httpMessageBuilder = createMessageBuilder(httpMessage);
        DefaultMessageBuilder messageContentBuilder = constructMessageBuilder(body, builder);

        httpMessageBuilder.setName(messageContentBuilder.getName());
        httpMessageBuilder.setPayloadBuilder(messageContentBuilder.getPayloadBuilder());
        messageContentBuilder.getHeaderBuilders().forEach(httpMessageBuilder::addHeaderBuilder);

        builder.addPropertyValue("messageBuilder", httpMessageBuilder);
        builder.addPropertyValue("validationContexts", validationContexts);
        builder.addPropertyValue("variableExtractors", getVariableExtractors(element));

        return builder;
    }

    protected HttpMessageBuilder createMessageBuilder(HttpMessage httpMessage) {
        HttpMessageBuilder httpMessageBuilder = new HttpMessageBuilder(httpMessage);
        return httpMessageBuilder;
    }

    /**
     * Validates the endpoint configuration for the given XML element.
     * <p>
     * This method is designed to be overridden by subclasses if custom validation logic is required.
     * By default, it checks whether the 'uri' or 'client' attributes are present in the element.
     * If neither is found, it throws a {@link BeanCreationException} indicating an invalid test action definition.
     * </p>
     *
     * @param element the XML element representing the endpoint configuration to validate
     * @throws BeanCreationException if neither 'uri' nor 'client' attributes are present
     */
    protected void validateEndpointConfiguration(Element element) {
        if (!element.hasAttribute("uri") && !element.hasAttribute("client")) {
            throw new BeanCreationException("Neither http request uri nor http client endpoint reference is given - invalid test action definition");
        }
    }
}
