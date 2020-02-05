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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.XMLUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Bean definition parser for payload element used in message 
 * elements in send and receive action.
 * 
 * @author Christoph Deppisch
 */
public abstract class PayloadElementParser {
    
    /**
     * Prevent instantiation.
     */
    private PayloadElementParser() {
    }
    
    /**
     * Static parse method taking care of payload element.
     * @param payloadElement
     */
    public static String parseMessagePayload(Element payloadElement) {
        if (payloadElement == null) {
            return "";
        }
        
        try {
            Document payload = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            payload.appendChild(payload.importNode(payloadElement, true));

            String payloadData = XMLUtils.serialize(payload);
            // temporary quickfix for unwanted testcase namespace in target payload
            payloadData = payloadData.replaceAll(" xmlns=\\\"http://www.citrusframework.org/schema/testcase\\\"", "");
            return payloadData.trim();
        } catch (DOMException e) {
            throw new CitrusRuntimeException("Error while constructing message payload", e);
        } catch (ParserConfigurationException e) {
            throw new CitrusRuntimeException("Error while constructing message payload", e);
        }
    }
}
