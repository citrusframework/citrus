/*
 * Copyright 2006-2012 the original author or authors.
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
package org.citrusframework.citrus.channel.selector;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.QNameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSException;

import javax.xml.namespace.QName;

/**
 * Message selector accepts XML messages according to specified root element QName.
 * 
 * @author Christoph Deppisch
 */
public class RootQNameMessageSelector extends AbstractMessageSelector {

    /** Target message XML root QName to look for */
    private QName rootQName;

    /** Special selector element name identifying this message selector implementation */
    public static final String SELECTOR_ID = "root-qname";

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(RootQNameMessageSelector.class);
    
    /**
     * Default constructor using fields.
     */
    public RootQNameMessageSelector(String name, String value, TestContext context) {
        super(name, value, context);

        Assert.isTrue(selectKey.equals(SELECTOR_ID),
                String.format("Invalid usage of root QName message selector - " +
                        "usage restricted to key '%s' but was '%s'",  SELECTOR_ID, selectKey));

        if (QNameUtils.validateQName(value)) {
            this.rootQName = QNameUtils.parseQNameString(value);
        } else {
            throw new CitrusRuntimeException("Invalid root QName string '" + value + "'");
        }
    }
    
    @Override
    public boolean accept(Message<?> message) {
        Document doc;
        
        try {
            doc = XMLUtils.parseMessagePayload(getPayloadAsString(message));
        } catch (LSException e) {
            log.warn("Root QName message selector ignoring not well-formed XML message payload", e);
            return false; // non XML message - not accepted
        }
        
        if (StringUtils.hasText(rootQName.getNamespaceURI())) {
            return rootQName.equals(QNameUtils.getQNameForNode(doc.getFirstChild())); 
        } else {
            return rootQName.getLocalPart().equals(doc.getFirstChild().getLocalName());
        }
    }

    /**
     * Message selector factory for this implementation.
     */
    public static class Factory implements MessageSelectorFactory<RootQNameMessageSelector> {
        @Override
        public boolean supports(String key) {
            return key.equals(SELECTOR_ID);
        }

        @Override
        public RootQNameMessageSelector create(String key, String value, TestContext context) {
            return new RootQNameMessageSelector(key, value, context);
        }
    }

}
