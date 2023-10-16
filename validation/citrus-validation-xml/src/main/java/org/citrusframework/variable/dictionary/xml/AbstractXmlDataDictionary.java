/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.variable.dictionary.xml;

import java.io.StringWriter;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.StringUtils;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.validation.xhtml.XhtmlMessageConverter;
import org.citrusframework.variable.dictionary.AbstractDataDictionary;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.traversal.NodeFilter;

/**
 * Abstract data dictionary works on XML message payloads only with parsing the document and translating each element
 * and attribute with respective value in dictionary.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractXmlDataDictionary extends AbstractDataDictionary<Node> {

    @Override
    protected void processMessage(Message message, TestContext context) {
        if (message.getPayload() == null || !StringUtils.hasText(message.getPayload(String.class))) {
            return;
        }

        String messagePayload = message.getPayload(String.class);
        if (MessageType.XHTML.name().equalsIgnoreCase(message.getType())) {
            messagePayload = new XhtmlMessageConverter().convert(messagePayload);
        }

        Document doc = XMLUtils.parseMessagePayload(messagePayload);

        LSSerializer serializer = XMLUtils.createLSSerializer();

        serializer.setFilter(new TranslateFilter(context));

        LSOutput output = XMLUtils.createLSOutput();
        String charset = XMLUtils.getTargetCharset(doc).displayName();
        output.setEncoding(charset);

        StringWriter writer = new StringWriter();
        output.setCharacterStream(writer);

        serializer.write(doc, output);

        message.setPayload(writer.toString());
    }

    /**
     * Serializer filter uses data dictionary translation on elements and attributes.
     */
    private class TranslateFilter implements LSSerializerFilter {
        private TestContext context;

        public TranslateFilter(TestContext context) {
            this.context = context;
        }

        @Override
        public int getWhatToShow() {
            return NodeFilter.SHOW_ALL;
        }

        @Override
        public short acceptNode(Node node) {
            if (node instanceof Element) {
                Element element = (Element) node;

                if (StringUtils.hasText(DomUtils.getTextValue(element))) {
                    element.setTextContent(translate(element, DomUtils.getTextValue(element), context));
                } else if (!element.hasChildNodes()) {
                    String translated = translate(element, "", context);
                    if (StringUtils.hasText(translated)) {
                        element.appendChild(element.getOwnerDocument().createTextNode(translated));
                    }
                }

                NamedNodeMap attributes = element.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Attr attribute = (Attr) attributes.item(i);
                    attribute.setValue(translate(attribute, attribute.getNodeValue(), context));
                }
            }

            return NodeFilter.FILTER_ACCEPT;
        }
    }

    @Override
    public boolean supportsMessageType(String messageType) {
        return MessageType.XML.toString().equalsIgnoreCase(messageType) || MessageType.XHTML.toString().equalsIgnoreCase(messageType);
    }


}
