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

package org.citrusframework.config.xml;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

            String payloadData = serialize(payload);
            // temporary quickfix for unwanted testcase namespace in target payload
            payloadData = payloadData.replaceAll(" xmlns=\\\"http://www.citrusframework.org/schema/testcase\\\"", "");
            return payloadData.trim();
        } catch (DOMException | ParserConfigurationException | TransformerException e) {
            throw new CitrusRuntimeException("Error while constructing message payload", e);
        }
    }

    private static String serialize(Document doc) throws TransformerException {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(domSource, result);
        return writer.toString();
    }
}
