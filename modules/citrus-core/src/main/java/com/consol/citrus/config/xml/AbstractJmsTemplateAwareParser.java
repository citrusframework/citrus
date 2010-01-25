/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
