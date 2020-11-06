/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.config.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.consol.citrus.message.DelegatingPathExpressionProcessor;
import com.consol.citrus.message.MessageHeaderType;
import com.consol.citrus.message.MessageProcessor;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.message.builder.DefaultHeaderBuilder;
import com.consol.citrus.message.builder.DefaultHeaderDataBuilder;
import com.consol.citrus.message.builder.DefaultPayloadBuilder;
import com.consol.citrus.message.builder.FileResourceHeaderDataBuilder;
import com.consol.citrus.message.builder.FileResourcePayloadBuilder;
import com.consol.citrus.message.builder.script.GroovyFileResourcePayloadBuilder;
import com.consol.citrus.message.builder.script.GroovyScriptPayloadBuilder;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.DefaultMessageContentBuilder;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.interceptor.BinaryMessageProcessor;
import com.consol.citrus.validation.interceptor.GzipMessageProcessor;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser providing basic message element configurations used in send and receive actions.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractMessageActionParser implements BeanDefinitionParser {

    /**
     * Static parse method taking care of basic message element parsing.
     *
     * @param messageElement
     */
    public DefaultMessageContentBuilder constructMessageBuilder(Element messageElement, BeanDefinitionBuilder actionBuilder) {
        DefaultMessageContentBuilder messageBuilder = null;

        if (messageElement != null) {
            messageBuilder = parsePayloadTemplateBuilder(messageElement, actionBuilder);

            if (messageBuilder == null) {
                messageBuilder = parseScriptBuilder(messageElement);
            }
        }

        if (messageBuilder == null) {
            messageBuilder = new DefaultMessageContentBuilder();
        }

        if (messageElement != null && messageElement.hasAttribute("name")) {
            messageBuilder.setName(messageElement.getAttribute("name"));
        }

        return messageBuilder;
    }

    /**
     * @param messageElement
     * @return
     */
    private DefaultMessageContentBuilder parseScriptBuilder(Element messageElement) {
        DefaultMessageContentBuilder scriptMessageBuilder = null;

        Element builderElement = DomUtils.getChildElementByTagName(messageElement, "builder");
        if (builderElement != null) {
            String builderType = builderElement.getAttribute("type");

            if (!StringUtils.hasText(builderType)) {
                throw new BeanCreationException("Missing message builder type - please define valid type " +
                        "attribute for message builder");
            } else if (builderType.equals("groovy")) {
                scriptMessageBuilder = new DefaultMessageContentBuilder();
            } else {
                throw new BeanCreationException("Unsupported message builder type: '" + builderType + "'");
            }

            String scriptResourcePath = builderElement.getAttribute("file");
            if (StringUtils.hasText(scriptResourcePath)) {
                if (builderElement.hasAttribute("charset")) {
                    scriptMessageBuilder.setPayloadBuilder(new GroovyFileResourcePayloadBuilder(scriptResourcePath, builderElement.getAttribute("charset")));
                } else {
                    scriptMessageBuilder.setPayloadBuilder(new GroovyFileResourcePayloadBuilder(scriptResourcePath));
                }
            } else {
                scriptMessageBuilder.setPayloadBuilder(new GroovyScriptPayloadBuilder(DomUtils.getTextValue(builderElement).trim()));
            }
        }

        if (scriptMessageBuilder != null && messageElement.hasAttribute("name")) {
            scriptMessageBuilder.setName(messageElement.getAttribute("name"));
        }

        return scriptMessageBuilder;
    }

    /**
     * Parses message payload template information given in message element.
     * @param messageElement
     * @param actionBuilder
     */
    private DefaultMessageContentBuilder parsePayloadTemplateBuilder(Element messageElement, BeanDefinitionBuilder actionBuilder) {
        DefaultMessageContentBuilder messageBuilder;

        messageBuilder = parsePayloadElement(messageElement);

        Element xmlDataElement = DomUtils.getChildElementByTagName(messageElement, "data");
        if (xmlDataElement != null) {
            messageBuilder = new DefaultMessageContentBuilder();
            messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder(DomUtils.getTextValue(xmlDataElement).trim()));
        }

        Element xmlResourceElement = DomUtils.getChildElementByTagName(messageElement, "resource");
        if (xmlResourceElement != null) {
            messageBuilder = new DefaultMessageContentBuilder();
            if (xmlResourceElement.hasAttribute("charset")) {
                messageBuilder.setPayloadBuilder(
                        new FileResourcePayloadBuilder(xmlResourceElement.getAttribute("file"), xmlResourceElement.getAttribute("charset")));
            } else {
                messageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder(xmlResourceElement.getAttribute("file")));
            }
        }

        if (messageBuilder != null) {
            Map<String, Object> pathExpressions = new HashMap<>();
            List<Element> messageValueElements = DomUtils.getChildElementsByTagName(messageElement, "element");
            for (Element messageValue : messageValueElements) {
                String pathExpression = messageValue.getAttribute("path");
                pathExpressions.put(pathExpression, messageValue.getAttribute("value"));
            }

            List<MessageProcessor> messageProcessors = new ArrayList<>();
            if (!pathExpressions.isEmpty()) {
                messageProcessors.add(new DelegatingPathExpressionProcessor(pathExpressions));
            }

            String messageType = messageElement.getAttribute("type");
            if (StringUtils.hasText(messageType)) {
                if (messageType.equalsIgnoreCase(MessageType.GZIP.name())) {
                    messageProcessors.add(new GzipMessageProcessor());
                }

                if (messageType.equalsIgnoreCase(MessageType.BINARY.name())) {
                    messageProcessors.add(new BinaryMessageProcessor());
                }
            }

            actionBuilder.addPropertyValue("messageProcessors", messageProcessors);
        }

        return messageBuilder;
    }

    /**
     * Parses the xs:any payload elements nested in message element.
     * @param messageElement
     */
    private DefaultMessageContentBuilder parsePayloadElement(Element messageElement) {
        DefaultMessageContentBuilder messageBuilder = null;

        // parse payload with xs-any element
        Element payloadElement = DomUtils.getChildElementByTagName(messageElement, "payload");
        if (payloadElement != null) {
            messageBuilder = new DefaultMessageContentBuilder();

            if (messageElement.hasAttribute("name")) {
                messageBuilder.setName(messageElement.getAttribute("name"));
            }

            List<Element> payload = DomUtils.getChildElements(payloadElement);
            if (!CollectionUtils.isEmpty(payload)) {
                messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder(PayloadElementParser.parseMessagePayload(payload.get(0))));
            }
        }

        return messageBuilder;
    }

    /**
     * Parse message header elements in action and add headers to
     * message content builder.
     *
     * @param actionElement the action DOM element.
     * @param messageBuilder the message content builder.
     * @param validationContexts list of validation contexts.
     */
    protected void parseHeaderElements(Element actionElement, DefaultMessageContentBuilder messageBuilder, List<ValidationContext> validationContexts) {
        Element headerElement = DomUtils.getChildElementByTagName(actionElement, "header");
        Map<String, Object> messageHeaders = new LinkedHashMap<>();

        if (headerElement != null) {
            List<Element> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Element headerValue : elements) {
                String name = headerValue.getAttribute("name");
                String value = headerValue.getAttribute("value");
                String type = headerValue.getAttribute("type");

                if (StringUtils.hasText(type)) {
                    value = MessageHeaderType.createTypedValue(type, value);
                }

                messageHeaders.put(name, value);
            }

            List<Element> headerDataElements = DomUtils.getChildElementsByTagName(headerElement, "data");
            for (Element headerDataElement : headerDataElements) {
                messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder(DomUtils.getTextValue(headerDataElement).trim()));
            }

            List<Element> headerResourceElements = DomUtils.getChildElementsByTagName(headerElement, "resource");
            for (Element headerResourceElement : headerResourceElements) {
                String charset = headerResourceElement.getAttribute("charset");
                messageBuilder.addHeaderBuilder(new FileResourceHeaderDataBuilder(
                        headerResourceElement.getAttribute("file") + (StringUtils.hasText(charset) ? FileUtils.FILE_PATH_CHARSET_PARAMETER + charset : "")));
            }

            // parse fragment with xs-any element
            List<Element> headerFragmentElements = DomUtils.getChildElementsByTagName(headerElement, "fragment");
            for (Element headerFragmentElement : headerFragmentElements) {
                List<Element> fragment = DomUtils.getChildElements(headerFragmentElement);
                if (!CollectionUtils.isEmpty(fragment)) {
                    messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder(PayloadElementParser.parseMessagePayload(fragment.get(0))));
                }
            }

            messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(messageHeaders));

            if (headerElement.hasAttribute("ignore-case")) {
                boolean ignoreCase = Boolean.parseBoolean(headerElement.getAttribute("ignore-case"));
                validationContexts.stream().filter(context -> context instanceof HeaderValidationContext)
                                            .map(context -> (HeaderValidationContext) context)
                                            .forEach(context -> context.setHeaderNameIgnoreCase(ignoreCase));
            }
        }
    }

    /**
     * Parses header extract information.
     * @param element the root action element.
     * @param variableExtractors the variable extractors to add new extractors to.
     */
    protected void parseExtractHeaderElements(Element element, List<VariableExtractor> variableExtractors) {
        Element extractElement = DomUtils.getChildElementByTagName(element, "extract");
        Map<String, String> extractHeaderValues = new HashMap<>();
        if (extractElement != null) {
            List<Element> headerValueElements = DomUtils.getChildElementsByTagName(extractElement, "header");
            for (Element headerValue : headerValueElements) {
                extractHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("variable"));
            }

            MessageHeaderVariableExtractor headerVariableExtractor = new MessageHeaderVariableExtractor.Builder()
                    .headers(extractHeaderValues)
                    .build();

            if (!CollectionUtils.isEmpty(extractHeaderValues)) {
                variableExtractors.add(headerVariableExtractor);
            }
        }
    }
}
