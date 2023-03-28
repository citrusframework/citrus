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

package org.citrusframework.xml.namespace;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.citrusframework.message.Message;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.SimpleNamespaceContext;

/**
 * Builds a namespace context for XPath expression evaluations. Builder supports default mappings
 * as well as dynamic mappings from received message.
 *
 * Namespace mappings are defined as key value pairs where key is definded as namespace prefix and value is the
 * actual namespace uri.
 *
 * @author Christoph Deppisch
 */
public class NamespaceContextBuilder {

    /** The default bean id in Spring application context*/
    public static final String DEFAULT_BEAN_ID = "namespaceContextBuilder";

    /** Default namepsace mappings for all tests */
    private Map<String, String> namespaceMappings = new HashMap<>();

    /**
     * Construct a basic namespace context from the received message and explicit namespace mappings.
     * @param receivedMessage the actual message received.
     * @param namespaces explicit namespace mappings for this construction.
     * @return the constructed namespace context.
     */
    public NamespaceContext buildContext(Message receivedMessage, Map<String, String> namespaces) {
        SimpleNamespaceContext simpleNamespaceContext = new SimpleNamespaceContext();

        //first add default namespace definitions
        if (namespaceMappings.size() > 0) {
            simpleNamespaceContext.setBindings(namespaceMappings);
        }

        Map<String, String> dynamicBindings = lookupNamespaces(receivedMessage.getPayload(String.class));
        if (!CollectionUtils.isEmpty(namespaces)) {
            //dynamic binding of namespaces declarations in root element of received message
            for (Entry<String, String> binding : dynamicBindings.entrySet()) {
                //only bind namespace that is not present in explicit namespace bindings
                if (!namespaces.containsValue(binding.getValue())) {
                    simpleNamespaceContext.bindNamespaceUri(binding.getKey(), binding.getValue());
                }
            }
            //add explicit namespace bindings
            simpleNamespaceContext.setBindings(namespaces);
        } else {
            simpleNamespaceContext.setBindings(dynamicBindings);
        }

        return simpleNamespaceContext;
    }

    /**
     * Sets the default mappings for this namespace context builder.
     * @param defaultMappings the defaultMappings to set
     */
    public void setNamespaceMappings(Map<String, String> defaultMappings) {
        this.namespaceMappings = defaultMappings;
    }

    /**
     * Gets the namespaceMappings.
     * @return the namespaceMappings the namespaceMappings to get.
     */
    public Map<String, String> getNamespaceMappings() {
        return namespaceMappings;
    }

    /**
     * Look up namespace attribute declarations in the XML fragment and
     * store them in a binding map, where the key is the namespace prefix and the value
     * is the namespace uri.
     *
     * @param xml XML fragment.
     * @return map containing namespace prefix - namespace uri pairs.
     */
    public static Map<String, String> lookupNamespaces(String xml) {
        Map<String, String> namespaces = new HashMap<String, String>();

        //TODO: handle inner CDATA sections because namespaces they might interfere with real namespaces in xml fragment
        if (xml.indexOf(XMLConstants.XMLNS_ATTRIBUTE) != -1) {
            String[] tokens = StringUtils.split(xml, XMLConstants.XMLNS_ATTRIBUTE);

            do {
                String token = tokens[1];

                String nsPrefix;
                if (token.startsWith(":")) {
                    nsPrefix = token.substring(1, token.indexOf('='));
                } else if (token.startsWith("=")) {
                    nsPrefix = XMLConstants.DEFAULT_NS_PREFIX;
                } else {
                    //we have found a "xmlns" phrase that is no namespace attribute - ignore and continue
                    tokens = StringUtils.split(token, XMLConstants.XMLNS_ATTRIBUTE);
                    continue;
                }

                String nsUri;
                try {
                    nsUri = token.substring(token.indexOf('\"')+1, token.indexOf('\"', token.indexOf('\"')+1));
                } catch (StringIndexOutOfBoundsException e) {
                    //maybe we have more luck with single "'"
                    nsUri = token.substring(token.indexOf('\'')+1, token.indexOf('\'', token.indexOf('\'')+1));
                }

                namespaces.put(nsPrefix, nsUri);

                tokens = StringUtils.split(token, XMLConstants.XMLNS_ATTRIBUTE);
            } while(tokens != null);
        }

        return namespaces;
    }
}
