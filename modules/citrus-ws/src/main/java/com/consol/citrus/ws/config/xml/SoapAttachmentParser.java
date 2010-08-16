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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser for SOAP attachment element in Citrus ws namespace.
 * 
 * @author Christoph Deppisch
 */
public class SoapAttachmentParser {
    /**
     * Prevent instantiation
     */
    private SoapAttachmentParser() {
        //prevent instantiation
    }
    
    /**
     * Parse the attachment element with all children and attributes.
     * @param builder
     * @param element
     * @param parserContext
     */
    public static void parseAttachment(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        Element attachmentElement = DomUtils.getChildElementByTagName(element, "attachment");
        if(attachmentElement == null) { return; }
        
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
