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

import com.consol.citrus.message.MessageHeaderType;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.*;
import com.consol.citrus.validation.xml.XpathMessageConstructionInterceptor;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

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
    public AbstractMessageContentBuilder constructMessageBuilder(Element messageElement) {
        AbstractMessageContentBuilder messageBuilder = null;
        
        if (messageElement != null) {
            messageBuilder = parsePayloadTemplateBuilder(messageElement);
            
            if (messageBuilder == null) {
                messageBuilder = parseScriptBuilder(messageElement);
            }
        }

        if (messageBuilder == null) {
            messageBuilder = new PayloadTemplateMessageBuilder();
        }

        if (messageElement != null && messageElement.hasAttribute("name")) {
            messageBuilder.setMessageName(messageElement.getAttribute("name"));
        }

        return messageBuilder;
    }
    
    /**
     * @param messageElement
     * @return
     */
    private GroovyScriptMessageBuilder parseScriptBuilder(Element messageElement) {
        GroovyScriptMessageBuilder scriptMessageBuilder = null;
        
        Element builderElement = DomUtils.getChildElementByTagName(messageElement, "builder");
        if (builderElement != null) {
            String builderType = builderElement.getAttribute("type");
            
            if (!StringUtils.hasText(builderType)) {
                throw new BeanCreationException("Missing message builder type - please define valid type " +
                        "attribute for message builder");
            } else if (builderType.equals("groovy")) {
                scriptMessageBuilder = new GroovyScriptMessageBuilder();
            } else {
                throw new BeanCreationException("Unsupported message builder type: '" + builderType + "'");
            }

            String scriptResourcePath = builderElement.getAttribute("file");
            if (StringUtils.hasText(scriptResourcePath)) {
                scriptMessageBuilder.setScriptResourcePath(scriptResourcePath);
                if (builderElement.hasAttribute("charset")) {
                    scriptMessageBuilder.setScriptResourceCharset(builderElement.getAttribute("charset"));
                }
            } else {
                scriptMessageBuilder.setScriptData(DomUtils.getTextValue(builderElement).trim());
            }
        }

        if (scriptMessageBuilder != null && messageElement.hasAttribute("name")) {
            scriptMessageBuilder.setMessageName(messageElement.getAttribute("name"));
        }
        
        return scriptMessageBuilder;
    }

    /**
     * Parses message payload template information given in message element.
     * @param messageElement
     */
    private PayloadTemplateMessageBuilder parsePayloadTemplateBuilder(Element messageElement) {
        PayloadTemplateMessageBuilder messageBuilder;
        
        messageBuilder = parsePayloadElement(messageElement);
        
        Element xmlDataElement = DomUtils.getChildElementByTagName(messageElement, "data");
        if (xmlDataElement != null) {
            messageBuilder = new PayloadTemplateMessageBuilder();
            messageBuilder.setPayloadData(DomUtils.getTextValue(xmlDataElement).trim());
        }

        Element xmlResourceElement = DomUtils.getChildElementByTagName(messageElement, "resource");
        if (xmlResourceElement != null) {
            messageBuilder = new PayloadTemplateMessageBuilder();
            messageBuilder.setPayloadResourcePath(xmlResourceElement.getAttribute("file"));
            if (xmlResourceElement.hasAttribute("charset")) {
                messageBuilder.setPayloadResourceCharset(xmlResourceElement.getAttribute("charset"));
            }
        }
        
        if (messageBuilder != null) {
            Map<String, String> overwriteXpath = new HashMap<>();
            Map<String, String> overwriteJsonPath = new HashMap<>();
            List<?> messageValueElements = DomUtils.getChildElementsByTagName(messageElement, "element");
            for (Iterator<?> iter = messageValueElements.iterator(); iter.hasNext();) {
                Element messageValue = (Element) iter.next();
                String pathExpression = messageValue.getAttribute("path");

                if (JsonPathMessageValidationContext.isJsonPathExpression(pathExpression)) {
                    overwriteJsonPath.put(pathExpression, messageValue.getAttribute("value"));
                } else {
                    overwriteXpath.put(pathExpression, messageValue.getAttribute("value"));
                }
            }
            
            if (!overwriteXpath.isEmpty()) {
                XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteXpath);
                messageBuilder.add(interceptor);
            }

            if (!overwriteJsonPath.isEmpty()) {
                JsonPathMessageConstructionInterceptor interceptor = new JsonPathMessageConstructionInterceptor(overwriteJsonPath);
                messageBuilder.add(interceptor);
            }
        } 
        
        return messageBuilder;
    }

    /**
     * Parses the xs:any payload elements nested in message element.
     * @param messageElement
     */
    private PayloadTemplateMessageBuilder parsePayloadElement(Element messageElement) {
        PayloadTemplateMessageBuilder messageBuilder = null;
        
        // parse payload with xs-any element
        Element payloadElement = DomUtils.getChildElementByTagName(messageElement, "payload");
        if (payloadElement != null) {
            messageBuilder = new PayloadTemplateMessageBuilder();

            if (messageElement.hasAttribute("name")) {
                messageBuilder.setMessageName(messageElement.getAttribute("name"));
            }

            List<Element> payload = DomUtils.getChildElements(payloadElement);
            if (CollectionUtils.isEmpty(payload)) {
                messageBuilder.setPayloadData("");
            } else {
                messageBuilder.setPayloadData(PayloadElementParser.parseMessagePayload(payload.get(0)));
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
    protected void parseHeaderElements(Element actionElement, AbstractMessageContentBuilder messageBuilder, List<ValidationContext> validationContexts) {
        Element headerElement = DomUtils.getChildElementByTagName(actionElement, "header");
        Map<String, Object> messageHeaders = new LinkedHashMap<>();

        if (headerElement != null) {
            List<?> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                
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
                messageBuilder.getHeaderData().add(DomUtils.getTextValue(headerDataElement).trim());
            }

            List<Element> headerResourceElements = DomUtils.getChildElementsByTagName(headerElement, "resource");
            for (Element headerResourceElement : headerResourceElements) {
                String charset = headerResourceElement.getAttribute("charset");
                messageBuilder.getHeaderResources().add(headerResourceElement.getAttribute("file") + (StringUtils.hasText(charset) ? FileUtils.FILE_PATH_CHARSET_PARAMETER + charset : ""));
            }

            // parse fragment with xs-any element
            List<Element> headerFragmentElements = DomUtils.getChildElementsByTagName(headerElement, "fragment");
            for (Element headerFragmentElement : headerFragmentElements) {
                List<Element> fragment = DomUtils.getChildElements(headerFragmentElement);
                if (!CollectionUtils.isEmpty(fragment)) {
                    messageBuilder.getHeaderData().add(PayloadElementParser.parseMessagePayload(fragment.get(0)));
                }
            }

            messageBuilder.setMessageHeaders(messageHeaders);

            if (headerElement.hasAttribute("ignore-case")) {
                boolean ignoreCase = Boolean.valueOf(headerElement.getAttribute("ignore-case"));
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
            List<?> headerValueElements = DomUtils.getChildElementsByTagName(extractElement, "header");
            for (Iterator<?> iter = headerValueElements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                extractHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("variable"));
            }
            
            MessageHeaderVariableExtractor headerVariableExtractor = new MessageHeaderVariableExtractor();
            headerVariableExtractor.setHeaderMappings(extractHeaderValues);

            if (!CollectionUtils.isEmpty(extractHeaderValues)) {
                variableExtractors.add(headerVariableExtractor);
            }
        }
    }
}
