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
