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

package org.citrusframework.config.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.common.Named;
import org.citrusframework.config.xml.parser.CitrusXmlConfigParser;
import org.citrusframework.config.xml.parser.ScriptMessageBuilderParser;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.MessageHeaderType;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.DefaultHeaderBuilder;
import org.citrusframework.message.builder.DefaultHeaderDataBuilder;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.message.builder.FileResourceHeaderDataBuilder;
import org.citrusframework.message.builder.FileResourcePayloadBuilder;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.interceptor.BinaryMessageProcessor;
import org.citrusframework.validation.interceptor.GzipMessageProcessor;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.VariableExtractor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.CollectionUtils;
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
    public DefaultMessageBuilder constructMessageBuilder(Element messageElement, BeanDefinitionBuilder actionBuilder) {
        DefaultMessageBuilder messageBuilder = null;

        if (messageElement != null) {
            messageBuilder = parsePayloadTemplateBuilder(messageElement, actionBuilder);

            if (messageBuilder == null) {
                messageBuilder = parseScriptBuilder(messageElement);
            }
        }

        if (messageBuilder == null) {
            messageBuilder = new DefaultMessageBuilder();
        }

        if (messageElement != null
                && messageElement.hasAttribute("name")
                && messageBuilder instanceof Named) {
            ((Named) messageBuilder).setName(messageElement.getAttribute("name"));
        }

        return messageBuilder;
    }

    /**
     * @param messageElement
     * @return
     */
    private DefaultMessageBuilder parseScriptBuilder(Element messageElement) {
        Element builderElement = DomUtils.getChildElementByTagName(messageElement, "builder");
        if (builderElement == null) {
            return null;
        }

        String builderType = builderElement.getAttribute("type");
        if (!StringUtils.hasText(builderType)) {
            throw new BeanCreationException("Missing message builder type - please define valid type " +
                    "attribute for message builder");
        }

        Optional<ScriptMessageBuilderParser> scriptMessageBuilderParser = Optional.ofNullable(
                CitrusXmlConfigParser.lookup("script").get(builderType))
                .filter(ScriptMessageBuilderParser.class::isInstance)
                .map(ScriptMessageBuilderParser.class::cast);

        if (scriptMessageBuilderParser.isEmpty()) {
            throw new BeanCreationException("Unsupported message builder type: '" + builderType + "'");
        }

        return scriptMessageBuilderParser.get().parse(messageElement);
    }

    /**
     * Parses message payload template information given in message element.
     * @param messageElement
     * @param actionBuilder
     */
    private DefaultMessageBuilder parsePayloadTemplateBuilder(Element messageElement, BeanDefinitionBuilder actionBuilder) {
        DefaultMessageBuilder messageBuilder;

        messageBuilder = parsePayloadElement(messageElement);

        Element xmlDataElement = DomUtils.getChildElementByTagName(messageElement, "data");
        if (xmlDataElement != null) {
            messageBuilder = new DefaultMessageBuilder();
            messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder(DomUtils.getTextValue(xmlDataElement).trim()));
        }

        Element xmlResourceElement = DomUtils.getChildElementByTagName(messageElement, "resource");
        if (xmlResourceElement != null) {
            messageBuilder = new DefaultMessageBuilder();
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
                messageProcessors.add(new DelegatingPathExpressionProcessor.Builder().expressions(pathExpressions).build());
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
    private DefaultMessageBuilder parsePayloadElement(Element messageElement) {
        DefaultMessageBuilder messageBuilder = null;

        // parse payload with xs-any element
        Element payloadElement = DomUtils.getChildElementByTagName(messageElement, "payload");
        if (payloadElement != null) {
            messageBuilder = new DefaultMessageBuilder();

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
    protected void parseHeaderElements(Element actionElement, DefaultMessageBuilder messageBuilder, List<ValidationContext> validationContexts) {
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
