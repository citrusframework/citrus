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

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser is aware of JMS template if present in element.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractJmsTemplateAwareParser extends AbstractJmsConfigParser {

    @Override
    protected BeanDefinitionBuilder doParse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = doParseComponent(element, parserContext);
        
        String jmsTemplate = element.getAttribute(JmsParserConstants.JMS_TEMPLATE_ATTRIBUTE);
        
        if (StringUtils.hasText(jmsTemplate)) {
            if (element.hasAttribute(JmsParserConstants.CONNECTION_FACTORY_ATTRIBUTE) ||
                    element.hasAttribute(JmsParserConstants.DESTINATION_ATTRIBUTE) ||
                    element.hasAttribute(JmsParserConstants.DESTINATION_NAME_ATTRIBUTE)) {
                throw new BeanCreationException("When providing a '" + JmsParserConstants.JMS_TEMPLATE_ATTRIBUTE + "' reference, none of " +
                        "'" + JmsParserConstants.CONNECTION_FACTORY_ATTRIBUTE + "', '" + JmsParserConstants.DESTINATION_ATTRIBUTE + 
                        "', or '" + JmsParserConstants.DESTINATION_NAME_ATTRIBUTE + "' should be provided.");
            }
            builder.addPropertyReference(JmsParserConstants.JMS_TEMPLATE_PROPERTY, jmsTemplate);
        }
        
        return builder;
    }
    
    /**
     * Subclasses must implement this method in order to provide detailed
     * bean definition parsing.
     * @param element
     * @param parserContext
     * @return
     */
    protected abstract BeanDefinitionBuilder doParseComponent(Element element, ParserContext parserContext);
}
