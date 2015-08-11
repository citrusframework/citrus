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

package com.consol.citrus.xml.namespace;

import com.consol.citrus.message.Message;
import com.consol.citrus.util.XMLUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
    private Map<String, String> namespaceMappings = new HashMap<String, String>();
    
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
        
        Map<String, String> dynamicBindings = XMLUtils.lookupNamespaces(receivedMessage.getPayload(String.class));
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
}
