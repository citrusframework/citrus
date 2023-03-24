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

package org.citrusframework.ws.config.xml;

import org.citrusframework.ws.message.SoapAttachment;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser for SOAP attachment element in Citrus ws namespace.
 * 
 * @author Christoph Deppisch
 */
public final class SoapAttachmentParser {
    /**
     * Prevent instantiation
     */
    private SoapAttachmentParser() {
        //prevent instantiation
    }
    
    /**
     * Parse the attachment element with all children and attributes.
     * @param attachmentElement
     */
    public static SoapAttachment parseAttachment(Element attachmentElement) {
        SoapAttachment soapAttachment = new SoapAttachment();

        if (attachmentElement.hasAttribute("content-id")) {
            soapAttachment.setContentId(attachmentElement.getAttribute("content-id"));
        }

        if (attachmentElement.hasAttribute("content-type")) {
            soapAttachment.setContentType(attachmentElement.getAttribute("content-type"));
        }

        if (attachmentElement.hasAttribute("charset-name")) {
            soapAttachment.setCharsetName(attachmentElement.getAttribute("charset-name"));
        }

        if (attachmentElement.hasAttribute("mtom-inline")) {
            soapAttachment.setMtomInline(Boolean.parseBoolean(attachmentElement.getAttribute("mtom-inline")));
        }

        if (attachmentElement.hasAttribute("encoding-type")) {
            soapAttachment.setEncodingType(attachmentElement.getAttribute("encoding-type"));
        }
        
        Element attachmentDataElement = DomUtils.getChildElementByTagName(attachmentElement, "data");
        if (attachmentDataElement != null) {
            soapAttachment.setContent(DomUtils.getTextValue(attachmentDataElement));
        }
        
        Element attachmentResourceElement = DomUtils.getChildElementByTagName(attachmentElement, "resource");
        if (attachmentResourceElement != null) {
            soapAttachment.setContentResourcePath(attachmentResourceElement.getAttribute("file"));
        }

        return soapAttachment;
    }
}
