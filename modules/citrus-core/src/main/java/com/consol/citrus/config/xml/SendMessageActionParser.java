/*
 * Copyright 2006-2010 the original author or authors.
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

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.variable.VariableExtractor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean definition parser for send action in test case.
 * 
 * @author Christoph Deppisch
 */
public class SendMessageActionParser extends AbstractMessageActionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String endpointUri = element.getAttribute("endpoint");

        if (!StringUtils.hasText(endpointUri)) {
            throw new BeanCreationException("Endpoint reference must not be empty");
        }

        BeanDefinitionBuilder builder = parseComponent(element, parserContext);
        builder.addPropertyValue("name", element.getLocalName());

        if (endpointUri.contains(":")) {
            builder.addPropertyValue("endpointUri", endpointUri);
        } else {
            builder.addPropertyReference("endpoint", endpointUri);
        }

        DescriptionElementParser.doParse(element, builder);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("fork"), "forkMode");
        
        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            String messageType = messageElement.getAttribute("type");
            if (StringUtils.hasText(messageType)) {
                builder.addPropertyValue("messageType", messageType);
            }

            String dataDictionary = messageElement.getAttribute("data-dictionary");
            if (StringUtils.hasText(dataDictionary)) {
                builder.addPropertyReference("dataDictionary", dataDictionary);
            }
        }

        AbstractMessageContentBuilder messageBuilder = constructMessageBuilder(messageElement);
        parseHeaderElements(element, messageBuilder);

        if (messageBuilder != null) {
            builder.addPropertyValue("messageBuilder", messageBuilder);
        }

        List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
        parseExtractHeaderElements(element, variableExtractors);
        
        if (!variableExtractors.isEmpty()) {
            builder.addPropertyValue("variableExtractors", variableExtractors);
        }

        return builder.getBeanDefinition();
    }

    /**
     * Parse component returning generic bean definition.
     * @param element
     * @param parserContext
     * @return
     */
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.genericBeanDefinition(getBeanDefinitionClass());
    }

    /**
     * Gets the bean definition builder class.
     * @return
     */
    protected Class<?> getBeanDefinitionClass() {
        return SendMessageAction.class;
    }
}
