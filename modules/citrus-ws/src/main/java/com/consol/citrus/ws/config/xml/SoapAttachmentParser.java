/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class SoapAttachmentParser {
    private SoapAttachmentParser() {
        //prevent instantiation
    }
    
    public static void parseAttachment(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        Element attachmentElement = DomUtils.getChildElementByTagName(element, "attachment");
        if(attachmentElement == null) {
            return;
        }
        
        String contentId = attachmentElement.getAttribute("content-id");
        if(StringUtils.hasText(contentId)) {
            builder.addPropertyValue("contentId", contentId);
        }
        
        String contentType = attachmentElement.getAttribute("content-type");
        if(StringUtils.hasText(contentType)) {
            builder.addPropertyValue("contentType", contentType);
        }
        
        String charset = attachmentElement.getAttribute("charset-name");
        if(StringUtils.hasText(charset)) {
            builder.addPropertyValue("charsetName", charset);
        }
        
        if (attachmentElement != null) {
            Element attachmentDataElement = DomUtils.getChildElementByTagName(attachmentElement, "data");
            if (attachmentDataElement != null) {
                builder.addPropertyValue("attachmentData", DomUtils.getTextValue(attachmentDataElement));
            }
            
            Element attachmentResourceElement = DomUtils.getChildElementByTagName(attachmentElement, "resource");
            if (attachmentResourceElement != null) {
                String attachmentFilePath = attachmentResourceElement.getAttribute("file");
                if (attachmentFilePath.startsWith("classpath:")) {
                    builder.addPropertyValue("attachmentResource", new ClassPathResource(attachmentFilePath.substring("classpath:".length())));
                } else if (attachmentFilePath.startsWith("file:")) {
                    builder.addPropertyValue("attachmentResource", new FileSystemResource(attachmentFilePath.substring("file:".length())));
                } else {
                    builder.addPropertyValue("attachmentResource", new FileSystemResource(attachmentFilePath));
                }
            }
        }
    }
}
