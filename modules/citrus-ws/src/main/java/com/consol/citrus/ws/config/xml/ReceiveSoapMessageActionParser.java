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

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.ReceiveMessageActionParser;
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for SOAP message receiver component in Citrus ws namespace.
 * 
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageActionParser extends ReceiveMessageActionParser {

    @Override
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ReceiveSoapMessageAction.class);

        List<Element> attachmentElements = DomUtils.getChildElementsByTagName(element, "attachment");
        List<SoapAttachment> attachments = new ArrayList<SoapAttachment>();
        for (Element attachment : attachmentElements) {
            attachments.add(SoapAttachmentParser.parseAttachment(attachment));
        }

        builder.addPropertyValue("attachments", attachments);
        
        if (!attachments.isEmpty()) {
            BeanDefinitionParserUtils.setPropertyReference(builder, attachmentElements.get(0).getAttribute("validator"),
                    "attachmentValidator", "soapAttachmentValidator");
        }
        
        return builder; 
    }
}
