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

package com.consol.citrus.ws.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.config.xml.ReceiveMessageActionParser;

/**
 * Parser for SOAP message receiver component in Citrus ws namespace.
 * 
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageActionParser extends ReceiveMessageActionParser {

    @Override
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition("com.consol.citrus.ws.actions.ReceiveSoapMessageAction");
        
        SoapAttachmentParser.parseAttachment(builder, element, parserContext);
        
        Element attachmentElement = DomUtils.getChildElementByTagName(element, "attachment");
        if(attachmentElement != null) {
            String attachmentValidator = attachmentElement.getAttribute("validator");
            if(StringUtils.hasText(attachmentValidator)) {
                builder.addPropertyReference("attachmentValidator", attachmentValidator);
            } else { //inject default soap attachment validator implementation
                builder.addPropertyReference("attachmentValidator", "soapAttachmentValidator");
            }
        }
        
        return builder; 
    }
}
